# Phase 2: Voice + Home Control MVP - Research

**Researched:** 2026-03-18
**Domain:** Android voice pipeline (SpeechRecognizer + TTS), Home Assistant REST API, Room persistence, AlarmManager reminders, Android Keystore encryption, MVI state machine
**Confidence:** HIGH (Android platform APIs from official docs) / MEDIUM (HA REST API from official HA dev docs)

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **Tap-to-toggle voice:** Tap mic once to start listening, tap again (or on TTS completion) to return to Idle. No press-and-hold.
- **VoiceScreen is the home screen:** Large centered mic button, start destination in AppNavHost.
- **Animated mic button for all states (VOIC-02):** Single button transforms per state — pulse (Listening), spinner (Transcribing/Processing), waveform (Speaking), red/error tint (Error). No separate status text label.
- **Last assistant reply shown as text below the mic button:** One card showing most recent reply.
- **Interrupt behavior (VOIC-06):** Tapping mic while TTS is Speaking stops TTS immediately and returns to Idle. No barge-in.
- **Audio error handling:** Toast with plain-language message + return to Idle. No persistent error card.
- **Bottom navigation bar:** Persistent Material 3 NavigationBar with 5 destinations: Voice, Chat, Home Control, Tasks, Settings.
- **TTS 'sorry' reply for unrecognized commands:** Logged with source_type = unknown.
- **No cloud AI fallback in Phase 2:** Deterministic-only.
- **Deterministic parser intent categories:** Home control (on/off/brightness/temperature), named routines, reminder creation, simple local queries.
- **REST API only for Phase 2:** HTTP POST to /api/services/{domain}/{service}. No WebSocket.
- **HA auth:** Bearer token in Authorization header, stored via Android Keystore + DataStore (NOT EncryptedSharedPreferences).
- **HOME-07 failure handling:** Speak plain-language TTS error + log to audit log.
- **HomeControlScreen:** Audit log only. Alias map editor on this screen or sub-screen (planner's discretion).
- **Separate ChatScreen:** Full conversation history on ChatScreen. VoiceScreen shows only last reply.
- **Chat bubble UI:** User right (filled), assistant left (outlined). Timestamp below each message.
- **Source badges (CONV-03):** Small chip on each assistant message: `deterministic` or `unknown` in Phase 2.
- **Flat scroll, newest at bottom:** LazyColumn, auto-scrolls. No date grouping headers.
- **Settings layout:** Claude's Discretion — standard scrollable form with HA URL, HA Token, TTS Voice, Privacy Mode toggle, Background Automation toggle.
- **Privacy Mode toggle persists to DataStore but network enforcement is Phase 3.**

### Claude's Discretion
- Exact Compose color/typography choices (use Material 3 color roles)
- Exact wording of deterministic parser regex patterns (must cover home control, routine, reminder, query categories)
- Whether alias map UI is inline on HomeControlScreen or a nav sub-destination
- AlarmManager vs WorkManager for reminder scheduling
- Exact HA REST API endpoints used for service calls
- Room schema design for conversation messages and audit log entries

### Deferred Ideas (OUT OF SCOPE)
- WebSocket connection to Home Assistant for real-time state
- Cloud AI fallback for unrecognized commands
- Live device state panel on HomeControlScreen
- Date/session grouping headers in ChatScreen
- Barge-in voice interruption (VOIC-V2-03)
</user_constraints>

---

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| VOIC-01 | User can initiate a voice session by pressing a button (button-to-talk) | MVI VoiceState machine; tap toggles IDLE→LISTENING |
| VOIC-02 | Assistant visual state reflects Idle, Listening, Transcribing, Processing, Speaking, Error | Compose Animatable + animated content per state; StateFlow drives recomposition |
| VOIC-03 | User speech captured via Android SpeechRecognizer and transcribed to text | SpeechRecognizer created on main thread, callbacks deliver text; RecognitionListener implementation |
| VOIC-04 | Voice session runs in a foreground service with correct foreground service type | `foregroundServiceType="microphone"` + `FOREGROUND_SERVICE_MICROPHONE` permission declared in manifest |
| VOIC-05 | Audio focus requested and abandoned correctly across voice session lifecycle | AudioFocusRequest.Builder with USAGE_ASSISTANT + CONTENT_TYPE_SPEECH; abandon on TTS done/error |
| VOIC-06 | User can interrupt or stop an in-progress assistant reply | Tap mic in Speaking state → tts.stop() + abandon focus → return IDLE |
| TTS-01 | Assistant replies spoken via Android TextToSpeech | TTS init pattern with onInit guard; speak() with QUEUE_FLUSH |
| TTS-02 | Short utility confirmations use local TTS for speed | Local TTS always used in Phase 2; cloud TTS deferred |
| TTS-03 | TTS state (speaking/idle) is observable so UI can react | UtteranceProgressListener posts to StateFlow; Speaking/Idle states observable |
| CONV-01 | Conversation history persists in Room across restarts | Room `MessageEntity` table; DAO with Flow<List<MessageEntity>> |
| CONV-02 | Conversation history viewable in scrollable chat timeline | LazyColumn ChatScreen; user/assistant bubbles with timestamp |
| CONV-03 | Each message records role, content, source type, timestamp | MessageEntity schema: id, role, content, sourceType, timestampMs |
| CONV-04 | Short-term in-session context enables multi-turn references | In-memory session list (last N messages) injected into parser context; not persisted separately |
| HOME-01 | User can control named devices by voice (on/off, brightness, temperature) | Parser extracts intent; POST /api/services/{domain}/{service} with entity_id + params |
| HOME-02 | User can execute named routines by voice | POST /api/services/script/turn_on or /api/services/scene/turn_on |
| HOME-03 | Device and room alias map translates spoken names to HA entity IDs | AliasEntity in Room; alias→entityId lookup before HA call |
| HOME-04 | User can add, edit, delete device and room aliases | Alias CRUD via Room DAO; UI on HomeControlScreen or sub-screen |
| HOME-05 | Assistant speaks confirmation after each home control action | TTS reply on successful HA service call response (HTTP 200) |
| HOME-06 | Each tool invocation recorded in audit log | AuditLogEntity in Room; written on every HA call with command, status, error, timestamp |
| HOME-07 | Failed tool invocations surface plain-language explanation | AppError.HomeAssistant wraps failure; TTS error reply + audit log entry |
| TASK-01 | User can create a reminder by voice with time/description | Parser extracts time + description; insert ReminderEntity to Room + schedule AlarmManager alarm |
| TASK-02 | Reminders trigger system notification at scheduled time | BroadcastReceiver receives PendingIntent; posts NotificationCompat via NotificationManager |
| TASK-03 | User can view list of pending reminders | TasksScreen LazyColumn; Room DAO Flow<List<ReminderEntity>> filtered by status=PENDING |
| TASK-04 | User can cancel or delete pending reminder | Cancel PendingIntent via AlarmManager.cancel(); update Room record status=CANCELLED |
| SETT-01 | HA server URL + token stored securely | DataStore for URL; Keystore AES-GCM encrypt/decrypt for token bytes stored in DataStore |
| SETT-02 | Privacy mode toggle | Switch persists boolean to DataStore; enforcement deferred to Phase 3 |
| SETT-03 | TTS voice preference | getVoices() → ExposedDropdownMenuBox; persist selected voice name to DataStore |
| SETT-04 | Background automation toggle | Switch persists boolean to DataStore |
</phase_requirements>

---

## Summary

Phase 2 is the most complex phase in the project — it delivers 28 requirements across five distinct technical domains: Android voice pipeline (SpeechRecognizer + TTS), MVI state machine, Room persistence, Home Assistant REST API integration, and AlarmManager reminders. All five domains have mature, well-documented Android patterns. The risk is coordination complexity, not API novelty.

The single most critical architectural constraint is that **SpeechRecognizer must be created on the main thread** — this is a hard Android SDK requirement that will cause silent callback failures if violated. Every other threading decision flows from this: the VoiceViewModel must dispatch recognizer creation to `Dispatchers.Main` even when called from a coroutine context. The second mandatory requirement is `foregroundServiceType="microphone"` in the manifest — missing this causes `SecurityException` on Android 14+ when the service starts.

The Home Assistant integration is the lowest-risk domain. The REST API is straightforward: `POST /api/services/{domain}/{service}` with a JSON body and `Authorization: Bearer {token}` header. Retrofit 3.0 (already in `libs.versions.toml`) with a `kotlinx-serialization` converter factory is the correct client setup. Token security requires Android Keystore AES-GCM encryption with the ciphertext + IV stored as bytes in DataStore — never plain SharedPreferences and never `EncryptedSharedPreferences` (deprecated).

For reminder scheduling, **AlarmManager with `setExactAndAllowWhileIdle()`** is the correct choice for Phase 2. WorkManager is for best-effort background work; AlarmManager fires reliably through Doze mode. On Android 12+, declare `SCHEDULE_EXACT_ALARM` permission and call `canScheduleExactAlarms()` before scheduling — with a graceful fallback to `setAndAllowWhileIdle()` if the permission is denied.

**Primary recommendation:** Build the voice state machine first (waves 1-3), then Room + DataStore (wave 4), then HA REST integration (wave 5), then the parser + reminder logic (wave 6), then all five screen UIs (wave 7), then wire the full pipeline end-to-end (wave 8).

---

## Standard Stack

### Core — All Already Declared in libs.versions.toml

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Android SpeechRecognizer | Platform API | STT transcription | Zero dependency; offline flag available; already in SUMMARY.md |
| Android TextToSpeech | Platform API | TTS playback | Zero latency for short confirmations; offline; no external dep |
| Room | 2.8.4 | Conversation messages, audit log, aliases, reminders | KSP-compatible; Flow support; standard for all persistence |
| DataStore Preferences | 1.2.1 | HA URL, TTS voice pref, Privacy Mode, Background Automation | Coroutine-native SharedPreferences replacement |
| OkHttp | 5.0.0 | HTTP client for HA REST API | Already in catalog; OkHttp 5 is suspend-first |
| Retrofit | 3.0.0 | HA REST API client | Already in catalog; suspend-function native in v3 |
| kotlinx-serialization-json | 1.10.0 | JSON encode/decode for HA request/response bodies | Already in catalog; works with Retrofit 3.0 built-in converter |
| Hilt + KSP | 2.57.1 | DI for ViewModels, repositories, use cases, services | Established in Phase 1; KSP-only, no kapt |
| Compose Material 3 | via BOM 2026.03.00 | All UI: NavigationBar, LazyColumn, switches, dropdowns | Established in Phase 1; UI-SPEC mandates Material 3 |
| Coroutines | 1.10.2 | Async pipeline, StateFlow, Flow | Already in catalog; viewModelScope throughout |

### Android Keystore (No New Dependency)

The Android Keystore system is a platform API — no additional library is needed. Use `KeyGenerator` with the `"AndroidKeyStore"` provider directly.

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `androidx.core:core-ktx` | via BOM | Notification building, PendingIntent helpers | Reminder notification in BroadcastReceiver |
| `compose-material-icons-extended` | via BOM | Material Symbols (Mic, Chat, Home, Alarm, Settings icons) | NavigationBar icons per UI-SPEC |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| AlarmManager | WorkManager OneTimeWorkRequest | WorkManager has no exact-time guarantee; OEM Doze kills it. AlarmManager `setExactAndAllowWhileIdle` is the right choice for user-visible reminders |
| Android TTS | ElevenLabs / OpenAI TTS | Cloud TTS is v2. Local TTS is zero-latency for confirmations and works offline |
| Retrofit | Ktor | No established KMP need; Retrofit 3.0 is already in catalog |
| EncryptedSharedPreferences | DataStore + Keystore | EncryptedSharedPreferences is deprecated as of July 2025 — never use it |

### Installation

All packages are already declared in `libs.versions.toml`. No new dependencies required for Phase 2. The following catalog entries activate for Phase 2:
- `room-runtime`, `room-ktx`, `room-compiler` (KSP)
- `datastore-preferences`
- `okhttp`, `okhttp-logging`, `retrofit`
- `kotlinx-serialization-json`
- `coroutines-android`, `coroutines-core`
- `hilt-android`, `hilt-compiler` (KSP), `hilt-navigation-compose`
- `compose-material3`, `navigation-compose`

The Retrofit 3.0 kotlinx-serialization converter is built in to `com.squareup.retrofit2:converter-kotlinx-serialization` (note: artifact group stays `retrofit2` even for version 3.0). Add this entry to `libs.versions.toml`:

```toml
retrofit-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
```

---

## Architecture Patterns

### Recommended Module Organization for Phase 2

```
core/audio/
├── di/AudioModule.kt           # SpeechRecognizer + TTS Hilt bindings (abstract class, @Binds)
├── speech/SpeechRecognizerManager.kt  # Main-thread SpeechRecognizer lifecycle wrapper
├── tts/TextToSpeechManager.kt  # TTS init/speak/stop/shutdown wrapper
└── model/SpeechResult.kt       # Success(text) / Error(code) sealed class

core/data/
├── di/DataModule.kt            # Room database + DataStore providers
├── database/AppDatabase.kt     # @Database with all entities
├── database/entity/MessageEntity.kt
├── database/entity/AuditLogEntity.kt
├── database/entity/AliasEntity.kt
├── database/entity/ReminderEntity.kt
├── database/dao/MessageDao.kt
├── database/dao/AuditLogDao.kt
├── database/dao/AliasDao.kt
├── database/dao/ReminderDao.kt
└── datastore/AppPreferences.kt # DataStore wrapper for settings

core/network/
├── di/NetworkModule.kt         # OkHttpClient + Retrofit + HA service
├── ha/HomeAssistantService.kt  # Retrofit interface
├── ha/HaServiceRequest.kt      # @Serializable data class
└── ha/HaAuthInterceptor.kt     # Adds Bearer token header

core/ai/                        # (Phase 2 creates the deterministic parser only)
├── parser/CommandParser.kt     # Regex-based intent extraction
└── model/ParsedIntent.kt       # Sealed class of intents

feature/voice/
├── VoiceViewModel.kt           # @HiltViewModel; MVI state machine
├── VoiceScreen.kt              # Composable; observes state, fires events
└── model/VoiceState.kt         # Sealed class + UiEffect

feature/chat/
├── ChatViewModel.kt
└── ChatScreen.kt

feature/homecontrol/
├── HomeControlViewModel.kt
└── HomeControlScreen.kt

feature/tasks/
├── TasksViewModel.kt
└── TasksScreen.kt

feature/settings/
├── SettingsViewModel.kt
└── SettingsScreen.kt

app/
└── receiver/ReminderReceiver.kt  # BroadcastReceiver for AlarmManager alarms
```

### Pattern 1: MVI Voice State Machine

**What:** VoiceViewModel holds a `StateFlow<VoiceState>` and a `Channel<VoiceUiEffect>` for one-shot events. All user interactions enter as `VoiceEvent` sealed class instances. State transitions are deterministic.

**When to use:** Any screen with a strictly ordered interaction sequence and multiple terminal/error states.

**VoiceState sealed class:**
```kotlin
// Source: MVI Android pattern, verified against community best practice 2024-2026
sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data object Transcribing : VoiceState
    data class Processing(val transcript: String) : VoiceState
    data class Speaking(val reply: String) : VoiceState
    data class Error(val error: AppError) : VoiceState
}

sealed interface VoiceEvent {
    data object MicTapped : VoiceEvent
    data object TtsDone : VoiceEvent
    data class TranscriptReceived(val text: String) : VoiceEvent
    data class RecognitionError(val code: Int) : VoiceEvent
    data class ReplyReady(val text: String) : VoiceEvent
    data class HaError(val error: AppError.HomeAssistant) : VoiceEvent
}

sealed interface VoiceUiEffect {
    data class ShowToast(val message: String) : VoiceUiEffect
    data object Vibrate : VoiceUiEffect
}
```

**ViewModel core pattern:**
```kotlin
// Source: Android Architecture Guidelines + MVI community pattern
@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val speechManager: SpeechRecognizerManager,
    private val ttsManager: TextToSpeechManager,
    private val commandParser: CommandParser,
    private val haRepository: HomeAssistantRepository,
    private val messageRepository: MessageRepository,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _effects = Channel<VoiceUiEffect>(Channel.BUFFERED)
    val effects: Flow<VoiceUiEffect> = _effects.receiveAsFlow()

    fun onEvent(event: VoiceEvent) {
        viewModelScope.launch {
            when (val current = _state.value) {
                is VoiceState.Idle -> handleIdleEvent(event)
                is VoiceState.Listening -> handleListeningEvent(event)
                is VoiceState.Processing -> handleProcessingEvent(event)
                is VoiceState.Speaking -> handleSpeakingEvent(event)
                else -> Unit
            }
        }
    }
}
```

### Pattern 2: SpeechRecognizer Main-Thread Creation

**What:** SpeechRecognizer is created via `Handler(Looper.getMainLooper()).post{}` or by suspending to `Dispatchers.Main` inside the manager. The ViewModel never touches the recognizer directly — it calls `SpeechRecognizerManager.startListening()` which dispatches internally.

**Critical constraint:** SpeechRecognizer creation MUST be on main thread (hard Android SDK requirement). Callbacks fire on main thread automatically.

```kotlin
// Source: developer.android.com/reference/android/speech/SpeechRecognizer
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) {
    private var recognizer: SpeechRecognizer? = null

    // Called from ViewModel; suspends to main thread for recognizer creation
    suspend fun initialize(listener: RecognitionListener) {
        withContext(mainDispatcher) {
            check(Looper.myLooper() == Looper.getMainLooper()) {
                "SpeechRecognizer MUST be created on the main thread"
            }
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }
        }
    }

    suspend fun startListening(intent: Intent) = withContext(mainDispatcher) {
        recognizer?.startListening(intent)
    }

    suspend fun stopListening() = withContext(mainDispatcher) {
        recognizer?.stopListening()
    }

    suspend fun destroy() = withContext(mainDispatcher) {
        recognizer?.destroy()
        recognizer = null
    }
}
```

**RecognitionListener error codes to handle (ALL must cause state transition):**
- `SpeechRecognizer.ERROR_NETWORK` (2) — offline; surface "offline" message
- `SpeechRecognizer.ERROR_SPEECH_TIMEOUT` (6) — no speech detected; return Idle
- `SpeechRecognizer.ERROR_RECOGNIZER_BUSY` (7) — auto-retry once after 500ms
- `SpeechRecognizer.ERROR_NO_MATCH` (8) — transcription failed; return Idle with error toast
- All other codes — return to Idle with generic error

**Timeout guard:**
```kotlin
// Wrap recognition call with timeout to prevent stuck-in-LISTENING state
withTimeout(8_000L) {
    speechManager.startListening(recognizerIntent)
}
```

### Pattern 3: TextToSpeech Lifecycle

**What:** TTS must be initialized before any `speak()` call. Gate all speech behind an initialized StateFlow flag. Use `UtteranceProgressListener` to observe `onDone`/`onError` and drive the VoiceState back to Idle.

```kotlin
// Source: developer.android.com/reference/kotlin/android/speech/tts/TextToSpeech
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var tts: TextToSpeech? = null
    private var initialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Must be called before any speak()
    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            initialized = (status == TextToSpeech.SUCCESS)
            if (initialized) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { _isSpeaking.value = true }
                    override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
                    override fun onError(utteranceId: String?) { _isSpeaking.value = false }
                })
                onReady()
            }
        }
    }

    // QUEUE_FLUSH: interrupt any current speech immediately
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!initialized) return
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }

    fun getAvailableVoices(): Set<Voice>? = tts?.voices

    fun setVoice(voiceName: String) {
        tts?.voices?.firstOrNull { it.name == voiceName }?.let { tts?.voice = it }
    }
}
```

### Pattern 4: Audio Focus Management

**What:** Request `AUDIOFOCUS_GAIN_TRANSIENT` with `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH` before every TTS `speak()`. Abandon on completion. Pause TTS on `AUDIOFOCUS_LOSS_TRANSIENT`.

```kotlin
// Source: developer.android.com/media/optimize/audio-focus
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    fun requestFocusForTts(): Boolean {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attrs)
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> ttsManager?.stop()
                    AudioManager.AUDIOFOCUS_LOSS -> ttsManager?.stop()
                }
            }
            .build()

        focusRequest = request
        return audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}
```

**Rule:** Never call `tts.speak()` if `requestFocusForTts()` returns false. Always call `abandonFocus()` in the `onDone`/`onError` utterance callbacks and in the interrupt-stop path.

### Pattern 5: Android Keystore AES-GCM Token Encryption

**What:** Generate a 256-bit AES key in the AndroidKeyStore. Encrypt the HA token bytes to ciphertext + IV. Store both as separate ByteArray values in DataStore (proto or bytes). Decrypt on read.

**No new library needed** — platform `javax.crypto` + `android.security.keystore` APIs.

```kotlin
// Source: developer.android.com/privacy-and-security/keystore
class HaTokenCrypto {
    private val keyAlias = "ha_token_key_v1"

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(keyAlias, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
            }.generateKey()
    }

    fun encrypt(plaintext: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Pair(ciphertext, cipher.iv)  // ciphertext, iv
    }

    fun decrypt(ciphertext: ByteArray, iv: ByteArray): String {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val key = ks.getKey(keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }
}
```

**DataStore storage:** Store ciphertext and IV as separate `Preferences.Key<ByteArray>` entries (or use Proto DataStore with a bytes field). Never store the raw token string.

**Important:** All cryptographic operations must run on `Dispatchers.IO`, not main thread.

### Pattern 6: Home Assistant REST API

**Endpoint format (verified against developers.home-assistant.io/docs/api/rest/):**

```
POST http(s)://{ha_host}:{port}/api/services/{domain}/{service}
Authorization: Bearer {long_lived_access_token}
Content-Type: application/json
```

**Common domain/service combinations:**
| Action | Domain | Service | Key body params |
|--------|--------|---------|----------------|
| Turn on light | `light` | `turn_on` | `entity_id`, optional: `brightness` (0-255), `color_temp` |
| Turn off light | `light` | `turn_off` | `entity_id` |
| Turn on switch | `switch` | `turn_on` | `entity_id` |
| Turn off switch | `switch` | `turn_off` | `entity_id` |
| Run script | `script` | `turn_on` | `entity_id` |
| Activate scene | `scene` | `turn_on` | `entity_id` |
| Set climate temp | `climate` | `set_temperature` | `entity_id`, `temperature` |

**Response:** HTTP 200 returns a JSON array of changed state objects. HTTP 401 = bad token. HTTP 404 = service not found.

**Retrofit 3.0 setup:**
```kotlin
// Retrofit 3.0 + kotlinx-serialization converter
val retrofit = Retrofit.Builder()
    .baseUrl(haBaseUrl)  // from DataStore — NOT hardcoded
    .client(okHttpClient)  // with HaAuthInterceptor
    .addConverterFactory(
        Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
    )
    .build()

interface HomeAssistantService {
    @POST("api/services/{domain}/{service}")
    suspend fun callService(
        @Path("domain") domain: String,
        @Path("service") service: String,
        @Body request: HaServiceRequest,
    ): Response<List<HaStateResponse>>
}

@Serializable
data class HaServiceRequest(
    @SerialName("entity_id") val entityId: String,
    val brightness: Int? = null,
    val temperature: Float? = null,
)
```

**Auth interceptor:**
```kotlin
class HaAuthInterceptor(private val tokenProvider: suspend () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider() } ?: return chain.proceed(chain.request())
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

**Error handling:** Map `Response.code()` 401 → `AppError.HomeAssistant("Authentication failed")`, 404 → `AppError.HomeAssistant("Service not found: $domain/$service")`, IOException → `AppError.Network(cause)`.

### Pattern 7: Deterministic Command Parser

**What:** Pure Kotlin regex-based parser. Input is lowercase-normalized transcript. Returns a `ParsedIntent` sealed class. No network calls, no coroutines — pure synchronous.

```kotlin
// Source: CONTEXT.md intent categories + Kotlin Regex stdlib
sealed interface ParsedIntent {
    data class HomeControl(
        val entityAlias: String,
        val action: HomeAction,
        val params: Map<String, Any> = emptyMap(),
    ) : ParsedIntent

    data class Routine(val routineAlias: String) : ParsedIntent

    data class CreateReminder(
        val description: String,
        val triggerTimeMs: Long,
    ) : ParsedIntent

    data class LocalQuery(val queryType: QueryType) : ParsedIntent
    data object Unknown : ParsedIntent
}

enum class HomeAction { TURN_ON, TURN_OFF, SET_BRIGHTNESS, SET_TEMPERATURE }
enum class QueryType { CURRENT_TIME, LIST_REMINDERS }
```

**Regex patterns (Phase 2 must cover at minimum):**
```kotlin
private val TURN_ON = Regex(
    """(?:turn|switch|put)\s+(?:on\s+)?(?:the\s+)?(.+?)(?:\s+on)?$""",
    RegexOption.IGNORE_CASE
)
private val TURN_OFF = Regex(
    """(?:turn|switch|shut)\s+off\s+(?:the\s+)?(.+)$""",
    RegexOption.IGNORE_CASE
)
private val BRIGHTNESS = Regex(
    """(?:dim|set|change)\s+(?:the\s+)?(.+?)\s+(?:to|at)\s+(\d+)\s*(?:percent|%)?""",
    RegexOption.IGNORE_CASE
)
private val ROUTINE = Regex(
    """(?:run|start|execute|activate|trigger)\s+(?:the\s+)?(.+?)\s+(?:routine|scene|automation)""",
    RegexOption.IGNORE_CASE
)
private val REMINDER = Regex(
    """remind\s+me\s+(?:to\s+)?(.+?)\s+at\s+(.+)$""",
    RegexOption.IGNORE_CASE
)
private val TIME_QUERY = Regex("""what(?:'s|\s+is)\s+(?:the\s+)?(?:current\s+)?time""", RegexOption.IGNORE_CASE)
private val REMINDERS_QUERY = Regex("""what\s+reminders\s+do\s+I\s+have|list\s+(?:my\s+)?reminders""", RegexOption.IGNORE_CASE)
```

**Alias resolution flow:** Parser extracts entity alias (spoken name) → `AliasDao.findByAlias(alias)` → HA entity ID. If no alias found, return `ParsedIntent.Unknown`.

### Pattern 8: Room Schema Design

**Four tables for Phase 2:**

```kotlin
// Source: Room documentation patterns + CONTEXT.md schema requirements

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,               // "user" | "assistant"
    val content: String,
    val sourceType: String,         // "deterministic" | "unknown"
    val timestampMs: Long,
    @ColumnInfo(index = true) val sessionId: String,  // for future multi-turn windowing
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,            // human-readable command text
    val domain: String,             // "light", "switch", "script", "scene"
    val service: String,            // "turn_on", "turn_off", etc.
    val entityId: String,
    val status: String,             // "success" | "failure"
    val errorReason: String?,
    @ColumnInfo(index = true) val timestampMs: Long,
)

