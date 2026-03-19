package com.example.aicompanion.core.data.repository

import com.example.aicompanion.core.data.database.dao.AuditLogDao
import com.example.aicompanion.core.data.database.entity.AuditLogEntity
import com.example.aicompanion.core.domain.model.AuditLogEntry
import com.example.aicompanion.core.domain.model.AuditStatus
import com.example.aicompanion.core.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao,
) : AuditLogRepository {

    override fun observeRecent(limit: Int): Flow<List<AuditLogEntry>> =
        auditLogDao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(entry: AuditLogEntry): Long =
        auditLogDao.insert(entry.toEntity())

    private fun AuditLogEntity.toDomain() = AuditLogEntry(
        id = id,
        command = command,
        domain = domain,
        service = service,
        entityId = entityId,
        status = AuditStatus.valueOf(status),
        errorReason = errorReason,
        timestampMs = timestampMs,
    )

    private fun AuditLogEntry.toEntity() = AuditLogEntity(
        id = id,
        command = command,
        domain = domain,
        service = service,
        entityId = entityId,
        status = status.name,
        errorReason = errorReason,
        timestampMs = timestampMs,
    )
}
