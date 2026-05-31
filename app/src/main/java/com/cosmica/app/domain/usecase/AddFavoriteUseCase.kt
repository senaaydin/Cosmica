package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    suspend operator fun invoke(apod: Apod) = repository.addFavorite(apod)
}