@Entity(tableName = "aliases", indices = [Index(value = ["alias"], unique = true)])
data class AliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,              // spoken name, e.g. "living room lights"
    val entityId: String,           // HA entity ID, e.g. "light.living_room"
    val domain: String,             // "light" | "switch" | "script" | "scene" | "climate"
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val triggerTimeMs: Long,
    @ColumnInfo(index = true) val status: String,  // "PENDING" | "FIRED" | "CANCELLED"
    val createdAtMs: Long,
)
```

**Index on `timestampMs` in audit_log and `status` in reminders** — these will be the common query predicates.

### Pattern 9: AlarmManager Reminder Scheduling (TASK-01/02/04)

**Decision: Use AlarmManager `setExactAndAllowWhileIdle()`** (not WorkManager). Reasoning:
- Reminders are user-visible, time-critical events
- WorkManager has no exact-time guarantee and is killed by OEM battery optimizations
- `setExactAndAllowWhileIdle()` fires reliably through Doze mode

```kotlin
// Source: developer.android.com/develop/background-work/services/alarms
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminderId: Long, triggerTimeMs: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Graceful fallback: inexact alarm — fires within ~10 min window
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
    }

    fun cancel(reminderId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
    }
}
```

**Manifest permissions required:**
```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<receiver android:name=".receiver.ReminderReceiver"
          android:exported="false" />
