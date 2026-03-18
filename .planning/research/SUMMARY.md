# Project Research Summary

**Project:** Android Kotlin Voice-First AI Companion App
**Domain:** Android voice assistant with smart-home control, hybrid AI routing, expressive avatar, and persistent memory
**Researched:** 2026-03-18
**Confidence:** HIGH (Android platform stack) / MEDIUM (AI routing, avatar, competitor feature analysis)

## Executive Summary

This is a personal-use, voice-first AI companion app for Android built with Kotlin and Jetpack Compose. The product sits at the intersection of three well-understood domains — Android voice assistants, smart-home automation via Home Assistant, and AI companion presence — but combines them in a way few open products have. The research-backed approach is a 15-module Clean Architecture project with a tiered AI router (deterministic parser → on-device Gemini Nano → cloud LLM), button-to-talk voice interaction, a state-driven 2D expressive avatar, and Home Assistant as the unified smart-home backend. The full recommended stack is mature and well-documented; all core library versions are verified against official Android release notes as of March 2026.

The recommended strategy is to ship the voice shell and home control integration first, treating the AI router's deterministic tier as the primary value driver for daily use. This keeps latency low and avoids privacy exposure for the majority of commands. Cloud LLM is the fallback for open-ended reasoning, not the default path. The avatar enhances emotional presence but must be architecturally decoupled from the TTS pipeline to avoid performance coupling. Privacy mode is a first-class, trust-defining feature and must be enforced in a network interceptor — not just in the UI layer.

The highest-risk areas are audio pipeline correctness (SpeechRecognizer threading, foreground service types, audio focus management) and module dependency discipline. Both must be correct from the first build — retrofitting either is disproportionately expensive. The lowest-risk area is the persistence and settings layer (Room + DataStore + Hilt), which follows completely standard Android patterns. The overall concept is buildable by a single developer iterating in phases; the main risk is scope creep into deferred features (always-on hotword, 3D avatar, streaming voice) before the daily-use core is validated.

## Key Findings

### Recommended Stack

The Android platform stack is fully determined and version-pinned against official sources. Kotlin 2.3.20, Compose BOM 2026.03.00, Hilt 2.57.1 with KSP (not kapt), Room 2.8.4, DataStore 1.2.1, WorkManager 2.11.1, Navigation Compose 2.9.7, and Lifecycle 2.10.0 are all verified stable. Several third-party versions (OkHttp, Retrofit, kotlinx-serialization, kotlinx-coroutines, ML Kit GenAI, Lottie, Turbine, MockK, Robolectric) carry `TO_BE_VERIFIED` flags and must be confirmed against their respective GitHub release pages before the build is configured.

Two critical deprecation traps are confirmed and must be avoided: `security-crypto` (`EncryptedSharedPreferences`) is fully deprecated as of July 2025 — use Android Keystore directly with DataStore; and the Google AI Edge SDK is deprecated — use ML Kit Prompt API (Gemini Nano via AICore) as the on-device AI path. The Compose compiler no longer uses `kotlinCompilerExtensionVersion`; configure via the Compose Compiler Gradle plugin instead.

**Core technologies:**
- Kotlin 2.3.20: Language — current stable, coroutines/Flow/value classes are first-class
- Jetpack Compose BOM 2026.03.00: UI — single BOM version pins all Compose deps consistently
- Hilt 2.57.1 + KSP: DI — compile-time code gen, no runtime reflection in a 15-module graph
- Room 2.8.4: Persistence — KSP-based, coroutines + Flow, required for conversations, memory, and reminders
- DataStore 1.2.1: Settings — coroutine-native SharedPreferences replacement for user prefs and encrypted secrets
- WorkManager 2.11.1: Background tasks — OS-restart-safe scheduler with Android 15 network fix
- OkHttp + Retrofit (versions TO_BE_VERIFIED): Networking — WebSocket for HA real-time events, REST for cloud AI
- ML Kit Prompt API (version TO_BE_VERIFIED): On-device AI — system-managed Gemini Nano, no APK size cost
- Media3 ExoPlayer 1.9.2: Audio playback — streaming cloud TTS audio
- Android SpeechRecognizer (platform): STT — no dependency cost, offline flag available
- Android TextToSpeech (platform): TTS — zero latency for short confirmations

### Expected Features

