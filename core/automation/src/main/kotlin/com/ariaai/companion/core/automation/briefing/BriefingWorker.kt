package com.ariaai.companion.core.automation.briefing

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ariaai.companion.core.data.database.AppDatabase
import com.ariaai.companion.core.data.database.entity.MemoryEntity
import com.ariaai.companion.core.data.database.entity.ReminderEntity
import timber.log.Timber

class BriefingWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "ai_companion_db",
            ).fallbackToDestructiveMigration().build()

            val pendingReminders: List<ReminderEntity> = db.reminderDao().getAllPending()
            val memoryEntries: List<MemoryEntity> = db.memoryDao().getAll()

            val message = buildBriefingText(pendingReminders.size, memoryEntries.size)
            sendBriefingNotification(message)
            Timber.d("Morning briefing sent: %s", message)

            Result.success()
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Failed to run briefing worker")
            Result.retry()
        }
    }

    private fun buildBriefingText(reminderCount: Int, memoryCount: Int): String {
        val remindersPart = if (reminderCount > 0) {
            "You have $reminderCount pending reminder${if (reminderCount == 1) "" else "s"}."
        } else {
            "No reminders pending."
        }
        val memoryPart = if (memoryCount > 0) {
            "You have $memoryCount memories available."
        } else {
            "No saved memories yet."
        }
        return "$remindersPart $memoryPart"
    }

    private fun sendBriefingNotification(summary: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, "briefings")
            .setContentTitle("Morning Briefing")
            .setContentText(summary)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(38291, notification)
    }
}
