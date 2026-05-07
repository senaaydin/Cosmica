package com.cosmica.app.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.GetApodRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    getApodRangeUseCase: GetApodRangeUseCase,
) : ViewModel() {

    val apodPagingData: Flow<PagingData<Apod>> = getApodRangeUseCase()
        .cachedIn(viewModelScope)
}