Research confirms a clear MVP boundary. The P1 feature set is everything needed to validate the concept in daily use. Features that "feel fundamental" (always-on hotword, streaming voice, 3D avatar, multi-user) are anti-features for v1 — they add months of work to a use case that a button-to-talk interaction model already satisfies.

**Must have (table stakes):**
- Button-to-talk voice shell with Idle/Listening/Processing/Speaking/Error state machine — the entire product depends on this working reliably
- STT (Android SpeechRecognizer) + TTS (local Android TTS as baseline) — speech in, speech out
- Deterministic command parser covering the top 20+ home control intents — >90% of home commands must never reach cloud
- Home Assistant integration with device/room alias resolution — the core daily use case
- Hybrid AI router with privacy mode toggle enforced at network interceptor — handles edge cases, respects privacy
- Conversation history in Room — users scroll back to verify what happened
- Persistent memory with explicit user review and delete screen — trust through transparency
- Basic reminders via AlarmManager/WorkManager — universal personal assistant expectation
- Offline degraded mode — local commands, reminders, and Room data work without network
- Visual feedback state machine (avatar stub or ring indicator acceptable in early phases)

**Should have (competitive differentiation):**
- Expressive 2D avatar with voice-state-driven animation — presence creates emotional connection at 10% of the cost of 3D
- WorkManager background briefings with personalized content — proactive behavior distinguishes from purely reactive assistants
- Two-tier TTS routing (local for confirmations, cloud neural for companion responses) — right voice for context without sacrificing speed
- Conversation context window (multi-turn "turn it up" references) — natural stateful home control
- Cloud neural TTS upgrade (ElevenLabs / OpenAI TTS) — adds warmth to companion interactions after local TTS proves insufficient
- On-device LLM via Gemini Nano (ML Kit Prompt API) — offline reasoning quality for open-ended questions

**Defer to v2+:**
- Always-on hotword detection — battery impact unknown; validate after baseline is established
- Real-time streaming duplex voice — VAD, echo cancellation, barge-in are each significant engineering problems
- Autonomous purchasing or high-risk transactions — needs dedicated confirmation architecture
- Full 3D avatar — 2-6 months of art/engineering; validate 2D presence first
- Cross-app UI automation via AccessibilityService — Play Store policy risk, OEM skin fragility
- Multi-user household profiles — personal use must be proven first

### Architecture Approach

The project is a standard modular Clean Architecture Android app with 15 modules organized into three levels: `core.*` (domain, data, network, audio, AI, automation, UI), `feature.*` (voice, chat, avatar, homecontrol, memory, tasks, settings), and `:app` (NavHost entry point). The critical rule is that `feature.*` modules depend only on `core.*` modules — never on each other. Shared state (e.g., VoiceState) flows through a `StateFlow` in `core.domain` or a NavGraph-scoped ViewModel, not via direct inter-feature imports. This rule must be enforced from the first commit via a Gradle DAG check; retrofitting cyclic dependencies is rated HIGH recovery cost.

The voice session uses MVI (not MVVM) because the interaction sequence is strictly ordered and needs deterministic state transitions. The AI router implements a sealed `ToolCall` interface dispatched through a `ToolExecutor` — no raw string parsing outside `:core.ai`. The avatar observes `VoiceState` passively and must never be in the TTS execution path.

**Major components:**
1. `:core.domain` — Pure Kotlin models, use case interfaces, repository contracts; no Android imports; testable without device
2. `:core.ai` — Tiered AI router (deterministic parser → local model → cloud LLM), ToolExecutor, MemoryExtractor; privacy gate lives here
3. `:core.audio` — SpeechRecognizer + TTS abstractions; threading contract enforced here (SpeechRecognizer must be main-thread)
4. `:core.network` — OkHttp/Retrofit clients for HA REST, HA WebSocket, cloud LLM, cloud TTS; all auth interceptors and privacy mode interceptor live here
5. `:core.automation` — WorkManager workers for briefings and reminders; no direct feature imports
6. `:feature.voice` — MVI ViewModel orchestrating the voice state machine; delegates everything to use cases
7. `:feature.avatar` — Composable observing VoiceState; animation is a side effect of state, never a blocker

### Critical Pitfalls

1. **SpeechRecognizer must be created on the main thread** — enforce via `check(Looper.myLooper() == Looper.getMainLooper())` in the recognizer factory; move result processing to IO dispatchers. Failure mode: recognition silently never fires, state machine stuck in LISTENING.

