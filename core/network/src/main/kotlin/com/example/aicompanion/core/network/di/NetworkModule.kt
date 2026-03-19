package com.example.aicompanion.core.network.di

import com.example.aicompanion.core.domain.repository.HomeAssistantRepository
import com.example.aicompanion.core.network.ai.CloudAiService
import com.example.aicompanion.core.network.ha.BaseUrlInterceptor
import com.example.aicompanion.core.network.ha.HaAuthInterceptor
import com.example.aicompanion.core.network.ha.HomeAssistantService
import com.example.aicompanion.core.network.privacy.PrivacyInterceptor
import com.example.aicompanion.core.network.repository.HomeAssistantRepositoryImpl
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindHomeAssistantRepository(impl: HomeAssistantRepositoryImpl): HomeAssistantRepository

    companion object {
        @Provides
        @Singleton
        fun provideHaAuthInterceptor(): HaAuthInterceptor = HaAuthInterceptor()

        @Provides
        @Singleton
        fun provideBaseUrlInterceptor(): BaseUrlInterceptor = BaseUrlInterceptor()

        @Provides
        @Singleton
        fun providePrivacyInterceptor(): PrivacyInterceptor = PrivacyInterceptor()

        @Provides
        @Singleton
        fun provideOkHttpClient(
            authInterceptor: HaAuthInterceptor,
            baseUrlInterceptor: BaseUrlInterceptor,
            privacyInterceptor: PrivacyInterceptor,
        ): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(privacyInterceptor)
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
            val json = Json { ignoreUnknownKeys = true }
            return Retrofit.Builder()
                .baseUrl("http://localhost/") // Placeholder — overridden by BaseUrlInterceptor
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }

        @Provides
        @Singleton
        fun provideHomeAssistantService(retrofit: Retrofit): HomeAssistantService =
            retrofit.create(HomeAssistantService::class.java)

        @Provides
        @Singleton
        fun provideCloudAiService(): CloudAiService = CloudAiService()
    }
}
