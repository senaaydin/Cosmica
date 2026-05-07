package com.cosmica.app.presentation.asteroids

import app.cash.turbine.test
import com.cosmica.app.domain.model.NearEarthObject
import com.cosmica.app.domain.usecase.GetAsteroidsThisWeekUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AsteroidsViewModelTest {

    private val testDispatcher             = StandardTestDispatcher()
    private val getAsteroidsThisWeekUseCase = mockk<GetAsteroidsThisWeekUseCase>()

    private val fakeNeo = NearEarthObject(
        id = "1", name = "2024 AB1", absoluteMagnitude = 22.5,
        estimatedDiameterMinKm = 0.05, estimatedDiameterMaxKm = 0.12,
        isPotentiallyHazardous = false, closeApproachDate = "2024-01-05",
        relativeVelocityKph = 50000.0, missDistanceKm = 1_500_000.0,
        missDistanceLunar = 3.9, orbitingBody = "Earth",
    )

    @Before fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @After  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `emits Success when asteroids are loaded`() = runTest {
        coEvery { getAsteroidsThisWeekUseCase() } returns Result.success(listOf(fakeNeo))

        val viewModel = AsteroidsViewModel(getAsteroidsThisWeekUseCase)

        viewModel.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is ScreenUiState.Success)
        }
    }

    @Test
    fun `emits Empty when list is empty`() = runTest {
        coEvery { getAsteroidsThisWeekUseCase() } returns Result.success(emptyList())

        val viewModel = AsteroidsViewModel(getAsteroidsThisWeekUseCase)

        viewModel.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is ScreenUiState.Empty)
        }
    }

    @Test
    fun `selectAsteroid and clearSelection update selectedAsteroid`() = runTest {
        coEvery { getAsteroidsThisWeekUseCase() } returns Result.success(listOf(fakeNeo))
        val viewModel = AsteroidsViewModel(getAsteroidsThisWeekUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectedAsteroid.test {
            awaitItem() // null initial
            viewModel.selectAsteroid(fakeNeo)
            assertTrue(awaitItem() == fakeNeo)
            viewModel.clearSelection()
            assertTrue(awaitItem() == null)
        }
    }
}