2. **Missing `foregroundServiceType="microphone"` in manifest** — required since API 29, causes `SecurityException` on Android 14+. Declare service type and `FOREGROUND_SERVICE_MICROPHONE` permission from the first voice build.

3. **Audio focus not managed** — use `AudioFocusRequest.Builder` with `CONTENT_TYPE_SPEECH` + `USAGE_ASSISTANT`; verify `AUDIOFOCUS_REQUEST_GRANTED` before every `speak()` call. On Android 15+, background apps without a foreground service silently get `AUDIOFOCUS_REQUEST_FAILED`.

4. **Cyclic Gradle module dependencies** — `feature.*` modules must never import each other. Install a CI check from Phase 0. Recovery cost is HIGH (1-3 days of restructuring).

5. **Privacy mode bypassed by pending background requests** — privacy mode must be checked at execution time inside a `core.network` interceptor, not only at enqueue time or in the UI. Cancel queued cloud-dependent WorkManager workers when privacy mode activates.

6. **Hybrid AI router falls through to cloud for all commands** — build the deterministic parser with regex/synonym coverage before wiring home control; target >90% of home control commands resolved deterministically. Cloud should only handle open-ended reasoning.

7. **HA token stored unencrypted** — never use plain `SharedPreferences` for the long-lived access token. Use Android Keystore-backed AES-GCM encryption with DataStore from day one. (`security-crypto` is deprecated; use Keystore directly.)

8. **Avatar animation delays TTS onset** — TTS `speak()` must be called immediately on voice state change; avatar animation is a parallel side effect. Profile TTS onset time with and without avatar before shipping avatar integration.

## Implications for Roadmap

Based on the combined research, the module dependency graph and feature dependency tree strongly suggest a 7-phase build order. The architecture research explicitly documents this build order; the pitfall research confirms which phase each pitfall must be addressed in.

### Phase 0: Scaffold and Module DAG
**Rationale:** Module boundaries and build tooling must be correct before any feature code is written. Cyclic dependency recovery cost is HIGH — this is not recoverable cheaply mid-project.
**Delivers:** Working multi-module Gradle project with Version Catalogs, Hilt configured, build-logic convention plugins, CI DAG check preventing feature-to-feature imports.
**Avoids:** Cyclic module dependency pitfall (Pitfall 12 — HIGH recovery cost).
**Stack elements:** Kotlin 2.3.20, AGP 9.1.0, Gradle 9.3.1, Hilt 2.57.1, KSP (verify version), `libs.versions.toml` with all pinned versions.
**Research flag:** Standard Android patterns — skip deeper research.

### Phase 1: Voice Pipeline Foundation
**Rationale:** Voice shell is the primary interaction model. Everything else depends on STT/TTS/state machine working correctly. Audio pipeline bugs (threading, foreground service, audio focus) are both critical and cheap to fix early — expensive to fix late.
**Delivers:** Working button-to-talk loop: Idle → Listening → Processing → Speaking → Idle with full error handling. Local TTS confirmation working.
**Features addressed:** Button-to-talk voice shell, STT (SpeechRecognizer), local TTS (Android TextToSpeech), visual state machine, spoken confirmation, offline STT fallback.
**Modules built:** `:core.domain` (initial models), `:core.ui`, `:core.audio`, `:feature.voice` (state machine only).
**Pitfalls to address in this phase:** SpeechRecognizer main-thread requirement (P1), foreground service type declaration (P2), audio focus management (P3), voice state machine timeout/error handling (P4), SpeechRecognizer offline failure graceful fallback (P13).
**Research flag:** Well-documented platform APIs — skip deeper research. All pitfall mitigations are verified from official Android docs.

### Phase 2: AI Routing Layer
**Rationale:** The deterministic parser must be built and its coverage validated before home control is wired. The router determines which tier handles each command; building it first prevents the pitfall of cloud becoming the default for trivial commands.
**Delivers:** Functioning `AiRouter` with deterministic parser covering the home control command vocabulary, cloud LLM fallback via OkHttp/Retrofit direct HTTP, privacy mode toggle enforced at interceptor level.
**Features addressed:** Natural language command parsing, hybrid AI router, privacy mode toggle.
**Modules built:** `:core.ai` (deterministic parser + cloud tier), `:core.network` (OkHttp/Retrofit clients + privacy interceptor), `:feature.settings` (privacy mode toggle).
**Pitfalls to address in this phase:** Router falls through to cloud too often (P5), privacy mode bypass via pending requests (P11).
**Research flag:** ML Kit Prompt API (on-device Gemini Nano) version and availability API are NOT_VERIFIED — needs research when local AI tier is added in v1.x. Cloud LLM HTTP integration follows standard patterns.

