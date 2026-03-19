---
phase: 03-ai-routing-privacy
plan: 04
subsystem: ui, routing
tags: [degraded-mode, privacy-mode, offline, ui-indicators, connectivity, voice-screen]

# Dependency graph
requires:
  - phase: 03-03
    provides: PrivacyInterceptor with @Volatile privacyModeEnabled field
  - phase: 03-02
    provides: CloudAiService with @Volatile apiKey field
  - phase: 03-01
    provides: SettingsRepository.privacyModeEnabled Flow

provides:
  - AiRouterImpl: degraded mode CloudResponse with contextual message for Privacy Mode
  - AiRouterImpl: degraded mode CloudResponse with contextual message for Offline mode
  - SettingsViewModel: wires PrivacyInterceptor.privacyModeEnabled from DataStore Flow
  - VoiceViewModel: exposes privacyModeEnabled StateFlow from SettingsRepository
  - VoiceViewModel: exposes isOnline StateFlow via ConnectivityManager NetworkCallback
  - VoiceScreen: Privacy Mode visual indicator (VisibilityOff icon + label) at top
  - VoiceScreen: Offline visual indicator (WifiOff icon + label) at top

affects: [feature-voice, feature-settings, core-ai]

# Tech tracking
tech-stack:
  added:
    - material-icons-extended in feature/voice (VisibilityOff, WifiOff icons)
    - android.net.ConnectivityManager NetworkCallback for live connectivity tracking
  patterns:
    - "ConnectivityManager.NetworkCallback registered in ViewModel.init, unregistered in onCleared — no leaks"
    - "ConnectivityManager declared before _isOnline MutableStateFlow — field init order matters when one uses the other"
    - "PrivacyInterceptor.privacyModeEnabled check before cloud call in AiRouterImpl — fast-path avoids unnecessary API invocation"
    - "CloudAiService.apiKey null-check before generateResponse() — distinguishes 'not configured' (ParsedIntent.Unknown) from 'offline' (degraded message)"

key-files:
  created: []
  modified:
    - core/ai/src/main/kotlin/com/example/aicompanion/core/ai/routing/AiRouterImpl.kt
    - core/ai/src/test/kotlin/com/example/aicompanion/core/ai/routing/AiRouterTest.kt
    - feature/settings/src/main/kotlin/com/example/aicompanion/feature/settings/SettingsViewModel.kt
    - feature/voice/build.gradle.kts
    - feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceScreen.kt
    - feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceViewModel.kt

key-decisions:
  - "AiRouterImpl checks privacyInterceptor.privacyModeEnabled before cloud call — returns specific Privacy Mode message (not Unknown) so VoiceViewModel speaks it via TTS"
  - "Offline vs no-key distinction: apiKey == null returns ParsedIntent.Unknown (not configured); apiKey set + cloud returns null returns degraded Offline message"
  - "ConnectivityManager field declared before _isOnline in VoiceViewModel — Kotlin field init order; isCurrentlyOnline() uses connectivityManager"
  - "material-icons-extended added to feature/voice — VisibilityOff (privacy) and WifiOff (offline) icons; consistent with feature/settings pattern"
  - "Status indicators shown in error color at top of VoiceScreen — visible but non-blocking; both can appear simultaneously"

# Metrics
duration: 4m
completed: 2026-03-19
---

# Phase 03 Plan 04: Degraded Mode Feedback and UI Indicators Summary

**Degraded mode spoken responses for Privacy Mode and Offline mode, plus VisibilityOff/WifiOff visual indicators in VoiceScreen top bar**

## Performance

- **Duration:** 4 min
- **Started:** 2026-03-19T20:12:21Z
- **Completed:** 2026-03-19
- **Tasks:** 2 (+ 1 auto-approved checkpoint)
- **Files modified:** 6

## Accomplishments

- `AiRouterImpl` now detects `privacyInterceptor.privacyModeEnabled = true` before calling cloud and returns `ParsedIntent.CloudResponse("I can't answer that right now because Privacy Mode is active. I can still control your home and set reminders.")` — spoken via TTS automatically
- When API key is set but cloud returns null (network failure), `AiRouterImpl` returns the Offline degraded message: `"I can't answer that right now because Offline mode is active. I can still control your home and set reminders."`
- No-API-key case still returns `ParsedIntent.Unknown` (cloud not configured, not a degraded mode scenario)
- `SettingsViewModel` now wires `PrivacyInterceptor.privacyModeEnabled` from DataStore — completing deferred work from 03-03; the `@Volatile` flag on `PrivacyInterceptor` is now kept in sync with the DataStore value
- `VoiceViewModel` exposes `privacyModeEnabled: StateFlow<Boolean>` (from `SettingsRepository`) and `isOnline: StateFlow<Boolean>` (from `ConnectivityManager.NetworkCallback`)
- `VoiceScreen` shows status indicator row at the top when Privacy Mode or Offline mode is active — icons in error color with text labels; indicators are conditionally rendered and can appear simultaneously
- Deterministic commands (home control, reminders) pass through AiRouter unchanged regardless of privacy or offline state — requirement confirmed by test

