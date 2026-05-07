package com.cosmica.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cosmica.app.domain.model.NasaImage
import com.cosmica.app.domain.usecase.SearchNasaImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchNasaImagesUseCase: SearchNasaImagesUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _mediaTypeFilter = MutableStateFlow<String?>(null)
    val mediaTypeFilter: StateFlow<String?> = _mediaTypeFilter.asStateFlow()

    val searchResults: Flow<PagingData<NasaImage>> =
        combine(_query, _mediaTypeFilter) { q, mt -> q to mt }
            .debounce(300L)
            .flatMapLatest { (query, mediaType) ->
                if (query.isBlank()) flowOf(PagingData.empty())
                else searchNasaImagesUseCase(query, mediaType)
            }
            .cachedIn(viewModelScope)

    private val _selectedImage = MutableStateFlow<NasaImage?>(null)
    val selectedImage: StateFlow<NasaImage?> = _selectedImage.asStateFlow()

    fun onQueryChange(newQuery: String) { _query.value = newQuery }
    fun onMediaTypeFilterChange(mediaType: String?) { _mediaTypeFilter.value = mediaType }
    fun selectImage(image: NasaImage) { _selectedImage.value = image }
    fun clearSelectedImage() { _selectedImage.value = null }
}
