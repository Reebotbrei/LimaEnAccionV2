package com.limaenaccion.feature_auth.presentation

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.entities.UsuarioEntity
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.RegisterRequest
import com.limaenaccion.core.data.network.model.SendRegisterCodeRequest
import com.limaenaccion.core.data.network.model.VerifyResetCodeRequest
import com.limaenaccion.core.util.LimaLocations
import com.limaenaccion.feature_auth.data.model.Gender
import com.limaenaccion.feature_auth.data.model.Usuario
import com.limaenaccion.feature_auth.data.repository.AuthActionResult
import com.limaenaccion.feature_auth.data.repository.AuthErrorField
import com.limaenaccion.feature_auth.data.repository.AuthLocalRepository
import com.limaenaccion.feature_auth.data.repository.AuthLocalRepositoryImpl
import com.limaenaccion.feature_auth.data.repository.UsuarioRepository
import com.limaenaccion.feature_auth.data.repository.UsuarioRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Listas oficiales de distritos de Lima Metropolitana y vecindarios (compatibilidad)
val MOCK_DISTRICTS = LimaLocations.ALL_DISTRICT_NAMES
val MOCK_NEIGHBORHOODS = LimaLocations.getNeighborhoods("San Juan de Lurigancho")

data class RegisterUiState(
    // Paso 1: Datos Personales
    val fullName: String = "",
    val dni: String = "",
    val gender: Gender? = null,
    val fullNameError: String? = null,
    val dniError: String? = null,
    val genderError: String? = null,

    // Paso 2: Datos de Acceso y Contacto
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,

    // Paso 3: Zona de Residencia
    val district: String? = null,
    val neighborhood: String? = null,
    val availableNeighborhoods: List<String> = emptyList(),
    val isCustomNeighborhood: Boolean = false,
    val customNeighborhoodText: String = "",
    val isLocatingGps: Boolean = false,
    val gpsSuggestionFeedback: String? = null,
    val districtError: String? = null,
    val neighborhoodError: String? = null,

    val generalError: String? = null,
    val isLoading: Boolean = false,
    val step1Completed: Boolean = false,
    val step2Completed: Boolean = false,
    val registrationFinished: Boolean = false,

    // Datos OTP Pre-Registro
    val generatedOtpCode: String? = null,
    val otpExpiresAt: Long = 0L,
    val simulatedSmsNotice: String? = null,
    val isOtpVerified: Boolean = false,
    val registrationCompletedSuccessfully: Boolean = false
) {
    val isFormComplete: Boolean
        get() = fullName.isNotBlank() &&
                dni.length == 8 &&
                gender != null &&
                email.isNotBlank() &&
                phoneNumber.length == 9 &&
                phoneNumber.startsWith("9") &&
                password.length >= 8 &&
                !district.isNullOrBlank() &&
                (!neighborhood.isNullOrBlank() || (isCustomNeighborhood && customNeighborhoodText.isNotBlank()))
}

