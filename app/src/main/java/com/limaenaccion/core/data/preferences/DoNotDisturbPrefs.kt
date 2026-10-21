package com.limaenaccion.core.data.preferences

// Pref #10 - Modo No Molestar
data class DoNotDisturbPrefs(
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val endHour: Int = 7
)
