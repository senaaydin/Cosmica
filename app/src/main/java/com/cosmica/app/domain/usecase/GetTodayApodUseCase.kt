package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.ApodRepository
import javax.inject.Inject

class GetTodayApodUseCase @Inject constructor(
    private val repository: ApodRepository,
) {
    suspend operator fun invoke(): Result<Apod> = repository.getTodayApod()
}
