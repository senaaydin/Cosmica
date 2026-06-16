package com.cosmica.app.presentation.moonphase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmica.app.domain.model.Coordinates
import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.repository.CitySearchResult
import com.cosmica.app.domain.usecase.GetCurrentLocationUseCase
import com.cosmica.app.domain.usecase.GetMoonPhaseUseCase
import com.cosmica.app.domain.usecase.SearchCitiesUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LocationPermissionState { Unknown, Granted, Denied, DeniedPermanently }

@OptIn(FlowPreview::class)
@HiltViewModel
class MoonPhaseViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getMoonPhaseUseCase: GetMoonPhaseUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreenUiState<MoonPhase>>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState<MoonPhase>> = _uiState.asStateFlow()

    private val _permissionState = MutableStateFlow(LocationPermissionState.Unknown)
    val permissionState: StateFlow<LocationPermissionState> = _permissionState.asStateFlow()

    private val _cityQuery = MutableStateFlow("")
    val cityQuery: StateFlow<String> = _cityQuery.asStateFlow()

    private val _citySuggestions = MutableStateFlow<List<CitySearchResult>>(emptyList())
    val citySuggestions: StateFlow<List<CitySearchResult>> = _citySuggestions.asStateFlow()

    init {
        viewModelScope.launch {
            _cityQuery
                .drop(1)
                .debounce(350L)
                .onEach { query ->
                    searchCitiesUseCase(query)
                        .onSuccess { _citySuggestions.value = it }
                        .onFailure { _citySuggestions.value = emptyList() }
                }
                .collect {}
        }
    }

    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        _permissionState.value = when {
            granted              -> LocationPermissionState.Granted
            shouldShowRationale  -> LocationPermissionState.Denied
            else                 -> LocationPermissionState.DeniedPermanently
        }
        if (granted) loadFromCurrentLocation()
    }

    fun loadFromCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = ScreenUiState.Loading
            getCurrentLocationUseCase()
                .onSuccess { fetchMoonPhase(it) }
                .onFailure { _uiState.value = ScreenUiState.Error(it.toUserMessage()) }
        }
    }

    fun selectCity(city: CitySearchResult) {
        _cityQuery.value = city.displayName
        _citySuggestions.value = emptyList()
        viewModelScope.launch {
            _uiState.value = ScreenUiState.Loading
            fetchMoonPhase(city.coordinates)
        }
    }

    fun onCityQueryChange(query: String) { _cityQuery.value = query }

    private suspend fun fetchMoonPhase(coordinates: Coordinates) {
        getMoonPhaseUseCase(coordinates)
            .onSuccess { _uiState.value = ScreenUiState.Success(it) }
            .onFailure { _uiState.value = ScreenUiState.Error(it.toUserMessage()) }
    }
}
