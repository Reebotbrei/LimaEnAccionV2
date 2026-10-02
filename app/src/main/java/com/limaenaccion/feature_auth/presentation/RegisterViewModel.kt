package com.limaenaccion.feature_auth.presentation

import androidx.lifecycle.ViewModel
import com.limaenaccion.feature_auth.data.model.Gender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Listas Mock — en producción vendrían de un catálogo/backend.
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

    // Datos de Acceso
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
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // 1
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

    fun onStep1ContinueClicked() {
        val state = _uiState.value
        val fullNameError = if (state.fullName.isBlank()) "Ingresa tu nombre completo" else null
        val dniError = if (state.dni.length != 8) "El DNI debe tener 8 dígitos" else null
        val genderError = if (state.gender == null) "Selecciona una opción" else null

        if (fullNameError != null || dniError != null || genderError != null) {
            _uiState.value = state.copy(fullNameError = fullNameError, dniError = dniError, genderError = genderError)
            return
        }
        _uiState.value = state.copy(step1Completed = true)
    }

    // 2
    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null)
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
            _uiState.value = state.copy(emailError = emailError, phoneError = phoneError, passwordError = passwordError)
            return
        }
        _uiState.value = state.copy(step2Completed = true)
    }

    // 3
    fun onDistrictChange(value: String) {
        _uiState.value = _uiState.value.copy(district = value, districtError = null)
    }

    fun onNeighborhoodChange(value: String) {
        _uiState.value = _uiState.value.copy(neighborhood = value, neighborhoodError = null)
    }

    fun onFinishRegistrationClicked() {
        val state = _uiState.value
        val districtError = if (state.district.isNullOrBlank()) "Selecciona tu distrito" else null
        val neighborhoodError = if (state.neighborhood.isNullOrBlank()) "Selecciona tu urbanización/vecindario" else null

        if (districtError != null || neighborhoodError != null) {
            _uiState.value = state.copy(districtError = districtError, neighborhoodError = neighborhoodError)
            return
        }
        // Se marca terminado y se pasa a verificación SMS.
        _uiState.value = state.copy(registrationFinished = true)
    }
}