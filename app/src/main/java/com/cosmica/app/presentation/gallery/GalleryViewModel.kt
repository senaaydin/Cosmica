package com.cosmica.app.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.AddFavoriteUseCase
import com.cosmica.app.domain.usecase.GetAllFavoritesUseCase
import com.cosmica.app.domain.usecase.GetApodRangeUseCase
import com.cosmica.app.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    getApodRangeUseCase: GetApodRangeUseCase,
    getAllFavoritesUseCase: GetAllFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
) : ViewModel() {

    val apodPagingData: Flow<PagingData<Apod>> = getApodRangeUseCase()
        .cachedIn(viewModelScope)

    val favoriteDates: StateFlow<Set<String>> = getAllFavoritesUseCase()
        .map { list -> list.map { it.date }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(apod: Apod, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (currentlyFavorite) removeFavoriteUseCase(apod.date)
            else addFavoriteUseCase(apod)
        }
    }
}
