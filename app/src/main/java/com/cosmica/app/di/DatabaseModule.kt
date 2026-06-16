package com.cosmica.app.di

import android.content.Context
import androidx.room.Room
import com.cosmica.app.data.local.CosmicaDatabase
import com.cosmica.app.data.local.dao.FavoriteApodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCosmicaDatabase(@ApplicationContext context: Context): CosmicaDatabase =
        Room.databaseBuilder(context, CosmicaDatabase::class.java, "cosmica.db")
            .addMigrations(CosmicaDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideFavoriteApodDao(db: CosmicaDatabase): FavoriteApodDao = db.favoriteApodDao()
}
