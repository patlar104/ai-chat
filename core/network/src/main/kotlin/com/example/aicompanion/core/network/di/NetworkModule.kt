package com.example.aicompanion.core.network.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Placeholder — OkHttp client, Retrofit, and WebSocket bindings added in Phase 2+
    // Example: @Provides @Singleton fun provideOkHttpClient(): OkHttpClient
}
