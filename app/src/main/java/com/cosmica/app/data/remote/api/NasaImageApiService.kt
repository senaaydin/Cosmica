package com.cosmica.app.data.remote.api

import com.cosmica.app.data.remote.dto.NasaImageSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaImageApiService {

    @GET("search")
    suspend fun searchImages(
        @Query("q")          query: String,
        @Query("media_type") mediaType: String? = null,
        @Query("page")       page: Int = 1,
        @Query("page_size")  pageSize: Int = 20,
    ): NasaImageSearchResponseDto
}
