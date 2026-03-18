# Stack Research

**Domain:** Android Kotlin voice-first AI companion app (modular, hybrid AI, Home Assistant, expressive avatar)
**Researched:** 2026-03-18
**Confidence:** HIGH (core Android stack via official docs) / MEDIUM (AI/local inference layer) / LOW (cloud TTS/STT exact versions)

---

## Platform Baseline

| Component | Value | Source |
|-----------|-------|--------|
| Language | Kotlin 2.3.20 | Official kotlinlang.org (March 16 2026) |
| Min SDK | API 26 (Android 8.0) | Recommended floor — covers 95%+ of active devices, required for modern audio APIs |
| Target SDK | API 35 (Android 15, stable) | Android 15 = current stable, API 36 = Android 16 in beta |
| AGP | 9.1.0 | Official release notes (March 2026) |
| Gradle | 9.3.1 | Required by AGP 9.1.0 |
| JDK | 17 | Required by AGP 9.1.0 |
| Build system | Gradle with Version Catalogs (libs.versions.toml) | Standard for multi-module projects; avoids version duplication |

---

## Recommended Stack

### Core Framework

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Kotlin | 2.3.20 | Language | Official stable as of March 16 2026. Coroutines, Flow, and value classes are first-class; no Java interop debt for greenfield Android |
| Jetpack Compose BOM | 2026.03.00 | UI framework | BOM maps to Compose UI 1.10.5 + Material3 1.4.0. Single BOM version pins all Compose deps consistently. Compose is the only supported path for new Android UIs |
| Material3 | 1.4.0 (via BOM) | Design system | Included in BOM; provides adaptive components, dynamic color, and theming primitives needed for avatar + companion UI |
| Compose Compiler | Bundled with Kotlin 2.x via plugin | Compose compilation | Since Kotlin 2.0 the Compose compiler ships in the Kotlin repository — configure via the Compose Compiler Gradle plugin, NOT `kotlinCompilerExtensionVersion`. This eliminates the separate version coupling |
| Activity Compose | 1.13.0 | Entry point / edge-to-edge | Latest stable (March 2026); provides `ComponentActivity`, edge-to-edge support, and PiP hooks |
| Navigation Compose | 2.9.7 | In-app navigation | Latest stable (Jan 2026); includes type-safe navigation via Kotlin Serialization and predictive back gesture support |

### Dependency Injection

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Hilt | 2.57.1 | Dependency injection | Official stable (verified via hilt-android training docs). Standard DI for Android with first-class Compose, ViewModel, and WorkManager integration. Generates code at compile time via KSP; no runtime reflection overhead |
| KSP | TO_BE_VERIFIED (must match Kotlin 2.3.x) | Annotation processing for Hilt + Room | KSP 2.x is required for Kotlin 2.x. Exact version must be verified against the KSP/Kotlin compatibility matrix at github.com/google/ksp before build setup |
| androidx.hilt:hilt-navigation-compose | 1.3.0 | `hiltViewModel()` in Compose | Decoupled from Navigation artifact in 1.3.0; always use this companion artifact with Hilt + Compose |

### Architecture / Lifecycle

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Lifecycle ViewModel KTX | 2.10.0 | ViewModel + coroutine scope | Latest stable (Nov 2025); `viewModelScope` for coroutines, new scoped lifecycles API for Compose |
| Lifecycle Runtime Compose | 2.10.0 | `collectAsStateWithLifecycle()` | Required to safely consume StateFlow in Compose respecting the Activity lifecycle; prevents background collection crashes |
| Kotlin Coroutines | TO_BE_VERIFIED | Async, Flow, structured concurrency | kotlinx.coroutines 1.x is the async primitive. Exact version must be verified at github.com/Kotlin/kotlinx.coroutines/releases for Kotlin 2.3.x compatibility |
| kotlinx-coroutines-android | TO_BE_VERIFIED | Main thread dispatcher | Companion to coroutines core for Android main dispatcher |

