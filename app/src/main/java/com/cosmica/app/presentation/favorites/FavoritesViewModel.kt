package com.cosmica.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.AddFavoriteUseCase
import com.cosmica.app.domain.usecase.GetAllFavoritesUseCase
import com.cosmica.app.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getAllFavoritesUseCase: GetAllFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
) : ViewModel() {

    val favorites: StateFlow<List<Apod>> = getAllFavoritesUseCase()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // One-shot snackbar events; replay = 0 so re-subscribing doesn't replay old snackbars
    private val _snackbarEvents = Channel<DeletedEvent>(capacity = Channel.BUFFERED)
    val snackbarEvents: Flow<DeletedEvent> = _snackbarEvents.receiveAsFlow()

    fun remove(apod: Apod) {
        viewModelScope.launch {
            removeFavoriteUseCase(apod.date)
            _snackbarEvents.send(DeletedEvent(apod))
        }
    }

    fun undoDelete(apod: Apod) {
        viewModelScope.launch {
            addFavoriteUseCase(apod)
        }
    }

    data class DeletedEvent(val apod: Apod)
}
