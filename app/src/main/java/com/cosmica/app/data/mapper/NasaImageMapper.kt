package com.cosmica.app.data.mapper

import com.cosmica.app.data.remote.dto.NasaImageItemDto
import com.cosmica.app.domain.model.NasaImage

fun NasaImageItemDto.toDomain(): NasaImage? {
    val data = data.firstOrNull() ?: return null
    val previewUrl = links?.firstOrNull { it.rel == "preview" }?.href ?: return null
    return NasaImage(
        nasaId      = data.nasaId,
        title       = data.title,
        description = data.description.orEmpty(),
        dateCreated = data.dateCreated,
        mediaType   = data.mediaType,
        previewUrl  = previewUrl,
        keywords    = data.keywords.orEmpty(),
    )
}
