package com.example.aicompanion.core.audio.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    // Placeholder — SpeechRecognizer and TextToSpeech bindings added in Phase 2+
    // CRITICAL: SpeechRecognizer must be created on the main thread (Phase 2 constraint)
}
