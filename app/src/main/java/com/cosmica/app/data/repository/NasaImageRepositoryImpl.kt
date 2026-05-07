package com.cosmica.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cosmica.app.data.paging.NasaImagePagingSource
import com.cosmica.app.data.remote.api.NasaImageApiService
import com.cosmica.app.domain.model.NasaImage
import com.cosmica.app.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NasaImageRepositoryImpl @Inject constructor(
    private val nasaImageApiService: NasaImageApiService,
) : NasaImageRepository {

    override fun searchImages(query: String, mediaType: String?): Flow<PagingData<NasaImage>> =
        Pager(
            config = PagingConfig(
                pageSize           = 20,
                enablePlaceholders = false,
                prefetchDistance   = 4,
            ),
            pagingSourceFactory = {
                NasaImagePagingSource(
                    nasaImageApiService = nasaImageApiService,
                    query               = query,
                    mediaType           = mediaType,
                )
            },
        ).flow
}
