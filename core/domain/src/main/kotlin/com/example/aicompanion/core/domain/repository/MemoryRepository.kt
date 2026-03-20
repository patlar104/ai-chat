package com.example.aicompanion.core.domain.repository

import com.example.aicompanion.core.domain.model.MemoryEntry
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun observeAll(): Flow<List<MemoryEntry>>
    suspend fun insert(memoryEntry: MemoryEntry): Long
    suspend fun delete(id: Long)
}
