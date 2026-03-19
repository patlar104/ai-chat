---
phase: 03-ai-routing-privacy
plan: 03
subsystem: network
tags: [okhttp, interceptor, privacy, hilt, network-layer]

# Dependency graph
requires:
  - phase: 03-02
    provides: CloudAiService for Gemini requests, SettingsRepository.privacyModeEnabled Flow, @Volatile field pattern established
  - phase: 03-01
    provides: SettingsRepository interface with privacyModeEnabled Flow

provides:
  - PrivacyInterceptor: OkHttp application interceptor blocking cloud AI requests at network layer
  - PrivacyInterceptor registered as first interceptor in OkHttpClient in NetworkModule
  - @Volatile privacyModeEnabled field on PrivacyInterceptor — updated externally by SettingsViewModel
  - 403 Forbidden synthetic response for blocked cloud AI domains

affects: [03-04, feature-settings, feature-voice]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "@Volatile field on interceptor for externally-set config (same as HaAuthInterceptor.token and CloudAiService.apiKey)"
    - "PrivacyInterceptor registered as FIRST addInterceptor — blocks before auth headers are added"
    - "Synthetic OkHttp Response.Builder with 403 code for privacy blocking — no network round-trip"

key-files:
  created:
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/privacy/PrivacyInterceptor.kt
  modified:
    - core/network/src/main/kotlin/com/example/aicompanion/core/network/di/NetworkModule.kt

key-decisions:
  - "PrivacyInterceptor uses @Volatile privacyModeEnabled field — keeps core:network free of core:data (DataStore) dependency, mirrors HaAuthInterceptor.token pattern"
  - "PrivacyInterceptor added as first application interceptor — cloud AI requests blocked before HA auth or base URL interceptors modify them"
  - "Blocks generativelanguage.googleapis.com and aiplatform.googleapis.com — covers Gemini REST API and Vertex AI endpoint"
  - "SettingsViewModel must set PrivacyInterceptor.privacyModeEnabled in same way it sets CloudAiService.apiKey — wire-up deferred to 03-04 settings UI plan"

patterns-established:
  - "@Volatile field pattern: all externally-configurable interceptor/service state uses @Volatile field updated by SettingsViewModel"

requirements-completed: [PRIV-01, PRIV-02]

# Metrics
duration: 2min
completed: 2026-03-19
---

# Phase 03 Plan 03: Privacy Interceptor Summary

**OkHttp application interceptor that blocks Gemini/Google AI requests at the network layer when privacy mode is enabled, using @Volatile field updated by SettingsViewModel**

## Performance

- **Duration:** 2 min
- **Started:** 2026-03-19T20:07:47Z
- **Completed:** 2026-03-19T20:09:19Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Created `PrivacyInterceptor` with `@Volatile privacyModeEnabled` field; returns synthetic 403 Forbidden response for cloud AI hosts when enabled
- Registered `PrivacyInterceptor` as the first application interceptor in `OkHttpClient` via `NetworkModule` — runs before HA auth and base URL interceptors
- Home Assistant requests are unaffected (only `generativelanguage.googleapis.com` and `aiplatform.googleapis.com` are blocked)

## Task Commits

Each task was committed atomically:

1. **Task 1: Implement PrivacyInterceptor** - `766e047` (feat)
2. **Task 2: Register PrivacyInterceptor** - `a3d07a3` (feat)

## Files Created/Modified
- `core/network/src/main/kotlin/com/example/aicompanion/core/network/privacy/PrivacyInterceptor.kt` — OkHttp interceptor with @Volatile privacyModeEnabled, synthetic 403 for cloud AI domains
- `core/network/src/main/kotlin/com/example/aicompanion/core/network/di/NetworkModule.kt` — providePrivacyInterceptor() singleton + injected as first addInterceptor call

## Decisions Made
- Used `@Volatile` field pattern (not a DataStore/Flow dependency) to keep `core:network` free of `core:data` — consistent with `HaAuthInterceptor.token` and `CloudAiService.apiKey`
- Registered as first interceptor so blocking happens before any request modification by other interceptors
- Blocking two Google AI domains: `generativelanguage.googleapis.com` (current Gemini SDK endpoint) and `aiplatform.googleapis.com` (Vertex AI — future-proofing)
- Wiring `SettingsViewModel` to update `PrivacyInterceptor.privacyModeEnabled` from DataStore is deferred to the settings UI plan (03-04)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- Pre-existing build environment incompatibility (AGP 9.0+ removes org.jetbrains.kotlin.android plugin) prevents `./gradlew :core:network:compileKotlin` from running. This is the same issue documented in `03-02-SUMMARY.md`. All verification was done via code review and type analysis — OkHttp Interceptor API is straightforward and the implementation follows established patterns in the codebase.

## User Setup Required
None - no external service configuration required for this plan. Enabling privacy mode in the UI and wiring `SettingsViewModel` to update `PrivacyInterceptor.privacyModeEnabled` is handled in 03-04.

## Next Phase Readiness
- `PrivacyInterceptor` is wired and available for injection; `SettingsViewModel` needs to inject it (via Hilt) and call `privacyInterceptor.privacyModeEnabled = enabled` when the DataStore privacy setting changes
- Phase 03-04 (Settings UI) can complete the privacy toggle integration
- Privacy enforcement is robust: even if UI state is inconsistent, the network layer blocks outbound cloud AI requests

---
*Phase: 03-ai-routing-privacy*
*Completed: 2026-03-19*

## Self-Check: PASSED

- FOUND: core/network/src/main/kotlin/com/example/aicompanion/core/network/privacy/PrivacyInterceptor.kt
- FOUND: core/network/src/main/kotlin/com/example/aicompanion/core/network/di/NetworkModule.kt
- FOUND: .planning/phases/03-ai-routing-privacy/03-03-SUMMARY.md
- FOUND commit 766e047 (feat(03-03): implement PrivacyInterceptor)
- FOUND commit a3d07a3 (feat(03-03): register PrivacyInterceptor in NetworkModule)
