package com.example.aicompanion.core.audio.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    // SpeechRecognizerManager, TextToSpeechManager, and AudioFocusManager
    // all use @Inject constructor() — Hilt constructs them automatically.
    // No explicit @Provides needed for these classes.
    //
    // This module exists as an extension point for future @Provides
    // (e.g., providing a custom RecognitionService or TTS engine).
}
