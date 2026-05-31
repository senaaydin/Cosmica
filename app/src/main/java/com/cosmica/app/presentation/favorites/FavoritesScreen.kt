package com.cosmica.app.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cosmica.app.R
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.presentation.common.EmptyState
import com.cosmica.app.presentation.common.youtubeThumbnailUrl
import com.cosmica.app.presentation.theme.CardSurface
import com.cosmica.app.presentation.theme.CosmosBlack
import com.cosmica.app.presentation.theme.GlassSurface
import com.cosmica.app.presentation.theme.MeteorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onApodClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Listen for deletion events and show UNDO snackbar
    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { event ->
            val result = snackbarHostState.showSnackbar(
                message     = context.getString(R.string.favorites_removed, event.apod.title),
                actionLabel = context.getString(R.string.favorites_undo),
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(event.apod)
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = stringResource(R.string.favorites_eyebrow),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.favorites_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            if (favorites.isEmpty()) {
                EmptyState(
                    message = stringResource(R.string.favorites_empty),
                    icon    = Icons.Rounded.FavoriteBorder,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 108.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(favorites, key = { it.date }) { apod ->
                        SwipeableFavoriteRow(
                            apod = apod,
                            onClick = { onApodClick(apod.date) },
                            onRemove = { viewModel.remove(apod) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableFavoriteRow(
    apod: Apod,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onRemove()
                true
            } else false
        },
    )

    SwipeToDismissBox(
        state            = dismissState,
        backgroundContent = { DismissBackground() },
        content          = { FavoriteRow(apod = apod, onClick = onClick, onRemove = onRemove) },
    )
}

@Composable
private fun DismissBackground() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MeteorRed.copy(alpha = 0.84f), shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector        = Icons.Rounded.Delete,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onError,
            modifier           = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun FavoriteRow(
    apod: Apod,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurface)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GlassSurface),
        ) {
            if (apod.isVideo) {
                FavoriteVideoPreview(videoUrl = apod.url, title = apod.title)
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(apod.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.favorites_image_content_description, apod.title),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = apod.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = apod.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            apod.copyright?.let { credit ->
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.home_copyright, credit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MeteorRed.copy(alpha = 0.16f)),
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.cd_delete_favorite),
                tint = MeteorRed,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun FavoriteVideoPreview(
    videoUrl: String,
    title: String,
) {
    val thumbnailUrl = youtubeThumbnailUrl(videoUrl)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
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
                modifier = Modifier.size(30.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmosBlack.copy(alpha = 0.38f)),
        )
        Icon(
            imageVector = Icons.Rounded.PlayCircle,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(34.dp),
        )
    }
}