### Phase 3: Home Assistant Integration
**Rationale:** This is the core daily value proposition. Depends on the AI router being stable. Home control must ship with alias resolution — without it, commands are brittle. HA token storage must be correct from first write.
**Delivers:** Voice commands controlling Home Assistant devices via WebSocket API, device/room alias resolution, configurable HA URL (local + external), graceful degradation when HA is unreachable.
**Features addressed:** Home Assistant integration, device/room alias resolution, offline degraded mode (HA-specific).
**Modules built:** `:core.data` (Room schema — conversations, memory, aliases), `:core.network` (HA WebSocket + REST clients), `:feature.homecontrol`.
**Pitfalls to address in this phase:** HA hardcoded IP (P6), HA token unencrypted (P7).
**Research flag:** HA WebSocket authentication flow and entity domain mapping (light vs. switch vs. script vs. scene) may need verification against current HA API docs at implementation time — recommend shallow research-phase before this phase is planned in detail.

### Phase 4: Persistence, Memory, and Reminders
**Rationale:** Room schema for conversation history, persistent memory, and tasks can be built in parallel with or after home control. Memory promotion policy must be defined before any AI routing can write to memory — auto-promotion is a trust violation.
**Delivers:** Conversation history in Room with chat log UI, persistent memory with explicit user review/delete screen, basic reminders via AlarmManager, memory-informed AI routing.
**Features addressed:** Conversation history, persistent memory + review UI, basic reminders, memory-informed personalization.
**Modules built:** `:core.data` (memory + reminder schema), `:feature.memory`, `:feature.tasks`, `:feature.chat`.
**Pitfalls to address in this phase:** Auto memory promotion without user review (P10).
**Research flag:** Standard Room + AlarmManager patterns — skip deeper research.

### Phase 5: Background Automation and Briefings
**Rationale:** WorkManager infrastructure built in Phase 4 for reminders; briefings extend it. AlarmManager vs. WorkManager distinction (time-critical vs. best-effort) must be made correctly per-task type.
**Delivers:** Morning briefings via WorkManager (with AlarmManager for time-critical delivery on OEM devices), scheduled automations, notification channels.
**Features addressed:** WorkManager background briefings, scheduled automations (time-based), notification delivery for background events.
**Modules built:** `:core.automation` (WorkManager workers + AlarmManager briefing trigger), notification channels.
**Pitfalls to address in this phase:** WorkManager timing unreliable on OEM devices (P8) — use `AlarmManager.setAlarmClock()` for user-visible scheduled events; test on Samsung/MIUI physical devices.
**Research flag:** OEM battery optimization behavior is device-specific — test matrix should include Samsung and MIUI before shipping.

### Phase 6: Avatar Integration
**Rationale:** Avatar depends on the voice state machine being stable (Phase 1) and the TTS pipeline being performance-validated. Building avatar after the voice pipeline is proven prevents avatar animation from masking TTS latency regressions.
**Delivers:** Expressive 2D avatar with Compose animation driven by VoiceState (Idle/Listening/Processing/Speaking/Error), profiled to confirm no TTS onset regression.
**Features addressed:** Expressive 2D avatar (state-driven), enhanced avatar animation (Lottie, optional).
**Modules built:** `:feature.avatar`.
**Pitfalls to address in this phase:** Avatar animation blocking TTS onset (P9) — TTS must be called immediately on state change; avatar is a parallel side effect; measure with Android Studio profiler.
**Research flag:** Lottie version is TO_BE_VERIFIED. If avatar complexity demands it, verify latest Lottie Compose version at github.com/airbnb/lottie-android before this phase.

