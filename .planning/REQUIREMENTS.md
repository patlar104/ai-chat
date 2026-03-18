# Requirements: AI Companion App

**Defined:** 2026-03-18
**Core Value:** The assistant must handle real daily commands quickly and reliably via voice — home control, reminders, and spoken replies must work offline; everything else is an enhancement.

---

## v1 Requirements

Requirements for initial release across all 7 phases (Phases 0–7). Each maps to a roadmap phase.

### Scaffold & Foundation

- [ ] **SCAF-01**: Repository compiles a minimal Android shell across all 15 modules
- [ ] **SCAF-02**: Gradle module dependency rules prevent feature → feature imports (enforced by Lint or build tooling)
- [ ] **SCAF-03**: CI runs formatting checks and baseline build validation
- [ ] **SCAF-04**: Dependency injection (Hilt) is wired at the app level and available in all modules
- [ ] **SCAF-05**: Navigation host is established in the app module and routes to all feature screens
- [ ] **SCAF-06**: Baseline error model and logging conventions are in place

### Voice Shell

- [ ] **VOIC-01**: User can initiate a voice session by pressing a button (button-to-talk)
- [ ] **VOIC-02**: Assistant visual state reflects Idle, Listening, Transcribing, Processing, Speaking, and Error states
- [ ] **VOIC-03**: User speech is captured via Android SpeechRecognizer and transcribed to text
- [ ] **VOIC-04**: Voice session runs in a foreground service with correct foreground service type declared in manifest
- [ ] **VOIC-05**: Audio focus is requested and abandoned correctly across the voice session lifecycle
- [ ] **VOIC-06**: User can interrupt or stop an in-progress assistant reply

### Text-to-Speech

- [ ] **TTS-01**: Assistant replies are spoken aloud via Android TextToSpeech (local baseline)
- [ ] **TTS-02**: Short utility confirmations ("Done, turning off the lights") use local TTS for speed
- [ ] **TTS-03**: TTS state (speaking/idle) is observable so avatar and UI can react to it

### Conversation & Chat

- [ ] **CONV-01**: Conversation history persists across app restarts in Room
- [ ] **CONV-02**: Conversation history is viewable in a scrollable chat timeline UI showing both user and assistant turns
- [ ] **CONV-03**: Each conversation message records role, content, source type (deterministic/local-AI/cloud), and timestamp
- [ ] **CONV-04**: Short-term in-session context enables multi-turn references (e.g., "turn it up" after mentioning a device)

### AI Routing

- [ ] **ROUT-01**: All assistant requests are routed through a central AI gateway (no direct LLM calls from UI)
- [ ] **ROUT-02**: Deterministic command parser handles known home control, reminder, and routine intents without LLM
- [ ] **ROUT-03**: Cloud AI handles open-ended reasoning and conversation when deterministic and local AI tiers cannot resolve the request
- [ ] **ROUT-04**: System degrades gracefully to reduced local mode when cloud AI is unavailable
- [ ] **ROUT-05**: Every routing decision is observable (request class logged with each conversation message)

### Home Control

- [ ] **HOME-01**: User can control named smart-home devices by voice command (on/off, brightness, temperature)
- [ ] **HOME-02**: User can execute named routines by voice command or alias
- [ ] **HOME-03**: Device and room alias map translates spoken names to Home Assistant entity IDs
- [ ] **HOME-04**: User can add, edit, and delete device and room aliases
- [ ] **HOME-05**: Assistant speaks a confirmation after each home control action
- [ ] **HOME-06**: Each tool invocation (device command, routine) is recorded in an audit log viewable by the user
- [ ] **HOME-07**: Failed tool invocations surface a plain-language explanation to the user

### Tasks & Reminders

- [ ] **TASK-01**: User can create a reminder by voice command with a specified time or description
- [ ] **TASK-02**: Reminders trigger a system notification at the scheduled time
- [ ] **TASK-03**: User can view a list of pending reminders in the Tasks screen
- [ ] **TASK-04**: User can cancel or delete a pending reminder

### Avatar

