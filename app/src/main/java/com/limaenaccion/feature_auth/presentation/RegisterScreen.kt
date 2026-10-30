package com.limaenaccion.feature_auth.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limaenaccion.core.ui.components.AppLogo
import com.limaenaccion.core.ui.components.BrandLayout
import com.limaenaccion.core.ui.components.DropdownField
import com.limaenaccion.core.ui.components.LogoVariant
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.util.LimaLocations
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.feature_auth.data.model.Gender

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onContinueToVerification: () -> Unit,
    onLoginClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Solicitud de permisos para sugerencia de ubicación por GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.onSuggestLocationFromGps(context)
        }
    }

    // Si la validación se completa con éxito, avanza a la verificación por SMS
    LaunchedEffect(uiState.registrationFinished) {
        if (uiState.registrationFinished) {
            onContinueToVerification()
        }
    }

    // Auto-scroll hacia arriba si hay errores en los campos superiores
    LaunchedEffect(uiState.generalError) {
        if (!uiState.generalError.isNullOrBlank()) {
            if (uiState.fullNameError != null || uiState.dniError != null || uiState.genderError != null) {
                scrollState.animateScrollTo(0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.registerScreenTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppLogo(
                    variant = LogoVariant.FULL_BRAND,
                    layout = BrandLayout.HORIZONTAL,
                    size = 48.dp,
                    useSquircleInFullBrand = true,
                    titleFontSize = 22.sp
                )

                // Mensaje introductorio
                Text(
                    text = strings.registerIntroNotice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ───────────────────────────────────────────────
                // 1. DATOS PERSONALES
                // ───────────────────────────────────────────────
                SectionHeader("${strings.registerSectionPersonal} *")

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text("${strings.registerFullNameLabel} *") },
                    placeholder = { Text(strings.registerFullNamePlaceholder) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    singleLine = true,
                    isError = uiState.fullNameError != null,
                    supportingText = uiState.fullNameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.dni,
                    onValueChange = viewModel::onDniChange,
                    label = { Text("${strings.registerDniLabel} *") },
                    placeholder = { Text("12345678") },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.dniError != null,
                    supportingText = uiState.dniError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Género con contenedor que se marca en rojo si falta seleccionar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (uiState.genderError != null) {
                                Modifier
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            } else Modifier
                        )
                ) {
                    Text(
                        text = "${strings.registerGenderLabel} *",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.genderError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Gender.entries.forEach { gender ->
                            val genderText = when (gender) {
                                Gender.MASCULINO -> strings.registerGenderMale
                                Gender.FEMENINO -> strings.registerGenderFemale
                                else -> gender.label
                            }
                            FilterChip(
                                selected = uiState.gender == gender,
                                onClick = { viewModel.onGenderChange(gender) },
                                label = { Text(genderText) }
                            )
                        }
                    }
                    if (uiState.genderError != null) {
                        Text(
                            text = uiState.genderError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ───────────────────────────────────────────────
                // 2. CONTACTO Y ACCESO
                // ───────────────────────────────────────────────
                SectionHeader(strings.registerSectionAccess)

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("${strings.loginEmailLabel} *") },
                    placeholder = { Text("vecino.lima@gmail.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    label = { Text(strings.registerPhoneLabel) },
                    placeholder = { Text("987654321") },
                    prefix = { Text("+51 ") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = uiState.phoneError != null,
                    supportingText = uiState.phoneError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("${strings.loginPasswordLabel} * (Mínimo 8 caracteres)") },
                    placeholder = { Text(strings.registerPasswordHelp) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (uiState.isPasswordVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ───────────────────────────────────────────────
                // 3. ZONA DE RESIDENCIA
                // ───────────────────────────────────────────────
                SectionHeader(strings.registerSectionResidence)

                // Botón para sugerir zona con GPS
                OutlinedButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLocatingGps) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Obteniendo posición GPS...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sugerir zona con mi ubicación actual", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (!uiState.gpsSuggestionFeedback.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.gpsSuggestionFeedback.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Selector de Distrito
                DropdownField(
                    selectedValue = uiState.district,
                    placeholder = strings.registerDistrictPlaceholder,
                    options = LimaLocations.ALL_DISTRICT_NAMES,
                    isError = uiState.districtError != null,
                    errorMessage = uiState.districtError,
                    icon = Icons.Filled.Map,
                    onOptionSelected = viewModel::onDistrictChange
                )

                // Selector de Urbanización / Barrio Dinámico según el Distrito
                val neighborhoods = if (!uiState.district.isNullOrBlank()) {
                    uiState.availableNeighborhoods.ifEmpty { LimaLocations.getNeighborhoods(uiState.district) }
                } else {
                    listOf(LimaLocations.OTRO_BARRIO)
                }

                val currentSelectedNeighborhood = if (uiState.isCustomNeighborhood) {
                    LimaLocations.OTRO_BARRIO
                } else {
                    uiState.neighborhood
                }

                DropdownField(
                    selectedValue = currentSelectedNeighborhood,
                    placeholder = if (uiState.district.isNullOrBlank()) "Primero selecciona un distrito" else strings.registerNeighborhoodPlaceholder,
                    options = neighborhoods,
                    isError = uiState.neighborhoodError != null && !uiState.isCustomNeighborhood,
                    errorMessage = if (!uiState.isCustomNeighborhood) uiState.neighborhoodError else null,
                    icon = Icons.Filled.Home,
                    onOptionSelected = viewModel::onNeighborhoodChange
                )

                // Campo de texto libre si seleccionó "Otro"
                if (uiState.isCustomNeighborhood) {
                    OutlinedTextField(
                        value = uiState.customNeighborhoodText,
                        onValueChange = viewModel::onCustomNeighborhoodChange,
                        label = { Text("Escribe tu urbanización o barrio *") },
                        placeholder = { Text("Ej. Urb. Las Palmeras, Sector 4, Asoc. Los Pinos") },
                        singleLine = true,
                        isError = uiState.neighborhoodError != null,
                        supportingText = uiState.neighborhoodError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Banner de error general visible encima del botón
                if (!uiState.generalError.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.generalError.orEmpty(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Botón principal de registro responsivo
                PrimaryButton(
                    text = if (uiState.isLoading) "Registrando cuenta..." else strings.registerSubmitBtn,
                    onClick = viewModel::onRegisterSubmitClicked,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Enlace a Login
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.registerAlreadyHaveAccount,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = strings.registerLoginLink,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable(onClick = onLoginClick)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.1.sp
    )
}