### Phase 7: Hardening, Offline, and v1 Polish
**Rationale:** Final integration phase validates all offline paths, security posture, and the "looks done but isn't" checklist from PITFALLS.md. Cloud neural TTS and conversation context window are natural additions here once the core loop is stable.
**Delivers:** Fully hardened offline mode (deterministic parser + local TTS + Room all function without network from cold start), cloud neural TTS for companion responses, conversation context window for multi-turn interactions, full security audit.
**Features addressed:** Offline degraded mode (full hardening), cloud neural TTS, conversation context window, two-tier TTS routing.
**Modules built:** All modules — integration and hardening pass.
**Pitfalls to address in this phase:** Full "Looks Done But Isn't" checklist verification (PITFALLS.md), privacy mode audit across all network interceptors.
**Research flag:** Cloud neural TTS provider selection (OpenAI TTS vs. ElevenLabs vs. Google WaveNet) needs provider-specific API research at planning time.

### Phase Ordering Rationale

- **Audio pipeline correctness before everything else.** Three of the most critical pitfalls (SpeechRecognizer threading, foreground service type, audio focus) are Phase 1 concerns. They are cheap to fix early and expensive to retrofit. The voice shell is also the dependency root for all voice-driven features.
- **Deterministic parser before home control.** If home control is wired before the parser has sufficient coverage, cloud becomes the de facto default and latency/cost expectations are set incorrectly. Parser quality must be validated with telemetry before home control goes end-to-end.
- **Home control before persistence UI.** Room is needed for home control (alias storage), but the full memory/chat UI can be deferred until the core interaction loop is proven.
- **Avatar last among core features.** Avatar depends on a stable voice state machine and must be profiled against it. Adding avatar early risks masking pipeline performance problems.
- **Module DAG enforced from day one.** This is Phase 0 because no other phase is recoverable from a cyclic dependency without days of restructuring.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 3 (Home Assistant):** HA WebSocket authentication message format and entity domain mapping (light/switch/script/scene command types) should be verified against current HA API documentation before detailed planning. The local-vs-external URL connectivity selector has no established Android pattern — needs design work.
- **Phase 6 (Avatar / Lottie):** Lottie Compose version is unverified. If Lottie is chosen, verify compatibility with Compose BOM 2026.03.00 before planning begins.
- **v1.x: On-device AI (Gemini Nano via ML Kit Prompt API):** ML Kit GenAI artifact name and version are unverified (WebFetch was blocked during research). Device availability check API also needs confirmation. Needs targeted research before this capability is planned.
- **Phase 7 (Cloud neural TTS):** Provider API specifics (OpenAI TTS streaming format, ElevenLabs latency characteristics) need research at planning time. Media3 ExoPlayer streaming integration with chunked HTTP is the implementation pattern.

Phases with standard patterns (skip research-phase):
- **Phase 0 (Scaffold):** Kotlin/AGP/Hilt/KSP/Gradle Version Catalogs — fully documented official patterns.
- **Phase 1 (Voice Pipeline):** Android SpeechRecognizer, TextToSpeech, AudioFocusRequest — all stable platform APIs with comprehensive official documentation.
- **Phase 4 (Persistence/Memory/Reminders):** Room, DataStore, AlarmManager — standard Android patterns, well-documented.
- **Phase 5 (Background Automation):** WorkManager + AlarmManager — established patterns; the only variation is OEM battery behavior (test on physical Samsung/MIUI devices, no additional research needed).

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH (core) / LOW (TO_BE_VERIFIED entries) | All AndroidX versions verified against official release notes. OkHttp, Retrofit, kotlinx-serialization, kotlinx-coroutines, ML Kit GenAI, Lottie, Turbine, MockK, Robolectric are marked TO_BE_VERIFIED — must be confirmed before build setup |
| Features | MEDIUM | Feature categorizations are solid; competitor analysis is training-data-based (Aug 2025 cutoff). Anti-feature list (especially AccessibilityService Play Store policy) should be validated against current Google Play policy before v1.x planning |
| Architecture | MEDIUM (patterns) / HIGH (official APIs) | Clean Architecture and MVI patterns for Android are mature and stable. AI routing and avatar integration patterns are less standardized — the research represents best-known patterns, not a single canonical source |
| Pitfalls | HIGH (core Android) / MEDIUM (AI/avatar patterns) | Audio pipeline pitfalls (threading, foreground service type, audio focus) are directly sourced from official Android docs. AI routing and memory promotion pitfalls are from known ecosystem behavior |

**Overall confidence:** MEDIUM-HIGH

### Gaps to Address

