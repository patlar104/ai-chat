package com.example.aicompanion.core.domain.repository

import com.example.aicompanion.core.domain.model.Reminder
import com.example.aicompanion.core.domain.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observePending(): Flow<List<Reminder>>
    suspend fun insert(reminder: Reminder): Long
    suspend fun updateStatus(id: Long, status: ReminderStatus)
    suspend fun getAllPending(): List<Reminder>
}
