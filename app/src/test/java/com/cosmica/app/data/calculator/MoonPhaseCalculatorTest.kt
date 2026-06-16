package com.cosmica.app.data.calculator

import com.cosmica.app.domain.model.MoonPhaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MoonPhaseCalculatorTest {

    /** Each entry is a verified full moon date (NASA / timeanddate.com). */
    private val knownFullMoons = listOf(
        LocalDate.of(2024, 1, 25),   // Wolf Moon
        LocalDate.of(2024, 8, 19),   // Sturgeon supermoon
        LocalDate.of(2025, 3, 14),   // Worm Moon
        LocalDate.of(2026, 1, 3),    // Wolf Moon
    )

    /** Each entry is a verified new moon date. */
    private val knownNewMoons = listOf(
        LocalDate.of(2024, 2, 9),
        LocalDate.of(2024, 9, 3),
        LocalDate.of(2025, 4, 27),
    )

    @Test
    fun `phase near 0_5 on known full moon dates`() {
        knownFullMoons.forEach { date ->
            val phase = MoonPhaseCalculator.phaseAt(date)
            // Allow ±0.05 (≈ ±1.5 days) given the fixed-synodic approximation
            assertTrue(
                "Expected full moon (~0.5) on $date but was $phase",
                phase in 0.45..0.55,
            )
        }
    }

    @Test
    fun `phase near 0_or_1 on known new moon dates`() {
        knownNewMoons.forEach { date ->
            val phase = MoonPhaseCalculator.phaseAt(date)
            val nearZero = phase <= 0.05 || phase >= 0.95
            assertTrue("Expected new moon on $date but was $phase", nearZero)
        }
    }

    @Test
    fun `phaseType classifies known full moon as FULL_MOON`() {
        val moon = MoonPhaseCalculator.calculate(LocalDate.of(2024, 8, 19))
        assertEquals(MoonPhaseType.FULL_MOON, moon.phaseType)
    }

    @Test
    fun `illumination is 100 percent on full moon and near 0 on new moon`() {
        val full = MoonPhaseCalculator.calculate(LocalDate.of(2024, 8, 19))
        val new  = MoonPhaseCalculator.calculate(LocalDate.of(2024, 9, 3))

        assertTrue("Full moon should be highly illuminated", full.illuminationPercent >= 95)
        assertTrue("New moon should be barely illuminated", new.illuminationPercent <= 5)
    }

    @Test
    fun `daysUntilFullMoon is near zero on a full moon day`() {
        // Fixed-synodic approximation can be off by ±1 day on either side of full
        val moon = MoonPhaseCalculator.calculate(LocalDate.of(2024, 8, 19))
        val days = moon.daysUntilFullMoon
        assertTrue(
            "Expected 0–2 days to next full moon on a full moon day but was $days",
            days in 0..2 || days in 27..30,
        )
    }

    @Test
    fun `phase value always in 0 to 1 range`() {
        // Spot-check across a wide date range
        val dates = listOf(
            LocalDate.of(1900, 1, 1),
            LocalDate.of(2024, 5, 15),
            LocalDate.of(2100, 12, 31),
        )
        dates.forEach { date ->
            val phase = MoonPhaseCalculator.phaseAt(date)
            assertTrue("Phase out of range on $date: $phase", phase in 0.0..1.0)
        }
    }
}
