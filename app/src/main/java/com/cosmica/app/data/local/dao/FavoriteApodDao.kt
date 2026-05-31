package com.cosmica.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cosmica.app.data.local.entity.FavoriteApodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteApodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteApodEntity)

    @Delete
    suspend fun delete(entity: FavoriteApodEntity)

    @Query("DELETE FROM favorite_apods WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_apods WHERE date = :date)")
    fun isFavoriteFlow(date: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_apods WHERE date = :date)")
    suspend fun isFavoriteOnce(date: String): Boolean

    @Query("SELECT * FROM favorite_apods ORDER BY date DESC")
    fun getAllFavorites(): Flow<List<FavoriteApodEntity>>

    @Query("SELECT COUNT(*) FROM favorite_apods")
    fun favoritesCount(): Flow<Int>
}
