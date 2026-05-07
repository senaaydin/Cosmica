package com.cosmica.app.presentation.home

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
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

@Composable
fun YoutubePlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    val videoId = extractYouTubeId(videoUrl) ?: return

    val lifecycleOwner = LocalLifecycleOwner.current
    var webView by remember { mutableStateOf<WebView?>(null) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Hardware layer required to render video frames in a Compose hierarchy
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                // WebChromeClient needed for fullscreen and media permissions
                webChromeClient = WebChromeClient()
                // Load as an HTML page with youtube.com as base URL so YouTube's JS
                // accepts the origin — loadUrl() alone is often rejected by YouTube
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

    // Tie WebView lifecycle to the Activity lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> webView?.onResume()
                Lifecycle.Event.ON_PAUSE   -> webView?.onPause()
                Lifecycle.Event.ON_DESTROY -> webView?.destroy()
                else                       -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webView?.destroy()
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
    Regex("(?:youtu\\.be/|youtube\\.com/embed/|v=)([A-Za-z0-9_-]{11})")
        .find(url)?.groupValues?.getOrNull(1)
