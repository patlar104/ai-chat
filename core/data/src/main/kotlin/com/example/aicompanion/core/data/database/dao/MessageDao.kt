package com.example.aicompanion.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aicompanion.core.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestampMs ASC")
    fun observeAll(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE timestampMs > :sinceMs ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun getRecentMessages(sinceMs: Long, limit: Int): List<MessageEntity>
}
