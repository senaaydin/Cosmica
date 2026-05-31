package com.cosmica.app.presentation.common

import android.net.Uri
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

fun youtubeThumbnailUrl(videoUrl: String): String? =
    extractYouTubeId(videoUrl)?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

@Composable
fun ApodVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    val videoId = extractYouTubeId(videoUrl)
    if (videoId != null) {
        YouTubeEmbedPlayer(videoId = videoId, modifier = modifier)
    } else {
        NativeVideoPlayer(videoUrl = videoUrl, modifier = modifier)
    }
}

@Composable
private fun YouTubeEmbedPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var webView by remember { mutableStateOf<WebView?>(null) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webChromeClient = WebChromeClient()
                loadDataWithBaseURL(
                    "https://www.youtube.com",
                    buildEmbedHtml(videoId),
                    "text/html",
                    "utf-8",
                    null,
                )
            }.also { webView = it }
        },
        modifier = modifier,
    )

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
private fun NativeVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                val mediaController = MediaController(context)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)
                setVideoURI(Uri.parse(videoUrl))
                setOnPreparedListener { start() }
                setOnErrorListener { _, _, _ -> true }
            }.also { videoView = it }
        },
        update = { view ->
            if (view.tag != videoUrl) {
                view.tag = videoUrl
                view.setVideoURI(Uri.parse(videoUrl))
                view.start()
            }
        },
        modifier = modifier,
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> videoView?.pause()
                Lifecycle.Event.ON_RESUME -> if (videoView?.isPlaying == false) videoView?.start()
                Lifecycle.Event.ON_DESTROY -> videoView?.stopPlayback()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            videoView?.stopPlayback()
        }
    }
}

private fun buildEmbedHtml(videoId: String) = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { background: #000; width: 100%; height: 100%; }
            iframe { position: absolute; width: 100%; height: 100%; border: 0; }
        </style>
    </head>
    <body>
        <iframe
            src="https://www.youtube.com/embed/$videoId?rel=0&playsinline=1"
            allow="autoplay; encrypted-media; fullscreen"
            allowfullscreen>
        </iframe>
    </body>
    </html>
""".trimIndent()

private fun extractYouTubeId(url: String): String? =
    Regex("(?:youtu\\.be/|youtube\\.com/(?:embed/|watch\\?v=)|v=)([A-Za-z0-9_-]{11})")
        .find(url)
        ?.groupValues
        ?.getOrNull(1)
