package com.example.aicompanion.core.network.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CancellationException
import javax.inject.Singleton

/**
 * Service for interacting with the Google Gemini cloud AI.
 *
 * The API key is held in a @Volatile field and updated externally by
 * SettingsViewModel after reading from DataStore — mirroring the pattern
 * used by HaAuthInterceptor for the HA access token.
 */
@Singleton
class CloudAiService {

    @Volatile
    var apiKey: String? = null

    /**
     * Sends [prompt] to Gemini and returns the generated text response.
     *
     * Returns null when no API key is configured or the request fails.
     */
    suspend fun generateResponse(prompt: String): String? {
        val key = apiKey ?: return null
        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = key,
            )
            val response = model.generateContent(prompt)
            response.text
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
