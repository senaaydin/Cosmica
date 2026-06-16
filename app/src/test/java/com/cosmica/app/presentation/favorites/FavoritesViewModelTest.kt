package com.cosmica.app.presentation.favorites

import app.cash.turbine.test
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.AddFavoriteUseCase
import com.cosmica.app.domain.usecase.GetAllFavoritesUseCase
import com.cosmica.app.domain.usecase.RemoveFavoriteUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher  = StandardTestDispatcher()
    private val getAllFavorites = mockk<GetAllFavoritesUseCase>()
    private val addFavorite     = mockk<AddFavoriteUseCase>(relaxed = true)
    private val removeFavorite  = mockk<RemoveFavoriteUseCase>(relaxed = true)

    private val apod = Apod(
        date = "2024-01-25", title = "Wolf Moon", explanation = "...",
        url = "u", hdUrl = null, mediaType = "image", copyright = null,
    )

    @Before fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @After  fun tearDown() { Dispatchers.resetMain() }

    private fun newViewModel() = FavoritesViewModel(
        getAllFavoritesUseCase = getAllFavorites,
        addFavoriteUseCase     = addFavorite,
        removeFavoriteUseCase  = removeFavorite,
    )

    @Test
    fun `favorites StateFlow mirrors repository emissions`() = runTest {
        every { getAllFavorites() } returns flowOf(listOf(apod))

        val vm = newViewModel()

        vm.favorites.test {
            assertEquals(emptyList<Apod>(), awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(listOf(apod), awaitItem())
        }
    }

    @Test
    fun `remove emits DeletedEvent on snackbarEvents`() = runTest {
        every { getAllFavorites() } returns flowOf(emptyList())
        coEvery { removeFavorite(apod.date) } returns Unit

        val vm = newViewModel()

        vm.snackbarEvents.test {
            vm.remove(apod)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(apod, awaitItem().apod)
        }
        coVerify(exactly = 1) { removeFavorite(apod.date) }
    }

    @Test
    fun `undoDelete calls AddFavoriteUseCase`() = runTest {
        every { getAllFavorites() } returns flowOf(emptyList())
        coEvery { addFavorite(apod) } returns Unit

        val vm = newViewModel()
        vm.undoDelete(apod)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { addFavorite(apod) }
    }
}
