package com.cosmica.app.data.repository

import com.cosmica.app.BuildConfig
import com.cosmica.app.data.mapper.toDomain
import com.cosmica.app.data.remote.api.NeoApiService
import com.cosmica.app.domain.model.NearEarthObject
import com.cosmica.app.domain.repository.NeoRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class NeoRepositoryImpl @Inject constructor(
    private val neoApiService: NeoApiService,
) : NeoRepository {

    override suspend fun getAsteroidsThisWeek(): Result<List<NearEarthObject>> = runCatching {
        val today     = LocalDate.now()
        val weekEnd   = today.plusDays(7)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val response = neoApiService.getAsteroids(
            apiKey    = BuildConfig.NASA_API_KEY,
            startDate = today.format(formatter),
            endDate   = weekEnd.format(formatter),
        )
        response.toDomain().sortedBy { it.closeApproachDate }
    }
}
