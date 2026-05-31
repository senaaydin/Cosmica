package com.cosmica.app.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.cosmica.app.data.calculator.MoonPhaseCalculator
import com.cosmica.app.domain.model.Coordinates
import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.repository.CitySearchResult
import com.cosmica.app.domain.repository.MoonPhaseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class MoonPhaseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : MoonPhaseRepository {

    private val geocoder: Geocoder? =
        if (Geocoder.isPresent()) Geocoder(context) else null

    override suspend fun getMoonPhase(coordinates: Coordinates): Result<MoonPhase> = runCatching {
        val locationName = withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder?.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                    ?.firstOrNull()
                    ?.toDisplayName()
            }.getOrNull().orEmpty()
        }

        MoonPhaseCalculator.calculate(LocalDate.now(), locationName)
    }

    override suspend fun searchCities(query: String): Result<List<CitySearchResult>> = runCatching {
        withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            geocoder?.getFromLocationName(query, 5).orEmpty().map { it.toCitySearchResult() }
        }
    }

    private fun Address.toDisplayName(): String =
        listOfNotNull(locality ?: subAdminArea ?: adminArea, countryName)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    private fun Address.toCitySearchResult(): CitySearchResult = CitySearchResult(
        name        = locality ?: subAdminArea ?: adminArea ?: featureName ?: "",
        country     = countryName,
        state       = adminArea,
        coordinates = Coordinates(latitude, longitude),
    )
}
