package com.cosmica.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cosmica.app.data.mapper.toDomain
import com.cosmica.app.data.remote.api.NasaImageApiService
import com.cosmica.app.domain.model.NasaImage

private const val STARTING_PAGE = 1

class NasaImagePagingSource(
    private val nasaImageApiService: NasaImageApiService,
    private val query: String,
    private val mediaType: String?,
) : PagingSource<Int, NasaImage>() {

    override fun getRefreshKey(state: PagingState<Int, NasaImage>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.run {
                prevKey?.plus(1) ?: nextKey?.minus(1)
            }
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NasaImage> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = nasaImageApiService.searchImages(
                query     = query,
                mediaType = mediaType,
                page      = page,
                pageSize  = params.loadSize,
            )
            val items = response.collection.items.mapNotNull { it.toDomain() }
            val hasNext = response.collection.links?.any { it.rel == "next" } == true
            LoadResult.Page(
                data    = items,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (hasNext) page + 1 else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
