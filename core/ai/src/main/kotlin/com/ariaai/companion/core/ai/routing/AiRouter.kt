package com.ariaai.companion.core.ai.routing

import com.ariaai.companion.core.domain.model.ParsedIntent
import com.ariaai.companion.core.domain.model.SourceType

data class AiRouterResult(
    val intent: ParsedIntent,
    val sourceType: SourceType
)

interface AiRouter {
    suspend fun resolveIntent(transcript: String): AiRouterResult
}