<receiver android:name=".receiver.BootReceiver"
          android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

**BootReceiver** reschedules all `PENDING` reminders from Room on device restart (AlarmManager alarms are lost on reboot).

### Pattern 10: Foreground Service Declaration

**VoiceRecognitionService manifest declaration:**
```xml
<!-- In AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />

<service
    android:name=".audio.VoiceRecognitionService"
    android:foregroundServiceType="microphone"
    android:exported="false" />
```

**Notification channel** must be created before calling `startForeground()` (required API 26+):
```kotlin
// Must be called before startForeground() — do in Application.onCreate()
val channel = NotificationChannel(
    "voice_session",
    "Voice Session",
    NotificationManager.IMPORTANCE_LOW
)
notificationManager.createNotificationChannel(channel)
```

**startForeground() call (API 29+ requires serviceType parameter):**
```kotlin
// Inside VoiceRecognitionService.onStartCommand()
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
} else {
    startForeground(NOTIFICATION_ID, notification)
}
```

### Anti-Patterns to Avoid

- **Creating SpeechRecognizer off main thread:** Callbacks never fire; state machine stuck in LISTENING forever.
- **Calling tts.speak() before onInit():** Silently does nothing; user hears no confirmation.
- **Calling tts.speak() without audio focus:** Silent failure on Android 15+; phone call interruption on older versions.
- **Calling tts.speak() without QUEUE_FLUSH for new replies:** Previous utterances stack up and play in sequence.
- **Storing HA token in plain DataStore or SharedPreferences:** Token extractable via ADB; security violation.
- **Using EncryptedSharedPreferences:** Deprecated July 2025; will break on future Android.
- **Hardcoding HA base URL:** Breaks outside home network; must come from DataStore.
- **Using WorkManager for exact reminders:** OEM Doze kills it; use AlarmManager.
- **Not calling recognizer.destroy() in ViewModel.onCleared():** Memory leak; recognizer keeps process alive.
- **Not calling tts.shutdown() on app exit:** Memory leak; TTS engine keeps process alive.
- **Not implementing all RecognitionListener.onError() codes:** State machine gets stuck in LISTENING on any error not handled.
- **Room queries on main thread:** ANR; always use suspend functions or Flow.
- **Network calls from UI composables:** Violates architecture rule; all network goes through ViewModel → use case → repository.

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JSON serialization for HA request/response | Custom JSON string builder | `kotlinx-serialization` + `@Serializable` | Edge cases in escaping, null handling, type coercion |
| HTTP client with auth headers | Raw HttpURLConnection | OkHttp + Retrofit + `HaAuthInterceptor` | Retry logic, connection pooling, timeout management |
| Coroutine cancellation/cleanup | Manual job tracking | `viewModelScope` + structured concurrency | Cancellation propagation on ViewModel clear |
| Database type converters for enums | String if/else chains | `@TypeConverter` annotations in Room | Compile-time safety, schema migrations |
| Notification channel registration | Inline in Service | `Application.onCreate()` call | Channel must exist before first notification fires |
| Time parsing from natural language | Custom time parser | `java.time` (LocalTime, LocalDate) + regex for "8 PM", "tomorrow at 9" | DST transitions, 12/24h format edge cases |
| Thread dispatch for SpeechRecognizer | Custom thread management | `withContext(Dispatchers.Main)` | Platform guarantees correct Looper association |

