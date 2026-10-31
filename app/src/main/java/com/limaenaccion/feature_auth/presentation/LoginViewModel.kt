package com.limaenaccion.feature_auth.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.SqliteDatabaseHelper
import com.limaenaccion.core.data.local.entities.ContactoEntity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private const val MAX_LOGIN_ATTEMPTS = 5

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
    val showServerConfig: Boolean = false,

    // Bloqueo por 5 intentos fallidos
    val remainingAttempts: Int = MAX_LOGIN_ATTEMPTS,
    val isLockedOut: Boolean = false,

    // Diálogo flotante "¿Desea Recordar la sesión?" post-login
    val showRememberSessionDialog: Boolean = false,
    val pendingUserUid: String? = null
)

class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val authLocalRepository: AuthLocalRepository = AuthLocalRepositoryImpl(application),
    private val prefsRepository: PreferencesRepository = PreferencesRepositoryImpl(application),
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao(),
        contactoDao = AppDatabase.getInstance(application).contactoDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    private val contactoDao = AppDatabase.getInstance(application).contactoDao()
    private val sqliteHelper = SqliteDatabaseHelper.getInstance(application)

    init {
        // Cargar URL del servidor
        viewModelScope.launch {
            val savedUrl = prefsRepository.serverBaseUrl.first()
            _uiState.value = _uiState.value.copy(
                serverUrl = savedUrl
            )
            RetrofitClient.setBaseUrl(savedUrl)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null,
            generalError = if (!_uiState.value.isLockedOut) null else _uiState.value.generalError
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null,
            generalError = if (!_uiState.value.isLockedOut) null else _uiState.value.generalError
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

    fun onServerUrlChanged(newUrl: String) {
        viewModelScope.launch {
            prefsRepository.setServerBaseUrl(newUrl)
            RetrofitClient.setBaseUrl(newUrl)
            _uiState.value = _uiState.value.copy(serverUrl = newUrl, showServerConfig = false)
        }
    }

    /**
     * Confirma la elección del diálogo flotante "¿Desea Recordar la sesión? SI / NO"
     */
    fun onConfirmRememberSession(remember: Boolean) {
        val uid = _uiState.value.pendingUserUid ?: ""
        viewModelScope.launch {
            persistirSesion(uid, remember)
            _uiState.value = _uiState.value.copy(
                showRememberSessionDialog = false,
                rememberSession = remember,
                loginSuccess = true
            )
        }
    }

    fun onLoginClicked() {
        val state = _uiState.value
        if (state.isLoading || state.isLockedOut) return

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
                val response = RetrofitClient.instance.loginUser(
                    LoginRequest(
                        email = state.email.trim(),
                        password = state.password
                    )
                )
                servidorNodeRespondio = true
                if (response.status == "success" && response.data != null) {
                    val user = response.data
                    val foto = user.fotoPerfil.ifBlank { user.profilePhotoUrl }
                    val entity = UsuarioEntity(
                        correo = user.email.ifBlank { user.correo },
                        dni = user.dni,
                        nombreCompleto = user.fullName.ifBlank { user.nombreCompleto },
                        genero = user.gender.ifBlank { user.genero },
                        telefono = user.phoneNumber.ifBlank { user.telefono },
                        distrito = user.district.ifBlank { user.distrito },
                        urbanizacion = user.neighborhood.ifBlank { user.urbanizacion },
                        contrasena = state.password,
                        fotoPerfil = foto
                    )
                    SessionManager.setCurrentUser(entity)

                    withContext(Dispatchers.IO) {
                        try {
                            usuarioDao.guardarUsuario(entity)
                            sqliteHelper.guardarUsuario(entity)
                            if (user.contactosAuxilio.isNotEmpty()) {
                                contactoDao.eliminarTodos()
                                for (c in user.contactosAuxilio) {
                                    contactoDao.guardarContacto(
                                        ContactoEntity(
                                            id = c.id.toLongOrNull() ?: 0L,
                                            nombre = c.nombre,
                                            telefono = c.telefono,
                                            parentesco = c.parentesco,
                                            esPrincipal = c.esPrincipal,
                                            correoUsuario = entity.correo
                                        )
                                    )
                                }
                            } else {
                                usuarioRepository.sincronizarContactosDesdeNube(entity.correo)
                            }
                        } catch (e: Exception) {
                            Log.w("LOGIN_FLOW", "Error guardando cache de usuario: ${e.message}")
                        }
                    }

                    Log.i("LOGIN_FLOW", "✅ Login exitoso consumiendo API Service (Node.js): ${entity.correo}")
                    handleLoginSuccess(user.uid.ifBlank { user.id })
                    return@launch
                } else {
                    handleFailedAttempt("Contraseña incorrecta")
                    return@launch
                }
            } catch (e: retrofit2.HttpException) {
                servidorNodeRespondio = true
                Log.w("LOGIN_FLOW", "❌ API Service rechazó credenciales (HTTP ${e.code()})")
                handleFailedAttempt("Credenciales incorrectas")
                return@launch
            } catch (e: java.io.IOException) {
                Log.w("LOGIN_FLOW", "⚠️ API Service Node.js no disponible (${e.message}). Intentando respaldos...")
            } catch (e: Exception) {
                Log.w("LOGIN_FLOW", "⚠️ Excepción en login (${e.message})")
            }

            if (servidorNodeRespondio) return@launch

            // ── 2. Fallback a Firebase Cloud Firestore ──
            val resultFirestore = usuarioRepository.loginConFirestore(state.email.trim(), state.password)
            val fbUser = resultFirestore.getOrNull()
            if (fbUser != null) {
                val foto = fbUser.profilePhotoUrl
                val entity = UsuarioEntity(
                    correo = fbUser.email,
                    dni = fbUser.dni,
                    nombreCompleto = fbUser.fullName,
                    genero = fbUser.gender,
                    telefono = fbUser.phoneNumber,
                    distrito = fbUser.district,
                    urbanizacion = fbUser.neighborhood,
                    contrasena = state.password,
                    fotoPerfil = foto
                )
                SessionManager.setCurrentUser(entity)

                withContext(Dispatchers.IO) {
                    try {
                        usuarioDao.guardarUsuario(entity)
                        sqliteHelper.guardarUsuario(entity)
                        usuarioRepository.sincronizarContactosDesdeNube(entity.correo)
                    } catch (e: Exception) {
                        Log.w("LOGIN_FLOW", "Error guardando cache de usuario: ${e.message}")
                    }
                }

                Log.i("LOGIN_FLOW", "✅ Login exitoso via Firebase Cloud Firestore: ${entity.correo}")
                handleLoginSuccess(fbUser.uid)
                return@launch
            } else if (resultFirestore.isFailure) {
                Log.w("LOGIN_FLOW", "⚠️ Firebase no disponible (${resultFirestore.exceptionOrNull()?.message}). Probando SQLite local...")
            }

            // ── 3. Fallback a base de datos SQLite / Room local (Requerimiento b) ──
            when (val result = authLocalRepository.loginUser(state.email, state.password)) {
                is AuthActionResult.Success -> {
                    Log.i("LOGIN_FLOW", "✅ Login exitoso via SQLite local: ${result.user.correo}")
                    handleLoginSuccess(result.user.correo)
                }

                is AuthActionResult.Error -> {
                    handleFailedAttempt(result.message)
                }
            }
        }
    }

    private fun handleLoginSuccess(uid: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            remainingAttempts = MAX_LOGIN_ATTEMPTS,
            isLockedOut = false,
            pendingUserUid = uid,
            showRememberSessionDialog = true // Muestra el diálogo flotante "¿Desea Recordar la sesión?"
        )
    }

    private fun handleFailedAttempt(message: String) {
        val newRemaining = (_uiState.value.remainingAttempts - 1).coerceAtLeast(0)
        if (newRemaining <= 0) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                remainingAttempts = 0,
                isLockedOut = true,
                passwordError = null,
                generalError = "Credenciales Incorrectas.. recuperar Cuenta"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                remainingAttempts = newRemaining,
                isLockedOut = false,
                passwordError = null,
                generalError = "$message (Te quedan $newRemaining intentos de 5)"
            )
        }
    }

    private suspend fun persistirSesion(uid: String, recordar: Boolean) {
        prefsRepository.setRememberSession(recordar)
        if (recordar && uid.isNotBlank()) {
            prefsRepository.setActiveSessionUid(uid)
        } else {
            prefsRepository.clearSession()
        }
    }
}
