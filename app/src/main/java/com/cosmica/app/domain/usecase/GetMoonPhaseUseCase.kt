package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Coordinates
import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.repository.MoonPhaseRepository
import javax.inject.Inject

class GetMoonPhaseUseCase @Inject constructor(
    private val repository: MoonPhaseRepository,
) {
    suspend operator fun invoke(coordinates: Coordinates): Result<MoonPhase> =
        repository.getMoonPhase(coordinates)
}
