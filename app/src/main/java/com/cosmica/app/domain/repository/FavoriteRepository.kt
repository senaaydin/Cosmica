package com.cosmica.app.domain.repository

import com.cosmica.app.domain.model.Apod
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun addFavorite(apod: Apod)
    suspend fun removeFavorite(date: String)
    fun getFavorites(): Flow<List<Apod>>
    fun isFavorite(date: String): Flow<Boolean>
    suspend fun isFavoriteOnce(date: String): Boolean
    fun favoritesCount(): Flow<Int>
}