### Persistence

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Room | 2.8.4 | Structured local DB (conversations, memory, tasks, reminders) | Latest stable (Nov 2025); KSP-based code gen, coroutines + Flow support, prepared statement cache added in 2.8.x. Required for conversation history, memory facts, and aliases |
| DataStore Preferences | 1.2.1 | Key-value settings (user prefs, privacy mode, alias maps) | Latest stable (March 2026); coroutine-native, replaces SharedPreferences; multiprocess support added in 1.2.0. Use for non-relational config |

### Background Work

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| WorkManager KTX | 2.11.1 | Background briefings, scheduled automations | Latest stable (Jan 2026); the only battery-safe, OS-restart-safe background scheduler on Android. Android 15 network constraint fixes included. Required for morning briefings and recurring automations |

### Networking

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| OkHttp | TO_BE_VERIFIED | HTTP client + WebSocket transport | Square's OkHttp is the industry-standard HTTP client for Android. Its built-in WebSocket client is the correct primitive for the Home Assistant WebSocket API. Do NOT use Retrofit for WebSocket — OkHttp WebSocket is the right layer. Verify latest stable at github.com/square/okhttp/releases |
| Retrofit | TO_BE_VERIFIED | REST API client (HA REST API, cloud AI) | Type-safe HTTP layer over OkHttp for REST calls to Home Assistant REST API and cloud AI APIs. Use with kotlinx-serialization-json converter. Verify at github.com/square/retrofit/releases |
| kotlinx-serialization-json | TO_BE_VERIFIED | JSON serialization | Kotlin-native serialization; no reflection overhead vs Gson. Use the `retrofit2-kotlinx-serialization-converter` adapter. Verify at github.com/Kotlin/kotlinx.serialization/releases |

### Audio

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Android SpeechRecognizer | Platform API (no dep) | STT — button-to-talk speech recognition | Built-in platform API. `RecognizerIntent.EXTRA_PREFER_OFFLINE` enables on-device-first recognition. No dependency cost. Platform-provided with Google Play Services updates. Use as the primary STT path |
| Android TextToSpeech | Platform API (no dep) | TTS — utility confirmations ("Lights turned off") | Built-in; zero latency for short confirmations. Required offline capability. Use for all deterministic tool-result confirmations |
| AudioRecord | Platform API (no dep) | Low-level PCM capture for future streaming STT | Standard `android.media.AudioRecord` for 16kHz/16-bit mono capture. Required if SpeechRecognizer is replaced with a streaming cloud STT API (e.g., OpenAI Whisper streaming) |
| Media3 ExoPlayer | 1.9.2 | Playback of cloud TTS audio streams | Latest stable (Feb 2026). Use for streaming MP3/Opus from cloud TTS (e.g., OpenAI TTS API). Far more robust than MediaPlayer for network audio buffering and gapless playback |

### AI — Local (On-Device)

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| ML Kit GenAI Prompt API | TO_BE_VERIFIED | On-device Gemini Nano inference (local AI router tier) | **Preferred path** as of 2026. Accesses Gemini Nano via Android AICore (system service). No model bundling required — model is system-managed and auto-updated. The Google AI Edge SDK is **deprecated**; ML Kit Prompt API is its successor. Device requirement: Pixel 9 series (initially); broader rollout ongoing. Verify artifact at developers.google.com/ml-kit/genai/prompt/android |
| Deterministic NLU (custom) | N/A | Regex/rule-based parser for common commands | No library needed. A hand-crafted intent parser for "turn off lights", "set timer", "what's the weather" commands runs in < 10ms and has zero failure modes. This is tier 0 in the routing stack — never skip it |

### AI — Cloud (Fallback)

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| OpenAI Android/HTTP client (via OkHttp + Retrofit) | N/A (use REST directly) | Cloud reasoning fallback via GPT-4o / o-series | No official Android SDK with guaranteed stability. Use OkHttp + Retrofit + kotlinx-serialization directly against the OpenAI chat completions API. This gives full control over streaming (SSE via OkHttp `EventSource`), privacy mode gating, and retry logic |
| Google Gemini API (via HTTP) | TO_BE_VERIFIED | Alternative cloud reasoning | If Gemini cloud is preferred, the `com.google.ai.client.generativeai` Android artifact exists. Verify current version at github.com/google/generative-ai-android. Can be used as primary or secondary cloud tier |

