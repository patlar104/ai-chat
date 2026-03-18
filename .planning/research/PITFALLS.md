# Pitfalls Research

**Domain:** Android AI Companion / Voice Assistant App
**Researched:** 2026-03-18
**Confidence:** HIGH (core Android pitfalls from official docs + MEDIUM for AI routing/avatar/memory patterns from known ecosystem behavior)

---

## Critical Pitfalls

### Pitfall 1: SpeechRecognizer Must Live on the Main Thread

**What goes wrong:**
`SpeechRecognizer` is created in a background coroutine or service worker thread. It silently fails to deliver callbacks, or throws an exception at runtime. Recognition never completes and the voice state machine gets stuck in `LISTENING` with no transition trigger.

**Why it happens:**
Developers move it off the main thread trying to avoid UI jank, not knowing the SDK enforces main-thread creation. The Kotlin coroutine pattern (`launch(Dispatchers.IO)`) is everywhere in the codebase and it's tempting to apply it uniformly.

**How to avoid:**
Create `SpeechRecognizer` only on the main thread — `Dispatchers.Main` / `Looper.getMainLooper()`. Move only the post-processing of results to IO dispatchers. In the `core.audio` module, enforce this via an assertion: `check(Looper.myLooper() == Looper.getMainLooper())` in the recognizer factory.

**Warning signs:**
- `startListening()` is called but `onResults()` never fires
- Logcat shows `android.os.Looper` threading warnings
- Voice state machine never exits `LISTENING` state

**Phase to address:**
Voice pipeline foundation phase (Phase 1 / voice feature module scaffolding). Establish the threading contract before any higher-level voice logic is built on top.

---

### Pitfall 2: Foreground Service `foregroundServiceType` Missing — Microphone Silently Blocked

**What goes wrong:**
On Android 10+ (API 29+) the app uses `RECORD_AUDIO` in a background service without declaring `foregroundServiceType="microphone"` in the manifest. The microphone is silently blocked after the app leaves the foreground — no exception is thrown, but no audio is captured. On Android 14+, missing the manifest declaration causes an outright `SecurityException` at service start.

**Why it happens:**
Developers declare `<uses-permission android:name="android.permission.RECORD_AUDIO" />` and assume that is sufficient. The foreground service type requirement is a separate, non-obvious system.

**How to avoid:**
In `AndroidManifest.xml`:
```xml
<service
    android:name=".audio.VoiceRecognitionService"
    android:foregroundServiceType="microphone"
    android:exported="false" />
```
Also declare `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />`. Verify `RECORD_AUDIO` runtime permission is granted before starting the service.

**Warning signs:**
- Voice works in foreground but silently fails after switching apps
- No audio data but no crash
- On Android 14 emulator: `SecurityException` on service start

**Phase to address:**
Phase 1 — voice pipeline scaffold. The manifest and service type contract must be correct from the first working build.

---

### Pitfall 3: Audio Focus Not Managed — TTS Talks Over System Sounds, Phone Calls

**What goes wrong:**
The TTS engine plays spoken replies without requesting audio focus. Phone calls, navigation apps, or media players are interrupted without ducking. Worse: on Android 15+, a background app that doesn't hold a foreground service cannot request audio focus at all — `AUDIOFOCUS_REQUEST_FAILED` is returned silently and TTS plays nothing.

**Why it happens:**
TTS `speak()` calls are easy to make. Audio focus management is a separate concern that's easy to skip in early development and painful to retrofit.

**How to avoid:**
- Use `AudioFocusRequest.Builder` (not deprecated `requestAudioFocus()`) with `CONTENT_TYPE_SPEECH` + `USAGE_ASSISTANT`
- Verify the return value is `AUDIOFOCUS_REQUEST_GRANTED` before calling `speak()`
- Implement `onAudioFocusChange()` listener: pause TTS on `AUDIOFOCUS_LOSS_TRANSIENT`, stop on `AUDIOFOCUS_LOSS`
- Abandon focus via `abandonAudioFocusRequest()` when TTS completes
- Never request focus from a background context unless running a foreground service

