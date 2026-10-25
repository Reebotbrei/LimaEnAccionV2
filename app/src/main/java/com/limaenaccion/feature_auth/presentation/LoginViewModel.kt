package com.limaenaccion.feature_auth.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.entities.UsuarioEntity
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.LoginRequest
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.feature_auth.data.repository.AuthActionResult
import com.limaenaccion.feature_auth.data.repository.AuthErrorField
import com.limaenaccion.feature_auth.data.repository.AuthLocalRepository
import com.limaenaccion.feature_auth.data.repository.AuthLocalRepositoryImpl
import com.limaenaccion.feature_auth.data.repository.UsuarioRepository
import com.limaenaccion.feature_auth.data.repository.UsuarioRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val rememberSession: Boolean = false,
    val serverUrl: String = RetrofitClient.baseUrl,
    val showServerConfig: Boolean = false
)

class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val authLocalRepository: AuthLocalRepository = AuthLocalRepositoryImpl(application),
    private val prefsRepository: PreferencesRepository = PreferencesRepositoryImpl(application),
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Cargar URL del servidor y preferencia de "Recordar sesión"
        viewModelScope.launch {
            val savedUrl = prefsRepository.serverBaseUrl.first()
            val savedRemember = prefsRepository.rememberSession.first()
            _uiState.value = _uiState.value.copy(
                serverUrl = savedUrl,
                rememberSession = savedRemember
            )
            RetrofitClient.setBaseUrl(savedUrl)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null,
            generalError = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null,
            generalError = null
        )
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun onRememberSessionChange(remember: Boolean) {
        _uiState.value = _uiState.value.copy(rememberSession = remember)
    }

    fun onShowServerConfig() {
        _uiState.value = _uiState.value.copy(showServerConfig = true)
    }

    fun onDismissServerConfig() {
        _uiState.value = _uiState.value.copy(showServerConfig = false)
    }

    /** Guarda la nueva URL/IP y actualiza Retrofit en caliente */
    fun onServerUrlChanged(newUrl: String) {
        viewModelScope.launch {
            prefsRepository.setServerBaseUrl(newUrl)
            RetrofitClient.setBaseUrl(newUrl)
            _uiState.value = _uiState.value.copy(serverUrl = newUrl, showServerConfig = false)
        }
    }

    fun onLoginClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = when {
            state.email.isBlank() -> "Ingresa tu correo"
            !EMAIL_REGEX.matches(state.email.trim()) -> "Correo inválido"
            else -> null
        }
        val passwordError = if (state.password.isBlank()) "Ingresa tu contraseña" else null

        if (emailError != null || passwordError != null) {
            _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        _uiState.value = state.copy(
            isLoading = true,
            emailError = null,
            passwordError = null,
            generalError = null
        )

        viewModelScope.launch {
            // ── 1. Intentar con el API Service de Node.js (Requerimiento c) ──
            var servidorNodeRespondio = false
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(
                        email = state.email.trim(),
                        password = state.password
                    )
                )
                servidorNodeRespondio = true
                if (response.status == "success" && response.data != null) {
                    val user = response.data
                    val entity = UsuarioEntity(
                        correo = user.email.ifBlank { user.correo },
                        dni = user.dni,
                        nombreCompleto = user.fullName.ifBlank { user.nombreCompleto },
                        genero = user.gender.ifBlank { user.genero },
                        telefono = user.phoneNumber.ifBlank { user.telefono },
                        distrito = user.district.ifBlank { user.distrito },
                        urbanizacion = user.neighborhood.ifBlank { user.urbanizacion },
                        contrasena = state.password
                    )
                    SessionManager.setCurrentUser(entity)
                    persistirSesion(user.uid.ifBlank { user.id }, state.rememberSession)
                    Log.i("LOGIN_FLOW", "✅ Login exitoso consumiendo API Service (Node.js): ${entity.correo}")
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                    return@launch
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        passwordError = "Contraseña incorrecta"
                    )
                    return@launch
                }
            } catch (e: retrofit2.HttpException) {
                // El servidor Node.js está activo y rechazó activamente las credenciales (HTTP 401, 400, etc.)
                servidorNodeRespondio = true
                Log.w("LOGIN_FLOW", "❌ API Service rechazó credenciales (HTTP ${e.code()})")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    passwordError = if (e.code() == 401) "Contraseña incorrecta" else null,
                    generalError = if (e.code() != 401) "Credenciales incorrectas" else null
                )
                return@launch
            } catch (e: java.io.IOException) {
                Log.w("LOGIN_FLOW", "⚠️ API Service Node.js no disponible (${e.message}). Intentando respaldos...")
            } catch (e: Exception) {
                Log.w("LOGIN_FLOW", "⚠️ Excepción en login (${e.message})")
            }

            if (servidorNodeRespondio) return@launch

            // ── 2. Fallback a Firebase Cloud Firestore (Anthony) ──
            val resultFirestore = usuarioRepository.loginConFirestore(state.email.trim(), state.password)
            var authPorFirebase = false
            resultFirestore.fold(
                onSuccess = { fbUser ->
                    if (fbUser != null) {
                        val entity = UsuarioEntity(
                            correo = fbUser.email,
                            dni = fbUser.dni,
                            nombreCompleto = fbUser.fullName,
                            genero = fbUser.gender,
                            telefono = fbUser.phoneNumber,
                            distrito = fbUser.district,
                            urbanizacion = fbUser.neighborhood,
                            contrasena = state.password
                        )
                        SessionManager.setCurrentUser(entity)
                        persistirSesion(fbUser.uid, state.rememberSession)
                        Log.i("LOGIN_FLOW", " Login exitoso via Firebase Cloud Firestore: ${entity.correo}")
                        _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                        authPorFirebase = true
                    }
                },
                onFailure = { error ->
                    Log.w("LOGIN_FLOW", "⚠️ Firebase no disponible (${error.message}). Probando SQLite local...")
                }
            )

            if (authPorFirebase) return@launch

            // ── 3. Fallback a base de datos SQLite / Room local (Requerimiento b) ──
            when (val result = authLocalRepository.loginUser(state.email, state.password)) {
                is AuthActionResult.Success -> {
                    persistirSesion(result.user.correo, state.rememberSession)
                    Log.i("LOGIN_FLOW", " Login exitoso via SQLite local: ${result.user.correo}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                }

                is AuthActionResult.Error -> {
                    _uiState.value = when (result.field) {
                        AuthErrorField.EMAIL -> _uiState.value.copy(
                            isLoading = false,
                            emailError = result.message
                        )
                        AuthErrorField.PASSWORD -> _uiState.value.copy(
                            isLoading = false,
                            passwordError = result.message
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

    private suspend fun persistirSesion(uid: String, recordar: Boolean) {
        prefsRepository.setRememberSession(recordar)
        if (recordar) {
            prefsRepository.setActiveSessionUid(uid)
        } else {
            prefsRepository.clearSession()
        }
    }
}
