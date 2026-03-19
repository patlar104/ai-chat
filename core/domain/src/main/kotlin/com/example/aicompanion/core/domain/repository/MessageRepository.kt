package com.example.aicompanion.core.domain.repository

import com.example.aicompanion.core.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeAll(): Flow<List<Message>>
    suspend fun insert(message: Message): Long
    suspend fun getRecentMessages(sinceMs: Long, limit: Int = 20): List<Message>
}
