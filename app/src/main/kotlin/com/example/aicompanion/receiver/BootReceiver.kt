package com.example.aicompanion.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.aicompanion.core.automation.reminder.ReminderScheduler
import com.example.aicompanion.core.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pending = reminderRepository.getAllPending()
                pending.forEach { reminder ->
                    if (reminder.triggerTimeMs > System.currentTimeMillis()) {
                        reminderScheduler.schedule(reminder.id, reminder.triggerTimeMs, reminder.description)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
