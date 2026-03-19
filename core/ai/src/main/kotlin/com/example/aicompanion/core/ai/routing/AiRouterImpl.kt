package com.example.aicompanion.core.ai.routing

import com.example.aicompanion.core.ai.parser.CommandParser
import com.example.aicompanion.core.domain.model.ParsedIntent
import com.example.aicompanion.core.domain.model.SourceType
import com.example.aicompanion.core.network.ai.CloudAiService
import com.example.aicompanion.core.network.privacy.PrivacyInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRouterImpl @Inject constructor(
    private val commandParser: CommandParser,
    private val cloudAiService: CloudAiService,
    private val privacyInterceptor: PrivacyInterceptor,
) : AiRouter {
    override suspend fun resolveIntent(transcript: String): AiRouterResult {
        val deterministicIntent = commandParser.parse(transcript)
        if (deterministicIntent != null && deterministicIntent !is ParsedIntent.Unknown) {
            return AiRouterResult(deterministicIntent, SourceType.DETERMINISTIC)
        }

        // Degraded mode: privacy mode blocks cloud AI requests
        if (privacyInterceptor.privacyModeEnabled) {
            return AiRouterResult(
                ParsedIntent.CloudResponse(
                    "I can't answer that right now because Privacy Mode is active. " +
                        "I can still control your home and set reminders.",
                ),
                SourceType.UNKNOWN,
            )
        }

        // No API key configured — cloud AI unavailable
        if (cloudAiService.apiKey == null) {
            return AiRouterResult(ParsedIntent.Unknown, SourceType.UNKNOWN)
        }

        // Tier 2: Cloud AI fallback via Gemini
        val cloudResponse = cloudAiService.generateResponse(transcript)
        if (cloudResponse != null) {
            return AiRouterResult(ParsedIntent.CloudResponse(cloudResponse), SourceType.CLOUD)
        }

        // Tier 3: API key is set but request failed (offline / network error)
        return AiRouterResult(
            ParsedIntent.CloudResponse(
                "I can't answer that right now because Offline mode is active. " +
                    "I can still control your home and set reminders.",
            ),
            SourceType.UNKNOWN,
        )
    }
}
