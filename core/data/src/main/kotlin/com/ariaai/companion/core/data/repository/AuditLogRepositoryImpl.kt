package com.ariaai.companion.core.data.repository

import com.ariaai.companion.core.data.database.dao.AuditLogDao
import com.ariaai.companion.core.data.database.entity.AuditLogEntity
import com.ariaai.companion.core.domain.model.AuditLogEntry
import com.ariaai.companion.core.domain.model.AuditStatus
import com.ariaai.companion.core.domain.repository.AuditLogRepository
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
