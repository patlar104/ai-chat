package com.example.aicompanion.core.data.repository

import com.example.aicompanion.core.data.database.dao.MessageDao
import com.example.aicompanion.core.data.database.entity.MessageEntity
import com.example.aicompanion.core.domain.model.Message
import com.example.aicompanion.core.domain.model.MessageRole
import com.example.aicompanion.core.domain.model.SourceType
import com.example.aicompanion.core.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
) : MessageRepository {

    override fun observeAll(): Flow<List<Message>> =
        messageDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(message: Message): Long =
        messageDao.insert(message.toEntity())

    override suspend fun getRecentMessages(sinceMs: Long, limit: Int): List<Message> =
        messageDao.getRecentMessages(sinceMs, limit).map { it.toDomain() }

    private fun MessageEntity.toDomain() = Message(
        id = id,
        role = MessageRole.valueOf(role),
        content = content,
        sourceType = SourceType.valueOf(sourceType),
        timestampMs = timestampMs,
        sessionId = sessionId,
    )

    private fun Message.toEntity() = MessageEntity(
        id = id,
        role = role.name,
        content = content,
        sourceType = sourceType.name,
        timestampMs = timestampMs,
        sessionId = sessionId,
    )
}