**Key insight:** The Android voice pipeline has a large number of subtle threading, lifecycle, and state-ordering requirements. The platform APIs are correct when used in the prescribed order — do not invent alternatives to avoid the prescribed order.

---

## Common Pitfalls

### Pitfall 1: SpeechRecognizer Silent Failure Off Main Thread
**What goes wrong:** `createSpeechRecognizer()` called in `viewModelScope.launch` (runs on Default dispatcher). Recognizer is created but callbacks never fire. App appears frozen in Listening state.
**Why it happens:** Developers apply `Dispatchers.IO` uniformly for "non-UI" work. SpeechRecognizer has a hidden main-thread Looper dependency.
**How to avoid:** Always use `withContext(Dispatchers.Main)` or `Handler(Looper.getMainLooper()).post{}` for recognizer creation. Add `check(Looper.myLooper() == Looper.getMainLooper())` assertion in `SpeechRecognizerManager.initialize()`.
**Warning signs:** `onReadyForSpeech()` and `onResults()` never fire; logcat shows Looper warnings.

### Pitfall 2: Missing foregroundServiceType Causes SecurityException on Android 14+
**What goes wrong:** Service starts and works on API 33 emulator. Crashes with `SecurityException` on Android 14+ physical device.
**Why it happens:** API 34 made foreground service type declaration mandatory. Without `android:foregroundServiceType="microphone"`, the system rejects the service start.
**How to avoid:** Declare both `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MICROPHONE` permissions and `android:foregroundServiceType="microphone"` on the service. Test on a API 34+ emulator/device.
**Warning signs:** Works in development (API 33 or lower), crashes in QA on API 34 device.