**Warning signs:**
- TTS speaks over phone calls
- TTS silently skips speech with no error in logcat (Android 15+)
- Other media apps do not resume after the assistant finishes speaking

**Phase to address:**
Phase 1 (TTS integration). Bake audio focus management into the `core.audio` abstraction so all callers inherit correct behavior automatically.

---

### Pitfall 4: Voice State Machine Has No Timeout — App Gets Stuck Listening

**What goes wrong:**
The voice state machine enters `LISTENING` and never transitions out if the recognizer fails silently (network loss, `ERROR_RECOGNIZER_BUSY`, hardware glitch). The UI shows the listening indicator indefinitely, the microphone stays locked, and subsequent presses are ignored.

**Why it happens:**
Happy-path testing only. Recognition error codes like `ERROR_RECOGNIZER_BUSY` (7) and `ERROR_SPEECH_TIMEOUT` (6) are not handled, so the state machine has no trigger to return to `IDLE`.

**How to avoid:**
- Handle all `RecognitionListener.onError()` codes — every code must trigger a state transition back to `IDLE` with an appropriate error state
- Add a coroutine timeout (`withTimeout(8000)`) that force-transitions to `IDLE` if no result arrives
- `ERROR_RECOGNIZER_BUSY` should auto-retry once after a 500ms delay before showing error
- Log and surface `ERROR_NO_MATCH` as a distinct UI state (not a crash, not silence)

**Warning signs:**
- QA finds the listening indicator stuck after network toggle
- Microphone icon unresponsive after second tap
- Voice session never ends on slow/offline connections

**Phase to address:**
Phase 1 — voice state machine. All error transitions are part of the state contract, not an afterthought.

---

### Pitfall 5: Hybrid AI Router Falls Through to Cloud for All Commands

**What goes wrong:**
The deterministic parser is built too narrowly (matches only exact phrases). The router escalates common commands like "turn off lights" to cloud AI unnecessarily, adding 1-3 seconds of latency. Users experience the assistant as "slow" even for simple requests.

**Why it happens:**
The deterministic layer is built quickly with literal string matching. Developers don't invest in the intent vocabulary up front because the cloud fallback "works."

**How to avoid:**
- The deterministic layer must use regex/pattern matching with synonym coverage, not literal equality
- Define a closed vocabulary for home control verbs + device aliases before implementing
- Add a routing telemetry log (local only, debug builds) that shows which tier handled each command — use this to expand parser coverage iteratively
- Set a target: >90% of home control commands handled by deterministic tier

**Warning signs:**
- Latency for common commands is consistently 2-4 seconds
- Cloud API call counts are unexpectedly high
- "Turn off the bedroom light" goes to cloud but "turn bedroom lights off" doesn't

**Phase to address:**
Phase 2 — AI routing layer. Define the deterministic vocabulary contract before home control integration is wired up.

---

### Pitfall 6: Home Assistant Integration Hardcodes Internal IP — Breaks Outside Home Network

**What goes wrong:**
The Home Assistant base URL is stored as `http://192.168.x.x:8123`. Works on home Wi-Fi. Silently fails or crashes when the device leaves the local network. Background automations that run via WorkManager also fail without surfacing a useful error.

**Why it happens:**
Development happens at home, so the local IP path always works. The app is personal-use, so the developer assumes it will only run on home Wi-Fi.

**How to avoid:**
- Store the base URL as a user-configurable `DataStore` preference (not a compile-time constant)
- Support both a local URL and an external URL (e.g., Nabu Casa / reverse proxy)
- In `core.network`, implement a connectivity-aware URL selector that tries local first, falls back to external
- Surface `HomeAssistantUnreachable` as a distinct domain error, not a generic network error, so the UI can show "Home control unavailable" rather than crashing

**Warning signs:**
- Home control works in development but fails during testing away from home
- WorkManager logs show repeated HTTP connection timeouts
- No user-facing error when HA is unreachable

