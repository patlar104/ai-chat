---
phase: 02-voice-home-control-mvp
plan: 03
subsystem: database
tags: [room, datastore, hilt, android-keystore, aes-gcm, repository-pattern, kotlin-coroutines, flow]

# Dependency graph
requires:
  - phase: 02-01
    provides: Domain model classes and repository interfaces (MessageRepository, AuditLogRepository, AliasRepository, ReminderRepository, SettingsRepository, dispatcher qualifiers)
provides:
  - Room database (AppDatabase) with 4 entity tables and 4 DAOs
  - AES-256-GCM token encryption via Android Keystore (HaTokenCrypto)
  - DataStore preferences wrapper (AppPreferences) storing encrypted HA token as ciphertext+IV pair
  - 5 repository implementations fully mapping Entity <-> Domain model
  - DataModule Hilt bindings wiring all repositories, DAOs, and AppDatabase into DI graph
affects: [feature-settings, feature-home, feature-conversation, feature-reminders, 02-04, 02-05, 02-06]

# Tech tracking
tech-stack:
  added:
    - Room (runtime, ktx, compiler/KSP)
    - DataStore Preferences
    - Android Keystore (AES-256-GCM via javax.crypto)
  patterns:
    - Repository impl maps Entity <-> Domain via private extension functions (toDomain/toEntity)
    - DataModule uses abstract class (not object) — required for @Binds alongside @Provides companion object
    - HA token stored as two Base64 keys (ciphertext + IV) — never plaintext in DataStore
    - Crypto operations dispatched to IoDispatcher via @IoDispatcher qualifier
    - All DAO queries use suspend or Flow — zero main-thread DB access

key-files:
  created:
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/AppDatabase.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/entity/MessageEntity.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/entity/AuditLogEntity.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/entity/AliasEntity.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/entity/ReminderEntity.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/dao/MessageDao.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/dao/AuditLogDao.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/dao/AliasDao.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/database/dao/ReminderDao.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/crypto/HaTokenCrypto.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/datastore/AppPreferences.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/repository/MessageRepositoryImpl.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/repository/AuditLogRepositoryImpl.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/repository/AliasRepositoryImpl.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/repository/ReminderRepositoryImpl.kt
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/repository/SettingsRepositoryImpl.kt
  modified:
    - core/data/src/main/kotlin/com/example/aicompanion/core/data/di/DataModule.kt
    - core/data/build.gradle.kts

key-decisions:
  - "DataModule is abstract class (not object) — @Binds requires abstract functions, companion object holds @Provides"
  - "HA access token split into ciphertext + IV stored as two separate DataStore keys — never plaintext"
  - "AES-256-GCM via Android Keystore with key alias ha_token_key_v1 — key created on first use"
  - "fallbackToDestructiveMigration() on Room DB — dev phase, schema stability not required yet"
  - "Alias lookup uses LOWER() on both sides for case-insensitive spoken-name matching"

patterns-established:
  - "Entity<->Domain mapping: private extension functions toDomain()/toEntity() inside repository impl"
  - "Crypto on IO thread: SettingsRepositoryImpl uses withContext(ioDispatcher) wrapping all encrypt/decrypt calls"
  - "DataStore via Context.dataStore property delegate — single store per process, injected via AppPreferences"

requirements-completed: [CONV-01, CONV-03, HOME-03, HOME-06, TASK-01, TASK-03, SETT-01, SETT-02, SETT-03, SETT-04]

# Metrics
duration: 20min
completed: 2026-03-19
---

# Phase 02 Plan 03: Data Layer Summary

**Room database with 4 entity tables, AES-256-GCM Android Keystore token encryption, DataStore preferences, 5 repository implementations, and complete Hilt DI bindings**

## Performance

- **Duration:** ~20 min
- **Started:** 2026-03-19T13:00:00Z
- **Completed:** 2026-03-19T13:20:00Z
- **Tasks:** 2 of 2
- **Files modified:** 18

## Accomplishments