class RegisterViewModel @JvmOverloads constructor(
    application: Application,
    private val authLocalRepository: AuthLocalRepository = AuthLocalRepositoryImpl(application),
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Datos temporales retenidos hasta que el código sea verificado
    private var pendingUsuarioEntity: UsuarioEntity? = null
    private var pendingFirebaseUser: Usuario? = null
    private var pendingRegisterRequest: RegisterRequest? = null
    private var currentOtpCode: String? = null
    private var otpExpiresAt: Long = 0L

    // 1. Datos Personales
    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, fullNameError = null, generalError = null)
    }

    fun onDniChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(8)
        _uiState.value = _uiState.value.copy(dni = digitsOnly, dniError = null, generalError = null)
    }

    fun onGenderChange(value: Gender) {
        _uiState.value = _uiState.value.copy(gender = value, genderError = null, generalError = null)
    }

    // 2. Datos de Acceso
    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value.trim(), emailError = null, generalError = null)
    }

    fun onPhoneNumberChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(9)
        val phoneError = when {
            digitsOnly.isNotEmpty() && !digitsOnly.startsWith("9") -> "El celular debe iniciar con 9"
            digitsOnly.length in 1..8 -> "El celular debe tener 9 dígitos"
            else -> null
        }
        _uiState.value = _uiState.value.copy(
            phoneNumber = digitsOnly,
            phoneError = phoneError,
            generalError = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, generalError = null)
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    // 3. Zona de Residencia (Dinámico por Distrito de Lima)
    fun onDistrictChange(value: String) {
        val neighborhoods = LimaLocations.getNeighborhoods(value)
        _uiState.value = _uiState.value.copy(
            district = value,
            districtError = null,
            neighborhood = null,
            availableNeighborhoods = neighborhoods,
            isCustomNeighborhood = false,
            customNeighborhoodText = "",
            neighborhoodError = null,
            generalError = null
        )
    }

    fun onNeighborhoodChange(value: String) {
        if (value == LimaLocations.OTRO_BARRIO) {
            _uiState.value = _uiState.value.copy(
                neighborhood = "",
                isCustomNeighborhood = true,
                customNeighborhoodText = "",
                neighborhoodError = null,
                generalError = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                neighborhood = value,
                isCustomNeighborhood = false,
                customNeighborhoodText = "",
                neighborhoodError = null,
                generalError = null
            )
        }
    }

    fun onCustomNeighborhoodChange(value: String) {
        _uiState.value = _uiState.value.copy(
            customNeighborhoodText = value,
            neighborhood = value.trim(),
            neighborhoodError = null,
            generalError = null
        )
    }

    /**
     * Consulta el sensor GPS y el Geocoder para sugerir distrito y vecindario
     */
    fun onSuggestLocationFromGps(context: Context) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            _uiState.value = _uiState.value.copy(
                gpsSuggestionFeedback = "Debes otorgar permisos de ubicación para sugerir tu zona."
            )
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null || (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            _uiState.value = _uiState.value.copy(
                gpsSuggestionFeedback = "El GPS está apagado. Enciéndelo para sugerir tu zona."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLocatingGps = true, gpsSuggestionFeedback = null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var lastLoc: Location? = null
                if (fineGranted) {
                    lastLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                if (lastLoc == null && (fineGranted || coarseGranted)) {
                    lastLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (lastLoc == null) {
                    _uiState.value = _uiState.value.copy(
                        isLocatingGps = false,
                        gpsSuggestionFeedback = "Obteniendo señal GPS... Intenta de nuevo en unos segundos."
                    )
                    return@launch
                }

                val geocoder = Geocoder(context, Locale("es", "PE"))
                val addresses = try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lastLoc.latitude, lastLoc.longitude, 3)
                } catch (e: Exception) {
                    null
                }

                if (!addresses.isNullOrEmpty()) {
                    var matchedDistrict: String? = null
                    var suggestedNeighborhood: String? = null

                    val lat = lastLoc.latitude
                    val lon = lastLoc.longitude
                    val isHuarochiriCoords = (lat in -12.45..-11.45) && (lon in -76.85..-76.00)
                    val isHuarochiriText = addresses.any { addr ->
                        val line = "${addr.adminArea} ${addr.subAdminArea} ${addr.locality} ${addr.subLocality} ${addr.getAddressLine(0)}".lowercase()
                        line.contains("huarochiri") || line.contains("huarochirí") || line.contains("palomar") || line.contains("santa eulalia") || line.contains("san antonio")
                    }

                    for (addr in addresses) {
                        matchedDistrict = LimaLocations.findMatchingDistrict(addr.subAdminArea)
                            ?: LimaLocations.findMatchingDistrict(addr.locality)
                            ?: LimaLocations.findMatchingDistrict(addr.subLocality)
                            ?: LimaLocations.findMatchingDistrict(addr.adminArea)
                            ?: LimaLocations.findMatchingDistrict(addr.getAddressLine(0))

                        if (matchedDistrict != null) {
                            val line = addr.getAddressLine(0).orEmpty()
                            suggestedNeighborhood = when {
                                line.contains("Palomar", ignoreCase = true) -> "El Palomar"
                                else -> addr.subLocality ?: addr.thoroughfare
                            }
                            break
                        }
                    }

                    // Caso A: Coincidencia en distrito dentro de Lima Metropolitana o Huarochirí
                    if (matchedDistrict != null) {
                        val neighborhoods = LimaLocations.getNeighborhoods(matchedDistrict)
                        val exactNeighborhood = neighborhoods.firstOrNull {
                            it.equals(suggestedNeighborhood?.trim(), ignoreCase = true)
                        }

                        val isCustom = exactNeighborhood == null && !suggestedNeighborhood.isNullOrBlank()
                        val finalNeighborhood = exactNeighborhood ?: suggestedNeighborhood ?: neighborhoods.firstOrNull() ?: ""

                        val feedbackMsg = if (matchedDistrict == "Huarochirí") {
                            "⚠️ Estás fuera de Lima Metropolitana. Se autocompletó Huarochirí ($finalNeighborhood) para tus pruebas."
                        } else {
                            "📍 Zona sugerida: $matchedDistrict${if (finalNeighborhood.isNotBlank()) " ($finalNeighborhood)" else ""}"
                        }

                        _uiState.value = _uiState.value.copy(
                            district = matchedDistrict,
                            districtError = null,
                            availableNeighborhoods = neighborhoods,
                            neighborhood = finalNeighborhood,
                            isCustomNeighborhood = isCustom,
                            customNeighborhoodText = if (isCustom) finalNeighborhood else "",
                            neighborhoodError = null,
                            isLocatingGps = false,
                            gpsSuggestionFeedback = feedbackMsg
                        )
                    } else if (isHuarochiriCoords || isHuarochiriText) {
                        // Caso B: Zona de pruebas Huarochirí (El Palomar)
                        val neighborhoods = LimaLocations.getNeighborhoods("Huarochirí")
                        _uiState.value = _uiState.value.copy(
                            district = "Huarochirí",
                            districtError = null,
                            availableNeighborhoods = neighborhoods,
                            neighborhood = "El Palomar",
                            isCustomNeighborhood = false,
                            customNeighborhoodText = "",
                            neighborhoodError = null,
                            isLocatingGps = false,
                            gpsSuggestionFeedback = "⚠️ Estás fuera de Lima Metropolitana. Se autocompletó Huarochirí (El Palomar) para tus pruebas."
                        )
                    } else {
                        // Caso C: Fuera de Lima Metropolitana en cualquier otra región
                        _uiState.value = _uiState.value.copy(
                            isLocatingGps = false,
                            gpsSuggestionFeedback = "⚠️ Estás fuera de Lima Metropolitana. Elige un distrito de Lima manualmente para registrarte."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLocatingGps = false,
                        gpsSuggestionFeedback = "No se pudo determinar tu dirección exacta. Selecciona tu zona manualmente."
                    )
                }
            } catch (e: Exception) {
                Log.e("GPS_SUGGESTION", "Error obteniendo ubicación GPS: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLocatingGps = false,
                    gpsSuggestionFeedback = "Error al consultar GPS. Intenta seleccionar manualmente."
                )
            }
        }
    }

    /**
     * Paso final: Validación y Solicitud de Código OTP previo a la creación
     */
    fun onRegisterSubmitClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        val fullNameError = if (state.fullName.isBlank()) "Ingresa tu nombre completo" else null
        val dniError = when {
            state.dni.isBlank() -> "Ingresa tu DNI"
            state.dni.length != 8 -> "El DNI debe tener 8 dígitos"
            else -> null
        }
        val genderError = if (state.gender == null) "Selecciona tu género" else null

        val emailError = when {
            state.email.isBlank() -> "Ingresa tu correo"
            !EMAIL_REGEX.matches(state.email) -> "Correo inválido"
            else -> null
        }
        val phoneError = when {
            state.phoneNumber.isBlank() -> "Ingresa tu número de celular"
            !state.phoneNumber.startsWith("9") -> "El celular debe iniciar con 9"
            state.phoneNumber.length != 9 -> "El celular debe tener 9 dígitos"
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "Ingresa tu contraseña"
            state.password.length < 8 -> "Mínimo 8 caracteres"
            else -> null
        }

        val districtError = if (state.district.isNullOrBlank()) "Selecciona tu distrito" else null
        val effectiveNeighborhood = if (state.isCustomNeighborhood) state.customNeighborhoodText.trim() else state.neighborhood.orEmpty()
        val neighborhoodError = if (effectiveNeighborhood.isBlank()) "Indica tu urbanización o barrio" else null

        val hasErrors = fullNameError != null || dniError != null || genderError != null ||
                emailError != null || phoneError != null || passwordError != null ||
                districtError != null || neighborhoodError != null

        if (hasErrors) {
            val generalMessage = "⚠️ Completa todos los campos obligatorios marcados en rojo"
            _uiState.value = state.copy(
                fullNameError = fullNameError,
                dniError = dniError,
                genderError = genderError,
                emailError = emailError,
                phoneError = phoneError,
                passwordError = passwordError,
                districtError = districtError,
                neighborhoodError = neighborhoodError,
                generalError = generalMessage
            )
            return
        }

        // PREPARAR DATOS EN MEMORIA (NO SE GUARDA AÚN EN BASE DE DATOS NI FIREBASE)
        pendingUsuarioEntity = UsuarioEntity(
            correo = state.email.trim().lowercase(),
            dni = state.dni,
            nombreCompleto = state.fullName.trim(),
            genero = state.gender?.label ?: "",
            telefono = state.phoneNumber,
            contrasena = state.password,
            distrito = state.district.orEmpty(),
            urbanizacion = effectiveNeighborhood
        )

        val newUid = UUID.randomUUID().toString()
        pendingFirebaseUser = Usuario(
            uid = newUid,
            fullName = state.fullName.trim(),
            dni = state.dni,
            gender = state.gender?.name ?: "",
            email = state.email.trim().lowercase(),
            phoneNumber = state.phoneNumber,
            district = state.district.orEmpty(),
            neighborhood = effectiveNeighborhood,
            contrasena = state.password,
            password = state.password,
            createdAt = System.currentTimeMillis()
        )

        pendingRegisterRequest = RegisterRequest(
            id = newUid,
            dni = state.dni,
            nombreCompleto = state.fullName.trim(),
            correo = state.email.trim().lowercase(),
            telefono = state.phoneNumber,
            genero = state.gender?.label ?: "",
            distrito = state.district.orEmpty(),
            urbanizacion = effectiveNeighborhood,
            contrasena = state.password
        )

        _uiState.value = state.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            dispatchVerificationCode(isResend = false)
        }
    }

    private suspend fun dispatchVerificationCode(isResend: Boolean) {
        val state = _uiState.value
        val email = state.email.trim().lowercase()
        val phone = state.phoneNumber.trim()
        val nombre = state.fullName.trim()
        val dni = state.dni.trim()

        var remoteCode: String? = null

        try {
            RetrofitClient.autoSelectReachableBackend()
            val req = SendRegisterCodeRequest(
                email = email,
                dni = dni,
                fullName = nombre,
                phoneNumber = phone
            )
            val resp = RetrofitClient.instance.sendRegisterCode(req)
            if (resp.status == "success") {
                remoteCode = resp.data?.debugCode
                Log.d("REGISTER_OTP", "✅ Código recibido del backend (enviado por Gmail): $remoteCode")
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("REGISTER_OTP", "Http error ${e.code()}: $errorBody")
            if (e.code() == 409) {
                val msg = if (errorBody?.contains("correo", ignoreCase = true) == true) {
                    "Este correo electrónico ya está registrado en Lima en Acción"
                } else {
                    "Este DNI ya se encuentra vinculado a otra cuenta"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = "❌ $msg",
                    emailError = if (msg.contains("correo")) msg else null,
                    dniError = if (msg.contains("DNI")) msg else null
                )
                return
            }
        } catch (e: Exception) {
            Log.w("REGISTER_OTP", "Backend no disponible (${e.message}). Generando código local para entorno offline.")
        }

        // Si no se obtuvo código del backend, generar código local aleatorio de 6 dígitos
        val finalCode = remoteCode ?: (100000..999999).random().toString()
        currentOtpCode = finalCode
        otpExpiresAt = System.currentTimeMillis() + 60_000L // 1 minuto (60 segundos)

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            generatedOtpCode = finalCode,
            otpExpiresAt = otpExpiresAt,
            simulatedSmsNotice = null,
            registrationFinished = true
        )
    }

    /**
     * Valida el código OTP de 6 dígitos y, SI ES CORRECTO, crea el usuario en las 3 capas
     */
    fun verifyCodeAndRegister(enteredCode: String, onResult: (Boolean, String?) -> Unit) {
        val state = _uiState.value
        val trimmedCode = enteredCode.trim()

        if (trimmedCode.length != 6) {
            onResult(false, "Ingresa el código completo de 6 dígitos")
            return
        }

        if (System.currentTimeMillis() > otpExpiresAt) {
            onResult(false, "El código ha expirado (validez de 1 minuto). Pulsa en 'Reenviar' para generar uno nuevo.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            var isValid = false
            var errorMsg: String? = null

            // 1. Validar contra el backend si está disponible
            try {
                RetrofitClient.autoSelectReachableBackend()
                val verifyReq = VerifyResetCodeRequest(
                    email = state.email.trim().lowercase(),
                    code = trimmedCode
                )
                val resp = RetrofitClient.instance.verifyRegisterCode(verifyReq)
                if (resp.status == "success") {
                    isValid = true
                } else {
                    errorMsg = resp.message
                }
            } catch (e: Exception) {
                Log.w("REGISTER_OTP", "Backend no respondió verificación (${e.message}). Verificando localmente.")
                if (trimmedCode == currentOtpCode) {
                    isValid = true
                } else {
                    errorMsg = "Código de verificación incorrecto"
                }
            }

            // Validar también si coincide con el código emitido
            if (!isValid && trimmedCode == currentOtpCode) {
                isValid = true
                errorMsg = null
            }

            if (!isValid) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult(false, errorMsg ?: "Código de verificación incorrecto")
                return@launch
            }

            // ── CÓDIGO VALIDADO CON ÉXITO: PROCEDER A CREAR EN LAS 3 CAPAS ──
            val fbUser = pendingFirebaseUser
            val apiReq = pendingRegisterRequest
            val localEntity = pendingUsuarioEntity

            if (fbUser == null || apiReq == null || localEntity == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult(false, "Error en los datos de registro. Inténtalo nuevamente.")
                return@launch
            }

            // Registrar en Backend y Firestore mediante el repositorio unificado
            try {
                usuarioRepository.registrarUsuario(fbUser)
                Log.d("REGISTER_FLOW", "✅ Usuario registrado en Backend y Firebase Firestore")
            } catch (e: Exception) {
                Log.w("REGISTER_FLOW", "⚠️ Error al registrar usuario (${e.message})")
            }

            // Guardar en SQLite Room local para sesión y soporte offline
            authLocalRepository.registerUser(localEntity)
            Log.d("REGISTER_FLOW", "✅ Usuario registrado en SQLite Room cache local")

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOtpVerified = true,
                registrationCompletedSuccessfully = true
            )
            onResult(true, null)
        }
    }

    /**
     * Reenvío del código OTP (solamente permitido tras cumplirse el minuto)
     */
    fun resendRegistrationCode(onResult: (Boolean, String?) -> Unit) {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                dispatchVerificationCode(isResend = true)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    private fun showSmsSystemNotification(context: Context, phoneNumber: String, code: String) {
        try {
            val channelId = "lima_sms_otp_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "SMS Seguridad Lima en Acción",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Códigos de verificación por SMS"
                }
                notificationManager.createNotificationChannel(channel)
            }
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("SMS Seguridad (Lima en Acción)")
                .setContentText("Código de activación: $code (Válido 1 min)")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Tu código de verificación SMS para el celular $phoneNumber es: $code. Válido durante 1 minuto.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            notificationManager.notify(1002, builder.build())
        } catch (e: Exception) {
            Log.w("SMS_NOTIFICATION", "No se pudo emitir notificación del sistema: ${e.message}")
        }
    }

    // Permite retroceder desde la pantalla SMS sin quedar atrapado en el bucle
    fun onResetRegistrationFinished() {
        _uiState.value = _uiState.value.copy(registrationFinished = false)
    }

    // Métodos heredados por compatibilidad
    fun onStep1ContinueClicked() { onRegisterSubmitClicked() }
    fun onStep2ContinueClicked() { onRegisterSubmitClicked() }
    fun onFinishRegistrationClicked() { onRegisterSubmitClicked() }
}
