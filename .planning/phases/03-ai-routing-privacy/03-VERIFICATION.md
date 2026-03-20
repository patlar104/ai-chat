---
phase: 03-ai-routing-privacy
verified: 2026-03-19T20:30:00Z
status: human_needed
score: 11/11 must-haves verified
re_verification: false
human_verification:
  - test: "Run AiRouterTest suite"
    expected: "All 7 test cases pass (deterministic priority, cloud fallback, privacy block, offline degraded, deterministic pass-through in privacy mode, no-key UNKNOWN)"
    why_human: "Gradle build environment incompatibility (AGP 9.0+ removes org.jetbrains.kotlin.android plugin) prevented any automated test execution across all 4 plans. All implementation was verified via code review only. Tests exist and are substantive, but have never actually run."
  - test: "Disable internet, ask open-ended question"
    expected: "Assistant speaks 'I can't answer that right now because Offline mode is active. I can still control your home and set reminders.' WifiOff icon appears at top of VoiceScreen."
    why_human: "ConnectivityManager NetworkCallback behavior and TTS spoken output cannot be verified programmatically."
  - test: "Enable Privacy Mode in Settings, ask open-ended question"
    expected: "Assistant speaks 'I can't answer that right now because Privacy Mode is active...' VisibilityOff icon appears at top of VoiceScreen. No outbound network request to generativelanguage.googleapis.com in logs."
    why_human: "Network blocking and TTS output require a running device. The @Volatile field wiring from SettingsViewModel to PrivacyInterceptor requires runtime validation."
  - test: "Enable Privacy Mode or go Offline, say 'Turn on the lights'"
    expected: "Device command executes normally. Deterministic tier is unaffected by privacy or offline state."
    why_human: "End-to-end Home Assistant command flow with HA running locally."
  - test: "Ask open-ended question with valid Google AI API key configured"
    expected: "Gemini responds. Chat history shows 'cloud' badge on the assistant message."
    why_human: "Requires a real Google AI API key and network access to Gemini."
---

# Phase 03: AI Routing + Privacy Verification Report

**Phase Goal:** Implement AI routing with privacy controls — deterministic parsing for home control, cloud Gemini fallback for open-ended queries, network-level privacy interceptor, and degraded mode UX indicators.
**Verified:** 2026-03-19T20:30:00Z
**Status:** human_needed (all automated checks pass; test suite has never run due to build environment issue)
**Re-verification:** No — initial verification

---

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Every assistant turn records its routing source (DETERMINISTIC, CLOUD, or UNKNOWN) | VERIFIED | `Message.sourceType: SourceType` persisted via `messageRepository.insert(assistantMessage)` in `VoiceViewModel.handleTranscript`; `SourceType` enum has all three values |
| 2 | Deterministic parser matches take precedence over cloud routing | VERIFIED | `AiRouterImpl` tier-1: `commandParser.parse(transcript)` checked first; non-Unknown result returned as `SourceType.DETERMINISTIC` before cloud path executes |
| 3 | VoiceViewModel resolves intents through AiRouter, not direct parser calls | VERIFIED | `VoiceViewModel` imports `AiRouter` (not `CommandParser`); `aiRouter.resolveIntent(text)` is the single call; no `CommandParser` import anywhere in feature modules |
| 4 | Unrecognized commands are sent to the cloud AI tier | VERIFIED | `AiRouterImpl` tier-2 calls `cloudAiService.generateResponse(transcript)` when `CommandParser` returns null/Unknown and privacy mode is off and API key is present |
| 5 | Chat messages from the cloud show a distinguishing badge | VERIFIED | `MessageBubble` renders a `SuggestionChip` for all assistant messages with label "cloud" when `sourceType == SourceType.CLOUD`, "deterministic" otherwise |
| 6 | Enabling privacy mode blocks all cloud AI network requests at the interceptor level | VERIFIED | `PrivacyInterceptor.intercept()` returns synthetic 403 for hosts ending in `generativelanguage.googleapis.com` or `aiplatform.googleapis.com` when `privacyModeEnabled == true`; registered as first `addInterceptor` in `NetworkModule` |
| 7 | Privacy mode state is persisted in DataStore | VERIFIED | `SettingsRepository.privacyModeEnabled` Flow; `SettingsViewModel.init` collects it and writes to `privacyInterceptor.privacyModeEnabled`; DataStore key exists in `AppPreferences` |
| 8 | Assistant speaks a degraded mode message when cloud is blocked | VERIFIED | `AiRouterImpl` returns `ParsedIntent.CloudResponse("I can't answer that right now because Privacy Mode is active...")` when `privacyInterceptor.privacyModeEnabled == true`; VoiceViewModel handles `CloudResponse` via `intent.text` which feeds `speakReply()` |
| 9 | UI shows a visual indicator when Privacy Mode or Offline mode is active | VERIFIED | `VoiceScreen` renders `Row` with `VisibilityOff` icon + "Privacy Mode" label and/or `WifiOff` icon + "Offline" label in `MaterialTheme.colorScheme.error` when `privacyModeEnabled || !isOnline` |
| 10 | Deterministic commands still function correctly in degraded mode | VERIFIED | Privacy/offline check in `AiRouterImpl` runs only after `CommandParser` fails; deterministic results returned before reaching the privacy check; `AiRouterTest` contains explicit test case for this |
| 11 | Offline degraded mode is clearly communicated to user when relevant | VERIFIED | `AiRouterImpl` returns `ParsedIntent.CloudResponse("I can't answer that right now because Offline mode is active...")` when API key is set but `generateResponse` returns null; `WifiOff` icon shown in UI |

