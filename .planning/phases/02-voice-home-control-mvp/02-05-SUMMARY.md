---
phase: 02-voice-home-control-mvp
plan: 05
subsystem: api
tags: [retrofit, okhttp, home-assistant, interceptor, command-parser, regex, hilt, kotlinx-serialization]

# Dependency graph
requires:
  - phase: 02-01
    provides: HomeAssistantRepository interface, ParsedIntent sealed class, AppError types, domain models
provides:
  - Retrofit HA REST API service with POST /api/services/{domain}/{service}
  - HaAuthInterceptor with @Volatile token field (no runBlocking)
  - BaseUrlInterceptor with @Volatile baseUrl for dynamic user-configurable HA URL
  - HomeAssistantRepositoryImpl mapping 401/404 to AppError.HomeAssistant, IOException to AppError.Network
  - NetworkModule DI (abstract class with @Binds + companion @Provides)
  - CommandParser with 8 regex patterns covering all Phase 2 voice intent categories
  - parseTime supporting relative ("in 30 minutes") and absolute ("8 PM") time expressions
affects:
  - feature/voice (VoiceViewModel uses CommandParser and HomeAssistantRepository)
  - core/automation (reminder scheduling uses CreateReminder intent)
  - 02-06 (settings flow populates HaAuthInterceptor.token and BaseUrlInterceptor.baseUrl)

# Tech tracking
tech-stack:
  added:
    - retrofit 3.0.0 (Retrofit HTTP client)
    - converter-kotlinx-serialization (Retrofit JSON converter)
    - okhttp 5.0.0 (HTTP engine with interceptor chain)
    - okhttp logging-interceptor (debug logging)
    - kotlinx-serialization-json (JSON codec)
  patterns:
    - Volatile-field interceptors updated from DataStore emissions without runBlocking
    - Abstract class NetworkModule for @Binds + companion object for @Provides
    - Regex-first deterministic parser with sealed class return types
    - Brightness scaling: UI% (0-100) to HA value (0-255) at parse time
    - TURN_OFF matched before TURN_ON to prevent false positive greedy matches

key-files:
  created:
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/HomeAssistantService.kt
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/HaServiceRequest.kt
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/HaStateResponse.kt
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/HaAuthInterceptor.kt
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/BaseUrlInterceptor.kt
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/repository/HomeAssistantRepositoryImpl.kt
    - core/ai/src/main/kotlin/com/example/aicompanion/core/ai/parser/CommandParser.kt
  modified:
    - core/network/build.gradle.kts
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/di/NetworkModule.kt
    - core/ai/build.gradle.kts
    - core/ai/src/main/kotlin/com/example/aicompanion/core/ai/di/AiModule.kt
    - gradle/libs.versions.toml

key-decisions:
  - "@Volatile fields on interceptors — updated from DataStore/settings Flow without blocking OkHttp thread"
  - "Placeholder baseUrl(http://localhost/) in Retrofit — BaseUrlInterceptor overrides at call time, avoids Retrofit URL validation"
  - "TURN_OFF checked before TURN_ON — prevents greedy trailing-on match false positives"
  - "NetworkModule changed from object to abstract class — required for Hilt @Binds support"
  - "Brightness converted from % (0-100) to HA int (0-255) at parse time, not at HA call time"

patterns-established:
  - "Pattern: Volatile-field interceptors — token and baseUrl set asynchronously from Flow collectors"
  - "Pattern: Result<Unit> return type — callers use onSuccess/onFailure without checked exceptions"
  - "Pattern: Regex priority ordering — most-specific patterns first, Unknown as final fallback"

requirements-completed: [HOME-01, HOME-02, HOME-05, HOME-07, CONV-04]

# Metrics
duration: 15min
completed: 2026-03-19
---

# Phase 02 Plan 05: HA REST API + Command Parser Summary

**Retrofit HA REST API layer with volatile-field OkHttp interceptors and regex-based CommandParser covering 8 voice intent categories**

## Performance

- **Duration:** 15 min
- **Started:** 2026-03-19T09:05:00Z
- **Completed:** 2026-03-19T09:20:00Z
- **Tasks:** 2
- **Files modified:** 14

