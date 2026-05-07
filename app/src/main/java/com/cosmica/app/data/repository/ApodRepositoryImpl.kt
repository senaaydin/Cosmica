package com.cosmica.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cosmica.app.BuildConfig
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
            apodApiService.getApod(apiKey = BuildConfig.NASA_API_KEY)
        } catch (e: HttpException) {
            if (e.code() != 400) throw e
            // Today's APOD not yet published (NASA publishes on US Eastern time).
            val yesterday = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            apodApiService.getApod(apiKey = BuildConfig.NASA_API_KEY, date = yesterday)
        }
        dto.toDomain(isFavorite = favoriteApodDao.isFavorite(dto.date))
    }

    override suspend fun getApodByDate(date: String): Result<Apod> = runCatching {
        val dto = apodApiService.getApod(apiKey = BuildConfig.NASA_API_KEY, date = date)
        val fav = favoriteApodDao.isFavorite(dto.date)
        dto.toDomain(isFavorite = fav)
    }

    override fun getApodPager(): Flow<PagingData<Apod>> {
        return Pager(
            config = PagingConfig(
                pageSize         = 20,
                enablePlaceholders = false,
                prefetchDistance = 5,
            ),
            pagingSourceFactory = {
                ApodPagingSource(
                    apodApiService = apodApiService,
                    apiKey         = BuildConfig.NASA_API_KEY,
                )
            },
        ).flow
    }

    override suspend fun isFavorite(date: String): Boolean =
        favoriteApodDao.isFavorite(date)

    override suspend fun addFavorite(apod: Apod) =
        favoriteApodDao.insert(apod.toFavoriteEntity())

    override suspend fun removeFavorite(date: String) =
        favoriteApodDao.deleteByDate(date)

    override fun getFavorites(): Flow<List<Apod>> =
        favoriteApodDao.getAllFavorites().map { entities -> entities.map { it.toDomain() } }
}
