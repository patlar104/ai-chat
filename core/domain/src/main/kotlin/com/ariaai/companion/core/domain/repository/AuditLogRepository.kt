package com.ariaai.companion.core.domain.repository

import com.ariaai.companion.core.domain.model.AuditLogEntry
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun observeRecent(limit: Int = 50): Flow<List<AuditLogEntry>>
    suspend fun insert(entry: AuditLogEntry): Long
}
