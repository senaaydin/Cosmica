package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.ApodRepository
import javax.inject.Inject

class GetApodByDateUseCase @Inject constructor(
    private val repository: ApodRepository,
) {
    suspend operator fun invoke(date: String): Result<Apod> = repository.getApodByDate(date)
}
