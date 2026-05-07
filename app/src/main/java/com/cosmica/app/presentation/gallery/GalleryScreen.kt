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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cosmica.app.R
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.presentation.common.EmptyState
import com.cosmica.app.presentation.common.ErrorState
import com.cosmica.app.presentation.common.GridShimmer
import com.cosmica.app.presentation.common.ShimmerCard
import com.cosmica.app.presentation.theme.CardSurface
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onApodClick: (String) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val apods = viewModel.apodPagingData.collectAsLazyPagingItems()
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text     = stringResource(R.string.gallery_title),
                style    = MaterialTheme.typography.headlineMedium,
                color    = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            )
            IconButton(
                onClick  = { showDatePicker = true },
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    imageVector        = Icons.Rounded.CalendarMonth,
                    contentDescription = stringResource(R.string.gallery_jump_to_date),
                    tint               = MaterialTheme.colorScheme.onBackground,
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
            else -> ApodGrid(apods = apods, onApodClick = onApodClick)
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
    onApodClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        contentPadding        = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
        modifier              = Modifier.fillMaxSize(),
    ) {
        items(count = apods.itemCount) { index ->
            apods[index]?.let { apod ->
                ApodThumbnail(apod = apod, onClick = { onApodClick(apod.date) })
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
private fun ApodThumbnail(apod: Apod, onClick: () -> Unit) {
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
                .data(apod.url)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gallery_image_content_description, apod.title),
            contentScale = ContentScale.Crop,
            modifier     = Modifier.fillMaxSize(),
        )

        Text(
            text     = apod.title,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(
                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                )
                .fillMaxWidth()
                .padding(6.dp),
        )
    }
}
