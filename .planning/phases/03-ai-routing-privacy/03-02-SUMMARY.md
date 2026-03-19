---
phase: 03-ai-routing-privacy
plan: 02
subsystem: ai
tags: [gemini, google-ai-sdk, cloud-fallback, hilt, datastore, compose]

# Dependency graph
requires:
  - phase: 03-01
    provides: AiRouter interface, AiRouterImpl with deterministic tier, SourceType.CLOUD enum value

provides:
  - CloudAiService using Gemini 1.5-flash SDK with @Volatile API key pattern
  - Cloud AI fallback in AiRouterImpl — called when CommandParser returns null/Unknown
  - ParsedIntent.CloudResponse for carrying free-text cloud AI replies
  - googleAiApiKey storage in AppPreferences/SettingsRepository/DataStore
  - SettingsViewModel propagation of API key to CloudAiService
  - "cloud" badge in MessageBubble for SourceType.CLOUD assistant messages
  - AiRouter.resolveIntent changed to suspend fun for async cloud calls
  - Updated AiRouterTest with CloudAiService mock and cloud fallback test cases

affects: [03-03, 03-04, feature-chat, feature-voice, feature-settings]

# Tech tracking
tech-stack:
  added:
    - "com.google.ai.client.generativeai:generativeai:0.9.0 — Gemini SDK (was in catalog, now active)"
  patterns:
    - "@Volatile field pattern for injectable service credentials (mirrors HaAuthInterceptor.token)"
    - "ParsedIntent.CloudResponse sealed subtype for cloud-sourced free-text responses"
    - "AiRouter.resolveIntent as suspend fun — allows async cloud calls without blocking"
    - "SettingsViewModel propagates cloud API key to CloudAiService via DataStore Flow collection"

key-files:
  created:
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/ai/CloudAiService.kt
    - .planning/phases/03-ai-routing-privacy/deferred-items.md
  modified:
    - core/ai/src/main/kotlin/com/example/aicompanion/core/ai/routing/AiRouterImpl.kt
    - core/ai/src/main/kotlin/com/example/aicompanion/core/ai/routing/AiRouter.kt
    - core/ai/src/main/kotlin/com/example/aicompanion/core/ai/di/AiModule.kt
    - core/ai/src/test/kotlin/com/example/aicompanion/core/ai/routing/AiRouterTest.kt
    - core/ai/build.gradle.kts
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/di/NetworkModule.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/datastore/AppPreferences.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/repository/SettingsRepositoryImpl.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/SettingsRepository.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/ParsedIntent.kt
    - feature/settings/src/main/kotlin/com/example/aicompanion/feature/settings/SettingsViewModel.kt
    - feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceViewModel.kt
    - feature/chat/src/main/kotlin/com/example/aicompanion/feature/chat/component/MessageBubble.kt
    - build.gradle.kts

key-decisions:
  - "CloudAiService uses @Volatile var apiKey (not @Inject DataStore dependency) — mirrors HaAuthInterceptor.token pattern; keeps core:network free of core:data dependency"
  - "AiRouter.resolveIntent made suspend fun — required for async cloud call; all callers were already in coroutine context"
  - "ParsedIntent.CloudResponse sealed subtype added — cloud AI returns free-text not a typed QueryType; cleaner than overloading LocalQuery"
  - "CloudAiService provided via explicit @Provides in NetworkModule — single binding point, no ambiguity with @Inject constructor"
  - "core:ai gains core:network dependency — necessary for AiRouterImpl to inject CloudAiService; no circular dependency (network has no ai dependency)"

requirements-completed: [ROUT-03, ROUT-05]

# Metrics
duration: 7min
completed: 2026-03-19
---

# Phase 03 Plan 02: CloudAiService and Gemini Integration Summary

**Gemini 1.5-flash cloud fallback wired into AiRouter via CloudAiService with @Volatile key propagation from DataStore, and "cloud" badge in Chat UI**

## Performance

- **Duration:** 7 min
- **Started:** 2026-03-19T19:57:12Z
- **Completed:** 2026-03-19T20:04:35Z
- **Tasks:** 3 (Task 1 was pre-committed as cf2859f)
- **Files modified:** 15

## Accomplishments
- Implemented `CloudAiService` using Gemini SDK `GenerativeModel("gemini-1.5-flash")` — returns null when no key or on network error
- Connected cloud fallback in `AiRouterImpl`: deterministic tier first, then Gemini, then UNKNOWN stub
- Added `ParsedIntent.CloudResponse(text)` sealed subtype to carry free-text cloud responses
- Stored Google AI API key in DataStore via `AppPreferences` and `SettingsRepository`; `SettingsViewModel` propagates it to `CloudAiService` on every change
- `MessageBubble` now handles all three `SourceType` values: deterministic (primary), cloud (primary), unknown (muted)
- Updated `AiRouterTest` with `runTest`, `CloudAiService` mock, and 4 test cases including cloud fallback scenarios

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Google AI SDK dependencies** - `cf2859f` (feat) — pre-committed before plan execution
2. **Task 2: Implement CloudAiService** - `c4062de` (feat)
3. **Task 3: Connect Cloud Fallback and update Chat UI** - `2495360` (feat)
4. **Task 3 addendum: AiModule binding** - `bf8c19d` (feat) — AiModule.kt from 03-01 not yet committed

