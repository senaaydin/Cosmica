package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.ApodRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ApodRepository,
) {
    suspend operator fun invoke(apod: Apod) {
        if (repository.isFavorite(apod.date)) {
            repository.removeFavorite(apod.date)
        } else {
            repository.addFavorite(apod)
        }
    }
}
