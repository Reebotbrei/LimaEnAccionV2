package com.limaenaccion.feature_auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Si la validación se completa con éxito, avanza a la verificación por SMS
    LaunchedEffect(uiState.registrationFinished) {
        if (uiState.registrationFinished) {
            onContinueToVerification()
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
                SectionHeader(strings.registerSectionPersonal)

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text(strings.registerFullNameLabel) },
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
                    label = { Text(strings.registerDniLabel) },
                    placeholder = { Text("12345678") },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.dniError != null,
                    supportingText = uiState.dniError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Género con chips responsivos
                Column {
                    Text(
                        text = strings.registerGenderLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
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
                    label = { Text("${strings.loginPasswordLabel} *") },
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

                DropdownField(
                    selectedValue = uiState.district,
                    placeholder = strings.registerDistrictPlaceholder,
                    options = MOCK_DISTRICTS,
                    isError = uiState.districtError != null,
                    errorMessage = uiState.districtError,
                    icon = Icons.Filled.Map,
                    onOptionSelected = viewModel::onDistrictChange
                )

                DropdownField(
                    selectedValue = uiState.neighborhood,
                    placeholder = strings.registerNeighborhoodPlaceholder,
                    options = MOCK_NEIGHBORHOODS,
                    isError = uiState.neighborhoodError != null,
                    errorMessage = uiState.neighborhoodError,
                    icon = Icons.Filled.Home,
                    onOptionSelected = viewModel::onNeighborhoodChange
                )

                if (uiState.generalError != null) {
                    Text(
                        text = uiState.generalError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón principal de registro responsivo
                PrimaryButton(
                    text = if (uiState.isLoading) "..." else strings.registerSubmitBtn,
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
