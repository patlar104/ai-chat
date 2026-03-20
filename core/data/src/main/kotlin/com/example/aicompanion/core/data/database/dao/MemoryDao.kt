package com.example.aicompanion.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.aicompanion.core.data.database.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY createdAtMs DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY createdAtMs DESC")
    suspend fun getAll(): List<MemoryEntity>

    @Insert
    suspend fun insert(entity: MemoryEntity): Long

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