**Phase to address:**
Phase 3 — home control integration. The URL configuration UX must be part of the initial implementation, not retrofitted.

---

### Pitfall 7: Home Assistant Long-Lived Token Stored in SharedPreferences (Unencrypted)

**What goes wrong:**
The HA long-lived access token is stored in `SharedPreferences` as a plain string. On rooted devices or via ADB backup, the token is trivially extractable. If the token leaks, the attacker has full Home Assistant access including automations, locks, and cameras.

**Why it happens:**
`SharedPreferences` is the path of least resistance for simple key-value storage. `DataStore` (the replacement) is slightly more involved, and `EncryptedSharedPreferences` / Android Keystore even more so.

**How to avoid:**
- Store the HA token using `EncryptedSharedPreferences` (from `androidx.security:security-crypto`) which wraps Android Keystore
- Or store in `DataStore` with encryption
- Never log the token value at any log level
- Provide a "Disconnect" action that clears the token on demand

**Warning signs:**
- Token found in plaintext in `adb shell run-as <package> cat shared_prefs/`
- Token logged in debug output during network requests

**Phase to address:**
Phase 3 (home control) — the token storage decision must be made at integration time, before the token is ever written.

---

### Pitfall 8: WorkManager Background Briefings Killed by Battery Optimization

**What goes wrong:**
Scheduled morning briefings (WorkManager periodic tasks) are deferred or skipped entirely on devices with aggressive battery optimization (MIUI, One UI, EMUI). The user sets up a 7am briefing and it fires at 9am or not at all.

**Why it happens:**
WorkManager guarantees eventual execution, not exact timing. OEM battery optimizations (beyond AOSP Doze) aggressively kill background work. Developers test on Pixel emulators where behavior is AOSP-standard.

**How to avoid:**
- Separate "best-effort periodic" tasks (WorkManager `PeriodicWorkRequest`) from "time-critical alarms" (`AlarmManager.setAlarmClock()`)
- For the morning briefing, use `AlarmManager.setAlarmClock()` which is user-visible in the clock app and not killed by Doze/OEM optimization
- Prompt the user to grant "Unrestricted" battery access via `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` on first-run setup
- Test on Samsung + MIUI emulators/physical devices, not only on Pixel

**Warning signs:**
- Briefing fires on Pixel emulator reliably but not on Samsung test device
- WorkManager status shows `ENQUEUED` for hours past scheduled time
- User reports "it worked once then stopped"

**Phase to address:**
Phase 4 — background automations. The alarm vs. WorkManager decision must be made per-task based on timing criticality.

---

### Pitfall 9: Avatar Compose Animations Block Voice Response

**What goes wrong:**
The avatar's animated Lottie/Compose transitions are running on the main thread or sharing the same rendering pipeline as the voice state machine. When the avatar starts its `THINKING` → `SPEAKING` transition animation, there is a perceptible 200-400ms delay before TTS audio begins. Users experience the assistant as sluggish.

**Why it happens:**
Jetpack Compose `Animatable` and transition APIs run on the main thread. When the avatar animation is heavy (complex vector, high frame rate), it competes for main thread time with the voice state update that triggers TTS.

**How to avoid:**
- Decouple avatar state from TTS trigger: TTS `speak()` must be called immediately on voice state change — avatar animation change is a side effect, not a prerequisite
- Use `LaunchedEffect` to observe voice state and fire TTS independently of the Compose recomposition caused by avatar state
- Keep avatar assets simple (2D state sprite or lightweight Lottie), not complex animated vector drawables
- Profile with Android Studio's Layout Inspector / Recomposition counter before shipping avatar integration

**Warning signs:**
- Adding avatar worsens perceived TTS response time
- Profiler shows main thread jank during state transitions
- TTS audio starts after the animation completes rather than concurrently

**Phase to address:**
Phase 5 — avatar integration. Establish the voice/TTS pipeline as performance-stable before layering avatar animations.

---

