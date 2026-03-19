package com.example.aicompanion.core.domain.repository

import com.example.aicompanion.core.domain.model.AuditLogEntry
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun observeRecent(limit: Int = 50): Flow<List<AuditLogEntry>>
    suspend fun insert(entry: AuditLogEntry): Long
}
