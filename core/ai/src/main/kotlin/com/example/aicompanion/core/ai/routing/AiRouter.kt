package com.example.aicompanion.core.ai.routing

import com.example.aicompanion.core.domain.model.ParsedIntent
import com.example.aicompanion.core.domain.model.SourceType

data class AiRouterResult(
    val intent: ParsedIntent,
    val sourceType: SourceType
)

interface AiRouter {
    suspend fun resolveIntent(transcript: String): AiRouterResult
}