**Score: 11/11 truths verified**

---

## Required Artifacts

### Plan 03-01 Artifacts

| Artifact | Provides | Status | Details |
|----------|----------|--------|---------|
| `core/ai/src/main/kotlin/.../core/ai/routing/AiRouter.kt` | Central AI gateway interface | VERIFIED | 14-line file; `AiRouterResult` data class + `suspend fun resolveIntent(transcript: String): AiRouterResult` interface |
| `core/ai/src/main/kotlin/.../core/ai/routing/AiRouterImpl.kt` | Tiered routing implementation | VERIFIED | 54-line file; 3-tier logic (deterministic → privacy check → cloud → offline degraded); no stubs |
| `core/domain/src/main/kotlin/.../core/domain/model/Message.kt` | SourceType.CLOUD enum value | VERIFIED | `enum class SourceType { DETERMINISTIC, CLOUD, UNKNOWN }` present |

### Plan 03-02 Artifacts

| Artifact | Provides | Status | Details |
|----------|----------|--------|---------|
| `core/network/src/main/kotlin/.../core/network/ai/CloudAiService.kt` | Gemini SDK integration | VERIFIED | Uses `GenerativeModel("gemini-1.5-flash")`; `@Volatile apiKey`; `generateResponse()` returns text or null; proper `CancellationException` rethrow |
| `core/ai/src/main/kotlin/.../core/ai/routing/AiRouterImpl.kt` | Cloud fallback logic | VERIFIED | Injects `CloudAiService`; calls `cloudAiService.generateResponse(transcript)` at tier 2 |
| `feature/chat/src/main/kotlin/.../feature/chat/component/MessageBubble.kt` | "cloud" badge for SourceType.CLOUD | VERIFIED | `when (message.sourceType)` handles all 3 values; "cloud" label with primary color chip rendered for `SourceType.CLOUD` |

### Plan 03-03 Artifacts

| Artifact | Provides | Status | Details |
|----------|----------|--------|---------|
| `core/network/src/main/kotlin/.../core/network/privacy/PrivacyInterceptor.kt` | Network-level blocking of cloud domains | VERIFIED | 49-line OkHttp `Interceptor`; blocks both `generativelanguage.googleapis.com` and `aiplatform.googleapis.com`; returns `Response.Builder` 403 synthetic response |
| `core/network/src/main/kotlin/.../core/network/di/NetworkModule.kt` | PrivacyInterceptor registration | VERIFIED | `providePrivacyInterceptor()` singleton; injected as first `addInterceptor` call before `baseUrlInterceptor` and `authInterceptor` |

### Plan 03-04 Artifacts

