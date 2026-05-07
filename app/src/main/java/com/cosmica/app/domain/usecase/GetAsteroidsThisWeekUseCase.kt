package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.NearEarthObject
import com.cosmica.app.domain.repository.NeoRepository
import javax.inject.Inject

class GetAsteroidsThisWeekUseCase @Inject constructor(
    private val repository: NeoRepository,
) {
    suspend operator fun invoke(): Result<List<NearEarthObject>> = repository.getAsteroidsThisWeek()
}
