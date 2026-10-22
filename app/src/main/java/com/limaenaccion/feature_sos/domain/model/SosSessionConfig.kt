package com.limaenaccion.feature_sos.domain.model

object SosSessionConfig {
    var isSilentMode: Boolean = false
    var isDemoMode: Boolean = false

    fun reset() {
        isSilentMode = false
        isDemoMode = false
    }
}
