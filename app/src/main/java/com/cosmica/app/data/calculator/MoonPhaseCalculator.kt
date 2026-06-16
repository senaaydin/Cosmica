package com.cosmica.app.data.calculator

import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.model.MoonPhaseType
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * Pure moon-phase math. No I/O, no Android types.
 *
 * Uses a fixed synodic month and a known new moon epoch:
 *   2000-01-06 18:14 UTC (J2000-anchored new moon, accuracy ≈ ±1 day).
 *
 * This is plenty for "what does the moon look like tonight" — the
 * naked-eye terminator only shifts noticeably over multi-day windows.
 */
object MoonPhaseCalculator {

    private const val SYNODIC_MONTH_DAYS = 29.530588853

    // Known new moon: 2000-01-06 18:14 UTC → expressed in fractional days since the epoch
    private val KNOWN_NEW_MOON = LocalDate.of(2000, 1, 6)
        .atTime(18, 14)
        .toEpochSecond(ZoneOffset.UTC) / 86_400.0

    fun calculate(date: LocalDate, locationName: String = ""): MoonPhase {
        val phase = phaseAt(date)
        return MoonPhase(
            phase               = phase,
            phaseType           = phase.toPhaseType(),
            illuminationPercent = phase.toIlluminationPercent(),
            daysUntilFullMoon   = phase.daysUntilFullMoon(),
            locationName        = locationName,
        )
    }

    /** Returns lunar phase as a value in [0, 1) where 0 = new, 0.5 = full. */
    fun phaseAt(date: LocalDate): Double {
        val daysSinceEpoch = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) / 86_400.0
        val cycles = (daysSinceEpoch - KNOWN_NEW_MOON) / SYNODIC_MONTH_DAYS
        val phase = cycles - cycles.toLong()
        return if (phase < 0) phase + 1.0 else phase
    }

    private fun Double.toPhaseType(): MoonPhaseType = when (this) {
        in 0.0..0.0625, in 0.9375..1.0 -> MoonPhaseType.NEW_MOON
        in 0.0625..0.1875              -> MoonPhaseType.WAXING_CRESCENT
        in 0.1875..0.3125              -> MoonPhaseType.FIRST_QUARTER
        in 0.3125..0.4375              -> MoonPhaseType.WAXING_GIBBOUS
        in 0.4375..0.5625              -> MoonPhaseType.FULL_MOON
        in 0.5625..0.6875              -> MoonPhaseType.WANING_GIBBOUS
        in 0.6875..0.8125              -> MoonPhaseType.LAST_QUARTER
        else                            -> MoonPhaseType.WANING_CRESCENT
    }

    // Illumination = (1 − cos 2πp) / 2 — the standard fraction-of-disc-lit formula
    private fun Double.toIlluminationPercent(): Int =
        ((1.0 - cos(2.0 * PI * this)) / 2.0 * 100.0).roundToInt().coerceIn(0, 100)

    private fun Double.daysUntilFullMoon(): Int {
        val phaseDelta = if (this <= 0.5) 0.5 - this else 1.5 - this
        return (phaseDelta * SYNODIC_MONTH_DAYS).roundToInt()
    }
}
