package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    operator fun invoke(date: String): Flow<Boolean> = repository.isFavorite(date)
}
