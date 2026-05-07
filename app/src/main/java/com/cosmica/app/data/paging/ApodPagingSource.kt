package com.cosmica.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cosmica.app.data.mapper.toDomain
import com.cosmica.app.data.remote.api.ApodApiService
import com.cosmica.app.domain.model.Apod
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ApodPagingSource(
    private val apodApiService: ApodApiService,
    private val favoritesDates: Set<String> = emptySet(),
) : PagingSource<LocalDate, Apod>() {

    override fun getRefreshKey(state: PagingState<LocalDate, Apod>): LocalDate? = null

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Apod> {
        // Start from yesterday — APOD publishes on US Eastern time so today may not exist yet.
        val endDay = params.key ?: LocalDate.now().minusDays(1)
        val requestedStart = endDay.minusDays(params.loadSize.toLong() - 1)
        val startDay = if (requestedStart.isBefore(APOD_EPOCH)) APOD_EPOCH else requestedStart

        return try {
            val dtos = apodApiService.getApodRange(
                startDate = startDay.format(DATE_FORMATTER),
                endDate   = endDay.format(DATE_FORMATTER),
            )
            // API returns oldest-first; reverse to show newest first
            val items = dtos.reversed().map { it.toDomain(isFavorite = it.date in favoritesDates) }
            LoadResult.Page(
                data    = items,
                prevKey = null,
                nextKey = if (startDay.isAfter(APOD_EPOCH)) startDay.minusDays(1) else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        // APOD archive starts on 1995-06-16
        val APOD_EPOCH: LocalDate = LocalDate.of(1995, 6, 16)
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
