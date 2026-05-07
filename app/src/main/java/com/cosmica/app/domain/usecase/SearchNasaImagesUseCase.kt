package com.cosmica.app.domain.usecase

import androidx.paging.PagingData
import com.cosmica.app.domain.model.NasaImage
import com.cosmica.app.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNasaImagesUseCase @Inject constructor(
    private val repository: NasaImageRepository,
) {
    operator fun invoke(query: String, mediaType: String?): Flow<PagingData<NasaImage>> =
        repository.searchImages(query, mediaType)
}
