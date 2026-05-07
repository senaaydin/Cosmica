package com.cosmica.app.di

import com.cosmica.app.data.repository.ApodRepositoryImpl
import com.cosmica.app.data.repository.NasaImageRepositoryImpl
import com.cosmica.app.data.repository.NeoRepositoryImpl
import com.cosmica.app.domain.repository.ApodRepository
import com.cosmica.app.domain.repository.NasaImageRepository
import com.cosmica.app.domain.repository.NeoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindApodRepository(impl: ApodRepositoryImpl): ApodRepository

    @Binds
    @Singleton
    abstract fun bindNeoRepository(impl: NeoRepositoryImpl): NeoRepository

    @Binds
    @Singleton
    abstract fun bindNasaImageRepository(impl: NasaImageRepositoryImpl): NasaImageRepository
}
