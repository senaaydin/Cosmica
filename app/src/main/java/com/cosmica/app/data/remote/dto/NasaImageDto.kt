package com.cosmica.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NasaImageSearchResponseDto(
    @SerializedName("collection") val collection: NasaImageCollectionDto,
)

data class NasaImageCollectionDto(
    @SerializedName("items") val items: List<NasaImageItemDto>,
    @SerializedName("links") val links: List<NasaCollectionLinkDto>?,
)

data class NasaImageItemDto(
    @SerializedName("data")  val data: List<NasaImageDataDto>,
    @SerializedName("links") val links: List<NasaImageLinkDto>?,
)

data class NasaImageDataDto(
    @SerializedName("nasa_id")      val nasaId: String,
    @SerializedName("title")        val title: String,
    @SerializedName("description")  val description: String?,
    @SerializedName("date_created") val dateCreated: String,
    @SerializedName("media_type")   val mediaType: String,
    @SerializedName("keywords")     val keywords: List<String>?,
)

data class NasaImageLinkDto(
    @SerializedName("href") val href: String,
    @SerializedName("rel")  val rel: String,
)

data class NasaCollectionLinkDto(
    @SerializedName("href") val href: String,
    @SerializedName("rel")  val rel: String,
)
