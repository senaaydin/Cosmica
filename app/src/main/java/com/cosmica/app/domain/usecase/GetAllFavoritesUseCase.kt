package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository,
) {
    operator fun invoke(): Flow<List<Apod>> = repository.getFavorites()
}
