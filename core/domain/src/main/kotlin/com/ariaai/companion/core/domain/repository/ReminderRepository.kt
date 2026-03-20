package com.ariaai.companion.core.domain.repository

import com.ariaai.companion.core.domain.model.Reminder
import com.ariaai.companion.core.domain.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observePending(): Flow<List<Reminder>>
    suspend fun insert(reminder: Reminder): Long
    suspend fun updateStatus(id: Long, status: ReminderStatus)
    suspend fun getAllPending(): List<Reminder>
}
