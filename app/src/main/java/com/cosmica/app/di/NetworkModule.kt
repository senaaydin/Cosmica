package com.cosmica.app.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.cosmica.app.BuildConfig
import com.cosmica.app.data.remote.api.ApodApiService
import com.cosmica.app.data.remote.api.NasaImageApiService
import com.cosmica.app.data.remote.api.NeoApiService
import com.cosmica.app.data.remote.interceptor.ApiKeyInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG)
                        HttpLoggingInterceptor.Level.BODY
                    else
                        HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()

    // Extends the base client with the NASA API key — used only for api.nasa.gov
    @Provides
    @Singleton
    @Named("nasa_client")
    fun provideNasaOkHttpClient(base: OkHttpClient): OkHttpClient =
        base.newBuilder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.NASA_API_KEY))
            .build()

    @Provides
    @Singleton
    @Named("nasa")
    fun provideNasaRetrofit(@Named("nasa_client") client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("nasa_images")
    fun provideNasaImagesRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://images-api.nasa.gov/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideApodApiService(@Named("nasa") retrofit: Retrofit): ApodApiService =
        retrofit.create(ApodApiService::class.java)

    @Provides
    @Singleton
    fun provideNeoApiService(@Named("nasa") retrofit: Retrofit): NeoApiService =
        retrofit.create(NeoApiService::class.java)

    @Provides
    @Singleton
    fun provideNasaImageApiService(@Named("nasa_images") retrofit: Retrofit): NasaImageApiService =
        retrofit.create(NasaImageApiService::class.java)
}
