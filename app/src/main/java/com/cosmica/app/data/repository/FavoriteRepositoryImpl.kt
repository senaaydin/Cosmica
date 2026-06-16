package com.cosmica.app.data.repository

import com.cosmica.app.data.local.dao.FavoriteApodDao
import com.cosmica.app.data.mapper.toDomain
import com.cosmica.app.data.mapper.toFavoriteEntity
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteApodDao,
) : FavoriteRepository {

    override suspend fun addFavorite(apod: Apod) = dao.insert(apod.toFavoriteEntity())

    override suspend fun removeFavorite(date: String) = dao.deleteByDate(date)

    override fun getFavorites(): Flow<List<Apod>> =
        dao.getAllFavorites().map { entities -> entities.map { it.toDomain() } }

    override fun isFavorite(date: String): Flow<Boolean> = dao.isFavoriteFlow(date)

    override suspend fun isFavoriteOnce(date: String): Boolean = dao.isFavoriteOnce(date)

    override fun favoritesCount(): Flow<Int> = dao.favoritesCount()
}
