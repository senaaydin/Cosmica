package com.cosmica.app.presentation.home

import app.cash.turbine.test
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.GetTodayApodUseCase
import com.cosmica.app.domain.usecase.ToggleFavoriteUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher       = StandardTestDispatcher()
    private val getTodayApodUseCase  = mockk<GetTodayApodUseCase>()
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>(relaxed = true)

    private val fakeApod = Apod(
        date = "2024-01-01", title = "Nebula", explanation = "Beautiful nebula.",
        url = "https://apod.nasa.gov/image.jpg", hdUrl = null,
        mediaType = "image", copyright = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then Success when apod loads`() = runTest {
        coEvery { getTodayApodUseCase() } returns Result.success(fakeApod)

        val viewModel = HomeViewModel(getTodayApodUseCase, toggleFavoriteUseCase)

        viewModel.uiState.test {
            assertTrue(awaitItem() is ScreenUiState.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is ScreenUiState.Success)
            assertEquals(fakeApod, (success as ScreenUiState.Success).data)
        }
    }

    @Test
    fun `emits Error state when repository fails`() = runTest {
        coEvery { getTodayApodUseCase() } returns Result.failure(RuntimeException("timeout"))

        val viewModel = HomeViewModel(getTodayApodUseCase, toggleFavoriteUseCase)

        viewModel.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val error = awaitItem()
            assertTrue(error is ScreenUiState.Error)
        }
    }

    @Test
    fun `toggleFavorite calls use case and reloads`() = runTest {
        coEvery { getTodayApodUseCase() } returns Result.success(fakeApod)

        val viewModel = HomeViewModel(getTodayApodUseCase, toggleFavoriteUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite(fakeApod)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { toggleFavoriteUseCase(fakeApod) }
        coVerify(atLeast = 2) { getTodayApodUseCase() }
    }
}
