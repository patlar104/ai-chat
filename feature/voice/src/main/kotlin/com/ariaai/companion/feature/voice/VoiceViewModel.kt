package com.ariaai.companion.feature.voice

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariaai.companion.core.audio.focus.AudioFocusManager
import com.ariaai.companion.core.audio.model.SpeechResult
import com.ariaai.companion.core.audio.speech.SpeechRecognizerManager
import com.ariaai.companion.core.audio.tts.TextToSpeechManager
import com.ariaai.companion.core.ai.routing.AiRouter
import com.ariaai.companion.core.domain.di.MainDispatcher
import com.ariaai.companion.core.domain.error.AppError
import com.ariaai.companion.core.domain.logging.Logger
import com.ariaai.companion.core.domain.model.AuditLogEntry
import com.ariaai.companion.core.domain.model.AuditStatus
import com.ariaai.companion.core.domain.model.HomeAction
import com.ariaai.companion.core.domain.model.Message
import com.ariaai.companion.core.domain.model.MessageRole
import com.ariaai.companion.core.domain.model.ParsedIntent
import com.ariaai.companion.core.domain.model.QueryType
import com.ariaai.companion.core.domain.model.Reminder
import com.ariaai.companion.core.domain.model.ReminderStatus
import com.ariaai.companion.core.domain.model.SourceType
import com.ariaai.companion.core.domain.model.VoiceEvent
import com.ariaai.companion.core.domain.model.VoiceState
import com.ariaai.companion.core.domain.model.VoiceUiEffect
import com.ariaai.companion.core.domain.repository.AliasRepository
import com.ariaai.companion.core.domain.repository.AuditLogRepository
import com.ariaai.companion.core.domain.repository.HomeAssistantRepository
import com.ariaai.companion.core.domain.repository.MessageRepository
import com.ariaai.companion.core.domain.repository.ReminderRepository
import com.ariaai.companion.core.domain.repository.SettingsRepository
import com.ariaai.companion.core.automation.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val speechManager: SpeechRecognizerManager,
    private val ttsManager: TextToSpeechManager,
    private val audioFocusManager: AudioFocusManager,
    private val aiRouter: AiRouter,
    private val haRepository: HomeAssistantRepository,
    private val aliasRepository: AliasRepository,
    private val messageRepository: MessageRepository,
    private val auditLogRepository: AuditLogRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _effects = Channel<VoiceUiEffect>(Channel.BUFFERED)
    val effects: Flow<VoiceUiEffect> = _effects.receiveAsFlow()

    private val _lastReply = MutableStateFlow<String?>(null)
    val lastReply: StateFlow<String?> = _lastReply.asStateFlow()

    /** True when Privacy Mode is active — observed from DataStore via SettingsRepository. */
    val privacyModeEnabled: StateFlow<Boolean> = settingsRepository.privacyModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ConnectivityManager must be declared before _isOnline since isCurrentlyOnline() uses it
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** True when the device has an active internet connection. */
    private val _isOnline = MutableStateFlow(isCurrentlyOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val sessionId = UUID.randomUUID().toString()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = isCurrentlyOnline()
        }
    }

    init {
        viewModelScope.launch {
            ttsManager.initialize { }
            speechManager.initialize()
        }
        // Observe TTS state for auto-transition Speaking -> Idle
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                if (!speaking && _state.value is VoiceState.Speaking) {
                    audioFocusManager.abandonFocus()
                    _state.value = VoiceState.Idle
                }
            }
        }
        // Register network callback for online/offline status
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun isCurrentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun onEvent(event: VoiceEvent) {
        viewModelScope.launch {
            when (event) {
                is VoiceEvent.MicTapped -> handleMicTapped()
                is VoiceEvent.TranscriptReceived -> handleTranscript(event.text)
                is VoiceEvent.RecognitionError -> handleRecognitionError(event.code)
                is VoiceEvent.ReplyReady -> handleReply(event.text)
                is VoiceEvent.CommandError -> handleCommandError(event.error)
                is VoiceEvent.TtsDone -> {
                    audioFocusManager.abandonFocus()
                    _state.value = VoiceState.Idle
                }
            }
        }
    }

    private suspend fun handleMicTapped() {
        when (_state.value) {
            is VoiceState.Idle -> startListening()
            is VoiceState.Listening -> {
                speechManager.stopListening()
                _state.value = VoiceState.Idle
            }
            is VoiceState.Speaking -> {
                // VOIC-06: Interrupt TTS
                ttsManager.stop()
                audioFocusManager.abandonFocus()
                _state.value = VoiceState.Idle
            }
            else -> { /* Ignore taps during Transcribing/Processing/Error */ }
        }
    }

    private suspend fun startListening() {
        _state.value = VoiceState.Listening
        try {
            withTimeout(8_000L) {
                speechManager.startListening { result ->
                    viewModelScope.launch {
                        when (result) {
                            is SpeechResult.Success -> {
                                _state.value = VoiceState.Transcribing
                                onEvent(VoiceEvent.TranscriptReceived(result.text))
                            }
                            is SpeechResult.Error -> {
                                onEvent(VoiceEvent.RecognitionError(result.code))
                            }
                        }
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            _effects.send(VoiceUiEffect.ShowToast("No speech detected"))
            _state.value = VoiceState.Error(AppError.AudioPipeline("Speech recognition timed out"))
            delay(2_000)
            _state.value = VoiceState.Idle
        }
    }

    private suspend fun handleTranscript(text: String) {
        _state.value = VoiceState.Processing(text)

        // Save user message
        val userMessage = Message(
            role = MessageRole.USER,
            content = text,
            sourceType = SourceType.DETERMINISTIC,
            timestampMs = System.currentTimeMillis(),
            sessionId = sessionId,
        )
        messageRepository.insert(userMessage)

        // Parse intent through router
        val result = aiRouter.resolveIntent(text)
        val intent = result.intent

        // Execute intent
        val replyText = when (intent) {
            is ParsedIntent.HomeControl -> executeHomeControl(intent)
            is ParsedIntent.Routine -> executeRoutine(intent)
            is ParsedIntent.CreateReminder -> executeCreateReminder(intent)
            is ParsedIntent.LocalQuery -> executeLocalQuery(intent)
            is ParsedIntent.CloudResponse -> intent.text
            is ParsedIntent.Unknown -> "Sorry, I didn't understand that. I can control devices, set reminders, and execute routines."
        }

        // Save assistant message with the source resolved by the router
        val assistantMessage = Message(
            role = MessageRole.ASSISTANT,
            content = replyText,
            sourceType = result.sourceType,
            timestampMs = System.currentTimeMillis(),
            sessionId = sessionId,
        )
        messageRepository.insert(assistantMessage)

        // Speak reply
        speakReply(replyText)
    }

    private suspend fun executeHomeControl(intent: ParsedIntent.HomeControl): String {
        val alias = aliasRepository.findByAlias(intent.entityAlias)
            ?: return "I don't know a device called \"${intent.entityAlias}\". You can add it in Home Control settings."

        val (domain, service) = when (intent.action) {
            HomeAction.TURN_ON -> alias.domain to "turn_on"
            HomeAction.TURN_OFF -> alias.domain to "turn_off"
            HomeAction.SET_BRIGHTNESS -> "light" to "turn_on"
            HomeAction.SET_TEMPERATURE -> "climate" to "set_temperature"
        }

        val result = haRepository.callService(domain, service, alias.entityId, intent.params)

        auditLogRepository.insert(
            AuditLogEntry(
                command = "${intent.action.name.lowercase()} ${intent.entityAlias}",
                domain = domain,
                service = service,
                entityId = alias.entityId,
                status = if (result.isSuccess) AuditStatus.SUCCESS else AuditStatus.FAILURE,
                errorReason = result.exceptionOrNull()?.message,
                timestampMs = System.currentTimeMillis(),
            )
        )

        return if (result.isSuccess) {
            when (intent.action) {
                HomeAction.TURN_ON -> "Done, turning on ${intent.entityAlias}"
                HomeAction.TURN_OFF -> "Done, turning off ${intent.entityAlias}"
                HomeAction.SET_BRIGHTNESS -> {
                    val pct = ((intent.params["brightness"] as? Int ?: 0) * 100 / 255)
                    "Done, setting ${intent.entityAlias} to $pct percent"
                }
                HomeAction.SET_TEMPERATURE -> "Done, setting ${intent.entityAlias} to ${intent.params["temperature"]} degrees"
            }
        } else {
            "I couldn't reach your Home Assistant — check the connection in Settings"
        }
    }

    private suspend fun executeRoutine(intent: ParsedIntent.Routine): String {
        val alias = aliasRepository.findByAlias(intent.routineAlias)
            ?: return "I don't know a routine called \"${intent.routineAlias}\". You can add it in Home Control settings."

        val result = haRepository.callService(alias.domain, "turn_on", alias.entityId)

        auditLogRepository.insert(
            AuditLogEntry(
                command = "run ${intent.routineAlias}",
                domain = alias.domain,
                service = "turn_on",
                entityId = alias.entityId,
                status = if (result.isSuccess) AuditStatus.SUCCESS else AuditStatus.FAILURE,
                errorReason = result.exceptionOrNull()?.message,
                timestampMs = System.currentTimeMillis(),
            )
        )

        return if (result.isSuccess) "Done, running ${intent.routineAlias}"
        else "I couldn't reach your Home Assistant — check the connection in Settings"
    }

    private suspend fun executeCreateReminder(intent: ParsedIntent.CreateReminder): String {
        val reminder = Reminder(
            description = intent.description,
            triggerTimeMs = intent.triggerTimeMs,
            status = ReminderStatus.PENDING,
            createdAtMs = System.currentTimeMillis(),
        )
        val id = reminderRepository.insert(reminder)
        reminderScheduler.schedule(id, intent.triggerTimeMs, intent.description)

        val timeText = SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(Date(intent.triggerTimeMs))
        return "Reminder set for $timeText: ${intent.description}"
    }

    private fun executeLocalQuery(intent: ParsedIntent.LocalQuery): String {
        return when (intent.queryType) {
            QueryType.CURRENT_TIME -> {
                val time = SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date())
                "It's $time"
            }
            QueryType.LIST_REMINDERS -> "Check your Tasks screen for pending reminders"
        }
    }

    private fun speakReply(text: String) {
        _lastReply.value = text
        _state.value = VoiceState.Speaking(text)

        val focusGranted = audioFocusManager.requestFocusForTts {
            // On focus loss, stop TTS
            ttsManager.stop()
        }

        if (focusGranted) {
            ttsManager.speak(text) {
                // onDone callback — handled by isSpeaking StateFlow observer
            }
        } else {
            logger.w("VoiceViewModel", "Audio focus not granted — skipping TTS")
            _state.value = VoiceState.Idle
        }
    }

    private suspend fun handleRecognitionError(code: Int) {
        val message = when (code) {
            SpeechRecognizer.ERROR_NETWORK -> "No network available for speech recognition"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected — try again"
            SpeechRecognizer.ERROR_NO_MATCH -> "Couldn't understand — try again"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                delay(500)
                startListening()
                return
            }
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            else -> "Speech recognition failed — try again"
        }
        _effects.send(VoiceUiEffect.ShowToast(message))
        _effects.send(VoiceUiEffect.Vibrate)
        _state.value = VoiceState.Error(AppError.AudioPipeline(message))
        delay(2_000)
        _state.value = VoiceState.Idle
    }

    private suspend fun handleCommandError(error: AppError) {
        val message = when (error) {
            is AppError.HomeAssistant -> "I couldn't reach your Home Assistant — check the connection in Settings"
            is AppError.Network -> "Network error — check your connection"
            else -> "Something went wrong"
        }
        _effects.send(VoiceUiEffect.ShowToast(message))
        speakReply(message)
    }

    private fun handleReply(text: String) {
        speakReply(text)
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        viewModelScope.launch {
            speechManager.destroy()
            ttsManager.shutdown()
            audioFocusManager.abandonFocus()
        }
    }
}
