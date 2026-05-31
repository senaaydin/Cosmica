package com.cosmica.app.data.mapper

import com.cosmica.app.data.local.entity.FavoriteApodEntity
import com.cosmica.app.data.remote.dto.ApodDto
import com.cosmica.app.domain.model.Apod

fun ApodDto.toDomain(isFavorite: Boolean = false): Apod = Apod(
    date        = date,
    title       = title,
    explanation = explanation,
    url         = url,
    hdUrl       = hdUrl,
    mediaType   = mediaType,
    copyright   = copyright?.trim(),
    isFavorite  = isFavorite,
)

fun Apod.toFavoriteEntity(): FavoriteApodEntity = FavoriteApodEntity(
    date        = date,
    title       = title,
    explanation = explanation,
    url         = url,
    hdUrl       = hdUrl,
    mediaType   = mediaType,
    copyright   = copyright,
)

fun FavoriteApodEntity.toDomain(): Apod = Apod(
    date        = date,
    title       = title,
    explanation = explanation,
    url         = url,
    hdUrl       = hdUrl,
    mediaType   = mediaType,
    copyright   = copyright,
    isFavorite  = true,
)
