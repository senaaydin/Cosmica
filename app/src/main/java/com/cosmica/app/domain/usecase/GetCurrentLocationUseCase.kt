package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Coordinates
import com.cosmica.app.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(): Result<Coordinates> = repository.getCurrentLocation()
}
