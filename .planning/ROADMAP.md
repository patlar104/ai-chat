# Roadmap: AI Companion App

## Overview

Six coarse phases take this project from a compilable multi-module scaffold to a fully hardened, daily-use voice-first Android companion. Phase 1 locks in the foundation that every subsequent phase depends on. Phase 2 delivers the full interactive MVP — voice in, spoken reply out, home control, reminders, and settings — everything a user needs to start using the app daily. Phase 3 hardens the intelligence layer: a proper hybrid AI router and an enforced privacy mode. Phase 4 adds proactive capability — background briefings, scheduled automations, and a persistent memory the user can inspect and control. Phase 5 gives the assistant a face: an expressive 2D avatar that reacts to voice state without impacting audio performance. Phase 6 is the hardening pass that validates every offline path, audit trail, and security surface before the v1 release.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Foundation & Scaffold** - Compilable 15-module Android project with enforced dependency rules, Hilt DI, navigation host, CI, and baseline conventions
- [ ] **Phase 2: Voice + Home Control MVP** - Button-to-talk voice loop, TTS confirmation, persistent conversation history, Home Assistant device control, reminders, and settings
- [ ] **Phase 3: AI Routing + Privacy** - Hybrid AI router (deterministic → cloud), privacy mode enforced at network interceptor, offline degraded mode
- [ ] **Phase 4: Background Ops + Memory** - WorkManager briefings and automations, persistent user memory with explicit review and delete controls
- [ ] **Phase 5: Avatar** - Expressive 2D avatar with voice-state-driven animation, decoupled from TTS pipeline for zero performance impact
- [ ] **Phase 6: Hardening & Release** - Full offline path validation, security audit, performance profiling, audit log completeness, and v1 release readiness

## Phase Details

### Phase 1: Foundation & Scaffold
**Goal**: A compilable, correctly structured 15-module Android project with all architectural guardrails in place before any feature code is written
**Depends on**: Nothing (first phase)
**Requirements**: SCAF-01, SCAF-02, SCAF-03, SCAF-04, SCAF-05, SCAF-06
**Success Criteria** (what must be TRUE):
  1. All 15 modules compile from a clean checkout with no build errors
  2. A feature module attempting to import another feature module fails the build (dependency rule enforced)
  3. CI runs on every push and reports formatting and build validation status
  4. Any module can inject a dependency via Hilt without additional wiring
  5. Navigating to any feature screen from the app module succeeds via the Navigation host
**Plans**: 6 plans

Plans:
- [ ] 01-01-PLAN.md — Gradle version catalog (libs.versions.toml), settings.gradle.kts, root build.gradle.kts with feature→feature ban, Gradle 9.3.1 wrapper
- [ ] 01-02-PLAN.md — Scaffold all 15 modules (:app + 8 core + 7 feature) with build.gradle.kts, AndroidManifest.xml, and placeholder Kotlin files
- [ ] 01-03-PLAN.md — GitHub Actions CI workflow (ktlint blocking, detekt warning-only, build validation, feature-dep check script), detekt.yml
- [ ] 01-04-PLAN.md — Hilt DI wiring: @HiltAndroidApp on AICompanionApp, @AndroidEntryPoint on MainActivity, Timber.plant, 6 core @Module @InstallIn stubs
- [ ] 01-05-PLAN.md — Navigation host: AppNavHost composable routing to all 7 feature placeholder screens via imported route constants
- [ ] 01-06-PLAN.md — Baseline error model: AppError sealed class + Logger interface in :core:domain, TimberLogger + AppModule @Binds in :app

### Phase 2: Voice + Home Control MVP
**Goal**: Users can speak commands, hear spoken replies, control smart-home devices and routines by voice, create and manage reminders, and configure the app — all working offline for core functions
**Depends on**: Phase 1
**Requirements**: VOIC-01, VOIC-02, VOIC-03, VOIC-04, VOIC-05, VOIC-06, TTS-01, TTS-02, TTS-03, CONV-01, CONV-02, CONV-03, CONV-04, HOME-01, HOME-02, HOME-03, HOME-04, HOME-05, HOME-06, HOME-07, TASK-01, TASK-02, TASK-03, TASK-04, SETT-01, SETT-02, SETT-03, SETT-04
**Success Criteria** (what must be TRUE):
  1. User presses a button, speaks a command, and hears a spoken reply — full loop completes without network on supported commands
  2. User says "turn off the living room lights" and the correct Home Assistant device turns off with a spoken confirmation
  3. User says "remind me to take my medication at 8 PM" and receives a system notification at the scheduled time
  4. User scrolls back through a chat timeline showing all past voice exchanges, including what tier handled each command
  5. User opens Settings and configures the Home Assistant URL, access token, TTS voice, privacy mode, and background automation toggle
**Plans**: 10 plans

