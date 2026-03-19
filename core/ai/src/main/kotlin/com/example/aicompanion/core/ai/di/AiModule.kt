package com.example.aicompanion.core.ai.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    // CommandParser uses @Inject constructor — Hilt provides it automatically.
    // Future: AI routing bindings (local model, cloud LLM) added in Phase 3.
}
