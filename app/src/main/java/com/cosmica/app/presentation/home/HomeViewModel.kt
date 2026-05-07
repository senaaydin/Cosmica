package com.cosmica.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.GetTodayApodUseCase
import com.cosmica.app.domain.usecase.ToggleFavoriteUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayApodUseCase: GetTodayApodUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreenUiState<Apod>>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState<Apod>> = _uiState.asStateFlow()

    init {
        loadTodayApod()
    }

    fun loadTodayApod() {
        viewModelScope.launch {
            _uiState.value = ScreenUiState.Loading
            getTodayApodUseCase()
                .onSuccess { apod -> _uiState.value = ScreenUiState.Success(apod) }
                .onFailure { t  -> _uiState.value = ScreenUiState.Error(t.toUserMessage()) }
        }
    }

    fun toggleFavorite(apod: Apod) {
        viewModelScope.launch {
            toggleFavoriteUseCase(apod)
            // Refresh to pick up the updated isFavorite flag
            loadTodayApod()
        }
    }
}