### Pitfall 10: Memory Store Promotes Data to Long-Term Without Explicit User Review

**What goes wrong:**
The app automatically promotes conversation excerpts to long-term memory based on heuristics (e.g., "contains preference-sounding language"). Users discover the app has stored sensitive or incorrect "facts" about them that they never intended to persist. This is especially damaging for a personal-use privacy-focused app.

**Why it happens:**
Automatic promotion reduces friction and is how many AI products work. Developers assume users want the assistant to "remember everything important."

**How to avoid:**
- Default: no automatic long-term promotion. Only explicit signals should trigger storage (user says "remember that", or user reviews and approves a pending memory)
- Implement a "memory inbox" — facts surface as pending review items before being committed to long-term store
- Provide a memory management screen where every stored item is visible and individually deletable
- Session conversation history (Ring buffer / sliding window) must be distinct from the long-term memory store

**Warning signs:**
- Memory store grows unboundedly without user action
- Sensitive health, financial, or location data appears in stored memories without explicit save action
- No way for user to audit what the assistant "knows"

**Phase to address:**
Phase 6 — memory and privacy. The data promotion policy must be defined before memory is wired into the AI routing layer.

---

### Pitfall 11: Privacy Mode Bypassed by Pending Background Request

**What goes wrong:**
The user enables "privacy mode" (cloud requests disabled). However, a background WorkManager task queued before privacy mode was enabled fires after the toggle and makes a cloud AI or TTS request. The privacy guarantee is violated even though the user believes it is enforced.

**Why it happens:**
Privacy mode is typically implemented as a DataStore flag checked at request call sites. Pending WorkManager tasks don't recheck this flag at execution time — only at enqueue time.

**How to avoid:**
- All cloud request points (`core.network`, `core.ai`, TTS client) must check privacy mode flag at the moment of execution, not at scheduling time
- WorkManager tasks must fail gracefully (not retry) when privacy mode is active at execution time
- Cancel queued network-dependent workers when privacy mode is activated
- Privacy mode state must be synchronously readable (not suspend) so it can be checked in network interceptors

**Warning signs:**
- Cloud API calls appear in network logs after privacy mode is toggled on
- Background worker logs show cloud requests executing after the privacy toggle
- Rate-limit hits on cloud APIs despite user believing privacy mode was active

**Phase to address:**
Phase 6 — memory and privacy. Privacy mode enforcement must be implemented as a cross-cutting network interceptor, not scattered per-call-site.

---

### Pitfall 12: Cyclic Gradle Module Dependencies Cause Build Failures

**What goes wrong:**
A feature module (e.g., `feature.voice`) imports from `feature.homecontrol` to display device state. `feature.homecontrol` imports from `feature.voice` to listen for commands. Gradle refuses to build. The team spends days restructuring module boundaries under deadline pressure.

**Why it happens:**
Feature modules feel like natural places to share data. The cyclic dependency isn't obvious during incremental development — it emerges when the second cross-feature import is added.

**How to avoid:**
- Enforce a strict DAG: `feature.*` modules may only depend on `core.*` modules, never on each other
- Shared domain types (device state, command events) belong in `core.domain`, not in a feature module
- Use the `api` vs `implementation` dependency audit: if a `feature.*` module ever uses `api(project(":feature:*"))`, that is an immediate red flag
- Add a Gradle task or CI check that fails if any `feature.*` → `feature.*` dependency is detected

**Warning signs:**
- Gradle sync shows `Circular dependency` error
- A feature module's `build.gradle.kts` references another feature module
- `core.domain` is sparse while feature modules grow large shared interfaces

**Phase to address:**
Phase 0 / scaffold phase — module dependency rules must be documented and enforced from day one. Retrofitting module boundaries after features are built is extremely costly.

---

### Pitfall 13: Android SpeechRecognizer Requires Network on Most Devices — No Offline Fallback

