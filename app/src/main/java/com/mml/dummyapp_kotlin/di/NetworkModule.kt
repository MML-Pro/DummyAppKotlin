package com.mml.dummyapp_kotlin.di

import com.mml.dummyapp_kotlin.data.network.PostAPIService
import com.mml.dummyapp_kotlin.util.Constatns
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
//    @Singleton
//    @Provides
//    fun provideMoshi(): Moshi {
//        return Moshi.Builder().build()
//    }

    @Singleton
    @Provides
    fun provideHTTPClient(): OkHttpClient {
        return OkHttpClient.Builder().readTimeout(
            15, TimeUnit.SECONDS
        ).connectTimeout(15, TimeUnit.SECONDS).build()
    }

    @Singleton
    @Provides
    fun postAPIService(): PostAPIService {
        return Retrofit.Builder()
            .baseUrl(Constatns.BASE_URL)
            .client(provideHTTPClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(PostAPIService::class.java)
    }
}
