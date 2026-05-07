package com.cosmica.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_apods")
data class FavoriteApodEntity(
    @PrimaryKey val date: String,
    val title: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: String,
    val copyright: String?,
)
