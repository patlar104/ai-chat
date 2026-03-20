package com.example.aicompanion.core.data.repository

import com.example.aicompanion.core.data.database.dao.MemoryDao
import com.example.aicompanion.core.data.database.entity.MemoryEntity
import com.example.aicompanion.core.domain.model.MemoryEntry
import com.example.aicompanion.core.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao,
) : MemoryRepository {

    override fun observeAll(): Flow<List<MemoryEntry>> =
        memoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(memoryEntry: MemoryEntry): Long =
        memoryDao.insert(memoryEntry.toEntity())

    override suspend fun delete(id: Long) =
        memoryDao.deleteById(id)

    private fun MemoryEntity.toDomain() = MemoryEntry(
        id = id,
        title = title,
        detail = detail,
        createdAtMs = createdAtMs,
    )

    private fun MemoryEntry.toEntity() = MemoryEntity(
        id = id,
        title = title,
        detail = detail,
        createdAtMs = createdAtMs,
    )
}