- Room database (AppDatabase) with 4 entity tables (messages, audit_log, aliases, reminders) and proper indexes
- AES-256-GCM token encryption via Android Keystore — HA access token never stored as plaintext
- AppPreferences DataStore wrapper with ciphertext+IV split storage for the encrypted token
- 5 repository implementations that map Entity <-> Domain model via private extension functions
- DataModule abstract class binds all 5 repositories and provides DB, 4 DAOs via Hilt SingletonComponent

## Task Commits

Each task was committed atomically:

1. **Task 1: Room entities, DAOs, AppDatabase, build.gradle** - `8099807` (feat)
2. **Task 2: Crypto, DataStore, repository implementations, DataModule DI** - `[pending]` (feat)

**Plan metadata:** `[pending]` (docs: complete plan)

## Files Created/Modified

- `core/data/build.gradle.kts` - Added Room, DataStore, core:domain dependencies
- `core/data/src/main/kotlin/.../database/AppDatabase.kt` - Room database with 4 entities
- `core/data/src/main/kotlin/.../database/entity/MessageEntity.kt` - Messages table with sessionId index
- `core/data/src/main/kotlin/.../database/entity/AuditLogEntity.kt` - Audit log with timestampMs index
- `core/data/src/main/kotlin/.../database/entity/AliasEntity.kt` - Aliases with unique index on alias column
- `core/data/src/main/kotlin/.../database/entity/ReminderEntity.kt` - Reminders with status index
- `core/data/src/main/kotlin/.../database/dao/MessageDao.kt` - CRUD with Flow and suspend
- `core/data/src/main/kotlin/.../database/dao/AuditLogDao.kt` - Insert + observe recent
- `core/data/src/main/kotlin/.../database/dao/AliasDao.kt` - Case-insensitive findByAlias
- `core/data/src/main/kotlin/.../database/dao/ReminderDao.kt` - Pending reminders CRUD
- `core/data/src/main/kotlin/.../crypto/HaTokenCrypto.kt` - AES-256-GCM Keystore encrypt/decrypt
- `core/data/src/main/kotlin/.../datastore/AppPreferences.kt` - DataStore wrapper with 6 preference keys
- `core/data/src/main/kotlin/.../repository/MessageRepositoryImpl.kt` - Implements MessageRepository
- `core/data/src/main/kotlin/.../repository/AuditLogRepositoryImpl.kt` - Implements AuditLogRepository
- `core/data/src/main/kotlin/.../repository/AliasRepositoryImpl.kt` - Implements AliasRepository
- `core/data/src/main/kotlin/.../repository/ReminderRepositoryImpl.kt` - Implements ReminderRepository
- `core/data/src/main/kotlin/.../repository/SettingsRepositoryImpl.kt` - Implements SettingsRepository with crypto
- `core/data/src/main/kotlin/.../di/DataModule.kt` - Hilt abstract class with @Binds + @Provides

## Decisions Made

- DataModule changed from `object` to `abstract class` — required for @Binds to work alongside @Provides in companion object. Follows same AppModule pattern from Phase 01-06.
- HA access token stored as two separate DataStore string keys (ciphertext + IV) encoded as Base64 — no plaintext token ever persisted.
- `fallbackToDestructiveMigration()` on Room builder — appropriate for dev phase; schema stability addressed before production.
- Alias search uses `LOWER(alias) = LOWER(:alias)` in SQL — handles case-insensitive spoken name matching from voice input.
- Crypto operations (encrypt/decrypt) wrapped in `withContext(ioDispatcher)` in SettingsRepositoryImpl — enforces off-main-thread KeyStore access.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Full data layer is injectable via Hilt — feature modules can inject any of the 5 repository interfaces
- SettingsRepositoryImpl ready for feature-settings screens to read/write all preferences
- MessageRepositoryImpl ready for conversation history persistence
- AliasRepositoryImpl supports spoken-name -> HA entity ID lookup for home control
- ReminderRepositoryImpl supports pending reminder queries for WorkManager/AlarmManager scheduling

---
*Phase: 02-voice-home-control-mvp*
*Completed: 2026-03-19*
