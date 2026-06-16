package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.repository.CitySearchResult
import com.cosmica.app.domain.repository.MoonPhaseRepository
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val repository: MoonPhaseRepository,
) {
    suspend operator fun invoke(query: String): Result<List<CitySearchResult>> =
        if (query.isBlank()) Result.success(emptyList())
        else repository.searchCities(query)
}