Plans:
- [ ] 02-01-PLAN.md — Domain models + contracts: VoiceState, ParsedIntent, Message, AuditLogEntry, Alias, Reminder models; repository interfaces; dispatcher qualifiers
- [ ] 02-02-PLAN.md — Theme + design system + bottom nav: AICompanionTheme (dark color scheme, typography, spacing), BottomNavBar with 5 destinations, Scaffold wiring
- [ ] 02-03-PLAN.md — Core data layer: Room database (4 tables), DAOs, DataStore preferences, Android Keystore AES-GCM token encryption, 5 repository implementations, DataModule DI
- [ ] 02-04-PLAN.md — Core audio layer: SpeechRecognizerManager (main-thread safe), TextToSpeechManager (lifecycle safe, observable state), AudioFocusManager
- [ ] 02-05-PLAN.md — Core network + parser: HA REST API (Retrofit + OkHttp, auth/baseURL interceptors), HomeAssistantRepositoryImpl, deterministic CommandParser with regex patterns
- [ ] 02-06-PLAN.md — System integration: AndroidManifest permissions, VoiceRecognitionService (foreground, microphone type), AlarmManager ReminderScheduler, ReminderReceiver, BootReceiver, notification channels
- [ ] 02-07-PLAN.md — Voice pipeline: VoiceViewModel MVI state machine (STT -> parse -> execute -> TTS), VoiceScreen with AnimatedMicButton and LastReplyCard
- [ ] 02-08-PLAN.md — Chat + HomeControl screens: ChatScreen with message bubbles and source badges, HomeControlScreen with audit log and alias editor
- [ ] 02-09-PLAN.md — Tasks + Settings screens: TasksScreen with pending reminders list and delete, SettingsScreen with HA connection, TTS voice, privacy/automation toggles
- [ ] 02-10-PLAN.md — Final wiring + human verification: app module dependencies, compilation check, end-to-end MVP verification checkpoint

### Phase 3: AI Routing + Privacy
**Goal**: All assistant requests flow through a single, observable AI gateway; the deterministic tier handles known commands without reaching the cloud; privacy mode is enforced at the network layer and cannot be bypassed
**Depends on**: Phase 2
**Requirements**: ROUT-01, ROUT-02, ROUT-03, ROUT-04, ROUT-05, PRIV-01, PRIV-02, PRIV-03, PRIV-04, PRIV-05, PRIV-06
**Success Criteria** (what must be TRUE):
  1. A home control command ("turn on the fan") resolves via the deterministic parser without any cloud network call being made
  2. An open-ended question ("what's a good recipe for tonight?") routes to cloud AI and returns a response
  3. Enabling privacy mode blocks all cloud AI and cloud TTS network requests — verified by observing no outbound cloud calls
  4. Privacy mode active state is visible in the UI, and a plain-language message explains degraded mode when offline
  5. Every conversation message in the chat log records which routing tier handled it (deterministic, local AI, or cloud)
**Plans**: 4 plans

Plans:
- [ ] 03-01-PLAN.md — AI Router Foundation (TDD): Central gateway logic, Tier 1 (Deterministic) integration, and VoiceViewModel refactor
- [ ] 03-02-PLAN.md — Cloud AI Integration: Gemini SDK setup, CloudAiService implementation, Tier 2 fallback, and Chat UI badges
- [ ] 03-03-PLAN.md — Privacy Interceptor: Network-level blocking of cloud AI domains when privacy mode is enabled
- [ ] 03-04-PLAN.md — UI & Degraded Mode: Spoken feedback for blocked cloud requests and visual privacy/offline indicators

### Phase 4: Background Ops + Memory
**Goal**: The assistant operates proactively in the background with morning briefings and scheduled automations, while persisting user preferences and memory facts the user can fully review and delete
**Depends on**: Phase 3
**Requirements**: BKGD-01, BKGD-02, BKGD-03, MEMO-01, MEMO-02, MEMO-03, MEMO-04, MEMO-05, MEMO-06
**Success Criteria** (what must be TRUE):
  1. User receives a morning briefing notification assembled from their data each day
  2. A failed background job retries and surfaces a plain-language failure reason to the user
  3. User opens the Memory screen and sees all stored preferences, aliases, and notable facts
  4. User can delete any stored memory item and it is immediately removed
  5. Conversational chatter from a session is not silently promoted to long-term memory without user review
**Plans**: TBD

### Phase 5: Avatar
**Goal**: The assistant has a visible, expressive face that reacts to voice session states in real time without causing frame drops or delaying TTS onset
**Depends on**: Phase 4
**Requirements**: AVAT-01, AVAT-02, AVAT-03, AVAT-04
**Success Criteria** (what must be TRUE):
  1. Avatar visually transitions through Idle, Listening, Processing, Speaking, and Error states during a voice session
  2. Avatar animation starts and stops in sync with STT listening and TTS speaking without any perceptible audio delay
  3. Profiler confirms avatar rendering does not cause dropped frames or increase TTS onset latency measurably
  4. On a low-performance device, avatar degrades to a minimal indicator rather than freezing or causing lag
**Plans**: TBD

### Phase 6: Hardening & Release
**Goal**: Every offline path, security surface, audit trail, and performance boundary is validated — the app is ready for daily use as a v1 release
**Depends on**: Phase 5
**Requirements**: (cross-cutting — validates all prior requirements in offline, security, and audit contexts)
**Success Criteria** (what must be TRUE):
  1. From a cold start with no network, the user can execute a home control command, set a reminder, and hear a local TTS reply without errors
  2. Home Assistant access token is stored encrypted via Android Keystore; no plaintext secrets appear in SharedPreferences or logcat
  3. Tool invocation audit log shows every device command and routine execution, viewable by the user
  4. All background job executions are traceable in the observable log with execution result and failure reason where applicable
  5. Privacy mode audit confirms no cloud calls escape to the network when privacy mode is enabled, across all code paths
**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation & Scaffold | 6/6 | Complete ✓ | 2026-03-18 |
| 2. Voice + Home Control MVP | 7/10 | In Progress|  |
| 3. AI Routing + Privacy | 0/4 | Not started | - |
| 4. Background Ops + Memory | 0/TBD | Not started | - |
| 5. Avatar | 0/TBD | Not started | - |
| 6. Hardening & Release | 0/TBD | Not started | - |
