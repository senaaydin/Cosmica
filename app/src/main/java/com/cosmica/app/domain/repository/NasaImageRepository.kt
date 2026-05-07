package com.cosmica.app.domain.repository

import androidx.paging.PagingData
import com.cosmica.app.domain.model.NasaImage
import kotlinx.coroutines.flow.Flow

interface NasaImageRepository {
    fun searchImages(query: String, mediaType: String?): Flow<PagingData<NasaImage>>
}