**What goes wrong:**
The app is designed to work offline. Android `SpeechRecognizer` (Google Speech Services) requires a network connection on most Android devices. When offline, `onError(ERROR_NETWORK)` fires immediately. The app shows a generic error instead of gracefully degrading.

**Why it happens:**
On-device speech recognition was added to Android in API 31+ via `RecognitionService` with `isOnDeviceRecognitionAvailable()`, but availability depends on the device having downloaded the offline model, which is not guaranteed.

**How to avoid:**
- Check `SpeechRecognizer.isOnDeviceRecognitionAvailable(context)` at startup and surface download status to user
- Implement an offline-graceful fallback: in offline mode, voice input falls back to text input with a clear UI indication
- Consider a two-path STT design in `core.audio`: online path (Android SpeechRecognizer) and offline path (text input or future Whisper integration)
- Never assume offline STT is available without explicit verification at runtime

**Warning signs:**
- Voice works on Wi-Fi but not in airplane mode
- `ERROR_NETWORK` (2) fires on every `startListening()` call without network
- No UI degradation — user sees spinner then generic error

**Phase to address:**
Phase 1 — voice pipeline. The offline fallback path must be designed into the STT abstraction, not added as a patch later.

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Hardcode HA local IP | Fast dev setup | Breaks outside home, breaks for any config change | Never — use DataStore from day 1 |
| Skip audio focus management | TTS just works in happy path | Interrupts phone calls, silent failure on Android 15+ | Never |
| Single string matching for intent parsing | Quick to implement | 80% cloud escalation rate for trivial commands | MVP only if telemetry is in place to fix it in sprint 2 |
| Store secrets in plain SharedPreferences | Zero setup | Token exfiltration risk on rooted devices | Never for HA token |
| Automatic long-term memory promotion | Feels "smart" | User trust violation, GDPR-type risk | Never without explicit review flow |
| One monolithic `viewModel` for all voice + chat + avatar state | Fast to prototype | Impossible to test, impossible to replace AI/avatar layer | Prototyping only, must split before Phase 3 |
| Room database on main thread (no `allowMainThreadQueries()`) | Avoid Room boilerplate | ANR on main thread blocking | Never — always use suspend / Flow |
| Skip Hilt, use manual DI | Simpler setup | Impossible to swap AI provider, test, or mock services | Only in throwaway prototypes |

---

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Home Assistant REST API | Using `http://` (plain) even locally | Allow user to configure, warn if using http vs https |
| Home Assistant REST API | Single long-lived token never refreshed | Expose "reconnect" and "revoke" flows in Settings |
| Home Assistant REST API | Treating all entity types as light toggle | Map entity domains (light, switch, script, scene) to distinct command types |
| Home Assistant WebSocket | Not reconnecting after app resume from background | Implement exponential backoff reconnect in `core.automation` |
| Android SpeechRecognizer | Not calling `destroy()` on lifecycle end | Destroy in `onCleared()` of ViewModel or service `onDestroy()` |
| Android TTS | Starting `speak()` before `onInit()` callback | Gate all `speak()` calls behind an initialized state flag |
| Android TTS | Not calling `shutdown()` on app exit | Memory leak; TTS engine keeps process alive |
| Cloud LLM API | Sending full conversation history on every message | Implement sliding window / summarization to control token costs |
| Cloud LLM API | No timeout on streaming responses | Long-running streams keep wakelock and network open; use `withTimeout()` |
| WorkManager | Using `PeriodicWorkRequest` for time-critical alerts | Use `AlarmManager.setAlarmClock()` for user-visible scheduled events |

