# Architecture Research

**Domain:** Android AI Companion / Voice Assistant App (Kotlin, Jetpack Compose)
**Researched:** 2026-03-18
**Confidence:** MEDIUM — Architecture patterns drawn from training data (cutoff August 2025). Android modular Clean Architecture is mature and stable; AI routing and avatar integration patterns are less standardized. Web search unavailable; cross-validate key decisions against official Android Architecture docs before build.

## Standard Architecture

### System Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                          UI / Presentation Layer                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │ :feature │  │ :feature │  │ :feature │  │ :feature │            │
│  │  .voice  │  │  .chat   │  │  .avatar │  │ .memory  │            │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘            │
│       │             │             │             │                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                          │
│  │ :feature │  │ :feature │  │ :feature │                          │
│  │.homecontrl│  │  .tasks  │  │.settings │                          │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘                          │
├───────┴─────────────┴─────────────┴──────────────────────────────────┤
│                          Domain / Core Layer                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │ :core.ai │  │:core.aud-│  │:core.auto│  │:core.    │            │
│  │ (router) │  │   io     │  │  mation  │  │ domain   │            │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘            │
├───────┴─────────────┴─────────────┴─────────────┴──────────────────-┤
│                      Infrastructure / Data Layer                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │:core.data│  │:core.net-│  │  Room DB │  │DataStore │            │
│  │(repos)   │  │  work    │  │          │  │          │            │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │
│  ┌───────────────────────────────────────────────────────┐          │
│  │                    :core.ui (shared design system)     │          │
│  └───────────────────────────────────────────────────────┘          │
└──────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| `:app` | App entry point, NavHost, DI graph root, Service declarations | Single Activity + NavController |
| `:feature.voice` | Voice session orchestration, STT trigger, voice state machine | ViewModel (MVI) + VoiceStateFlow |
| `:feature.chat` | Conversation UI, message history display, text fallback input | LazyColumn + ChatViewModel (MVVM) |
| `:feature.avatar` | Avatar animation state driven by voice session state | Composable reacting to VoiceState enum |
| `:feature.homecontrol` | Home Assistant command UI, device/room alias management | ViewModel + HA repository |
| `:feature.memory` | Memory review/delete UI, preference display | Simple CRUD ViewModel over MemoryRepo |
| `:feature.tasks` | Reminder creation, list, completion, notification linkage | ViewModel + TaskRepository + WorkManager |
| `:feature.settings` | Privacy mode toggle, TTS tier selection, API key entry | DataStore-backed ViewModel |
| `:core.ai` | Hybrid AI router: deterministic parser → local AI → cloud AI | Router class with tiered dispatch |
| `:core.audio` | STT service wrapper, TTS engine abstraction (local + cloud) | Android SpeechRecognizer + TTS abstraction |
| `:core.automation` | WorkManager job scheduling, briefing scheduler, reminder dispatch | WorkManager workers + scheduler facade |
| `:core.domain` | Shared domain models, use cases, repository interfaces | Pure Kotlin, no Android deps |
| `:core.data` | Repository implementations, Room DAOs, DataStore helpers | Room + DataStore, implementing :core.domain interfaces |
| `:core.network` | OkHttp/Retrofit clients, Home Assistant API, cloud AI API | Retrofit services, auth interceptors |
| `:core.ui` | Design system: colors, typography, shared Composables, icons | Material 3 tokens + Compose primitives |

---

## Recommended Project Structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml     # Permissions, services, boot receiver
│   └── kotlin/com/yourapp/
│       ├── MainActivity.kt     # Single activity, NavHost
│       └── App.kt              # Application class, Hilt entry point

core/
├── ui/                         # Shared design system
├── domain/                     # Models, use cases, repository interfaces
├── data/                       # Repository implementations, Room, DataStore
├── network/                    # API clients (HA, OpenAI, Gemini, etc.)
├── audio/                      # STT/TTS abstraction layer
├── ai/                         # AI router, deterministic parser, prompt mgmt
└── automation/                 # WorkManager workers, scheduler

