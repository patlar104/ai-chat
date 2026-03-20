package com.example.aicompanion.core.automation.briefing

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId

object BriefingScheduler {
    private const val BRIEFING_WORK_NAME = "daily_morning_briefing"

    fun scheduleDailyBriefing(context: Context) {
        val now = LocalTime.now(ZoneId.systemDefault())
        val target = LocalTime.of(7, 0)
        val initialDelay = if (now.isBefore(target)) {
            Duration.between(now, target)
        } else {
            Duration.between(now, target.plusHours(24))
        }

        val workRequest = PeriodicWorkRequestBuilder<BriefingWorker>(1, java.util.concurrent.TimeUnit.DAYS)
            .setInitialDelay(initialDelay)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BRIEFING_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest,
        )
    }
}