---

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Avatar Lottie animation on main thread during state change | TTS audio starts 300ms after visual change | Fire TTS immediately on state change; treat avatar as async side-effect | Any device; worsens on mid-range hardware |
| Room query in voice command hot path | First command after cold start is slow (200-500ms) | Pre-warm DAO on app start via background coroutine | After cold start or process death |
| Full entity list refresh on every HA command | Network round-trip before every command | Cache entity list in memory; refresh on periodic background sync or HA WebSocket event | After first use on slow home network |
| StateFlow `SharingStarted.WhileSubscribed` without 5s replay timeout | UI recompose storms on screen resume | Use `SharingStarted.WhileSubscribed(5_000)` universally for screen-level state | Screen rotation, app resume |
| Cloud TTS called for every utterance including confirmations | Unnecessary latency + cost for short confirmations | Local TTS for confirmations (<10 words), cloud TTS for conversational replies | Per-utterance — cumulative cost grows |
| Memory store query without index on timestamp | Memory retrieval slows as history grows | Index `created_at` and `type` columns in Room schema from day 1 | After ~1000 memory entries |

---

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| HA long-lived token in plain SharedPreferences | Token extractable via ADB or root | Use `EncryptedSharedPreferences` (Android Keystore) |
| Cloud API key in `BuildConfig` (committed to repo) | Key leaked in public/shared repo | Use local `local.properties` + inject via Gradle, never commit |
| Logging full AI prompts/responses at DEBUG level | Sensitive user data in logcat (visible to other apps on older Android) | Log only intent type + routing tier, not content |
| No certificate pinning on HA connection | MITM on untrusted networks | Not required for MVP (local network), but document as a future hardening task |
| Privacy mode as client-side UI toggle only | Determined attacker (or bug) bypasses toggle | Enforce at network interceptor layer — all cloud calls check flag |
| Deep link handler trusts arbitrary `entity_id` from intent extras | Intent injection controls HA devices | Validate entity IDs against allowlist before executing |

---

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Silent failure when STT gets ERROR_NETWORK | User thinks app is broken; no feedback | Surface "Offline — voice unavailable" with text input fallback |
| Avatar frozen in THINKING state during long cloud response | User doesn't know if app is working | Add maximum thinking animation duration; surface timeout indicator after 5s |
| "I don't understand" for out-of-vocabulary commands with no alternatives | Frustrating dead end | Suggest closest match or offer "What can I ask?" help |
| HA command confirmation is silent (TTS only, no visual) | User unsure if command executed when in another room | Always update home control UI state regardless of TTS confirmation |
| Memory management buried in settings | Users don't know what is stored | Surface recent memories on a dedicated, easily accessible screen |
| Background briefing fires during call or media playback | Audio interruption at worst time | Check audio focus before speaking; respect `AUDIOFOCUS_LOSS_TRANSIENT` |
| Cold start latency — app takes 3+ seconds before accepting voice input | User taps button, nothing happens, tries again | Show "warming up" state; initialize voice session eagerly on foreground resume |

---

## "Looks Done But Isn't" Checklist

- [ ] **Voice Pipeline:** Recording works in foreground — verify it also works when app is backgrounded mid-session, and that foreground service type is declared
- [ ] **TTS:** `speak()` works in happy path — verify it handles `onInit()` failure, queued utterances don't overlap, and focus is abandoned after completion
- [ ] **Home Control:** Commands work on local Wi-Fi — verify behavior when HA is unreachable, entity IDs change, or response is malformed JSON
- [ ] **AI Router:** Cloud path works — verify deterministic parser covers the top 20 command patterns before cloud is declared the default fallback
- [ ] **Background Briefing:** Fires once in testing — verify it fires on Samsung/MIUI under battery optimization, and that it respects privacy mode at execution time
- [ ] **Privacy Mode:** Toggle prevents cloud calls in UI — verify all `core.network` interceptors check the flag, and that queued WorkManager tasks are cancelled
- [ ] **Memory Store:** Items are stored — verify each item is individually deletable, promote does not happen automatically, and Room migrations handle schema changes
- [ ] **Avatar:** State transitions look correct — verify avatar animation does not delay TTS onset; profile with Android Studio Layout Inspector
- [ ] **Offline Mode:** Basic commands work — verify Room/DataStore are loaded before first command, HA local URL is tried first, and graceful error messages appear