### Home Assistant Integration

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| OkHttp WebSocket | (bundled with OkHttp) | HA WebSocket API for real-time state, service calls | HA WebSocket API is the authoritative channel: authenticate with long-lived access token (`auth` message), then `call_service` for device control and `subscribe_events` for state change push. REST API is simpler but polling-only |
| Retrofit + OkHttp | (same client) | HA REST API fallback + configuration | REST API (`/api/states`, `/api/services/{domain}/{service}`) is useful for initial entity discovery and one-shot commands. Bearer token auth (`Authorization: Bearer <token>`) |

### Avatar

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Compose Canvas + DrawScope | (via Compose BOM) | 2D expressive avatar rendering | The `Canvas` composable with `DrawScope` is the correct layer for custom 2D avatar drawing. Use `drawPath`, `drawCircle`, `drawArc`, and `Brush` gradients for face/body shapes. Use `Modifier.drawWithCache` for performance |
| Compose `animate*AsState` / `Transition` | (via Compose BOM) | Avatar state animation (idle → listening → thinking → speaking) | `updateTransition()` manages multi-property animations from a single state enum. Each voice state (Idle, Listening, Thinking, Speaking, Error) maps to a `Transition` target state. Drives scale, alpha, color, and glow simultaneously |
| Lottie Compose | TO_BE_VERIFIED | Optional: richer avatar micro-animations via JSON | If the avatar needs pre-authored animation sequences (e.g., wink, nod) beyond programmatic Compose animation, Lottie is the standard choice. Verify latest compose-compatible version at github.com/airbnb/lottie-android/releases |

### Security

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Android Keystore (platform) | Platform API | Store cloud API keys, HA long-lived access token | `security-crypto` 1.1.0 is deprecated. Use Android Keystore directly: generate an AES key bound to the Keystore, encrypt secrets with `Cipher`, store encrypted blob in DataStore. Keys cannot be exported from the device |
| DataStore (encrypted via Keystore) | 1.2.1 | Persist encrypted secrets | Combine DataStore with Keystore-backed AES-GCM encryption for API key storage. Do NOT use BuildConfig for secrets in production |

### Testing

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| JUnit 4 (via AndroidX Test) | 1.3.0 (junit-ext) | Unit and instrumented test runner | Android's standard test runner. JUnit 5 requires a separate plugin and has gaps in Android instrumented test support — use JUnit 4 unless the team has strong preference |
| Compose UI Test (ui-test-junit4) | via BOM 2026.03.00 | Compose UI testing | Covered by Compose BOM; provides `createComposeRule`, `onNodeWithText`, `performClick` semantics |
| Turbine | TO_BE_VERIFIED | StateFlow / Flow testing | Industry standard for testing Kotlin flows in unit tests. Verify current version at github.com/cashapp/turbine/releases |
| MockK | TO_BE_VERIFIED | Mocking for Kotlin-first tests | Native Kotlin mock library; far superior to Mockito for Kotlin code (handles `data class`, `object`, `suspend fun`). Verify at github.com/mockk/mockk/releases |
| Robolectric | TO_BE_VERIFIED | Fast Android unit tests without emulator | Allows testing Android SDK classes (ViewModel, Room, DataStore) on JVM. Verify current version for API 35 support at robolectric.org |
| Espresso | 3.7.0 | Instrumented UI integration tests | Standard for integration-level UI tests on device/emulator |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Android Studio Panda 2 (2025.3.2) | IDE | Latest stable as of March 2026 |
| App Startup (1.2.0) | Controlled initialization order for 15-module app | Prevents content provider explosion; still actively maintained (March 2026) |
| Detekt | Static analysis + style | Kotlin-native; enforce architecture boundaries (no network calls from UI modules). Version: TO_BE_VERIFIED |
| LeakCanary | Memory leak detection in debug builds | Standard Android leak detection; do not ship in release. Version: TO_BE_VERIFIED |

---

## Build Setup — Gradle Version Catalogs