## Task Commits

Each task was committed atomically:

1. **Task 1: Implement Degraded Mode Feedback** - `841a7c9` (feat)
2. **Task 2: Add UI Status Indicators** - `d995bee` (feat)

## Files Created/Modified

- `core/ai/src/main/kotlin/com/example/aicompanion/core/ai/routing/AiRouterImpl.kt` — added PrivacyInterceptor injection; privacy-blocked and offline degraded responses
- `core/ai/src/test/kotlin/com/example/aicompanion/core/ai/routing/AiRouterTest.kt` — updated for new PrivacyInterceptor param; added 3 new tests (privacy block, offline, deterministic pass-through in privacy mode)
- `feature/settings/src/main/kotlin/com/example/aicompanion/feature/settings/SettingsViewModel.kt` — inject PrivacyInterceptor; collect privacyModeEnabled Flow and update @Volatile field
- `feature/voice/build.gradle.kts` — added material-icons-extended dependency
- `feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceScreen.kt` — added status indicator Row with VisibilityOff + WifiOff icons; reads privacyModeEnabled and isOnline from ViewModel
- `feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceViewModel.kt` — added @ApplicationContext context; ConnectivityManager NetworkCallback for isOnline; privacyModeEnabled StateFlow from SettingsRepository

## Decisions Made

- `AiRouterImpl` checks `privacyInterceptor.privacyModeEnabled` before calling cloud (fast-path, no network round-trip required)
- Distinguished three cloud-unavailable cases: (1) privacy blocked → Privacy Mode message, (2) no API key → ParsedIntent.Unknown (silent), (3) API key set + request failed → Offline message
- `ConnectivityManager` field declared before `_isOnline` in `VoiceViewModel` — Kotlin initializes fields top-to-bottom; `isCurrentlyOnline()` reads `connectivityManager`
- Status indicators shown in `MaterialTheme.colorScheme.error` tint/color — visually distinct without custom theming

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed field initialization order in VoiceViewModel**
- **Found during:** Task 2 implementation
- **Issue:** `_isOnline = MutableStateFlow(isCurrentlyOnline())` was declared before `connectivityManager`, causing a NullPointerException at construction time since `isCurrentlyOnline()` uses `connectivityManager`
- **Fix:** Moved `connectivityManager` field declaration above `_isOnline`
- **Files modified:** `feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceViewModel.kt`
- **Commit:** `d995bee`

**2. [Rule 2 - Missing critical functionality] Wired PrivacyInterceptor in SettingsViewModel**
- **Found during:** Task 1 — 03-03 deferred this wiring to 03-04, but it was missing from the explicit plan task description
- **Fix:** Added `privacyInterceptor` constructor injection and `settingsRepository.privacyModeEnabled.collect` in `SettingsViewModel.init`
- **Files modified:** `feature/settings/src/main/kotlin/com/example/aicompanion/feature/settings/SettingsViewModel.kt`
- **Commit:** `841a7c9`

## Issues Encountered

- Pre-existing build environment incompatibility (AGP 9.0+ removes `org.jetbrains.kotlin.android` plugin) prevents Gradle from running. This is the same issue documented in `03-02-SUMMARY.md` and `03-03-SUMMARY.md`. Verification done via code review and type analysis.

## User Setup Required

None for this plan — the privacy and offline indicators appear automatically based on DataStore settings and system connectivity.

## Next Phase Readiness

Phase 03 is now complete:
- Hybrid AI routing (deterministic → cloud → degraded) is fully implemented
- Privacy mode blocks cloud AI at the network layer (PrivacyInterceptor) and at the routing layer (AiRouterImpl check)
- UI clearly communicates Privacy Mode and Offline state in VoiceScreen
- Deterministic commands (home control, reminders) work in all modes

---
*Phase: 03-ai-routing-privacy*
*Completed: 2026-03-19*

## Self-Check: PASSED

- FOUND: core/ai/src/main/kotlin/com/example/aicompanion/core/ai/routing/AiRouterImpl.kt
- FOUND: core/ai/src/test/kotlin/com/example/aicompanion/core/ai/routing/AiRouterTest.kt
- FOUND: feature/settings/src/main/kotlin/com/example/aicompanion/feature/settings/SettingsViewModel.kt
- FOUND: feature/voice/build.gradle.kts
- FOUND: feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceScreen.kt
- FOUND: feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceViewModel.kt
- FOUND: .planning/phases/03-ai-routing-privacy/03-04-SUMMARY.md
- FOUND commit 841a7c9 (feat(03-04): implement degraded mode feedback)
- FOUND commit d995bee (feat(03-04): add Privacy Mode and Offline status indicators)
