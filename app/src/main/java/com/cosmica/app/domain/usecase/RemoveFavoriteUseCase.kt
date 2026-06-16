package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.repository.FavoriteRepository
import javax.inject.Inject

class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    suspend operator fun invoke(date: String) = repository.removeFavorite(date)
}
