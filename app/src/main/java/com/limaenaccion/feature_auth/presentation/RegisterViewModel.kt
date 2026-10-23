package com.limaenaccion.feature_auth.presentation

import com.limaenaccion.feature_auth.data.model.Gender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.limaenaccion.feature_auth.data.model.Usuario
import com.limaenaccion.feature_auth.data.repository.UsuarioRepository
import kotlinx.coroutines.launch
import java.util.UUID
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.RegisterRequest


private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Listas Mock — catálogo de zonas urbanas para residencia
val MOCK_DISTRICTS = listOf("San Juan de Lurigancho", "San Juan de Miraflores", "Comas", "Villa El Salvador", "Ate")
val MOCK_NEIGHBORHOODS = listOf("Caja de Agua", "Zárate", "Mangomarca", "Bayóvar", "Canto Grande")

data class RegisterUiState(
    // Datos Personales
    val fullName: String = "",
    val dni: String = "",
    val gender: Gender? = null,
    val fullNameError: String? = null,
    val dniError: String? = null,
    val genderError: String? = null,

    // Datos de Acceso y Contacto
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,

    // Zona de Residencia
    val district: String? = null,
    val neighborhood: String? = null,
    val districtError: String? = null,
    val neighborhoodError: String? = null,

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

class RegisterViewModel(
    application: android.app.Application
) : androidx.lifecycle.AndroidViewModel(application) {

    private val usuarioRepository: UsuarioRepository = com.limaenaccion.feature_auth.data.repository.UsuarioRepositoryImpl(
        usuarioDao = com.limaenaccion.core.data.local.AppDatabase.getInstance(application).usuarioDao()
    )


    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // 1. Datos Personales
    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, fullNameError = null)
    }

    fun onDniChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(8)
        _uiState.value = _uiState.value.copy(dni = digitsOnly, dniError = null)
    }

    fun onGenderChange(value: Gender) {
        _uiState.value = _uiState.value.copy(gender = value, genderError = null)
    }

    // 2. Datos de Acceso
    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value.trim(), emailError = null)
    }

    fun onPhoneNumberChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(9)
        _uiState.value = _uiState.value.copy(phoneNumber = digitsOnly, phoneError = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null)
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    // 3. Zona de Residencia
    fun onDistrictChange(value: String) {
        _uiState.value = _uiState.value.copy(district = value, districtError = null)
    }

    fun onNeighborhoodChange(value: String) {
        _uiState.value = _uiState.value.copy(neighborhood = value, neighborhoodError = null)
    }

    // Validación Unificada de Pantalla Única
    fun onRegisterSubmitClicked() {
        val state = _uiState.value

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

        // Guardar en la Nube (Firestore) y en Caché (Room)
        viewModelScope.launch {
            val nuevoUsuario = Usuario(
                uid = UUID.randomUUID().toString(),
                fullName = state.fullName,
                dni = state.dni,
                gender = state.gender?.name ?: "",
                email = state.email,
                phoneNumber = state.phoneNumber,
                district = state.district ?: "",
                neighborhood = state.neighborhood ?: "",
                createdAt = System.currentTimeMillis()
            )

            usuarioRepository.registrarUsuario(nuevoUsuario)

            _uiState.value = state.copy(
                step1Completed = true,
                step2Completed = true,
                registrationFinished = true
            )
        }
    }

    // 1. Validar solo Paso 1 (Datos Personales) y avanzar
    fun onStep1ContinueClicked() {
        val state = _uiState.value
        val fullNameError = if (state.fullName.isBlank()) "Ingresa tu nombre completo" else null
        val dniError = if (state.dni.length != 8) "El DNI debe tener 8 dígitos" else null
        val genderError = if (state.gender == null) "Selecciona una opción" else null

        if (fullNameError != null || dniError != null || genderError != null) {
            _uiState.value = state.copy(
                fullNameError = fullNameError,
                dniError = dniError,
                genderError = genderError
            )
            return
        }
        _uiState.value = state.copy(step1Completed = true)
    }

    // 2. Validar solo Paso 2 (Acceso y Contacto) y avanzar
    fun onStep2ContinueClicked() {
        val state = _uiState.value
        val emailError = when {
            state.email.isBlank() -> "Ingresa tu correo"
            !EMAIL_REGEX.matches(state.email) -> "Correo inválido"
            else -> null
        }
        val phoneError = if (state.phoneNumber.length != 9) "El celular debe tener 9 dígitos" else null
        val passwordError = if (state.password.length < 8) "Mínimo 8 caracteres" else null

        if (emailError != null || phoneError != null || passwordError != null) {
            _uiState.value = state.copy(
                emailError = emailError,
                phoneError = phoneError,
                passwordError = passwordError
            )
            return
        }
        _uiState.value = state.copy(step2Completed = true)
    }

    // 3. Finalizar y Registrar consumiendo API Service + Firebase + Room
    fun onFinishRegistrationClicked() {
        val state = _uiState.value
        val districtError = if (state.district.isNullOrBlank()) "Selecciona tu distrito" else null
        val neighborhoodError = if (state.neighborhood.isNullOrBlank()) "Selecciona tu vecindario" else null

        if (districtError != null || neighborhoodError != null) {
            _uiState.value = state.copy(
                districtError = districtError,
                neighborhoodError = neighborhoodError
            )
            return
        }

        viewModelScope.launch {
            // 1. Consumir API Service del Backend (Node.js) con Bcrypt
            try {
                RetrofitClient.instance.register(
                    RegisterRequest(
                        fullName = state.fullName,
                        dni = state.dni,
                        gender = state.gender?.name ?: "",
                        email = state.email,
                        phoneNumber = state.phoneNumber,
                        password = state.password,
                        district = state.district ?: "",
                        neighborhood = state.neighborhood ?: ""
                    )
                )
            } catch (e: Exception) {
                // Si el backend no responde o falla, no bloqueamos la app (tolerancia a fallos)
            }

            // 2. Guardar en la Nube (Firebase Firestore) y en Caché Local (Room)
            val nuevoUsuario = Usuario(
                uid = UUID.randomUUID().toString(),
                fullName = state.fullName,
                dni = state.dni,
                gender = state.gender?.name ?: "",
                email = state.email,
                phoneNumber = state.phoneNumber,
                district = state.district ?: "",
                neighborhood = state.neighborhood ?: "",
                createdAt = System.currentTimeMillis()
            )

            usuarioRepository.registrarUsuario(nuevoUsuario)

            _uiState.value = state.copy(registrationFinished = true)
        }
    }

}