### Pitfall 3: TTS speak() Before onInit() Callback
**What goes wrong:** `TextToSpeech` constructor is called and `speak()` is immediately called on the returned object. TTS is not yet initialized; `speak()` returns an error code and nothing is spoken. User hears silence.
**Why it happens:** TTS initialization is asynchronous. The constructor returns immediately but the engine is not ready.
**How to avoid:** Gate all `speak()` calls behind an `initialized` flag that is set to `true` only inside the `onInit(status)` callback when `status == TextToSpeech.SUCCESS`.
**Warning signs:** First TTS call after app start always produces silence; works after a few seconds of waiting.

### Pitfall 4: Voice State Machine Stuck — No Timeout
**What goes wrong:** Network drops mid-recognition. `onError()` is never called (device-specific behavior). State machine stays in `LISTENING`. Subsequent mic taps are ignored.
**Why it happens:** Not all recognition failures fire `onError()` — some simply produce no result within the timeout window.
**How to avoid:** Wrap `startListening()` in `withTimeout(8_000L)`. On `TimeoutCancellationException`, transition to `Error` state and then `Idle`.
**Warning signs:** Mic icon stuck after turning on airplane mode; must force-quit app to recover.

### Pitfall 5: HA Token Visible via ADB Shell
**What goes wrong:** Token stored in plain DataStore preferences (as a String). Accessible via `adb shell run-as com.example.aicompanion cat files/datastore/app_preferences.preferences_pb`.
**Why it happens:** DataStore is not encrypted by default — it is only a file-based replacement for SharedPreferences.
**How to avoid:** Always encrypt the token bytes with Android Keystore AES-GCM before writing to DataStore. Never store the raw string.
**Warning signs:** Token value visible in DataStore file contents; token logged at any log level.