| Artifact | Provides | Status | Details |
|----------|----------|--------|---------|
| `feature/voice/src/main/kotlin/.../feature/voice/VoiceScreen.kt` | Privacy/Offline visual indicators | VERIFIED | Conditional `Row` renders `VisibilityOff` + `WifiOff` icons in error color; collects `privacyModeEnabled` and `isOnline` StateFlows from ViewModel |
| `feature/voice/src/main/kotlin/.../feature/voice/VoiceViewModel.kt` | privacyModeEnabled + isOnline StateFlows | VERIFIED | `privacyModeEnabled` via `settingsRepository.privacyModeEnabled.stateIn()`; `isOnline` via `ConnectivityManager.NetworkCallback`; callback unregistered in `onCleared()` |
| `feature/settings/src/main/kotlin/.../feature/settings/SettingsViewModel.kt` | PrivacyInterceptor wired from DataStore | VERIFIED | `init` block collects `settingsRepository.privacyModeEnabled` and assigns to `privacyInterceptor.privacyModeEnabled` |

---

## Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `VoiceViewModel` | `AiRouter` | dependency injection | WIRED | `private val aiRouter: AiRouter` constructor param; `aiRouter.resolveIntent(text)` called in `handleTranscript` |
| `AiRouterImpl` | `CommandParser` | tier 1 routing | WIRED | `commandParser.parse(transcript)` called first; result checked before cloud tier |
| `AiRouterImpl` | `CloudAiService` | tier 2 fallback | WIRED | `cloudAiService.generateResponse(transcript)` called when deterministic fails and privacy is off and key present |
| `AiRouterImpl` | `PrivacyInterceptor` | routing fast-path | WIRED | `privacyInterceptor.privacyModeEnabled` read before cloud call; returns degraded message if true |
| `PrivacyInterceptor` | (DataStore via SettingsViewModel) | @Volatile field updated externally | WIRED | `SettingsViewModel.init` collects `settingsRepository.privacyModeEnabled` and sets `privacyInterceptor.privacyModeEnabled = enabled` |
| `CloudAiService` | (DataStore via SettingsViewModel) | @Volatile field updated externally | WIRED | `SettingsViewModel.init` collects `settingsRepository.googleAiApiKey` and sets `cloudAiService.apiKey = key` |
| `VoiceScreen` | `VoiceViewModel.privacyModeEnabled` | `collectAsStateWithLifecycle` | WIRED | `val privacyModeEnabled by viewModel.privacyModeEnabled.collectAsStateWithLifecycle()` |
| `VoiceScreen` | `VoiceViewModel.isOnline` | `collectAsStateWithLifecycle` | WIRED | `val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()` |

---

## Requirements Coverage

All Phase 3 requirements were claimed across plans. Cross-reference against REQUIREMENTS.md:

| Requirement | Source Plan | Description | Code Status | REQUIREMENTS.md Status | Notes |
|-------------|------------|-------------|-------------|------------------------|-------|
| ROUT-01 | 03-01 | All assistant requests routed through central AI gateway | SATISFIED | Complete | VoiceViewModel uses only `AiRouter`; no direct LLM calls from any feature module. |
| ROUT-02 | 03-01 | Deterministic parser handles known intents without LLM | SATISFIED | Complete | `AiRouterImpl` tier 1 calls `CommandParser` first; matching intents never reach cloud. |
| ROUT-03 | 03-02 | Cloud AI handles open-ended reasoning | SATISFIED | Complete | Gemini SDK wired via `CloudAiService` |
| ROUT-04 | 03-04 | System degrades gracefully when cloud is unavailable | SATISFIED | Complete | Privacy + offline degraded messages; spoken and displayed |
| ROUT-05 | 03-01, 03-02 | Every routing decision is observable | SATISFIED | Complete | `SourceType` on every `Message`; "cloud"/"deterministic"/"unknown" chip in `MessageBubble` |
| PRIV-01 | 03-03 | Privacy mode prevents cloud AI requests | SATISFIED | Complete | PrivacyInterceptor + AiRouterImpl fast-path both block cloud calls |
| PRIV-02 | 03-03 | Privacy enforced in network interceptor, not just UI | SATISFIED | Complete | `PrivacyInterceptor` is first OkHttp interceptor; blocks regardless of UI state |
| PRIV-03 | 03-04 | Privacy mode visually indicated in UI | SATISFIED | Complete | `VisibilityOff` icon + "Privacy Mode" label in VoiceScreen |
| PRIV-04 | 03-04 | Core commands work without network in offline mode | SATISFIED | Complete | Deterministic tier executes before any network/privacy check in `AiRouterImpl` |
| PRIV-05 | (Phase 2 carry-over) | Local TTS in offline mode | SATISFIED | Complete | No cloud TTS added in Phase 3; Android `TextToSpeech` remains the only TTS engine |
| PRIV-06 | 03-04 | Offline degraded mode communicated to user | SATISFIED | Complete | Offline message spoken via TTS; `WifiOff` icon shown in VoiceScreen |