- **TO_BE_VERIFIED library versions (9 entries):** OkHttp, Retrofit, kotlinx-serialization, kotlinx-coroutines, KSP (Kotlin 2.3.x compatibility), ML Kit Prompt API, Lottie, Turbine, MockK, Robolectric. These must be verified and pinned in `libs.versions.toml` before the scaffold build compiles. Recommended action: verify all at project start, open a tracking note for any that are temporarily blocked.

- **ML Kit Prompt API device availability:** Gemini Nano via AICore is currently Pixel 9 series with broader rollout ongoing. For non-AICore devices, the fallback is MediaPipe LLM Inference with Play Asset Delivery (bundled ~300MB model). The device eligibility check API needs verification. This gap affects the on-device AI tier design.

- **Home Assistant WebSocket authentication flow:** HA WebSocket uses a specific JSON message protocol (`{"type": "auth", "access_token": "..."}` handshake). Verify current HA WebSocket API version and authentication message format against Home Assistant developer docs at implementation time.

- **Cloud neural TTS provider selection:** The TTS abstraction must be provider-agnostic (OpenAI TTS, ElevenLabs, Google WaveNet are all viable). Provider-specific streaming format, latency, and cost characteristics should be researched at Phase 7 planning time.

- **AccessibilityService Play Store policy:** The anti-feature analysis flags broad cross-app accessibility automation as risky. Before any v1.x planning that touches cross-app integration, verify current Google Play policy against official 2026 policy documentation.

## Sources

### Primary (HIGH confidence)
- developer.android.com/jetpack/compose/bom/bom-mapping — Compose BOM 2026.03.00, UI 1.10.5, Material3 1.4.0
- developer.android.com/jetpack/androidx/releases/* — Room 2.8.4, DataStore 1.2.1, WorkManager 2.11.1, Navigation 2.9.7, Lifecycle 2.10.0, Activity 1.13.0, Hilt AndroidX 1.3.0
- developer.android.com/training/dependency-injection/hilt-android — Hilt 2.57.1, KSP setup
- developer.android.com/build/releases/gradle-plugin — AGP 9.1.0, Gradle 9.3.1, JDK 17
- kotlinlang.org/docs/releases.html — Kotlin 2.3.20
- developer.android.com/jetpack/androidx/releases/media3 — Media3 1.9.2
- developer.android.com/jetpack/androidx/releases/security — security-crypto 1.1.0 deprecated
- developer.android.com/ai/gemini-nano/ml-kit-genai — ML Kit GenAI Prompt API = current recommended path
- developer.android.com/ai/gemini-nano/ai-edge-sdk — Google AI Edge SDK deprecated
- developer.android.com/develop/background-work/services/fgs/service-types — Foreground service types (microphone)
- developer.android.com/media/optimize/audio-focus — Audio focus management
- developer.android.com/reference/android/speech/SpeechRecognizer — Main-thread requirement, error codes
- developer.android.com/topic/modularization/patterns — Cyclic dependency patterns, api vs implementation

### Secondary (MEDIUM confidence)
- Android modular architecture guidance (developer.android.com/topic/modularization) — module structure patterns (training data, Aug 2025)
- Google Now On Android architecture sample — established reference for modular Clean Architecture
- MVI pattern for Android voice/chat flows — community-established pattern with multiple published implementations
- Home Assistant REST/WebSocket API — open-source project; integration patterns from training knowledge (verify at implementation time)
- Competitor feature analysis (Google Assistant, Bixby, Alexa, Replika, Pi, Character.AI, Home Assistant Companion) — training knowledge as of Aug 2025

### Tertiary (LOW confidence — verify before use)
- OkHttp, Retrofit, kotlinx-serialization, kotlinx-coroutines exact versions — marked TO_BE_VERIFIED; verify at github.com/square/okhttp, github.com/square/retrofit, github.com/Kotlin/kotlinx.serialization, github.com/Kotlin/kotlinx.coroutines
- ML Kit Prompt API artifact name and version — marked TO_BE_VERIFIED; verify at developers.google.com/ml-kit/genai/prompt/android
- Lottie Compose version — marked TO_BE_VERIFIED; verify at github.com/airbnb/lottie-android
- Android Studio Panda 2 (2025.3.2) — MEDIUM confidence from developer.android.com/studio/releases

---
*Research completed: 2026-03-18*
*Ready for roadmap: yes*
