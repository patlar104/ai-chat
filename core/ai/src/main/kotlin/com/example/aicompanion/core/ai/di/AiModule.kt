package com.example.aicompanion.core.ai.di

import com.example.aicompanion.core.ai.routing.AiRouter
import com.example.aicompanion.core.ai.routing.AiRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindAiRouter(impl: AiRouterImpl): AiRouter
}