### Pitfall 6: Audio Focus Not Requested Before speak()
**What goes wrong:** TTS speaks over phone calls. On Android 15+, TTS silently produces no audio when app is not holding audio focus.
**Why it happens:** `tts.speak()` does not automatically request audio focus.
**How to avoid:** Always call `audioFocusManager.requestFocusForTts()` and verify return is `AUDIOFOCUS_REQUEST_GRANTED` before `speak()`. Always call `abandonFocus()` on TTS done/error.
**Warning signs:** TTS silently skips utterances on Android 15; phone calls not interrupted/ducked correctly.

### Pitfall 7: AlarmManager Alarms Lost After Device Reboot
**What goes wrong:** User creates a reminder scheduled for tomorrow morning. Device reboots tonight (OTA update). Reminder never fires.
**Why it happens:** AlarmManager alarms are not persisted across device reboots by the OS.
**How to avoid:** Implement a `BootReceiver` that queries Room for all `PENDING` reminders and reschedules their AlarmManager alarms on `ACTION_BOOT_COMPLETED`.
**Warning signs:** Reminders created before a reboot never fire; post-reboot reminders work fine.

### Pitfall 8: Retrofit baseUrl Must Be Configurable — Never Hardcoded
**What goes wrong:** HA base URL is constructed at Hilt module initialization time from a constant. App breaks when user changes HA URL in settings or uses an external URL.
**Why it happens:** OkHttpClient and Retrofit are singletons; Retrofit's baseUrl is fixed at construction time.
**How to avoid:** Use OkHttp's `@Url` parameter on the Retrofit interface for dynamic URL support, OR rebuild the Retrofit instance when the URL changes, OR use a base URL of `"http://localhost/"` and override with a dynamic BaseUrl interceptor. Simplest for Phase 2: rebuild Retrofit on URL change via a factory pattern.
**Warning signs:** Works during development but fails after user edits HA URL in settings.

