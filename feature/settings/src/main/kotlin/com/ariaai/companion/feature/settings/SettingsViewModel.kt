package com.ariaai.companion.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariaai.companion.core.audio.tts.TextToSpeechManager
import com.ariaai.companion.core.domain.repository.SettingsRepository
import com.ariaai.companion.core.network.ai.CloudAiService
import com.ariaai.companion.core.network.ha.BaseUrlInterceptor
import com.ariaai.companion.core.network.ha.HaAuthInterceptor
import com.ariaai.companion.core.network.privacy.PrivacyInterceptor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TextToSpeechManager,
    private val haAuthInterceptor: HaAuthInterceptor,
    private val baseUrlInterceptor: BaseUrlInterceptor,
    private val cloudAiService: CloudAiService,
    private val privacyInterceptor: PrivacyInterceptor,
) : ViewModel() {

    val haServerUrl = settingsRepository.haServerUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val haAccessTokenPresent = settingsRepository.haAccessToken
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val ttsVoiceName = settingsRepository.ttsVoiceName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val privacyModeEnabled = settingsRepository.privacyModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val backgroundAutomationEnabled = settingsRepository.backgroundAutomationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val googleAiApiKeyPresent = settingsRepository.googleAiApiKey
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val availableVoices: List<String>
        get() = ttsManager.getAvailableVoices().map { it.name }

    // Keep interceptors and services updated with current settings
    init {
        viewModelScope.launch {
            settingsRepository.haServerUrl.collect { url ->
                if (url.isNotBlank()) {
                    baseUrlInterceptor.baseUrl = url.toHttpUrlOrNull()
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.haAccessToken.collect { token ->
                haAuthInterceptor.token = token
            }
        }
        viewModelScope.launch {
            settingsRepository.googleAiApiKey.collect { key ->
                cloudAiService.apiKey = key
            }
        }
        viewModelScope.launch {
            settingsRepository.privacyModeEnabled.collect { enabled ->
                privacyInterceptor.privacyModeEnabled = enabled
            }
        }
    }

    fun saveHaServerUrl(url: String) {
        viewModelScope.launch { settingsRepository.setHaServerUrl(url) }
    }

    fun saveHaAccessToken(token: String) {
        viewModelScope.launch { settingsRepository.setHaAccessToken(token) }
    }

    fun clearHaAccessToken() {
        viewModelScope.launch { settingsRepository.clearHaAccessToken() }
    }

    fun saveTtsVoice(voiceName: String) {
        viewModelScope.launch {
            settingsRepository.setTtsVoiceName(voiceName)
            ttsManager.setVoice(voiceName)
        }
    }

    fun setPrivacyMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setPrivacyModeEnabled(enabled) }
    }

    fun setBackgroundAutomation(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBackgroundAutomationEnabled(enabled) }
    }

    fun saveGoogleAiApiKey(key: String) {
        viewModelScope.launch { settingsRepository.setGoogleAiApiKey(key) }
    }

    fun clearGoogleAiApiKey() {
        viewModelScope.launch { settingsRepository.clearGoogleAiApiKey() }
    }

    fun clearAllData() {
        viewModelScope.launch { settingsRepository.clearAllUserData() }
    }
}