**REQUIREMENTS.md:** ROUT-01 and ROUT-02 updated to Complete on 2026-03-19.

---

## Anti-Patterns Found

No blocking anti-patterns detected in Phase 3 key files.

| File | Pattern | Severity | Impact |
|------|---------|----------|--------|
| `CloudAiService.kt` | Creates new `GenerativeModel` instance on every `generateResponse()` call | Info | Minor inefficiency — model instance is cheap to construct but could be cached per `apiKey`. Non-blocking; no correctness issue. |

---

## Human Verification Required

### 1. AiRouterTest Suite Execution

**Test:** Run `./gradlew :core:ai:test` (requires fixing the pre-existing Gradle/AGP build environment issue first)
**Expected:** All 7 test cases in `AiRouterTest` pass: deterministic priority, UNKNOWN when no API key, cloud fallback via null parser result, cloud fallback via Unknown intent, privacy mode degraded message, deterministic pass-through with privacy on, offline degraded message
**Why human:** Build environment incompatibility (AGP 9.0+ with `org.jetbrains.kotlin.android` plugin) prevented any automated test execution across all four plans in Phase 3. Tests are substantive and structurally correct per code review, but actual test execution has never occurred.

### 2. Privacy Mode End-to-End

**Test:** Enter a valid Google AI API key in Settings. Enable Privacy Mode. Ask "What is the capital of France?"
**Expected:** Assistant speaks "I can't answer that right now because Privacy Mode is active. I can still control your home and set reminders." Network logs show no outbound request to `generativelanguage.googleapis.com`. `VisibilityOff` icon visible at top of VoiceScreen.
**Why human:** `@Volatile` field propagation from `SettingsViewModel` to `PrivacyInterceptor` and spoken TTS output require a running device to validate end-to-end.

### 3. Offline Degraded Mode

**Test:** Configure a valid Google AI API key. Disable internet (airplane mode). Ask an open-ended question.
**Expected:** Assistant speaks "I can't answer that right now because Offline mode is active..." `WifiOff` icon appears at top of VoiceScreen. Local TTS speaks the response.
**Why human:** ConnectivityManager NetworkCallback behavior and the full offline path require a running Android device.

### 4. Deterministic Commands in Degraded Mode

**Test:** With Privacy Mode ON, say "Turn on the lights" (requires a configured device alias and Home Assistant running locally).
**Expected:** Home control command executes successfully. Lights turn on. Assistant confirms "Done, turning on lights." No degraded mode message.
**Why human:** Requires a running Home Assistant instance and network connectivity to local HA (not blocked by PrivacyInterceptor).

### 5. Cloud Badge in Chat History

**Test:** Configure a valid Google AI API key, Privacy Mode OFF, connected to internet. Ask "Tell me a joke." Then open the Chat screen.
**Expected:** The assistant's response bubble shows a "cloud" chip badge. A previous deterministic command shows "deterministic" chip. The visual distinction is clear.
**Why human:** Requires Gemini API key, network access, and visual UI inspection.

---

## Overall Assessment

All 11 must-have truths are verified by code inspection. All 11 required artifacts exist at all three levels (exists, substantive, wired). All 8 key links are confirmed wired. No blocker anti-patterns found.

The phase goal is achieved in code. The only outstanding items are:

1. **Test execution blocked** by a pre-existing Gradle/AGP build environment incompatibility — not introduced by Phase 3 work.
2. **Human device testing** needed to confirm end-to-end behavior of TTS, connectivity detection, and API key propagation.

---

_Verified: 2026-03-19T20:30:00Z_
_Verifier: Claude (gsd-verifier)_
