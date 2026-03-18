package com.example.aicompanion.di

import com.example.aicompanion.core.domain.logging.Logger
import com.example.aicompanion.logging.TimberLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * App-level Hilt module.
 * Binds the Logger interface to TimberLogger for all modules in the DI graph.
 * Any module depending on :app (or :core:domain transitively) can inject Logger.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindLogger(impl: TimberLogger): Logger
}
