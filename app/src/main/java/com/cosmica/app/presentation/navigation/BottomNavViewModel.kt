package com.cosmica.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmica.app.domain.usecase.GetFavoritesCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BottomNavViewModel @Inject constructor(
    getFavoritesCountUseCase: GetFavoritesCountUseCase,
) : ViewModel() {

    val favoritesCount: StateFlow<Int> = getFavoritesCountUseCase()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )
}