- [ ] **AVAT-01**: Avatar displays distinct visual states for Idle, Listening, Processing, Speaking, and Error
- [ ] **AVAT-02**: Avatar animates reactively — listening indicator during STT, speaking indicator during TTS
- [ ] **AVAT-03**: Avatar does not cause frame drops or audio delay (rendered independently of TTS pipeline)
- [ ] **AVAT-04**: Avatar degrades gracefully to a minimal indicator on low-performance devices

### Background Operations

- [ ] **BKGD-01**: Morning briefing WorkManager job assembles and delivers a daily summary notification
- [ ] **BKGD-02**: Background tasks retry on recoverable failure and surface failure reason to user
- [ ] **BKGD-03**: All background job executions are traceable in an observable log

### Memory & Personalization

- [ ] **MEMO-01**: User preferences (voice settings, assistant name, notification prefs) persist in DataStore
- [ ] **MEMO-02**: Memory facts (stable preferences, recurring patterns) are stored in Room and associated with a retention policy
- [ ] **MEMO-03**: User can view all stored memory items in a Memory screen
- [ ] **MEMO-04**: User can delete any stored memory item
- [ ] **MEMO-05**: Long-term memory promotion requires explicit user approval or review (no silent auto-save)
- [ ] **MEMO-06**: Transient conversational chatter is not promoted to long-term memory

### Privacy & Offline

- [ ] **PRIV-01**: Privacy mode toggle prevents all cloud AI and cloud TTS requests when enabled
- [ ] **PRIV-02**: Privacy mode state is enforced in a network interceptor — not just in the UI
- [ ] **PRIV-03**: Privacy mode active state is visually indicated in the UI
- [ ] **PRIV-04**: Core commands (home control for local HA, reminders, task capture) work without network in offline mode
- [ ] **PRIV-05**: Local TTS provides spoken feedback in offline mode
- [ ] **PRIV-06**: Offline degraded mode is clearly communicated to the user when relevant

### Settings

- [ ] **SETT-01**: User can configure Home Assistant server URL and long-lived access token (stored securely via Android Keystore + DataStore — not EncryptedSharedPreferences)
- [ ] **SETT-02**: User can toggle privacy mode (cloud AI lockout)
- [ ] **SETT-03**: User can configure TTS voice preference
- [ ] **SETT-04**: User can toggle background automation and briefings on/off

---

## v2 Requirements

Deferred to future release. Acknowledged but not in current roadmap.

### Advanced Voice

- **VOIC-V2-01**: Cloud neural TTS for companion-mode responses (ElevenLabs or equivalent)
- **VOIC-V2-02**: Two-tier TTS routing (local for utility confirmations, cloud for conversational replies)
- **VOIC-V2-03**: Real-time streaming voice conversation (continuous duplex listening, barge-in)

### On-Device AI

- **ROUT-V2-01**: On-device Gemini Nano via ML Kit Prompt API for offline reasoning on supported devices
- **ROUT-V2-02**: Graceful detection and fallback when on-device AI (AICore) is not available on device

### Advanced Home Control

- **HOME-V2-01**: Arrival/departure geofence automations trigger configured routines
- **HOME-V2-02**: Scheduled time-based automations ("every morning at 7, run wake-up routine")

### Advanced Avatar

- **AVAT-V2-01**: Expressive emotion states (happy, thinking, concerned) mapped to conversation context
- **AVAT-V2-02**: Lip-sync approximation driven by TTS audio amplitude envelope
- **AVAT-V2-03**: Idle personality animations (subtle breathing, eye movement)

### Advanced Memory

- **MEMO-V2-01**: Semantic retrieval of memory facts relevant to current conversation context
- **MEMO-V2-02**: Memory summary view with categorized preferences, aliases, and notable facts

---

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Always-on hotword detection | Android background mic restrictions; battery impact unvalidated; false positive trust issue |
| Autonomous purchasing / high-risk transactions | No confirmation model yet; real financial risk without proper auth flow |
| Full 3D animated avatar | 2–6 months additional work; GPU competes with audio on mid-range Android |
| Cross-app UI automation via AccessibilityService | Unstable across OEM skins; Play Store policy risk |
| Multi-user household profiles | Personal use first; single-user model must be proven before extension |
| Always-visible floating overlay widget | SYSTEM_ALERT_WINDOW restricted in Android 12+; UX confusion |
| iOS / cross-platform (KMP) app | Android-native first; cross-platform adds complexity before value proven |
| Fully local LLM for complex reasoning | 7B+ models need 4–8GB RAM; inference latency 10–60s on device |

