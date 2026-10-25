package com.limaenaccion.feature_auth.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.entities.UsuarioEntity
import com.limaenaccion.feature_auth.data.model.Gender
import com.limaenaccion.feature_auth.data.model.Usuario
import com.limaenaccion.feature_auth.data.repository.AuthActionResult
import com.limaenaccion.feature_auth.data.repository.AuthErrorField
import com.limaenaccion.feature_auth.data.repository.AuthLocalRepository
import com.limaenaccion.feature_auth.data.repository.AuthLocalRepositoryImpl
import com.limaenaccion.feature_auth.data.repository.UsuarioRepository
import com.limaenaccion.feature_auth.data.repository.UsuarioRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Listas de distritos y vecindarios para registro y perfil
val MOCK_DISTRICTS = listOf("San Juan de Lurigancho", "San Juan de Miraflores", "Comas", "Villa El Salvador", "Ate")
val MOCK_NEIGHBORHOODS = listOf("Caja de Agua", "Zárate", "Mangomarca", "Bayóvar", "Canto Grande")

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
    val districtError: String? = null,
    val neighborhoodError: String? = null,

    val generalError: String? = null,
    val isLoading: Boolean = false,
    val step1Completed: Boolean = false,
    val step2Completed: Boolean = false,
    val registrationFinished: Boolean = false
) {
    val isFormComplete: Boolean
        get() = fullName.isNotBlank() &&
                dni.length == 8 &&
                gender != null &&
                email.isNotBlank() &&
                phoneNumber.length == 9 &&
                password.length >= 8 &&
                !district.isNullOrBlank() &&
                !neighborhood.isNullOrBlank()
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
        _uiState.value = _uiState.value.copy(phoneNumber = digitsOnly, phoneError = null, generalError = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, generalError = null)
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    // 3. Zona de Residencia
    fun onDistrictChange(value: String) {
        _uiState.value = _uiState.value.copy(district = value, districtError = null, generalError = null)
    }

    fun onNeighborhoodChange(value: String) {
        _uiState.value = _uiState.value.copy(neighborhood = value, neighborhoodError = null, generalError = null)
    }

    // Validación Unificada + Consumo del API Service Node.js + Firebase Cloud + Sincronización SQLite
    fun onRegisterSubmitClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        val fullNameError = if (state.fullName.isBlank()) "Ingresa tu nombre completo" else null
        val dniError = if (state.dni.length != 8) "El DNI debe tener 8 dígitos" else null
        val genderError = if (state.gender == null) "Selecciona una opción" else null

        val emailError = when {
            state.email.isBlank() -> "Ingresa tu correo"
            !EMAIL_REGEX.matches(state.email) -> "Correo inválido"
            else -> null
        }
        val phoneError = if (state.phoneNumber.length != 9) "El celular debe tener 9 dígitos" else null
        val passwordError = if (state.password.length < 8) "Mínimo 8 caracteres" else null

        val districtError = if (state.district.isNullOrBlank()) "Selecciona tu distrito" else null
        val neighborhoodError = if (state.neighborhood.isNullOrBlank()) "Selecciona tu vecindario" else null

        val hasErrors = fullNameError != null || dniError != null || genderError != null ||
                emailError != null || phoneError != null || passwordError != null ||
                districtError != null || neighborhoodError != null

        if (hasErrors) {
            _uiState.value = state.copy(
                fullNameError = fullNameError,
                dniError = dniError,
                genderError = genderError,
                emailError = emailError,
                phoneError = phoneError,
                passwordError = passwordError,
                districtError = districtError,
                neighborhoodError = neighborhoodError
            )
            return
        }

        val nuevoUsuarioEntity = UsuarioEntity(
            correo = state.email.trim().lowercase(),
            dni = state.dni,
            nombreCompleto = state.fullName.trim(),
            genero = state.gender?.label ?: "",
            telefono = state.phoneNumber,
            contrasena = state.password,
            distrito = state.district.orEmpty(),
            urbanizacion = state.neighborhood.orEmpty()
        )

        _uiState.value = state.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            // Sincronizar en Firebase Firestore de forma asíncrona
            try {
                val fbUser = Usuario(
                    uid = UUID.randomUUID().toString(),
                    fullName = state.fullName.trim(),
                    dni = state.dni,
                    gender = state.gender?.name ?: "",
                    email = state.email.trim().lowercase(),
                    phoneNumber = state.phoneNumber,
                    district = state.district.orEmpty(),
                    neighborhood = state.neighborhood.orEmpty(),
                    createdAt = System.currentTimeMillis()
                )
                usuarioRepository.registrarUsuario(fbUser)
                Log.d("REGISTER_FLOW", "✅ Usuario respaldado en Firebase Firestore")
            } catch (e: Exception) {
                Log.w("REGISTER_FLOW", "⚠️ Firebase Firestore no sincronizado (${e.message})")
            }

            // Consumir API Service de Node.js y registrar en SQLite local (Rúbricas b y c)
            when (val result = authLocalRepository.registerUser(nuevoUsuarioEntity)) {
                is AuthActionResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step1Completed = true,
                        step2Completed = true,
                        registrationFinished = true
                    )
                }

                is AuthActionResult.Error -> {
                    _uiState.value = when (result.field) {
                        AuthErrorField.EMAIL -> _uiState.value.copy(
                            isLoading = false,
                            emailError = result.message
                        )
                        AuthErrorField.DNI -> _uiState.value.copy(
                            isLoading = false,
                            dniError = result.message
                        )
                        else -> _uiState.value.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                }
            }
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
