package com.cosmica.app.presentation.gallery

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.presentation.common.AnimatedFavoriteIconButton
import com.cosmica.app.presentation.common.EmptyState
import com.cosmica.app.presentation.common.ErrorState
import com.cosmica.app.presentation.common.GridShimmer
import com.cosmica.app.presentation.common.ShimmerCard
import com.cosmica.app.presentation.common.youtubeThumbnailUrl
import com.cosmica.app.presentation.theme.CardSurface
import com.cosmica.app.presentation.theme.CosmosBlack
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onApodClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val apods           = viewModel.apodPagingData.collectAsLazyPagingItems()
    val favoriteDates by viewModel.favoriteDates.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 16.dp, end = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.gallery_eyebrow),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.gallery_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search_title),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = stringResource(R.string.gallery_jump_to_date),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        when {
            apods.loadState.refresh is LoadState.Loading -> GridShimmer()
            apods.loadState.refresh is LoadState.Error -> {
                val e = (apods.loadState.refresh as LoadState.Error).error
                ErrorState(
                    message = e.message ?: stringResource(R.string.error_generic),
                    onRetry = { apods.retry() },
                )
            }
            apods.itemCount == 0 -> EmptyState(
                message = stringResource(R.string.gallery_empty),
                icon    = Icons.Rounded.BrokenImage,
            )
            else -> ApodGrid(
                apods           = apods,
                favoriteDates   = favoriteDates,
                onApodClick     = onApodClick,
                onToggleFavorite = viewModel::toggleFavorite,
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showDatePicker) {
        val apodEpochMillis = LocalDate.of(1995, 6, 16)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val maxMillis = LocalDate.now().minusDays(1)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = maxMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis in apodEpochMillis..maxMillis
            },
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            onApodClick(date)
                        }
                    }
                ) { Text(stringResource(R.string.gallery_date_picker_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.gallery_date_picker_dismiss))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ApodGrid(
    apods: LazyPagingItems<Apod>,
    favoriteDates: Set<String>,
    onApodClick: (String) -> Unit,
    onToggleFavorite: (Apod, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(apods) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val itemCount = apods.itemCount
                if (itemCount > 0 && lastVisibleIndex != null && lastVisibleIndex >= itemCount - 4) {
                    apods[itemCount - 1]
                }
            }
    }

    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        state                 = gridState,
        contentPadding        = PaddingValues(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        modifier              = modifier.fillMaxSize(),
    ) {
        items(count = apods.itemCount) { index ->
            apods[index]?.let { apod ->
                val isFav = apod.date in favoriteDates
                ApodThumbnail(
                    apod       = apod,
                    isFavorite = isFav,
                    onClick    = { onApodClick(apod.date) },
                    onToggleFavorite = { onToggleFavorite(apod, isFav) },
                )
            }
        }

        if (apods.loadState.append is LoadState.Error) {
            val e = (apods.loadState.append as LoadState.Error).error
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = e.message ?: stringResource(R.string.error_generic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { apods.retry() }) {
                        Text(stringResource(R.string.home_retry))
                    }
                }
            }
        }

        if (apods.loadState.append is LoadState.Loading) {
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
private fun ApodThumbnail(
    apod: Apod,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurface)
            .clickable(onClick = onClick),
    ) {
        if (apod.isVideo) {
            VideoPreview(
                videoUrl = apod.url,
                title = apod.title,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(apod.url)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.gallery_image_content_description, apod.title),
                contentScale = ContentScale.Crop,
                modifier     = Modifier.fillMaxSize(),
            )
        }

        AnimatedFavoriteIconButton(
            isFavorite         = isFavorite,
            onToggle           = onToggleFavorite,
            contentDescription = stringResource(R.string.cd_favorite_button),
            modifier           = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CosmosBlack.copy(alpha = 0f),
                            CosmosBlack.copy(alpha = 0.86f),
                        ),
                    ),
                )
                .padding(top = 28.dp),
        )

        Text(
            text     = apod.title,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun VideoPreview(
    videoUrl: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val thumbnailUrl = youtubeThumbnailUrl(videoUrl)

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.cd_video_thumbnail),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Videocam,
                contentDescription = stringResource(R.string.cd_video_thumbnail),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(42.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmosBlack.copy(alpha = 0.36f)),
        )
        Icon(
            imageVector = Icons.Rounded.PlayCircle,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(44.dp),
        )
    }
}
