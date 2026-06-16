package com.cosmica.app.presentation.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.cosmica.app.presentation.common.AnimatedFavoriteIconButton
import com.cosmica.app.presentation.common.ApodVideoPlayer
import com.cosmica.app.presentation.common.ErrorState
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.theme.CosmosBlack
import com.cosmica.app.presentation.theme.GlassSurface

private const val HERO_HEIGHT = 470

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HERO_HEIGHT.dp),
            ) {
                if (apod.isVideo) {
                    ApodVideoPlayer(
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
                                        CosmosBlack.copy(alpha = 0.04f),
                                        CosmosBlack.copy(alpha = 0.34f),
                                        CosmosBlack.copy(alpha = 0.96f),
                                    ),
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = apod.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .background(GlassSurface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = apod.date,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                apod.copyright?.let { credit ->
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

        AnimatedFavoriteIconButton(
            isFavorite = apod.isFavorite,
            onToggle = onToggleFavorite,
            size = 25.dp,
            contentDescription = stringResource(R.string.cd_favorite_button),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 80.dp),
        )
    }
}
