package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesCountUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    operator fun invoke(): Flow<Int> = repository.favoritesCount()
}
