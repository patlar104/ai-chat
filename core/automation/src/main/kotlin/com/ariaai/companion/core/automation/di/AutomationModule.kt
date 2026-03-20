package com.ariaai.companion.core.automation.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AutomationModule {
    // ReminderScheduler uses @Inject constructor — Hilt provides automatically.
    // Future: WorkManager workers for briefings added in Phase 4.
}