## Accomplishments
- Retrofit `HomeAssistantService` calls `POST /api/services/{domain}/{service}` with `@Serializable` JSON body
- `HaAuthInterceptor` and `BaseUrlInterceptor` use `@Volatile` fields updated from DataStore, no `runBlocking` on OkHttp thread
- `HomeAssistantRepositoryImpl` maps HTTP 401/404 to `AppError.HomeAssistant`, `IOException` to `AppError.Network`
- `CommandParser` handles all 8 Phase 2 intent categories: turn on/off, brightness, temperature, routines, reminders, time query, reminders query, unknown
- Time parser handles both relative ("in 30 minutes") and absolute ("8 PM", "8:30 AM") time expressions, scheduling next-day if past

## Task Commits

Each task was committed atomically:

1. **Task 1: HA REST API layer with Retrofit, interceptors, and repository** - `60dfdd5` (feat)
2. **Task 2: Deterministic CommandParser with regex patterns** - `60dfdd5` (feat)

**Plan metadata:** `b25423b` (docs: complete HA REST API and CommandParser plan)

## Files Created/Modified
- `core/network/src/main/kotlin/.../ha/HomeAssistantService.kt` - Retrofit interface: `@POST("api/services/{domain}/{service}")`
- `core/network/src/main/kotlin/.../ha/HaServiceRequest.kt` - `@Serializable` with entity_id, brightness, temperature
- `core/network/src/main/kotlin/.../ha/HaStateResponse.kt` - `@Serializable` response model
- `core/network/src/main/kotlin/.../ha/HaAuthInterceptor.kt` - `@Volatile var token: String?`, no runBlocking
- `core/network/src/main/kotlin/.../ha/BaseUrlInterceptor.kt` - `@Volatile var baseUrl: HttpUrl?` for dynamic URL
- `core/network/src/main/kotlin/.../repository/HomeAssistantRepositoryImpl.kt` - HTTP error mapping to AppError types
- `core/network/src/main/kotlin/.../di/NetworkModule.kt` - abstract class with @Binds + companion @Provides
- `core/ai/src/main/kotlin/.../parser/CommandParser.kt` - 8 regex patterns, parseTime, `@Singleton @Inject`
- `core/ai/build.gradle.kts` - Added :core:domain dependency
- `gradle/libs.versions.toml` - Added retrofit-kotlinx-serialization entry
- Deleted `CoreNetwork.kt` and `CoreAi.kt` placeholders

## Decisions Made
- `@Volatile` interceptor fields allow async updates from DataStore without blocking the OkHttp thread
- Placeholder `baseUrl("http://localhost/")` avoids Retrofit URL validation; `BaseUrlInterceptor` overrides at call time
- `TURN_OFF` matched before `TURN_ON` to prevent "turn off the lights" from matching the trailing ` on` group
- `NetworkModule` changed from `object` to `abstract class` — required for Hilt `@Binds` support
- Brightness converted from UI percentage (0-100) to HA int (0-255) inside `CommandParser`, not at call site

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- HA network layer ready for settings flow (Plan 06) to populate `HaAuthInterceptor.token` and `BaseUrlInterceptor.baseUrl`
- `CommandParser` ready for `VoiceViewModel` to call `parse(transcript)` and route intents
- Both classes injectable via Hilt `@Singleton`

---
*Phase: 02-voice-home-control-mvp*
*Completed: 2026-03-19*

## Self-Check: PASSED

Key files verified on disk:
- core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/HomeAssistantService.kt - EXISTS
- core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/HaAuthInterceptor.kt - EXISTS
- core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/BaseUrlInterceptor.kt - EXISTS
- core/network/src/main/kotlin/com/example/aicompanion/core/network/repository/HomeAssistantRepositoryImpl.kt - EXISTS
- core/ai/src/main/kotlin/com/example/aicompanion/core/ai/parser/CommandParser.kt - EXISTS
- core/network/src/main/kotlin/com/example/aicompanion/core/network/di/NetworkModule.kt - EXISTS

Key commits verified:
- 60dfdd5 feat(02-05): create CommandParser with regex patterns and update AiModule - EXISTS
- b25423b docs(02-05): complete HA REST API and CommandParser plan - EXISTS
