package com.cosmica.app.domain.model

data class NasaImage(
    val nasaId: String,
    val title: String,
    val description: String,
    val dateCreated: String,
    val mediaType: String,
    val previewUrl: String,
    val keywords: List<String>,
) {
    val isVideo: Boolean get() = mediaType == "video"
}
