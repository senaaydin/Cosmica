package com.cosmica.app.presentation.home

import app.cash.turbine.test
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.AddFavoriteUseCase
import com.cosmica.app.domain.usecase.GetTodayApodUseCase
import com.cosmica.app.domain.usecase.IsFavoriteUseCase
import com.cosmica.app.domain.usecase.RemoveFavoriteUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    private val addFavoriteUseCase    = mockk<AddFavoriteUseCase>(relaxed = true)
    private val removeFavoriteUseCase = mockk<RemoveFavoriteUseCase>(relaxed = true)
    private val isFavoriteUseCase     = mockk<IsFavoriteUseCase>()

    private val fakeApod = Apod(
        date = "2024-01-01", title = "Nebula", explanation = "Beautiful nebula.",
        url = "https://apod.nasa.gov/image.jpg", hdUrl = null,
        mediaType = "image", copyright = null,
    )

    @Before fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @After  fun tearDown() { Dispatchers.resetMain() }

    private fun newViewModel(): HomeViewModel {
        every { isFavoriteUseCase(any()) } returns flowOf(false)
        return HomeViewModel(
            getTodayApodUseCase   = getTodayApodUseCase,
            addFavoriteUseCase    = addFavoriteUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
            isFavoriteUseCase     = isFavoriteUseCase,
        )
    }

    @Test
    fun `initial state is Loading then Success when apod loads`() = runTest {
        coEvery { getTodayApodUseCase() } returns Result.success(fakeApod)

        val viewModel = newViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is ScreenUiState.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            val success = expectMostRecentItem()
            assertTrue(success is ScreenUiState.Success)
            assertEquals(fakeApod.date, (success as ScreenUiState.Success).data.date)
        }
    }

    @Test
    fun `emits Error state when repository fails`() = runTest {
        coEvery { getTodayApodUseCase() } returns Result.failure(RuntimeException("timeout"))

        val viewModel = newViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem() is ScreenUiState.Error)
        }
    }

    @Test
    fun `toggleFavorite calls AddFavoriteUseCase when not currently favorited`() = runTest {
        coEvery { getTodayApodUseCase() } returns Result.success(fakeApod)
        val viewModel = newViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite(fakeApod.copy(isFavorite = false))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { addFavoriteUseCase(any()) }
        coVerify(exactly = 0) { removeFavoriteUseCase(any()) }
    }

    @Test
    fun `toggleFavorite calls RemoveFavoriteUseCase when currently favorited`() = runTest {
        coEvery { getTodayApodUseCase() } returns Result.success(fakeApod)
        val viewModel = newViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite(fakeApod.copy(isFavorite = true))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { removeFavoriteUseCase(fakeApod.date) }
        coVerify(exactly = 0) { addFavoriteUseCase(any()) }
    }
}
