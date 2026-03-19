---
phase: 02-voice-home-control-mvp
plan: 01
subsystem: domain
tags: [kotlin, sealed-interface, repository-pattern, hilt, coroutines, flow]

# Dependency graph
requires:
  - phase: 01-project-foundation
    provides: AppError sealed class, Logger interface, Hilt/Dagger setup, coroutines-core dependency

provides:
  - VoiceState sealed interface (6 states) with VoiceEvent and VoiceUiEffect
  - ParsedIntent sealed interface (5 variants) with HomeAction and QueryType enums
  - Message, AuditLogEntry, Alias, Reminder domain data classes with supporting enums
  - MessageRepository, AuditLogRepository, AliasRepository, ReminderRepository interfaces
  - HomeAssistantRepository and SettingsRepository interfaces
  - @MainDispatcher and @IoDispatcher qualifier annotations
  - DispatcherModule providing testable coroutine dispatchers via Hilt

affects: [02-02, 02-03, 02-04, 02-05, 02-06, 02-07, 02-08, 02-09, 02-10, data-layer, audio-layer, network-layer, feature-viewmodels]

# Tech tracking
tech-stack:
  added: []
  patterns: [pure-kotlin-domain, repository-pattern, sealed-interface-state-machine, hilt-qualifier-injection]

key-files:
  created:
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/VoiceState.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/ParsedIntent.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/Message.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/AuditLogEntry.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/Alias.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/Reminder.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/repository/MessageRepository.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/repository/AuditLogRepository.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/repository/AliasRepository.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/repository/ReminderRepository.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/repository/HomeAssistantRepository.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/repository/SettingsRepository.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/di/MainDispatcher.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/di/IoDispatcher.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/di/DispatcherModule.kt
  modified: []

key-decisions:
  - "Domain models are pure Kotlin — zero android.* imports ensure testability without Android SDK"
  - "Repository interfaces use kotlinx.coroutines.flow.Flow for reactive streams and suspend for one-shot operations"
  - "Dispatcher qualifiers use javax.inject.Qualifier with AnnotationRetention.BINARY for Hilt injection"
  - "HomeAssistantRepository returns kotlin.Result<Unit> — callers handle success/failure without checked exceptions"
  - "SettingsRepository exposes all settings as Flow<T> properties for reactive UI binding"

patterns-established:
  - "Pattern 1: All domain layer files use package com.example.aicompanion.core.domain.* with zero android.* imports"
  - "Pattern 2: Sealed interfaces for state machines — when(state) is compiler-exhaustive-checked"
  - "Pattern 3: Repository interfaces define contracts, implementations live in :core:data module"
  - "Pattern 4: @MainDispatcher / @IoDispatcher qualifiers injected via constructor — never hardcoded Dispatchers.* in ViewModels"

requirements-completed: [VOIC-02, CONV-03, CONV-04, HOME-03, HOME-06, TASK-01]

# Metrics
duration: 2min
completed: 2026-03-19
---

# Phase 2 Plan 01: Domain Models and Repository Contracts Summary

**Pure Kotlin domain layer with sealed interface state machine (VoiceState), deterministic parser output (ParsedIntent), 6 repository interface contracts, and testable Hilt dispatcher qualifiers — zero android.* imports**

## Performance

- **Duration:** ~2 min
- **Started:** 2026-03-19T00:16:37Z
- **Completed:** 2026-03-19T00:18:13Z
- **Tasks:** 2
- **Files modified:** 15 created, 1 deleted (CoreDomain.kt placeholder)

## Accomplishments
- VoiceState sealed interface with 6 states (Idle, Listening, Transcribing, Processing, Speaking, Error) plus VoiceEvent and VoiceUiEffect
- ParsedIntent sealed interface with 5 variants covering all deterministic parser categories (HomeControl, Routine, CreateReminder, LocalQuery, Unknown)
- 6 repository interfaces defining the full data/network/settings access contract for all Phase 2 downstream modules
- @MainDispatcher and @IoDispatcher qualifier annotations with DispatcherModule enabling testable coroutine injection

## Task Commits

Each task was committed atomically:

1. **Task 1: Create domain models and sealed interfaces** - `772f3a8` (feat)
2. **Task 2: Create repository interfaces and dispatcher qualifiers** - `08c4c95` (feat)

**Plan metadata:** (docs commit below)

## Files Created/Modified
- `core/domain/.../model/VoiceState.kt` - VoiceState (6 states), VoiceEvent, VoiceUiEffect sealed interfaces
- `core/domain/.../model/ParsedIntent.kt` - ParsedIntent sealed interface, HomeAction enum, QueryType enum
- `core/domain/.../model/Message.kt` - Message data class, MessageRole enum, SourceType enum
- `core/domain/.../model/AuditLogEntry.kt` - AuditLogEntry data class, AuditStatus enum
- `core/domain/.../model/Alias.kt` - Alias data class for device/room aliases
- `core/domain/.../model/Reminder.kt` - Reminder data class, ReminderStatus enum
- `core/domain/.../repository/MessageRepository.kt` - Conversation persistence contract
- `core/domain/.../repository/AuditLogRepository.kt` - HA command audit log contract
- `core/domain/.../repository/AliasRepository.kt` - Device alias CRUD contract
- `core/domain/.../repository/ReminderRepository.kt` - Reminder management contract
- `core/domain/.../repository/HomeAssistantRepository.kt` - HA service call contract returning Result<Unit>
- `core/domain/.../repository/SettingsRepository.kt` - Settings Flows and suspend setters contract
- `core/domain/.../di/MainDispatcher.kt` - @MainDispatcher Hilt qualifier annotation
- `core/domain/.../di/IoDispatcher.kt` - @IoDispatcher Hilt qualifier annotation
- `core/domain/.../di/DispatcherModule.kt` - Hilt module providing both dispatchers

## Decisions Made
- Domain models are pure Kotlin with zero android.* imports — ensures the domain layer can be unit-tested without Android SDK
- Repository interfaces use `Flow<T>` for reactive streams and `suspend` for one-shot operations — consistent with Kotlin coroutines idiom
- HomeAssistantRepository returns `kotlin.Result<Unit>` — no checked exceptions, callers use `onSuccess`/`onFailure` or `fold`
- Dispatcher qualifiers use `AnnotationRetention.BINARY` — not needed at runtime reflection, BINARY is the correct choice for Hilt

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- All Phase 2 downstream modules (data layer, audio layer, network layer, feature ViewModels) can now import these contracts without circular dependencies
- Core domain layer is complete — ready for 02-02 (data module: Room entities + DAOs + repository implementations)
- DispatcherModule ensures coroutine testing is straightforward — inject TestCoroutineDispatcher via qualifier in tests

---
*Phase: 02-voice-home-control-mvp*
*Completed: 2026-03-19*

## Self-Check: PASSED

All 15 created files found on disk. Both task commits verified (772f3a8, 08c4c95). CoreDomain.kt placeholder confirmed deleted.
