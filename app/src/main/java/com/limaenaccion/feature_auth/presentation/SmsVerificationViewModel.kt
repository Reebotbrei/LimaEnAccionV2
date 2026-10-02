package com.limaenaccion.feature_auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val RESEND_SECONDS = 45

data class SmsVerificationUiState(
    val codeDigits: List<String> = List(6) { "" },
    val codeError: String? = null,
    val secondsRemaining: Int = RESEND_SECONDS,
    val verified: Boolean = false
) {
    val canResend: Boolean get() = secondsRemaining <= 0
}

class SmsVerificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SmsVerificationUiState())
    val uiState: StateFlow<SmsVerificationUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(secondsRemaining = RESEND_SECONDS)
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsRemaining > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(secondsRemaining = _uiState.value.secondsRemaining - 1)
            }
        }
    }

    fun onDigitChange(index: Int, digit: String) {
        val updated = _uiState.value.codeDigits.toMutableList()
        updated[index] = digit
        _uiState.value = _uiState.value.copy(codeDigits = updated, codeError = null)
    }

    fun onResendClicked() {
        if (_uiState.value.canResend) {
            _uiState.value = _uiState.value.copy(codeDigits = List(6) { "" })
            startTimer()
        }
    }

    fun onVerifyClicked() {
        val code = _uiState.value.codeDigits.joinToString("")
        if (code.length < 6) {
            _uiState.value = _uiState.value.copy(codeError = "Ingresa el código completo")
            return
        }
        _uiState.value = _uiState.value.copy(verified = true)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}