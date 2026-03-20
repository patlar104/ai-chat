package com.example.aicompanion

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AICompanionApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create notification channels (must exist before first notification)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Voice session channel (low priority — ongoing background indicator)
        val voiceChannel = NotificationChannel(
            "voice_session",
            "Voice Session",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows when the assistant is listening"
        }

        // Reminders channel (high priority — user-facing alerts)
        val reminderChannel = NotificationChannel(
            "reminders",
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Reminder notifications"
            enableVibration(true)
        }

        // Daily briefings channel (default priority)
        val briefingChannel = NotificationChannel(
            "briefings",
            "Briefings",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily morning briefing notifications"
        }

        notificationManager.createNotificationChannel(voiceChannel)
        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(briefingChannel)
    }
}
