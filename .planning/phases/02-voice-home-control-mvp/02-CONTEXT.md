# Phase 2: Voice + Home Control MVP - Context

**Gathered:** 2026-03-18
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can speak commands via button-to-talk, hear spoken TTS replies, control Home Assistant smart-home devices and routines by voice, create and manage reminders, view a persistent conversation history timeline, and configure the app via a Settings screen. This is the first phase that produces a runnable daily-use app. No cloud AI routing — Phase 2 uses a deterministic command parser only.

</domain>

<decisions>
## Implementation Decisions

### Voice Interaction Model
- **Tap-to-toggle:** Tap the mic button once to start listening, tap again (or on TTS completion) to return to Idle. No press-and-hold.
- **VoiceScreen is the home screen:** Large centered mic button. VoiceScreen is the start destination in AppNavHost (already wired in Phase 1).
- **Animated mic button for all states (VOIC-02):** Single button that visually transforms per state — pulses while Listening, spinner while Transcribing/Processing, audio waveform indicator while Speaking, red/error tint in Error state. No separate status text label.
- **Last assistant reply shown as text below the mic button:** One card/line showing the most recent assistant reply. Reinforces spoken confirmation visually (especially for device control).
- **Interrupt behavior (VOIC-06):** Tapping the mic while TTS is Speaking stops TTS immediately and returns to Idle. No barge-in (barge-in is a v2 feature — VOIC-V2-03).
- **Audio error (Error state):** Show a toast with a plain-language message ('Microphone permission required' / 'Speech recognition failed') and return to Idle. No persistent error card.
- **Bottom navigation bar:** Persistent Material 3 NavigationBar at the bottom of the app with 5 destinations: Voice (home), Chat, Home Control, Tasks, Settings. Accessible from all screens.

### Unrecognized Command Handling
- **TTS 'sorry' reply for unrecognized commands:** When the deterministic parser finds no matching intent, the assistant says "Sorry, I didn't understand that. I can control devices, set reminders, and execute routines." The turn is logged in conversation history with `source_type = unknown`.
- **No cloud AI fallback in Phase 2:** Phase 3 adds the full hybrid AI router with cloud escalation. Phase 2 is deterministic-only.
- **Deterministic parser handles these intent categories:**
  1. Home control — on/off, brightness, temperature for named devices ("turn off the living room lights", "dim the kitchen to 50%")
  2. Named routines — execute HA scripts/routines by name ("run morning routine", "activate bedtime scene")
  3. Reminder creation — parse time + description ("remind me to take medication at 8 PM") — maps to TASK-01
  4. Simple local queries — current time, list of pending reminders ("what reminders do I have?")

