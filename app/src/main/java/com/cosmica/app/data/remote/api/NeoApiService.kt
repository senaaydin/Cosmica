package com.cosmica.app.data.remote.api

import com.cosmica.app.data.remote.dto.NeoWsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NeoApiService {

    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(
        @Query("start_date") startDate: String,
        @Query("end_date")   endDate: String,
    ): NeoWsResponseDto
}
