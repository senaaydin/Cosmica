package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.NearEarthObject
import com.cosmica.app.domain.repository.NeoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAsteroidsThisWeekUseCaseTest {

    private val repository = mockk<NeoRepository>()
    private val useCase    = GetAsteroidsThisWeekUseCase(repository)

    private val fakeNeo = NearEarthObject(
        id                     = "1",
        name                   = "2024 AB1",
        absoluteMagnitude      = 22.5,
        estimatedDiameterMinKm = 0.05,
        estimatedDiameterMaxKm = 0.12,
        isPotentiallyHazardous = false,
        closeApproachDate      = "2024-01-05",
        relativeVelocityKph    = 50000.0,
        missDistanceKm         = 1_500_000.0,
        missDistanceLunar      = 3.9,
        orbitingBody           = "Earth",
    )

    @Test
    fun `returns sorted list on success`() = runTest {
        coEvery { repository.getAsteroidsThisWeek() } returns Result.success(listOf(fakeNeo))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `propagates failure from repository`() = runTest {
        coEvery { repository.getAsteroidsThisWeek() } returns Result.failure(Exception("API down"))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
