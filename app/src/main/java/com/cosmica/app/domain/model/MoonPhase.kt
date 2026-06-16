package com.cosmica.app.domain.model

enum class MoonPhaseType {
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS,
    FULL_MOON,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT,
}

data class MoonPhase(
    val phase: Double,
    val phaseType: MoonPhaseType,
    val illuminationPercent: Int,
    val daysUntilFullMoon: Int,
    val locationName: String,
)