Use `gradle/libs.versions.toml` for all versions. Do NOT hardcode version strings in module `build.gradle.kts` files. This is the standard for multi-module Android projects with 15+ modules.

```toml
# gradle/libs.versions.toml (representative — verify all TO_BE_VERIFIED entries)
[versions]
kotlin = "2.3.20"
agp = "9.1.0"
composeBom = "2026.03.00"
hilt = "2.57.1"
room = "2.8.4"
datastore = "1.2.1"
workmanager = "2.11.1"
lifecycle = "2.10.0"
navigationCompose = "2.9.7"
activityCompose = "1.13.0"
hiltAndroidx = "1.3.0"
media3 = "1.9.2"
appStartup = "1.2.0"
ksp = "TO_BE_VERIFIED"              # Must match Kotlin 2.3.x
coroutines = "TO_BE_VERIFIED"       # Verify at github.com/Kotlin/kotlinx.coroutines
okhttp = "TO_BE_VERIFIED"           # Verify at github.com/square/okhttp
retrofit = "TO_BE_VERIFIED"         # Verify at github.com/square/retrofit
kotlinxSerialization = "TO_BE_VERIFIED"  # Verify at github.com/Kotlin/kotlinx.serialization
mlKitGenAi = "TO_BE_VERIFIED"       # Verify at developers.google.com/ml-kit/genai
lottie = "TO_BE_VERIFIED"           # Verify at github.com/airbnb/lottie-android
turbine = "TO_BE_VERIFIED"          # Verify at github.com/cashapp/turbine
mockk = "TO_BE_VERIFIED"            # Verify at github.com/mockk/mockk
robolectric = "TO_BE_VERIFIED"      # Verify at robolectric.org
```

---

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| DI | Hilt | Koin | Koin is runtime DI (reflection-based). Hilt generates code at compile time — no runtime injection errors in a 15-module app with complex scope chains |
| DI | Hilt | Kodein | Similar runtime tradeoff to Koin; smaller ecosystem on Android |
| Persistence | Room | SQLDelight | SQLDelight is excellent but Kotlin Multiplatform focused. Room is better integrated with Android lifecycle, KSP, and Flow out of the box for Android-only |
| Persistence | Room | Realm | Realm's encryption and sync are features; its object-graph model creates impedance mismatch with clean architecture domain entities |
| Persistence | DataStore | SharedPreferences | SharedPreferences is synchronous, not coroutine-native, and deprecated for new code |
| Networking | OkHttp + Retrofit | Ktor Client | Ktor is excellent for KMP. For Android-only, OkHttp + Retrofit is more battle-tested, has better interceptor support for auth/logging/retry, and has a larger support ecosystem |
| Async | Coroutines + Flow | RxJava | RxJava has no new capabilities over Coroutines+Flow on Kotlin, adds complexity, and is effectively in maintenance mode for Android |
| UI | Compose | XML + ViewBinding | XML is legacy. All new Android UI work uses Compose — no new Compose features are backported to the View system |
| On-device AI | ML Kit Prompt API | MediaPipe LLM Inference | MediaPipe LLM Inference bundles a model into the APK (100–400MB size penalty). ML Kit Prompt API uses the system-managed Gemini Nano model — no APK size cost. Use MediaPipe only if the target device doesn't support AICore |
| On-device AI | ML Kit Prompt API | Google AI Edge SDK | Deprecated as of 2026. ML Kit Prompt API is its official successor |
| Cloud AI | HTTP/OkHttp direct | openai-kotlin SDK | Third-party SDKs wrap the REST API but add a dependency with its own release cycle. Direct OkHttp calls give complete control over request lifecycle, streaming, cancellation, and privacy gating |
| Background work | WorkManager | Android AlarmManager | AlarmManager requires exact time scheduling (battery-draining) and doesn't survive app restarts reliably. WorkManager handles both |
| Background work | WorkManager | Foreground Service (alone) | Foreground services are for persistent long-running work visible to the user. For scheduled briefings, WorkManager is the correct abstraction |
| Avatar animation | Compose Transition API | Rive | Rive produces excellent expressive animations and has Compose integration, but adds a runtime dependency. Start with native Compose animation; upgrade to Rive/Lottie only if animation complexity demands it |
| TTS | Cloud Neural TTS (companion mode) | Google Cloud TTS only | Tie the cloud TTS to an abstraction so the provider (OpenAI, ElevenLabs, Google) can be swapped. OpenAI TTS `tts-1-hd` is high quality and streams well via Media3 |
| Security | Android Keystore (platform) | androidx.security:security-crypto | security-crypto 1.1.0 is deprecated (all APIs deprecated July 2025). Use Keystore directly |