---

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| SpeechRecognizer on wrong thread | LOW | Move to Dispatchers.Main; typically a 1-file fix |
| Missing foreground service type | LOW | Manifest + permission addition; 30-min fix but requires re-test on Android 14+ |
| Audio focus not managed | MEDIUM | Add AudioFocusRequest wrapper to `core.audio`; all TTS call sites update |
| Cyclic module dependency | HIGH | Identify shared types, extract to `core.domain`, update all importers; 1-3 days |
| HA token in plain SharedPreferences | MEDIUM | Migrate to EncryptedSharedPreferences with one-time migration on app update |
| Automatic memory promotion shipped | HIGH | Requires data deletion UX, migration, and user-visible "we changed how memory works" — trust damage |
| Avatar blocking TTS | MEDIUM | Decouple animation trigger from TTS trigger; profile + fix per-state |
| WorkManager used for time-critical briefings | MEDIUM | Replace `PeriodicWorkRequest` with `AlarmManager.setAlarmClock()`; retest on OEM devices |

---

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| SpeechRecognizer main-thread requirement | Phase 1 — Voice Pipeline | Thread assertion in factory; test on background coroutine context (should assert) |
| Missing `foregroundServiceType="microphone"` | Phase 1 — Voice Pipeline | Manifest review; test on Android 14 emulator that service starts cleanly |
| Audio focus not managed | Phase 1 — TTS Integration | AudioFocusRequest wrapper in place; test that phone call interrupts TTS |
| Voice state machine no timeout | Phase 1 — Voice State Machine | State machine unit test: inject `ERROR_RECOGNIZER_BUSY`, verify IDLE transition |
| AI router falls through to cloud too often | Phase 2 — AI Routing | Routing telemetry log; >90% of home control commands hit deterministic tier |
| HA hardcoded IP | Phase 3 — Home Control | URL configurable in settings screen; integration test with wrong IP shows graceful error |
| HA token unencrypted | Phase 3 — Home Control | Code review + `adb shell` token check — must not be plaintext |
| WorkManager timing unreliable | Phase 4 — Background Automations | Test briefing on Samsung physical device; AlarmManager used for time-critical events |
| Avatar blocking voice response | Phase 5 — Avatar Integration | Profiler measurement: TTS onset time with/without avatar must be within 50ms |
| Auto memory promotion | Phase 6 — Memory & Privacy | Memory store query after long session — no entries unless user explicitly triggered save |
| Privacy mode bypass | Phase 6 — Memory & Privacy | Network interceptor unit test: privacy mode ON → all outbound cloud calls blocked |
| Cyclic module dependencies | Phase 0 — Scaffold | Gradle dependency graph check; CI rule: no `feature.*` → `feature.*` edges |
| SpeechRecognizer offline failure | Phase 1 — Voice Pipeline | Airplane mode test: app shows graceful "offline" message, not crash |

---

## Sources

- Android Developers — Foreground service types (microphone, mediaPlayback): https://developer.android.com/develop/background-work/services/fgs/service-types
- Android Developers — Background execution restrictions (Doze, OEM battery): https://developer.android.com/topic/performance/background-optimization
- Android Developers — Audio focus management (CONTENT_TYPE_SPEECH, Android 15+ restrictions): https://developer.android.com/media/optimize/audio-focus
- Android Developers — App architecture recommendations (ViewModel anti-patterns, UDF): https://developer.android.com/topic/architecture/recommendations
- Android Developers — Modularization patterns (cyclic deps, api vs implementation, coupling): https://developer.android.com/topic/modularization/patterns
- Android Developers — SpeechRecognizer API reference (main-thread requirement, error codes): https://developer.android.com/reference/android/speech/SpeechRecognizer
- Training knowledge (MEDIUM confidence): Android TTS `onInit` lifecycle, WorkManager OEM reliability, Room threading, Hilt scoping, EncryptedSharedPreferences patterns

---

*Pitfalls research for: Android AI Companion / Voice Assistant (Kotlin, Jetpack Compose, Home Assistant, hybrid AI routing)*
*Researched: 2026-03-18*
