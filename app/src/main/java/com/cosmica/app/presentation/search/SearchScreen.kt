package com.cosmica.app.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cosmica.app.R
import com.cosmica.app.domain.model.NasaImage
import com.cosmica.app.presentation.common.EmptyState
import com.cosmica.app.presentation.common.GridShimmer
import com.cosmica.app.presentation.common.ShimmerCard
import com.cosmica.app.presentation.theme.CardSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onImageClick: (NasaImage) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query           by viewModel.query.collectAsStateWithLifecycle()
    val mediaTypeFilter by viewModel.mediaTypeFilter.collectAsStateWithLifecycle()
    val results          = viewModel.searchResults.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = stringResource(R.string.search_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query            = query,
                    onQueryChange    = viewModel::onQueryChange,
                    onSearch         = {},
                    expanded         = false,
                    onExpandedChange = {},
                    placeholder      = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon      = { Icon(Icons.Rounded.Search, contentDescription = null) },
                )
            },
            expanded         = false,
            onExpandedChange = {},
            modifier         = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = {},
        )

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                stringResource(R.string.search_filter_all)   to null,
                stringResource(R.string.search_filter_image) to "image",
                stringResource(R.string.search_filter_video) to "video",
            ).forEach { (label, type) ->
                FilterChip(
                    selected = mediaTypeFilter == type,
                    onClick  = { viewModel.onMediaTypeFilterChange(type) },
                    label    = { Text(label) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }

        when {
            query.isBlank() -> EmptyState(
                message = stringResource(R.string.search_empty_start),
                icon    = Icons.Rounded.Theaters,
            )
            results.loadState.refresh is LoadState.Loading -> GridShimmer()
            results.itemCount == 0 && results.loadState.refresh is LoadState.NotLoading -> EmptyState(
                message = stringResource(R.string.search_empty_results, query),
                icon    = Icons.Rounded.Search,
            )
            else -> SearchResultGrid(results = results, onImageClick = onImageClick)
        }
    }
}

@Composable
private fun SearchResultGrid(
    results: LazyPagingItems<NasaImage>,
    onImageClick: (NasaImage) -> Unit,
) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        contentPadding        = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
        modifier              = Modifier.fillMaxSize(),
    ) {
        items(count = results.itemCount) { index ->
            results[index]?.let { image ->
                SearchResultItem(image = image, onClick = { onImageClick(image) })
            }
        }
        if (results.loadState.append is LoadState.Loading) {
            items(2) {
                ShimmerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(2.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(image: NasaImage, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurface)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.previewUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.search_result_content_description, image.title),
            contentScale = ContentScale.Crop,
            modifier     = Modifier.fillMaxSize(),
        )
        Text(
            text     = image.title,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                )
                .padding(6.dp),
        )
    }
}
