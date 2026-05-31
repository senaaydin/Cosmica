package com.cosmica.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.AddFavoriteUseCase
import com.cosmica.app.domain.usecase.GetTodayApodUseCase
import com.cosmica.app.domain.usecase.IsFavoriteUseCase
import com.cosmica.app.domain.usecase.RemoveFavoriteUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayApodUseCase: GetTodayApodUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreenUiState<Apod>>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState<Apod>> = _uiState.asStateFlow()

    init {
        loadTodayApod()

        // Reactively keep the isFavorite flag in sync with Room
        _uiState
            .flatMapLatest { state ->
                if (state is ScreenUiState.Success) isFavoriteUseCase(state.data.date)
                else flowOf(false)
            }
            .onEach { isFav ->
                val current = _uiState.value
                if (current is ScreenUiState.Success && current.data.isFavorite != isFav) {
                    _uiState.value = ScreenUiState.Success(current.data.copy(isFavorite = isFav))
                }
            }
            .launchIn(viewModelScope)
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
            if (apod.isFavorite) removeFavoriteUseCase(apod.date)
            else addFavoriteUseCase(apod)
            // Flow subscription above will pick up the new state automatically
        }
    }
}
