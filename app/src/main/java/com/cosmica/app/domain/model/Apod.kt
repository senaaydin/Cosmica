package com.cosmica.app.domain.model

data class Apod(
    val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: String,
    val copyright: String?,
    val isFavorite: Boolean = false,
) {
    val isVideo: Boolean get() = mediaType == "video"

    /** Best URL for display: HD first, falls back to standard URL. */
    val displayUrl: String get() = if (isVideo) url else (hdUrl ?: url)
}
