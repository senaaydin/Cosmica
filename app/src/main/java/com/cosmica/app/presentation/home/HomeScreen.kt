package com.cosmica.app.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cosmica.app.R
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.presentation.common.ErrorState
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.theme.CosmosBlack
import com.cosmica.app.presentation.theme.MeteorRed
import com.cosmica.app.presentation.theme.SafeGreen

private const val HERO_HEIGHT = 420

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ScreenUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is ScreenUiState.Error -> ErrorState(
            message = state.message,
            onRetry = viewModel::loadTodayApod,
        )
        is ScreenUiState.Success -> ApodContent(
            apod          = state.data,
            onToggleFavorite = { viewModel.toggleFavorite(state.data) },
        )
        is ScreenUiState.Empty -> Unit
    }
}

@Composable
private fun ApodContent(
    apod: Apod,
    onToggleFavorite: () -> Unit,
) {
    val scrollState: ScrollState = rememberScrollState()
    val parallaxOffset = scrollState.value * 0.4f

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            // ── Hero image / video with parallax ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HERO_HEIGHT.dp),
            ) {
                if (apod.isVideo) {
                    YoutubePlayer(
                        videoUrl = apod.url,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(apod.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.cd_apod_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { translationY = parallaxOffset },
                    )

                    // Gradient scrim for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CosmosBlack.copy(alpha = 0.1f),
                                        CosmosBlack.copy(alpha = 0.85f),
                                    ),
                                )
                            )
                    )
                }

                // Date chip
                Text(
                    text     = apod.date,
                    style    = MaterialTheme.typography.labelLarge,
                    color    = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp),
                )
            }

            // ── Metadata card ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text  = apod.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                apod.copyright?.let { credit ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = stringResource(R.string.home_copyright, credit),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text  = stringResource(R.string.home_explanation_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = apod.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(80.dp)) // space above nav bar
            }
        }

        // ── Floating favorite button ─────────────────────────────────────
        val favTint by animateColorAsState(
            targetValue  = if (apod.isFavorite) MeteorRed else SafeGreen,
            animationSpec = tween(durationMillis = 300),
            label        = "favColor",
        )
        FilledIconButton(
            onClick = onToggleFavorite,
            colors  = IconButtonDefaults.filledIconButtonColors(containerColor = favTint),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 80.dp),
        ) {
            Icon(
                imageVector        = if (apod.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(R.string.cd_favorite_button),
                tint               = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