---

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| `security-crypto` (EncryptedSharedPreferences) | All APIs deprecated in 1.1.0 (July 2025). Google explicitly points to Keystore platform APIs | Android Keystore + DataStore with AES-GCM |
| `kotlinCompilerExtensionVersion` in build.gradle | Deprecated approach. Since Kotlin 2.0, Compose compiler moves with Kotlin | Compose Compiler Gradle plugin |
| Gson | Java-origin, reflection-based, no null safety. Kotlin Serialization is the Kotlin-native replacement | kotlinx-serialization-json |
| Moshi with Kotlin reflect | `moshi-kotlin` uses reflection at runtime. `moshi-kotlin-codegen` with KSP is acceptable, but kotlinx-serialization is simpler for an all-Kotlin project | kotlinx-serialization-json |
| Google AI Edge SDK | Deprecated as of 2026, Pixel-9 only experimental | ML Kit Prompt API (Gemini Nano) |
| MediaPipe LLM Inference (as primary local AI) | Bundles a 100–400MB model into the APK. Correct only as fallback when AICore is unavailable | ML Kit Prompt API (system-managed model, no APK size) |
| `android.speech.RecognitionService` direct binding | Overly complex for this use case; SpeechRecognizer intent wraps it cleanly | `android.speech.SpeechRecognizer` |
| LiveData | Lifecycle-aware but Java-origin, no backpressure, not coroutine-native. StateFlow is the modern replacement | StateFlow + `collectAsStateWithLifecycle()` |
| XML layouts (for new UI) | No new Compose features backport to XML. Creates a two-system maintenance burden | Jetpack Compose exclusively |
| Always-on hotword (microphone hold-open) | Excessive battery drain. Explicitly out-of-scope in PROJECT.md | Button-to-talk (PTT) pattern |
| Thread.sleep() / Handler.postDelayed() for scheduling | Non-cancellable, non-restart-safe | WorkManager for scheduled tasks; `delay()` in coroutines for in-flight waits |

---

## Stack Patterns by Variant

**If the target device supports Android AICore (Pixel 9+ or AICore-capable device):**
- Use ML Kit Prompt API for local AI tier
- No model bundling; system manages Gemini Nano automatically
- Check availability with `FeatureCheckingRemoteModel.downloadConditions` before routing

**If the target device does NOT support AICore:**
- Fall back to MediaPipe LLM Inference with a bundled Gemma 3B model
- APK size impact: ~300MB (use Play Asset Delivery to keep base APK small)
- This is the fallback path in the hybrid AI router, not the primary path

**If cloud AI is unavailable (privacy mode ON or offline):**
- Route exclusively through deterministic NLU → ML Kit Prompt API (or MediaPipe fallback)
- DataStore `privacyModeEnabled` flag gates all outbound requests
- App MUST function for home control, reminders, and local TTS in this mode

**If Home Assistant is on the local network (no external access):**
- Use HA WebSocket API for real-time state and service calls
- Prefer WebSocket over REST for reactive device control (avoids polling)
- Authenticate with long-lived access token stored in Keystore-encrypted DataStore

**If Home Assistant is remote (Nabu Casa or nginx reverse proxy):**
- All same APIs work over HTTPS/WSS
- Add certificate pinning via OkHttp `CertificatePinner` for the known HA endpoint

---

## Version Compatibility Matrix

