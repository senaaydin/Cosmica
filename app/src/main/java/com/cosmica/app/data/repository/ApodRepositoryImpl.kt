package com.cosmica.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cosmica.app.data.local.dao.FavoriteApodDao
import com.cosmica.app.data.mapper.toDomain
import com.cosmica.app.data.mapper.toFavoriteEntity
import com.cosmica.app.data.paging.ApodPagingSource
import com.cosmica.app.data.remote.api.ApodApiService
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ApodRepositoryImpl @Inject constructor(
    private val apodApiService: ApodApiService,
    private val favoriteApodDao: FavoriteApodDao,
) : ApodRepository {

    override suspend fun getTodayApod(): Result<Apod> = runCatching {
        val dto = try {
            apodApiService.getApod()
        } catch (e: HttpException) {
            if (e.code() != 400) throw e
            // Today's APOD not yet published (NASA publishes on US Eastern time).
            val yesterday = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            apodApiService.getApod(date = yesterday)
        }
        dto.toDomain(isFavorite = favoriteApodDao.isFavorite(dto.date))
    }

    override suspend fun getApodByDate(date: String): Result<Apod> = runCatching {
        val dto = apodApiService.getApod(date = date)
        dto.toDomain(isFavorite = favoriteApodDao.isFavorite(dto.date))
    }

    override fun getApodPager(): Flow<PagingData<Apod>> =
        Pager(
            config = PagingConfig(
                pageSize           = 7,
                enablePlaceholders = false,
                prefetchDistance   = 3,
            ),
            pagingSourceFactory = { ApodPagingSource(apodApiService) },
        ).flow

    override suspend fun isFavorite(date: String): Boolean =
        favoriteApodDao.isFavorite(date)

    override suspend fun addFavorite(apod: Apod) =
        favoriteApodDao.insert(apod.toFavoriteEntity())

    override suspend fun removeFavorite(date: String) =
        favoriteApodDao.deleteByDate(date)

    override fun getFavorites(): Flow<List<Apod>> =
        favoriteApodDao.getAllFavorites().map { entities -> entities.map { it.toDomain() } }
}