feature/
├── voice/                      # Voice session shell (MVI ViewModel)
├── chat/                       # Conversation display (MVVM ViewModel)
├── avatar/                     # Avatar composable + state binding
├── homecontrol/                # HA device control UI
├── memory/                     # Memory review/delete UI
├── tasks/                      # Reminder creation + list UI
└── settings/                   # Privacy mode, TTS tier, API keys

build-logic/
└── convention/                 # Shared Gradle convention plugins
```

### Structure Rationale

- **`core/domain/`:** No Android framework imports — pure Kotlin. Ensures use cases are testable and framework-agnostic.
- **`core/ai/`:** Isolated from UI so the router can be swapped (e.g., replace local model engine) without touching any feature module.
- **`core/audio/`:** Abstraction layer lets TTS/STT backends be replaced without changing voice feature logic.
- **`feature/*/`:** Each feature depends on `core.domain` interfaces, never on sibling features — avoids dependency cycles.
- **`build-logic/`:** Convention plugins keep module-level `build.gradle.kts` files short and consistent across 15 modules.

---

## Architectural Patterns

### Pattern 1: Hybrid AI Router (Tiered Dispatch)

**What:** Commands are routed through three tiers in order: (1) deterministic regex/intent parser for known device commands and simple queries, (2) local on-device model (e.g., Gemma via ML Kit or LiteRT) for medium-complexity reasoning, (3) cloud LLM (OpenAI/Gemini) for open-ended conversation. Privacy mode blocks tier 3.

**When to use:** Always — this is the core intelligence dispatch path for every voice or text command.

**Trade-offs:** Fast and private for common commands. Cloud fallback adds latency and requires network. Deterministic tier must be kept updated as command vocabulary grows.

**Example (Kotlin sketch):**
```kotlin
// core/ai/AiRouter.kt
class AiRouter(
    private val deterministicParser: DeterministicParser,
    private val localModel: LocalAiEngine,
    private val cloudModel: CloudAiEngine,
    private val privacySettings: PrivacySettingsProvider
) {
    suspend fun route(input: ParsedInput): AiResponse {
        deterministicParser.tryParse(input)?.let { return it }
        localModel.tryInfer(input)?.let { return it }
        if (privacySettings.isCloudAllowed()) {
            return cloudModel.infer(input)
        }
        return AiResponse.OfflineFallback
    }
}
```

### Pattern 2: Voice State Machine (MVI in `:feature.voice`)

**What:** Voice session is modeled as a finite state machine with states: `Idle`, `Listening`, `Processing`, `Speaking`, `Error`. State transitions are driven by intents (user button press, STT result, TTS completion, error). Avatar and UI react to state changes via `StateFlow`.

**When to use:** Voice/chat flow. MVI is preferred over MVVM here because the interaction sequence is strictly ordered and needs deterministic replay for debugging.

**Trade-offs:** More boilerplate than MVVM. Benefits: impossible states are unrepresentable; UI and avatar stay in sync by construction.

**Example (Kotlin sketch):**
```kotlin
// feature/voice/VoiceViewModel.kt
sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data class Processing(val transcript: String) : VoiceState
    data class Speaking(val replyText: String) : VoiceState
    data class Error(val reason: String) : VoiceState
}

sealed interface VoiceIntent {
    data object TalkButtonPressed : VoiceIntent
    data class TranscriptReceived(val text: String) : VoiceIntent
    data object TtsDone : VoiceIntent
    data class SttError(val msg: String) : VoiceIntent
}
```

### Pattern 3: Tool Contract Layer (Typed Tool Execution)

**What:** All side-effecting actions (home control, reminders, memory writes) are expressed as typed `Tool` objects dispatched through a `ToolExecutor`. The AI router produces a `ToolCall` value; the executor maps it to the correct use case. No raw string parsing occurs outside `:core.ai`.

**When to use:** Every action the AI takes that mutates state or calls an external system.

**Trade-offs:** Adds an indirection layer but makes tool execution auditable, testable in isolation, and easily extensible (add a new tool without touching the router).

**Example (Kotlin sketch):**
```kotlin
// core/domain/tool/ToolCall.kt
sealed interface ToolCall {
    data class TurnOnDevice(val alias: String) : ToolCall
    data class SetReminder(val text: String, val at: Instant) : ToolCall
    data class SaveMemory(val key: String, val value: String) : ToolCall
    data class RunRoutine(val routineName: String) : ToolCall
}
```

### Pattern 4: Repository + Use Case (Clean Architecture Domain Layer)

**What:** Feature ViewModels call use cases (single-responsibility classes in `:core.domain`). Use cases call repository interfaces. Repository implementations in `:core.data` provide Room/network data. Feature modules never import `:core.data` directly — only `:core.domain`.

**When to use:** All data access. Enforced via Gradle module dependency rules.

**Trade-offs:** For simple CRUD screens this feels like extra layers. The payoff is that data sources (Room, HA API, cloud) can be replaced without touching features.

---

## Data Flow

### Voice Command End-to-End Flow

```
[User presses Talk button]
        |
        v
:feature.voice ViewModel
  emits VoiceIntent.TalkButtonPressed
  state -> Listening
        |
        v
:core.audio / SttService
  captures microphone input via Android SpeechRecognizer
  returns transcript string
        |
        v
:feature.voice ViewModel
  emits VoiceIntent.TranscriptReceived(text)
  state -> Processing
        |
        v
:core.ai / AiRouter
  tier 1: DeterministicParser  -- known device/reminder command? -> ToolCall
  tier 2: LocalAiEngine         -- medium query?                  -> ToolCall or text
  tier 3: CloudAiEngine         -- open question?                 -> text response
        |
        v
:core.ai / ToolExecutor         (if ToolCall produced)
  dispatches to HomeControlRepo / TaskRepo / MemoryRepo
        |
        v
:feature.voice ViewModel
  state -> Speaking(replyText)
        |
        v
:core.audio / TtsService
  tier 1: Android TTS (local)   -- utility confirmations
  tier 2: Cloud neural TTS      -- companion conversation mode
        |
        v
:feature.voice ViewModel
  TtsDone -> state -> Idle

[Avatar composable] reacts to VoiceState throughout
```

### Background Briefing Flow

```
[Scheduled trigger via WorkManager alarm]
        |
        v
:core.automation / BriefingWorker
  reads MemoryRepo (preferences, calendar facts)
  calls AiRouter (generates briefing text)
        |
        v
:core.automation / NotificationDispatcher
  posts foreground-eligible notification with briefing text
  optionally triggers TTS autoplay if user is active
```

### Memory Promotion Flow

```
[AI response or user statement contains notable fact]
        |
        v
:core.ai / MemoryExtractor
  identifies candidate memory (preference, alias, fact)
        |
        v
:core.domain / PromoteMemoryUseCase
  evaluates promotion policy (auto vs. explicit user confirmation)
        |
        v
:core.data / MemoryRepository -> Room DB
        |
        v
:feature.memory UI
  reflects updated memory list via StateFlow
```

### State Management (per-feature)

```
User action -> ViewModel.onIntent(intent)
                    |
                    v
               Use Case(s)
                    |
                    v
               Repository
                    |
                    v
           Room / DataStore / Network
                    |
                    v (Flow/suspend)
               ViewModel._uiState (StateFlow)
                    |
                    v (collectAsStateWithLifecycle)
               Composable recomposition
```

---

## Module Dependency Graph and Build Order

This is the most critical architectural constraint. Gradle modules must form a DAG (no cycles). The dependency direction is strictly: `feature -> core.domain <- core.data <- core.network`.

```
Build order (bottom-up):

Level 0 (no deps):
  :core.ui
  :core.domain

Level 1 (depends on :core.domain only):
  :core.data           (implements domain interfaces, adds Room + DataStore)
  :core.network        (implements remote data sources, used by :core.data)
  :core.audio          (STT/TTS abstractions, depends on :core.domain for models)
  :core.ai             (router logic, depends on :core.domain models)
  :core.automation     (WorkManager workers, depends on :core.domain use cases)

Level 2 (depends on :core.* modules):
  :feature.settings    (first — establishes DataStore preferences needed by all features)
  :feature.voice       (depends on :core.audio, :core.ai)
  :feature.chat        (depends on :core.ai, :core.data)
  :feature.homecontrol (depends on :core.network, :core.domain)
  :feature.memory      (depends on :core.data)
  :feature.tasks       (depends on :core.data, :core.automation)
  :feature.avatar      (depends on :core.ui, :feature.voice state — via shared StateFlow)

Level 3 (assembles everything):
  :app                 (depends on all :feature.* modules, NavHost)
```

**Recommended phase build order for roadmap:**

| Phase | Modules Built | Rationale |
|-------|--------------|-----------|
| 1 | `:core.domain`, `:core.ui`, `:core.data` (Room schema only) | Foundation — all other modules depend on these |
| 2 | `:core.audio`, `:feature.voice` (basic STT→text) | Voice shell is the primary interaction model; validate early |
| 3 | `:core.ai` (deterministic parser), `:core.network` (HA), `:feature.homecontrol` | Core daily value — home control via voice |
| 4 | `:core.ai` (local + cloud router), `:feature.chat` | Full AI routing; text conversation as fallback/parallel |
| 5 | `:feature.avatar`, `:feature.settings` (privacy mode) | Polish + privacy; avatar depends on voice state machine being stable |
| 6 | `:feature.memory`, `:feature.tasks`, `:core.automation` | Persistence layer, reminders, background ops |
| 7 | Integration, offline mode hardening, WorkManager briefings | Harden all offline paths; full background ops |

---

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| Home Assistant | REST API + WebSocket (for state subscriptions) via `:core.network` | Long-poll or SSE for device state push; auth via long-lived token in DataStore |
| Android SpeechRecognizer | System service, no key required | Free; requires RECORD_AUDIO permission; limited offline on some devices |
| Android TTS | System service via `TextToSpeech` class | Free; quality varies by device; no network needed |
| Cloud LLM (OpenAI/Gemini) | REST via `:core.network` Retrofit client | API key in DataStore; privacy mode must gate all calls |
| Cloud neural TTS (ElevenLabs / Google WaveNet) | REST via `:core.network` | Streaming preferred to reduce perceived latency; cache common phrases |
| WorkManager | Android Jetpack — scheduling for briefings and reminders | Survives reboots; requires RECEIVE_BOOT_COMPLETED permission |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| `:feature.voice` <-> `:core.audio` | Direct dependency; ViewModel calls audio use cases | Audio must not callback into feature UI directly — use Flow |
| `:feature.voice` <-> `:feature.avatar` | Shared `VoiceState` exposed as `StateFlow` from `:feature.voice` ViewModel, collected in `:feature.avatar` composable | Avatar must not depend on voice module directly — pass state via shared ViewModel scoped to NavGraph |
| `:core.ai` <-> `:core.domain` | Router calls domain use cases; produces typed `ToolCall` values | No String-based dispatch; all tool calls must be sealed types |
| `:feature.*` <-> `:core.data` | Features must NOT import `:core.data` directly — only `:core.domain` interfaces | Enforced via Gradle `api` vs `implementation` scoping |
| `:core.automation` <-> `:feature.*` | WorkManager workers use `:core.domain` use cases only; no direct feature module import | Background workers are domain-layer consumers, not feature consumers |

---

## Anti-Patterns

### Anti-Pattern 1: Business Logic in Composables

**What people do:** Put AI dispatch calls, Room queries, or TTS calls directly inside `@Composable` functions using `LaunchedEffect`.

**Why it's wrong:** Composables recompose frequently and unpredictably. Side effects become unreliable; state is untrackable; tests are painful.

**Do this instead:** All side effects live in ViewModels. Composables only observe `StateFlow` and emit `Intent` values upward. `LaunchedEffect` is acceptable only for one-shot UI effects like showing a Snackbar.

### Anti-Pattern 2: Feature Modules Depending on Sibling Features

**What people do:** `:feature.chat` imports `:feature.voice` to access the transcript, or `:feature.avatar` imports `:feature.voice` directly.

**Why it's wrong:** Creates circular or entangled dependency graphs that break modular builds and make feature isolation impossible.

**Do this instead:** Shared state (e.g., current voice session state) is owned in `:core.domain` as a shared repository/StateHolder, or passed down through the navigation graph via a shared ViewModel scoped to the parent NavGraph.

### Anti-Pattern 3: Network Calls from UI Modules

**What people do:** Calling Home Assistant API or OpenAI directly from a `:feature.*` ViewModel using a client defined in that feature.

**Why it's wrong:** Auth logic, retry policies, and base URLs become scattered. Privacy mode (block cloud) is impossible to enforce centrally.

**Do this instead:** All network clients live in `:core.network`. Features call use cases in `:core.domain`. Use cases call repositories. Repositories call network sources. Privacy gate lives in `:core.ai` router, not scattered across features.

### Anti-Pattern 4: Monolithic Voice ViewModel

**What people do:** One giant ViewModel handles STT, AI routing, TTS, avatar animation, home control execution, and memory writes.

**Why it's wrong:** Impossible to test individual behaviors; any change risks breaking unrelated flows; avatar and home control coupling makes voice feel slow when either is sluggish.

**Do this instead:** Voice ViewModel orchestrates a state machine only. It delegates to use cases from `:core.audio`, `:core.ai`, `:core.domain`. Each use case is independently testable. Avatar observes state passively.

### Anti-Pattern 5: Always-On Hotword Detection (Scope Creep)

**What people do:** Add hotword detection early because it seems like a fundamental feature.

**Why it's wrong:** Battery and memory impact are significant. Android background audio capture restrictions (introduced in Android 11+) make reliable hotword detection complex. This is explicitly out of scope for v1 in the project spec.

**Do this instead:** Button-to-talk is the validated interaction model for v1. Hotword can be added in a later phase behind a feature flag once battery impact is measured.

---

## Scaling Considerations

This is a personal-use single-user Android app. Traditional server-scaling concerns don't apply. Scaling concerns are device-resource-oriented.

| Concern | Approach |
|---------|----------|
| Cold start latency | Defer module initialization (lazy Hilt components); avoid Room DB init on main thread; don't load avatar assets at startup |
| Voice response latency | Deterministic parser must return < 50ms; local AI < 500ms; cloud AI < 2s on good network; TTS start < 200ms perceived |
| Memory pressure | Avatar animation assets should be vector/Lottie (not bitmap sequences); local AI model must fit in available RAM with headroom for other modules |
| Background battery | WorkManager constraints (network available, battery not low) on briefing workers; no foreground services running when idle |
| Room DB growth | Memory table needs periodic pruning policy; conversation history needs configurable retention (e.g., 90 days) |
| Offline degradation | Offline mode is a first-class requirement: deterministic parser + local TTS + Room data must all function without network from day one |

---

## Sources

- Android official modular architecture guidance (developer.android.com/topic/modularization) — MEDIUM confidence (training data, August 2025)
- Google Now On Android architecture sample — MEDIUM confidence (established reference pattern)
- Android Architecture Components (Hilt, Room, WorkManager, Navigation Compose) documentation — HIGH confidence (stable APIs, well-documented)
- MVI pattern for Android voice/chat flows — MEDIUM confidence (community established pattern, multiple published implementations)
- Clean Architecture on Android (Robert C. Martin principles adapted for Android) — HIGH confidence (stable, widely adopted)
- Home Assistant REST/WebSocket API integration patterns — MEDIUM confidence (training data; verify HA API version compatibility at implementation time)

---
*Architecture research for: Android AI Companion App (Kotlin + Jetpack Compose)*
*Researched: 2026-03-18*
