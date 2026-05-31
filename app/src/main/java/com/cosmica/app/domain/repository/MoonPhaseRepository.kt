package com.cosmica.app.domain.repository

import com.cosmica.app.domain.model.Coordinates
import com.cosmica.app.domain.model.MoonPhase

interface MoonPhaseRepository {
    suspend fun getMoonPhase(coordinates: Coordinates): Result<MoonPhase>
    suspend fun searchCities(query: String): Result<List<CitySearchResult>>
}

data class CitySearchResult(
    val name: String,
    val country: String?,
    val state: String?,
    val coordinates: Coordinates,
) {
    val displayName: String
        get() = listOfNotNull(name, state, country).joinToString(", ")
}