---

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| SCAF-01 | Phase 1 | Pending |
| SCAF-02 | Phase 1 | Pending |
| SCAF-03 | Phase 1 | Pending |
| SCAF-04 | Phase 1 | Pending |
| SCAF-05 | Phase 1 | Pending |
| SCAF-06 | Phase 1 | Pending |
| VOIC-01 | Phase 2 | Pending |
| VOIC-02 | Phase 2 | Pending |
| VOIC-03 | Phase 2 | Pending |
| VOIC-04 | Phase 2 | Pending |
| VOIC-05 | Phase 2 | Pending |
| VOIC-06 | Phase 2 | Pending |
| TTS-01 | Phase 2 | Pending |
| TTS-02 | Phase 2 | Pending |
| TTS-03 | Phase 2 | Pending |
| CONV-01 | Phase 2 | Pending |
| CONV-02 | Phase 2 | Pending |
| CONV-03 | Phase 2 | Pending |
| CONV-04 | Phase 2 | Pending |
| ROUT-01 | Phase 3 | Pending |
| ROUT-02 | Phase 3 | Pending |
| ROUT-03 | Phase 3 | Pending |
| ROUT-04 | Phase 3 | Pending |
| ROUT-05 | Phase 3 | Pending |
| HOME-01 | Phase 2 | Pending |
| HOME-02 | Phase 2 | Pending |
| HOME-03 | Phase 2 | Pending |
| HOME-04 | Phase 2 | Pending |
| HOME-05 | Phase 2 | Pending |
| HOME-06 | Phase 2 | Pending |
| HOME-07 | Phase 2 | Pending |
| TASK-01 | Phase 2 | Pending |
| TASK-02 | Phase 2 | Pending |
| TASK-03 | Phase 2 | Pending |
| TASK-04 | Phase 2 | Pending |
| AVAT-01 | Phase 5 | Pending |
| AVAT-02 | Phase 5 | Pending |
| AVAT-03 | Phase 5 | Pending |
| AVAT-04 | Phase 5 | Pending |
| BKGD-01 | Phase 4 | Pending |
| BKGD-02 | Phase 4 | Pending |
| BKGD-03 | Phase 4 | Pending |
| MEMO-01 | Phase 4 | Pending |
| MEMO-02 | Phase 4 | Pending |
| MEMO-03 | Phase 4 | Pending |
| MEMO-04 | Phase 4 | Pending |
| MEMO-05 | Phase 4 | Pending |
| MEMO-06 | Phase 4 | Pending |
| PRIV-01 | Phase 3 | Pending |
| PRIV-02 | Phase 3 | Pending |
| PRIV-03 | Phase 3 | Pending |
| PRIV-04 | Phase 3 | Pending |
| PRIV-05 | Phase 3 | Pending |
| PRIV-06 | Phase 3 | Pending |
| SETT-01 | Phase 2 | Pending |
| SETT-02 | Phase 2 | Pending |
| SETT-03 | Phase 2 | Pending |
| SETT-04 | Phase 2 | Pending |

**Coverage:**
- v1 requirements: 58 total
- Mapped to phases: 58
- Unmapped: 0 ✓

**Phase distribution:**
- Phase 1 (Foundation & Scaffold): 6 requirements
- Phase 2 (Voice + Home Control MVP): 28 requirements
- Phase 3 (AI Routing + Privacy): 11 requirements
- Phase 4 (Background Ops + Memory): 9 requirements
- Phase 5 (Avatar): 4 requirements
- Phase 6 (Hardening & Release): 0 new requirements (cross-cutting validation pass)

---
*Requirements defined: 2026-03-18*
*Last updated: 2026-03-18 — traceability updated after roadmap creation*
