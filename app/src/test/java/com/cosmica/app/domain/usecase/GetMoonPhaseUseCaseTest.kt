package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Coordinates
import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.model.MoonPhaseType
import com.cosmica.app.domain.repository.MoonPhaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMoonPhaseUseCaseTest {

    private val repository = mockk<MoonPhaseRepository>()
    private val useCase    = GetMoonPhaseUseCase(repository)

    private val coords = Coordinates(latitude = 41.0082, longitude = 28.9784)

    private val fakeMoonPhase = MoonPhase(
        phase               = 0.5,
        phaseType           = MoonPhaseType.FULL_MOON,
        illuminationPercent = 100,
        daysUntilFullMoon   = 0,
        locationName        = "Istanbul, TR",
    )

    @Test
    fun `returns moon phase on success`() = runTest {
        coEvery { repository.getMoonPhase(coords) } returns Result.success(fakeMoonPhase)

        val result = useCase(coords)

        assertTrue(result.isSuccess)
        assertEquals(fakeMoonPhase, result.getOrNull())
        coVerify(exactly = 1) { repository.getMoonPhase(coords) }
    }

    @Test
    fun `propagates failure from repository`() = runTest {
        coEvery { repository.getMoonPhase(coords) } returns Result.failure(Exception("OpenWeather 401"))

        val result = useCase(coords)

        assertTrue(result.isFailure)
    }
}
