package com.cosmica.app.presentation.common

import android.net.Uri
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cosmica.app.R
import com.cosmica.app.presentation.theme.CosmosBlack

fun youtubeThumbnailUrl(videoUrl: String): String? =
    extractYouTubeId(videoUrl)?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

@Composable
fun ApodVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = false,
    enableFullscreen: Boolean = true,
) {
    val isYouTube = extractYouTubeId(videoUrl) != null
    if (isYouTube) {
        YouTubeApodVideoPlayer(
            videoUrl = videoUrl,
            autoplay = autoplay,
            enableFullscreen = enableFullscreen,
            modifier = modifier,
        )
    } else {
        ExoApodVideoPlayer(
            videoUrl = videoUrl,
            autoplay = autoplay,
            enableFullscreen = enableFullscreen,
            modifier = modifier,
        )
    }
}

@Composable
private fun ExoApodVideoPlayer(
    videoUrl: String,
    autoplay: Boolean,
    enableFullscreen: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = autoplay
            prepare()
        }
    }
    var showPlayer by remember(videoUrl) { mutableStateOf(autoplay) }
    var isFullscreen by remember(videoUrl) { mutableStateOf(false) }

    DisposableEffect(player, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> player.pause()
                Lifecycle.Event.ON_RESUME -> if (showPlayer && !isFullscreen) player.play()
                Lifecycle.Event.ON_DESTROY -> player.release()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    Box(modifier = modifier.background(CosmosBlack)) {
        if (showPlayer) {
            if (isFullscreen) {
                Box(Modifier.fillMaxSize().background(CosmosBlack))
            } else {
                ExoPlayerView(player = player, modifier = Modifier.fillMaxSize())
            }
            if (enableFullscreen) {
                IconButton(
                    onClick = { isFullscreen = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(CosmosBlack.copy(alpha = 0.52f)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = stringResource(R.string.cd_fullscreen_video),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        } else {
            VideoPoster(
                videoUrl = videoUrl,
                onPlay = {
                    showPlayer = true
                    player.play()
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = {
                isFullscreen = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CosmosBlack),
            ) {
                ExoPlayerView(player = player, modifier = Modifier.fillMaxSize())
                IconButton(
                    onClick = {
                        isFullscreen = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .background(CosmosBlack.copy(alpha = 0.52f)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cd_close_fullscreen_video),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@Composable
private fun YouTubeApodVideoPlayer(
    videoUrl: String,
    autoplay: Boolean,
    enableFullscreen: Boolean,
    modifier: Modifier = Modifier,
) {
    val videoId = extractYouTubeId(videoUrl)
    var showPlayer by remember(videoUrl) { mutableStateOf(autoplay) }
    var isFullscreen by remember(videoUrl) { mutableStateOf(false) }

    Box(modifier = modifier.background(CosmosBlack)) {
        if (showPlayer && videoId != null) {
            YouTubeEmbedPlayer(videoId = videoId, modifier = Modifier.fillMaxSize())
            if (enableFullscreen) {
                IconButton(
                    onClick = { isFullscreen = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(CosmosBlack.copy(alpha = 0.52f)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = stringResource(R.string.cd_fullscreen_video),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        } else {
            VideoPoster(
                videoUrl = videoUrl,
                onPlay = { showPlayer = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (isFullscreen && videoId != null) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CosmosBlack),
            ) {
                YouTubeEmbedPlayer(videoId = videoId, modifier = Modifier.fillMaxSize())
                IconButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .background(CosmosBlack.copy(alpha = 0.52f)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cd_close_fullscreen_video),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoPoster(
    videoUrl: String,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnailUrl = youtubeThumbnailUrl(videoUrl)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPlay),
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
                modifier = Modifier.size(48.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmosBlack.copy(alpha = 0.38f)),
        )
        Icon(
            imageVector = Icons.Rounded.PlayCircle,
            contentDescription = stringResource(R.string.cd_play_video),
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(58.dp),
        )
    }
}

@Composable
private fun YouTubeEmbedPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember(videoId) { mutableStateOf(true) }

    Box(modifier = modifier.background(CosmosBlack)) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(android.graphics.Color.BLACK)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    loadDataWithBaseURL(
                        "https://www.youtube.com",
                        buildYouTubeHtml(videoId),
                        "text/html",
                        "utf-8",
                        null,
                    )
                }.also { webView = it }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(34.dp),
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView?.onResume()
                Lifecycle.Event.ON_PAUSE -> webView?.onPause()
                Lifecycle.Event.ON_DESTROY -> webView?.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webView?.destroy()
        }
    }
}

@Composable
private fun ExoPlayerView(
    player: ExoPlayer,
    modifier: Modifier = Modifier,
) {
    var isBuffering by remember(player) { mutableStateOf(player.playbackState == Player.STATE_BUFFERING) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    Box(modifier = modifier.background(CosmosBlack)) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view ->
                if (view.player !== player) view.player = player
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(34.dp),
            )
        }
    }
}

private fun buildYouTubeHtml(videoId: String) = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
            html, body {
                margin: 0;
                padding: 0;
                width: 100%;
                height: 100%;
                background: #000;
                overflow: hidden;
            }
            iframe {
                position: fixed;
                inset: 0;
                width: 100%;
                height: 100%;
                border: 0;
                background: #000;
            }
        </style>
    </head>
    <body>
        <iframe
            src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&rel=0&modestbranding=1&enablejsapi=1&origin=https://www.youtube.com"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowfullscreen>
        </iframe>
    </body>
    </html>
""".trimIndent()

private fun extractYouTubeId(url: String): String? {
    val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return null
    val host = uri.host.orEmpty().removePrefix("www.")
    if (host == "youtu.be") return uri.lastPathSegment?.takeIf { it.length == 11 }
    if (!host.endsWith("youtube.com")) return null

    val queryId = uri.getQueryParameter("v")?.takeIf { it.length == 11 }
    if (queryId != null) return queryId

    val segments = uri.pathSegments
    val embedIndex = segments.indexOfFirst { it == "embed" || it == "shorts" || it == "v" }
    return segments.getOrNull(embedIndex + 1)?.takeIf { it.length == 11 }
}
