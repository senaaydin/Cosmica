package com.cosmica.app.data.remote.api

import com.cosmica.app.data.remote.dto.ApodDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApiService {

    @GET("planetary/apod")
    suspend fun getApod(
        @Query("api_key") apiKey: String,
        @Query("date") date: String? = null,
    ): ApodDto

    @GET("planetary/apod")
    suspend fun getApodRange(
        @Query("api_key")    apiKey: String,
        @Query("start_date") startDate: String,
        @Query("end_date")   endDate: String,
    ): List<ApodDto>
}