### Home Assistant Connection
- **REST API only for Phase 2:** HTTP POST to HA REST API endpoints (`/api/services/{domain}/{service}`). No WebSocket connection in Phase 2. WebSocket for real-time state push deferred to a later phase.
- **Authentication:** Bearer token in `Authorization: Bearer {token}` header. Token stored encrypted via Android Keystore + DataStore (per SETT-01 requirement — NOT EncryptedSharedPreferences).
- **HA command failure handling (HOME-07):** On network error, auth failure, or timeout, the assistant speaks a plain-language TTS error reply ("I couldn't reach your Home Assistant — check the connection in Settings"). `AppError.HomeAssistant` wraps the failure. Logged in the audit log with error reason.
- **HomeControlScreen content:** Audit log only — a list of recent device commands executed and their results (success/failure). No live device state panel or static alias map editor on this screen. The alias map editor (HOME-04) gets its own UI section (within HomeControlScreen or as a settings sub-screen — planner's discretion).

### Conversation Layout
- **Separate ChatScreen:** Full conversation history lives on ChatScreen, accessible from the bottom nav bar. VoiceScreen only shows the last assistant reply. Clean separation of concerns.
- **Chat bubble UI:** Standard chat layout — user messages on the right (filled bubble), assistant messages on the left (outlined bubble). Timestamp shown below each message.
- **Source badges visible in Phase 2 (CONV-03):** Small chip badge on each assistant message showing `deterministic` or `unknown` as the source tier. Phase 3 adds `local-ai` and `cloud` values when the full router is in place.
- **Flat scroll, newest at bottom:** Simple `LazyColumn`, auto-scrolls to bottom after each new message. No date/session grouping headers in Phase 2.

### Settings Screen (SETT-01 through SETT-04)
- **Claude's Discretion for layout:** Standard scrollable settings screen. Fields include: HA Server URL (text input), HA Access Token (masked text input, stored via Android Keystore), TTS Voice (dropdown of available system voices), Privacy Mode toggle (switches cloud AI lockout — UI only in Phase 2; enforcement is Phase 3), Background Automation toggle.
- Privacy Mode toggle persists the value in DataStore but network enforcement (interceptor) is wired in Phase 3.

### Claude's Discretion
- Exact Compose color/typography choices for the mic button states (use Material 3 color roles)
- Exact wording of deterministic parser regex patterns (planner to design, must cover the command categories above)
- Whether alias map management UI lives inline on HomeControlScreen or as a nav sub-destination
- AlarmManager vs WorkManager for reminder scheduling (planner to pick based on reliability requirements)
- Exact HA REST API endpoints used for service calls (verify against HA API docs during research)
- Room schema design for conversation messages and audit log entries

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Requirements (Phase 2 scope)
- `.planning/REQUIREMENTS.md` — VOIC-01–06, TTS-01–03, CONV-01–04, HOME-01–07, TASK-01–04, SETT-01–04 define Phase 2 acceptance criteria
- `.planning/PROJECT.md` — Architecture constraints: no network calls from UI, no business logic in composables, MVI for voice/chat, offline core commands required
- `.planning/STATE.md` — Critical flags: SpeechRecognizer must be created on main thread; `foregroundServiceType="microphone"` required in manifest

### Phase 1 Artifacts (established patterns)
- `.planning/phases/01-foundation-scaffold/01-CONTEXT.md` — Architecture decisions: KSP-only, no kapt, no EncryptedSharedPreferences, no Google AI Edge SDK
- `.planning/phases/01-foundation-scaffold/VERIFICATION.md` — Phase 1 completion state; integration points (AppNavHost, Hilt graph, Logger, AppError)

### Research (from Phase 1 planning)
- `.planning/research/SUMMARY.md` — Stack decisions, critical deprecation warnings, library versions
- `.planning/research/PITFALLS.md` — Common Android voice assistant mistakes to avoid

### No external specs
No external API specs or ADRs beyond the above. HA REST API details should be verified against official Home Assistant documentation during Phase 2 research (https://developers.home-assistant.io/docs/api/rest/).

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `core/domain/src/main/kotlin/com/example/aicompanion/core/domain/error/AppError.kt` — `AppError.AudioPipeline` for voice errors, `AppError.HomeAssistant` for HA failures — use these, don't create new error types
- `core/domain/src/main/kotlin/com/example/aicompanion/core/domain/logging/Logger.kt` — inject Logger everywhere; no direct Timber calls outside :app
- `app/src/main/kotlin/com/example/aicompanion/navigation/AppNavHost.kt` — NavHost already routes to all 7 feature screens; Phase 2 replaces placeholder composables with real implementations; bottom nav wired here or in the feature screens
- `app/src/main/kotlin/com/example/aicompanion/di/AppModule.kt` — abstract class with @Binds; Phase 2 core module DI objects (AudioModule, NetworkModule, DataModule) fill in their placeholders

### Established Patterns
- KSP-only annotation processing — all Room, Hilt annotations must use KSP; no kapt anywhere
- `@Module @InstallIn(SingletonComponent::class)` object pattern established in all 6 core modules — extend these
- `AppNavHost` imports route constants from feature modules — follow this pattern (features own their routes)
- Phase 2 adds real ViewModels via `@HiltViewModel` — builds on the existing Hilt graph

### Integration Points
- `feature/voice/VoiceScreen.kt` — current placeholder; Phase 2 replaces with full voice UI + state machine
- `feature/chat/ChatScreen.kt` — current placeholder; Phase 2 replaces with LazyColumn chat bubble UI
- `feature/homecontrol/HomeControlScreen.kt` — current placeholder; Phase 2 replaces with audit log list
- `feature/tasks/TasksScreen.kt` — current placeholder; Phase 2 replaces with pending reminders list
- `feature/settings/SettingsScreen.kt` — current placeholder; Phase 2 replaces with settings form
- `core/audio/di/AudioModule.kt` — placeholder; Phase 2 fills with SpeechRecognizer + TTS bindings (CRITICAL: SpeechRecognizer must be created on main thread)
- `core/network/di/NetworkModule.kt` — placeholder; Phase 2 fills with OkHttpClient + HA REST API bindings
- `core/data/di/DataModule.kt` — placeholder; Phase 2 fills with Room database + DataStore bindings

</code_context>

<specifics>
## Specific Ideas

- SpeechRecognizer MUST be created on the main thread — this is a hard Android constraint flagged in STATE.md. Any ViewModel or service that manages SpeechRecognizer must dispatch creation to the main thread.
- `foregroundServiceType="microphone"` MUST be declared in AndroidManifest.xml for the voice foreground service — flagged as critical in STATE.md.
- The HA access token must NEVER touch EncryptedSharedPreferences (deprecated). The Android Keystore + DataStore path is mandatory per SETT-01 and Phase 1 CONTEXT.md.
- The bottom navigation bar is the primary navigation mechanism — users should not need to understand AppNavHost's back stack to use the app.

</specifics>

<deferred>
## Deferred Ideas

- WebSocket connection to Home Assistant for real-time entity state — deferred to Phase 3 or 4 when live device state panel is needed
- Cloud AI fallback for unrecognized commands — Phase 3 (full hybrid AI router)
- Live device state panel on HomeControlScreen — later phase when WebSocket is in place
- Date/session grouping headers in ChatScreen — can add in a later phase if needed
- Barge-in voice interruption (VOIC-V2-03) — explicitly v2, not Phase 2

</deferred>

---

*Phase: 02-voice-home-control-mvp*
*Context gathered: 2026-03-18*
