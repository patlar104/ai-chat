package com.ariaai.companion.core.domain.repository

interface HomeAssistantRepository {
    suspend fun callService(
        domain: String,
        service: String,
        entityId: String,
        params: Map<String, Any> = emptyMap(),
    ): Result<Unit>
}
