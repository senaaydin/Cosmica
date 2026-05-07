package com.cosmica.app.domain.repository

import androidx.paging.PagingData
import com.cosmica.app.domain.model.Apod
import kotlinx.coroutines.flow.Flow

interface ApodRepository {
    suspend fun getTodayApod(): Result<Apod>
    suspend fun getApodByDate(date: String): Result<Apod>
    fun getApodPager(): Flow<PagingData<Apod>>
    suspend fun isFavorite(date: String): Boolean
    suspend fun addFavorite(apod: Apod)
    suspend fun removeFavorite(date: String)
    fun getFavorites(): Flow<List<Apod>>
}