| Library | Constraint | Notes |
|---------|------------|-------|
| KSP | Must match Kotlin 2.3.x exactly | KSP version `2.3.20-1.0.x` (check github.com/google/ksp compatibility table) |
| Compose BOM 2026.03.00 | Requires Kotlin 2.0+ with Compose Compiler Gradle plugin | No `kotlinCompilerExtensionVersion` needed |
| Hilt 2.57.1 | Requires KSP; use `ksp` not `kapt` | kapt is deprecated for Kotlin 2.x |
| Room 2.8.4 | Requires KSP | Use `ksp("androidx.room:room-compiler:2.8.4")` |
| Navigation Compose 2.9.7 | Requires `kotlinx-serialization` plugin for type-safe routes | Add `kotlin("plugin.serialization")` to app module |
| AGP 9.1.0 | Requires Gradle 9.3.1 and JDK 17 | JDK 11 will fail the build |
| WorkManager 2.11.1 | Android 15 network constraint fix | Ensure this exact version or newer for Android 15 target devices |

---

## Sources

| Source | What Was Verified | Confidence |
|--------|-------------------|------------|
| developer.android.com/jetpack/compose/bom/bom-mapping | BOM 2026.03.00, Compose UI 1.10.5, Material3 1.4.0 | HIGH |
| developer.android.com/jetpack/androidx/releases/room | Room 2.8.4 stable | HIGH |
| developer.android.com/jetpack/androidx/releases/datastore | DataStore 1.2.1 stable | HIGH |
| developer.android.com/jetpack/androidx/releases/work | WorkManager 2.11.1 stable | HIGH |
| developer.android.com/jetpack/androidx/releases/navigation | Navigation Compose 2.9.7 stable | HIGH |
| developer.android.com/jetpack/androidx/releases/lifecycle | Lifecycle 2.10.0 stable | HIGH |
| developer.android.com/jetpack/androidx/releases/activity | Activity Compose 1.13.0 stable | HIGH |
| developer.android.com/jetpack/androidx/releases/hilt | Hilt AndroidX 1.3.0 stable | HIGH |
| developer.android.com/training/dependency-injection/hilt-android | Hilt 2.57.1 stable, KSP setup | HIGH |
| developer.android.com/build/releases/gradle-plugin | AGP 9.1.0, Gradle 9.3.1, JDK 17 | HIGH |
| kotlinlang.org/docs/releases.html | Kotlin 2.3.20 | HIGH |
| developer.android.com/jetpack/androidx/releases/media3 | Media3 1.9.2 stable | HIGH |
| developer.android.com/jetpack/androidx/releases/security | security-crypto 1.1.0 deprecated | HIGH |
| developer.android.com/training/articles/keystore | Keystore platform API recommendation | HIGH |
| developer.android.com/jetpack/androidx/releases/test | AndroidX Test JUnit 1.3.0, Espresso 3.7.0 | HIGH |
| developer.android.com/ai/gemini-nano | Gemini Nano / AICore overview | HIGH |
| developer.android.com/ai/gemini-nano/ai-edge-sdk | Google AI Edge SDK deprecated | HIGH |
| developer.android.com/ai/gemini-nano/ml-kit-genai | ML Kit GenAI Prompt API = current recommended path | HIGH |
| developer.android.com/topic/architecture/ui-layer | ViewModel + StateFlow + UDF official pattern | HIGH |
| developer.android.com/jetpack/compose/bom | BOM covers ui-test, Compose Compiler via Kotlin plugin | HIGH |
| developer.android.com/studio/releases | Android Studio Panda 2 (2025.3.2) | MEDIUM |
| developer.android.com/about/versions/15/overview | Android 15 = API 35 stable | HIGH |
| developer.android.com/about/versions/16/overview | Android 16 = API 36 in preview/beta | HIGH |
| OkHttp/Retrofit exact versions | NOT verified via official source | LOW — mark TO_BE_VERIFIED |
| kotlinx.coroutines exact version | NOT verified via official source | LOW — mark TO_BE_VERIFIED |
| Lottie version | NOT verified via official source | LOW — mark TO_BE_VERIFIED |
| ML Kit Prompt API artifact name/version | NOT verified (WebFetch blocked) | LOW — mark TO_BE_VERIFIED |

---

*Stack research for: Android Kotlin voice-first AI companion app*
*Researched: 2026-03-18*
