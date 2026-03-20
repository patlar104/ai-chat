package com.example.aicompanion.core.ai.routing

import com.example.aicompanion.core.ai.parser.CommandParser
import com.example.aicompanion.core.domain.model.HomeAction
import com.example.aicompanion.core.domain.model.ParsedIntent
import com.example.aicompanion.core.domain.model.SourceType
import com.example.aicompanion.core.network.ai.CloudAiService
import com.example.aicompanion.core.network.privacy.PrivacyInterceptor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiRouterTest {

    private lateinit var commandParser: CommandParser
    private lateinit var cloudAiService: CloudAiService
    private lateinit var privacyInterceptor: PrivacyInterceptor
    private lateinit var aiRouter: AiRouter

    @Before
    fun setup() {
        commandParser = mockk()
        cloudAiService = mockk()
        privacyInterceptor = PrivacyInterceptor() // real instance — just a flag
        aiRouter = AiRouterImpl(commandParser, cloudAiService, privacyInterceptor)
    }

    @Test
    fun `when CommandParser returns an intent, AiRouter returns it with SourceType DETERMINISTIC`() = runTest {
        val transcript = "turn on the light"
        val expectedIntent = ParsedIntent.HomeControl("light", HomeAction.TURN_ON)
        every { commandParser.parse(transcript) } returns expectedIntent

        val result = aiRouter.resolveIntent(transcript)

        assertEquals(expectedIntent, result.intent)
        assertEquals(SourceType.DETERMINISTIC, result.sourceType)
    }

    @Test
    fun `when CommandParser returns Unknown and no API key configured, AiRouter returns UNKNOWN`() = runTest {
        val transcript = "how is the weather"
        every { commandParser.parse(transcript) } returns ParsedIntent.Unknown
        every { cloudAiService.apiKey } returns null

        val result = aiRouter.resolveIntent(transcript)

        assertEquals(ParsedIntent.Unknown, result.intent)
        assertEquals(SourceType.UNKNOWN, result.sourceType)
    }

    @Test
    fun `when CommandParser returns Unknown and cloud responds, AiRouter returns CLOUD`() = runTest {
        val transcript = "tell me a joke"
        val cloudReply = "Why did the chicken cross the road? To get to the other side!"
        every { commandParser.parse(transcript) } returns ParsedIntent.Unknown
        every { cloudAiService.apiKey } returns "test-key"
        coEvery { cloudAiService.generateResponse(transcript) } returns cloudReply

        val result = aiRouter.resolveIntent(transcript)

        assertEquals(ParsedIntent.CloudResponse(cloudReply), result.intent)
        assertEquals(SourceType.CLOUD, result.sourceType)
    }

    @Test
    fun `when CommandParser returns Unknown and cloud responds with complex answer, AiRouter returns CLOUD`() = runTest {
        val transcript = "what is the capital of France"
        val cloudReply = "The capital of France is Paris."
        every { commandParser.parse(transcript) } returns ParsedIntent.Unknown
        every { cloudAiService.apiKey } returns "test-key"
        coEvery { cloudAiService.generateResponse(transcript) } returns cloudReply

        val result = aiRouter.resolveIntent(transcript)

        assertEquals(ParsedIntent.CloudResponse(cloudReply), result.intent)
        assertEquals(SourceType.CLOUD, result.sourceType)
    }

    @Test
    fun `when privacy mode is enabled, AiRouter returns degraded privacy message`() = runTest {
        val transcript = "what is the weather"
        every { commandParser.parse(transcript) } returns ParsedIntent.Unknown
        privacyInterceptor.privacyModeEnabled = true

        val result = aiRouter.resolveIntent(transcript)

        assertTrue(result.intent is ParsedIntent.CloudResponse)
        val responseText = (result.intent as ParsedIntent.CloudResponse).text
        assertTrue(responseText.contains("Privacy Mode"))
        assertEquals(SourceType.UNKNOWN, result.sourceType)
    }

    @Test
    fun `when deterministic command is issued and privacy mode is enabled, AiRouter still executes it`() = runTest {
        val transcript = "turn on the lights"
        val expectedIntent = ParsedIntent.HomeControl("lights", HomeAction.TURN_ON)
        every { commandParser.parse(transcript) } returns expectedIntent
        privacyInterceptor.privacyModeEnabled = true

        val result = aiRouter.resolveIntent(transcript)

        assertEquals(expectedIntent, result.intent)
        assertEquals(SourceType.DETERMINISTIC, result.sourceType)
    }

    @Test
    fun `when API key is set but cloud returns null, AiRouter returns offline degraded message`() = runTest {
        val transcript = "what is the weather"
        every { commandParser.parse(transcript) } returns ParsedIntent.Unknown
        every { cloudAiService.apiKey } returns "test-key"
        coEvery { cloudAiService.generateResponse(transcript) } returns null

        val result = aiRouter.resolveIntent(transcript)

        assertTrue(result.intent is ParsedIntent.CloudResponse)
        val responseText = (result.intent as ParsedIntent.CloudResponse).text
        assertTrue(responseText.contains("Offline mode"))
        assertEquals(SourceType.UNKNOWN, result.sourceType)
    }
}
