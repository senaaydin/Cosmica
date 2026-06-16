package com.cosmica.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cosmica.app.data.local.dao.FavoriteApodDao
import com.cosmica.app.data.local.entity.FavoriteApodEntity

@Database(
    entities = [FavoriteApodEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class CosmicaDatabase : RoomDatabase() {
    abstract fun favoriteApodDao(): FavoriteApodDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE favorite_apods ADD COLUMN explanation TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
