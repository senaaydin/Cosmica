package com.cosmica.app.domain.usecase

import androidx.paging.PagingData
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetApodRangeUseCase @Inject constructor(
    private val repository: ApodRepository,
) {
    operator fun invoke(): Flow<PagingData<Apod>> = repository.getApodPager()
}