---

## Code Examples

### Verified: RecognitionListener Full Implementation

```kotlin
// Source: developer.android.com/reference/android/speech/RecognitionListener
val listener = object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle) {
        viewModel.onEvent(VoiceEvent.ReadyForSpeech)
    }
    override fun onBeginningOfSpeech() {}  // optional — can update UI animation
    override fun onRmsChanged(rmsdB: Float) {}  // optional — for waveform animation
    override fun onBufferReceived(buffer: ByteArray) {}
    override fun onEndOfSpeech() {
        viewModel.onEvent(VoiceEvent.Transcribing)
    }
    override fun onError(error: Int) {
        viewModel.onEvent(VoiceEvent.RecognitionError(error))
    }
    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        viewModel.onEvent(VoiceEvent.TranscriptReceived(text))
    }
    override fun onPartialResults(partialResults: Bundle) {}
    override fun onEvent(eventType: Int, params: Bundle) {}
}
```

### Verified: SpeechRecognizer Intent

```kotlin
// Source: Android SpeechRecognizer docs
val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
}
```

### Verified: HA Service Call — Light Turn On with Brightness

```kotlin
// Source: developers.home-assistant.io/docs/api/rest/
// POST /api/services/light/turn_on
// Body: {"entity_id": "light.living_room", "brightness": 128}
val response = haService.callService(
    domain = "light",
    service = "turn_on",
    request = HaServiceRequest(entityId = "light.living_room", brightness = 128)
)
if (response.isSuccessful) {
    ttsManager.speak("Done, dimming the living room lights to 50%")
} else {
    throw AppError.HomeAssistant("HTTP ${response.code()}")
}
```

### Verified: Room DAO with Flow

```kotlin
// Source: Room documentation
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestampMs ASC")
    fun observeAll(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE timestampMs > :sinceMs ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun getRecentMessages(sinceMs: Long, limit: Int = 20): List<MessageEntity>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE status = 'PENDING' ORDER BY triggerTimeMs ASC")
    fun observePending(): Flow<List<ReminderEntity>>

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
```

### Verified: Hilt @MainDispatcher and @IoDispatcher Qualifiers

```kotlin
// Source: Android Coroutines Best Practices + Hilt DI docs
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher

@Module @InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @MainDispatcher fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    @Provides @IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

### Verified: Compose LazyColumn Auto-Scroll to Bottom

```kotlin
// Source: Jetpack Compose LazyList docs
val listState = rememberLazyListState()
val messages by viewModel.messages.collectAsStateWithLifecycle()

LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
        listState.animateScrollToItem(messages.size - 1)
    }
}

LazyColumn(state = listState) {
    items(messages, key = { it.id }) { message ->
        MessageBubble(message = message)
    }
}
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `EncryptedSharedPreferences` | Android Keystore directly + DataStore | July 2025 (deprecated) | Must not use; implement Keystore AES-GCM directly |
| `requestAudioFocus(listener, stream, focus)` | `AudioFocusRequest.Builder` + `AudioManager.requestAudioFocus(request)` | API 26 (2017) | Old API deprecated; use Builder pattern |
| `kapt` for Room/Hilt annotation processing | `ksp` | Kotlin 1.9+ stable | Already established in Phase 1; KSP only |
| Retrofit `GsonConverterFactory` | `kotlinx-serialization` converter factory | Retrofit 3.0 | Kotlin-native, no reflection, works with `@Serializable` |
| `AlarmManager.setExact()` (blocked by Doze) | `setExactAndAllowWhileIdle()` | API 23 | Alarms fire even in Doze; mandatory for reminders |
| `SharedPreferences` for settings | DataStore Preferences | Stable API 2022+ | Coroutine-native, no ANR risk |

