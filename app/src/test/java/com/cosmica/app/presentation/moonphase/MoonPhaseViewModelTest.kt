package com.cosmica.app.presentation.moonphase

import app.cash.turbine.test
import com.cosmica.app.domain.model.Coordinates
import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.model.MoonPhaseType
import com.cosmica.app.domain.usecase.GetCurrentLocationUseCase
import com.cosmica.app.domain.usecase.GetMoonPhaseUseCase
import com.cosmica.app.domain.usecase.SearchCitiesUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoonPhaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getCurrentLocationUseCase = mockk<GetCurrentLocationUseCase>()
    private val getMoonPhaseUseCase       = mockk<GetMoonPhaseUseCase>()
    private val searchCitiesUseCase       = mockk<SearchCitiesUseCase>()

    private val coords = Coordinates(latitude = 41.0, longitude = 29.0)

    private val fakeMoonPhase = MoonPhase(
        phase = 0.5, phaseType = MoonPhaseType.FULL_MOON,
        illuminationPercent = 100,
        daysUntilFullMoon = 0, locationName = "Istanbul",
    )

    @Before fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @After  fun tearDown() { Dispatchers.resetMain() }

    private fun newViewModel() = MoonPhaseViewModel(
        getCurrentLocationUseCase = getCurrentLocationUseCase,
        getMoonPhaseUseCase       = getMoonPhaseUseCase,
        searchCitiesUseCase       = searchCitiesUseCase,
    )

    @Test
    fun `granted permission triggers location fetch and emits Success`() = runTest {
        coEvery { getCurrentLocationUseCase() } returns Result.success(coords)
        coEvery { getMoonPhaseUseCase(coords) } returns Result.success(fakeMoonPhase)

        val viewModel = newViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is ScreenUiState.Loading)
            viewModel.onPermissionResult(granted = true, shouldShowRationale = false)
            testDispatcher.scheduler.advanceUntilIdle()
            val final = expectMostRecentItem()
            assertTrue(final is ScreenUiState.Success)
            assertEquals(fakeMoonPhase, (final as ScreenUiState.Success).data)
        }

        assertEquals(LocationPermissionState.Granted, viewModel.permissionState.value)
    }

    @Test
    fun `denied permission marks state Denied without fetching`() = runTest {
        val viewModel = newViewModel()
        viewModel.onPermissionResult(granted = false, shouldShowRationale = true)
        assertEquals(LocationPermissionState.Denied, viewModel.permissionState.value)
    }

    @Test
    fun `permanently denied permission marks state DeniedPermanently`() = runTest {
        val viewModel = newViewModel()
        viewModel.onPermissionResult(granted = false, shouldShowRationale = false)
        assertEquals(LocationPermissionState.DeniedPermanently, viewModel.permissionState.value)
    }

    @Test
    fun `location failure emits Error`() = runTest {
        coEvery { getCurrentLocationUseCase() } returns Result.failure(RuntimeException("No GPS"))

        val viewModel = newViewModel()
        viewModel.onPermissionResult(granted = true, shouldShowRationale = false)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem() is ScreenUiState.Error)
        }
    }

    @Test
    fun `selectCity fetches moon phase for chosen coordinates`() = runTest {
        coEvery { getMoonPhaseUseCase(coords) } returns Result.success(fakeMoonPhase)

        val viewModel = newViewModel()
        val city = com.cosmica.app.domain.repository.CitySearchResult(
            name = "Istanbul", country = "TR", state = null, coordinates = coords,
        )
        viewModel.selectCity(city)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem() is ScreenUiState.Success)
        }
        assertEquals("Istanbul, TR", viewModel.cityQuery.value)
    }
}
