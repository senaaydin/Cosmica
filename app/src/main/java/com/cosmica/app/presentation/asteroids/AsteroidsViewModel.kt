package com.cosmica.app.presentation.asteroids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmica.app.domain.model.NearEarthObject
import com.cosmica.app.domain.usecase.GetAsteroidsThisWeekUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AsteroidsViewModel @Inject constructor(
    private val getAsteroidsThisWeekUseCase: GetAsteroidsThisWeekUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreenUiState<List<NearEarthObject>>>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState<List<NearEarthObject>>> = _uiState.asStateFlow()

    // Selected asteroid for the detail bottom sheet
    private val _selectedAsteroid = MutableStateFlow<NearEarthObject?>(null)
    val selectedAsteroid: StateFlow<NearEarthObject?> = _selectedAsteroid.asStateFlow()

    init {
        loadAsteroids()
    }

    fun loadAsteroids() {
        viewModelScope.launch {
            _uiState.value = ScreenUiState.Loading
            getAsteroidsThisWeekUseCase()
                .onSuccess { list ->
                    _uiState.value = if (list.isEmpty()) ScreenUiState.Empty
                    else ScreenUiState.Success(list)
                }
                .onFailure { t -> _uiState.value = ScreenUiState.Error(t.toUserMessage()) }
        }
    }

    fun selectAsteroid(neo: NearEarthObject) { _selectedAsteroid.value = neo }
    fun clearSelection() { _selectedAsteroid.value = null }
}