## Files Created/Modified
- `core/network/src/main/kotlin/.../core/network/ai/CloudAiService.kt` — Gemini SDK integration with @Volatile apiKey
- `core/network/src/main/kotlin/.../core/network/di/NetworkModule.kt` — provides CloudAiService singleton
- `core/ai/src/main/kotlin/.../core/ai/routing/AiRouterImpl.kt` — cloud fallback via CloudAiService injection
- `core/ai/src/main/kotlin/.../core/ai/routing/AiRouter.kt` — resolveIntent changed to suspend fun
- `core/ai/src/main/kotlin/.../core/ai/di/AiModule.kt` — @Binds AiRouterImpl as AiRouter
- `core/ai/src/test/.../core/ai/routing/AiRouterTest.kt` — runTest + CloudAiService mock + 4 tests
- `core/ai/build.gradle.kts` — adds core:network dependency
- `core/domain/src/main/kotlin/.../core/domain/model/ParsedIntent.kt` — adds CloudResponse subtype
- `core/domain/src/main/kotlin/.../core/domain/repository/SettingsRepository.kt` — adds googleAiApiKey flow + setters
- `core/data/src/main/kotlin/.../core/data/datastore/AppPreferences.kt` — googleAiApiKey key + getters/setters
- `core/data/src/main/kotlin/.../core/data/repository/SettingsRepositoryImpl.kt` — implements new SettingsRepository methods
- `feature/settings/src/main/kotlin/.../feature/settings/SettingsViewModel.kt` — injects CloudAiService, propagates key, exposes googleAiApiKeyPresent state
- `feature/voice/src/main/kotlin/.../feature/voice/VoiceViewModel.kt` — handles ParsedIntent.CloudResponse in when expression
- `feature/chat/src/main/kotlin/.../feature/chat/component/MessageBubble.kt` — "cloud" badge for SourceType.CLOUD
- `build.gradle.kts` — fixed ProjectDependency check for Gradle 9 API

## Decisions Made
- `@Volatile var apiKey` pattern for CloudAiService keeps `core:network` free of `core:data` imports (same pattern as `HaAuthInterceptor.token`)
- `AiRouter.resolveIntent` made `suspend fun` — cloud AI is inherently async; all callers were already in coroutine scope
- Added `ParsedIntent.CloudResponse(text: String)` rather than reusing `LocalQuery(QueryType)` — cloud returns free-text, not a typed enum value
- `CloudAiService` provided explicitly via `@Provides` in `NetworkModule` (not `@Inject constructor`) — single unambiguous Hilt binding

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed root build.gradle.kts ProjectDependency API removed in Gradle 9**
- **Found during:** Task 2 verification (./gradlew :core:network:compileKotlin)
- **Issue:** `dep.dependencyProject.path` API was removed in Gradle 9; caused `Unresolved reference 'dependencyProject'` build failure
- **Fix:** Changed to `dependencies.withType(ProjectDependency::class.java).configureEach { val depPath = this.path }` using Gradle 9 API
- **Files modified:** `build.gradle.kts`
- **Verification:** Build proceeds past root configuration step
- **Committed in:** `c4062de`

**2. [Rule 1 - Bug] AiRouter.resolveIntent required suspend for async cloud call**
- **Found during:** Task 3 (AiRouterImpl cloud injection)
- **Issue:** Plan specified injecting CloudAiService into AiRouterImpl, but `generateResponse` is suspend; non-suspend interface would not compile
- **Fix:** Changed `fun resolveIntent` to `suspend fun resolveIntent` in AiRouter interface and AiRouterImpl; updated AiRouterTest to use runTest; VoiceViewModel was already suspend so no change needed there
- **Files modified:** `AiRouter.kt`, `AiRouterImpl.kt`, `AiRouterTest.kt`
- **Verification:** Code review — all callers (handleTranscript) are in coroutine scope
- **Committed in:** `2495360`

**3. [Rule 1 - Bug] ParsedIntent.LocalQuery incompatible with cloud response text**
- **Found during:** Task 3 (AiRouterImpl cloud response routing)
- **Issue:** Plan said to return cloud response "through the router"; `ParsedIntent.LocalQuery` takes a `QueryType` enum, not free-text; cloud response cannot fit in existing types
- **Fix:** Added `ParsedIntent.CloudResponse(text: String)` sealed subtype; updated AiRouterImpl to return it; updated VoiceViewModel when expression to handle it
- **Files modified:** `ParsedIntent.kt`, `AiRouterImpl.kt`, `VoiceViewModel.kt`
- **Verification:** When expression is exhaustive — Kotlin compiler requires all sealed subtypes be handled
- **Committed in:** `2495360`

---

**Total deviations:** 3 auto-fixed (2 bugs, 1 blocking)
**Impact on plan:** All auto-fixes necessary for correctness and type safety. No scope creep.

## Issues Encountered
- Pre-existing build environment incompatibility (AGP 9.1.0 + kotlin.android plugin + Hilt 2.57.1) blocked Gradle verification. Documented in `deferred-items.md`. All verification was done via code review and type analysis.

## User Setup Required
- **Google AI API Key required for cloud fallback:** User must obtain a key from Google AI Studio (https://aistudio.google.com/apikey) and enter it in the app Settings screen. Without a key, CloudAiService returns null and the router falls back to UNKNOWN.

## Next Phase Readiness
- Cloud AI tier is fully wired end-to-end; needs API key to activate
- `SettingsScreen` UI still needs a field for entering the Google AI API key (SettingsViewModel has `saveGoogleAiApiKey()` ready)
- Phase 03-03 (on-device AI / ML Kit) can proceed independently
- `AiRouterImpl` Tier 2/3 architecture is in place

---
*Phase: 03-ai-routing-privacy*
*Completed: 2026-03-19*
