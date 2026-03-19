package com.example.aicompanion.core.ai.routing

import com.example.aicompanion.core.ai.parser.CommandParser
import com.example.aicompanion.core.domain.model.ParsedIntent
import com.example.aicompanion.core.domain.model.SourceType
import com.example.aicompanion.core.network.ai.CloudAiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRouterImpl @Inject constructor(
    private val commandParser: CommandParser,
    private val cloudAiService: CloudAiService,
) : AiRouter {
    override suspend fun resolveIntent(transcript: String): AiRouterResult {
        val deterministicIntent = commandParser.parse(transcript)
        if (deterministicIntent != null && deterministicIntent !is ParsedIntent.Unknown) {
            return AiRouterResult(deterministicIntent, SourceType.DETERMINISTIC)
        }

        // Tier 2: Cloud AI fallback via Gemini
        val cloudResponse = cloudAiService.generateResponse(transcript)
        if (cloudResponse != null) {
            return AiRouterResult(ParsedIntent.CloudResponse(cloudResponse), SourceType.CLOUD)
        }

        // Tier 3 stub — cloud unavailable (no key or network error)
        return AiRouterResult(ParsedIntent.Unknown, SourceType.UNKNOWN)
    }
}
