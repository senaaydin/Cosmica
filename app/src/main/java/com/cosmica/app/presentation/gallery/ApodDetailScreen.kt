package com.cosmica.app.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.cosmica.app.presentation.common.ErrorState
import com.cosmica.app.presentation.common.ScreenUiState

@Composable
fun ApodDetailScreen(
    date: String,
    onBack: () -> Unit,
    viewModel: ApodDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(date) { viewModel.loadApod(date) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ScreenUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is ScreenUiState.Error   -> ErrorState(message = state.message, onRetry = { viewModel.loadApod(date) })
            is ScreenUiState.Success -> {
                val apod = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(apod.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.cd_apod_image),
                            contentScale = ContentScale.Crop,
                            modifier     = Modifier.fillMaxSize(),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(20.dp),
                    ) {
                        Text(apod.title, style = MaterialTheme.typography.headlineMedium)
                        Text(apod.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        apod.copyright?.let {
                            Text(
                                text      = stringResource(R.string.home_copyright, it),
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(apod.explanation, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
            is ScreenUiState.Empty -> Unit
        }

        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back_button),
                tint               = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
