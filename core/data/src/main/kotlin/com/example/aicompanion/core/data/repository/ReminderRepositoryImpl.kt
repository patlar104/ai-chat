package com.example.aicompanion.core.data.repository

import com.example.aicompanion.core.data.database.dao.ReminderDao
import com.example.aicompanion.core.data.database.entity.ReminderEntity
import com.example.aicompanion.core.domain.model.Reminder
import com.example.aicompanion.core.domain.model.ReminderStatus
import com.example.aicompanion.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
) : ReminderRepository {

    override fun observePending(): Flow<List<Reminder>> =
        reminderDao.observePending().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(reminder: Reminder): Long =
        reminderDao.insert(reminder.toEntity())

    override suspend fun updateStatus(id: Long, status: ReminderStatus) =
        reminderDao.updateStatus(id, status.name)

    override suspend fun getAllPending(): List<Reminder> =
        reminderDao.getAllPending().map { it.toDomain() }

    private fun ReminderEntity.toDomain() = Reminder(
        id = id,
        description = description,
        triggerTimeMs = triggerTimeMs,
        status = ReminderStatus.valueOf(status),
        createdAtMs = createdAtMs,
    )

    private fun Reminder.toEntity() = ReminderEntity(
        id = id,
        description = description,
        triggerTimeMs = triggerTimeMs,
        status = status.name,
        createdAtMs = createdAtMs,
    )
}
