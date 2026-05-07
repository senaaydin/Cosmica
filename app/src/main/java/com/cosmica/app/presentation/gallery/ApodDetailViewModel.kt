package com.cosmica.app.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.GetApodByDateUseCase
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApodDetailViewModel @Inject constructor(
    private val getApodByDateUseCase: GetApodByDateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreenUiState<Apod>>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState<Apod>> = _uiState.asStateFlow()

    fun loadApod(date: String) {
        viewModelScope.launch {
            _uiState.value = ScreenUiState.Loading
            getApodByDateUseCase(date)
                .onSuccess { apod -> _uiState.value = ScreenUiState.Success(apod) }
                .onFailure { t   -> _uiState.value = ScreenUiState.Error(t.toUserMessage()) }
        }
    }
}