**Deprecated/outdated — confirmed must-avoid:**
- `security-crypto` (`EncryptedSharedPreferences`): deprecated July 2025, do not use
- `kapt` annotation processor: replaced by KSP in this project
- `AudioManager.requestAudioFocus(listener, streamType, durationHint)`: deprecated API 26

---

## Open Questions

1. **Dynamic Retrofit baseUrl for HA URL changes**
   - What we know: Retrofit baseUrl is fixed at construction time; Phase 2 must support user-editable URL
   - What's unclear: Whether to use a `@Url` parameter on each endpoint (requires passing URL everywhere), or a dynamic interceptor that overrides the host, or rebuilding the Retrofit instance when URL changes
   - Recommendation: Use a `BaseUrlInterceptor` that reads the current URL from a cached in-memory value updated when DataStore emits a new URL. This keeps the Retrofit singleton and avoids passing URL to every call.

2. **Time parsing for reminder creation (TASK-01)**
   - What we know: Parser must extract "8 PM", "tomorrow at 9", "in 30 minutes" from natural language
   - What's unclear: How robust to make the time parser in Phase 2 vs. adding more patterns in Phase 3
   - Recommendation: Support three Phase 2 patterns only: `HH:MM AM/PM`, `h AM/PM` (e.g., "8 PM"), and "in X minutes/hours". Anything else returns ParsedIntent.Unknown with TTS "I couldn't understand the time."

3. **CONV-04 multi-turn context (session window)**
   - What we know: "turn it up" after "dim the living room to 50%" requires remembering the last mentioned entity
   - What's unclear: Whether to persist session context to Room or keep in-memory only
   - Recommendation: Keep a `recentMessages: List<MessageEntity>` in-memory in VoiceViewModel (last 5 turns). Not persisted to Room separately — reconstructed from Room on ViewModel init. Parser receives this context list.

4. **DataStore with ByteArray for encrypted token**
   - What we know: DataStore Preferences supports `byteArrayPreferencesKey` but is unmarked as experimental in some versions
   - What's unclear: Whether `Preferences.preferencesKey<ByteArray>` is stable in DataStore 1.2.1
   - Recommendation: Store ciphertext and IV as Base64-encoded Strings in DataStore Preferences (two separate keys). This avoids any ByteArray type concerns and is portable.

---

## Sources

### Primary (HIGH confidence)
- `developer.android.com/reference/android/speech/SpeechRecognizer` — main-thread requirement, RecognitionListener interface, error codes
- `developer.android.com/media/optimize/audio-focus` — AudioFocusRequest.Builder, USAGE_ASSISTANT, CONTENT_TYPE_SPEECH, abandonment
- `developer.android.com/reference/kotlin/android/speech/tts/TextToSpeech` — speak(), QUEUE_FLUSH, UtteranceProgressListener, getVoices()
- `developer.android.com/develop/background-work/services/fgs/service-types` — foregroundServiceType="microphone", FOREGROUND_SERVICE_MICROPHONE permission, API 34 requirements
- `developer.android.com/develop/background-work/services/alarms` — setExactAndAllowWhileIdle(), setAlarmClock(), canScheduleExactAlarms(), SCHEDULE_EXACT_ALARM
- `developer.android.com/privacy-and-security/keystore` — AES-GCM key generation, Cipher "AES/GCM/NoPadding", IV handling
- `developers.home-assistant.io/docs/api/rest/` — POST /api/services/{domain}/{service}, Authorization header, response format
- `developer.android.com/kotlin/coroutines/coroutines-best-practices` — Inject Dispatchers pattern, @MainDispatcher qualifier

### Secondary (MEDIUM confidence)
- `square.github.io/retrofit/configuration/` — Retrofit 3.0 kotlinx-serialization converter setup
- WebSearch "Retrofit 3.0 kotlinx-serialization converter" — confirmed `com.squareup.retrofit2:converter-kotlinx-serialization` artifact group stays `retrofit2` even for v3
- WebSearch "Home Assistant REST API light switch script scene" — confirmed domain/service combinations for common entity types
- WebSearch "Android MVI StateFlow Channel UiEffect 2024-2026" — confirmed Channel for one-shot effects, StateFlow for persistent state
- `github.com/airbnb/lottie-android` — Lottie 6.7.1 confirmed in libs.versions.toml (not used in Phase 2; Phase 5)

### Tertiary (LOW confidence — verify before use)
- Time parsing approach for TASK-01 reminder creation — based on common patterns; validate against actual user voice input during QA
- `byteArrayPreferencesKey` stability in DataStore 1.2.1 — recommended fallback is Base64-encoded String (HIGH confidence workaround)

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — all libraries already declared in libs.versions.toml; versions verified in Phase 1 SUMMARY.md
- SpeechRecognizer threading: HIGH — directly sourced from official Android developer docs
- TTS lifecycle: HIGH — directly sourced from official Android TextToSpeech API reference
- Audio focus: HIGH — directly sourced from official Android audio focus guide
- Android Keystore AES-GCM: HIGH — directly sourced from official keystore docs
- HA REST API: HIGH — verified against official Home Assistant developer docs
- AlarmManager exact alarms: HIGH — directly sourced from official Android alarms guide
- Foreground service type: HIGH — directly sourced from official Android foreground service types doc
- Room schema design: MEDIUM — standard Room patterns applied to project-specific schema
- Deterministic parser regex: MEDIUM — standard Kotlin regex patterns; exact coverage depends on user voice input patterns discovered in QA
- Multi-turn context approach: MEDIUM — in-memory session pattern is community-established; no single canonical source

**Research date:** 2026-03-18
**Valid until:** 2026-04-18 (30 days — all referenced APIs are stable; Android SDK and HA REST API do not change breaking patterns at this cadence)
