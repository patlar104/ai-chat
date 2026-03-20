package com.ariaai.companion.core.domain.repository

import com.ariaai.companion.core.domain.model.MemoryEntry
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun observeAll(): Flow<List<MemoryEntry>>
    suspend fun insert(memoryEntry: MemoryEntry): Long
    suspend fun delete(id: Long)
}
