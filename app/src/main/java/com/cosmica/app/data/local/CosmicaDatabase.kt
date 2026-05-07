package com.cosmica.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cosmica.app.data.local.dao.FavoriteApodDao
import com.cosmica.app.data.local.entity.FavoriteApodEntity

@Database(
    entities = [FavoriteApodEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CosmicaDatabase : RoomDatabase() {
    abstract fun favoriteApodDao(): FavoriteApodDao
}
