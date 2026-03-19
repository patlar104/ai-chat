---
phase: 02-voice-home-control-mvp
plan: 04
subsystem: audio
tags: [speech-recognizer, text-to-speech, audio-focus, hilt, coroutines, stateflow, android]

# Dependency graph
requires:
  - phase: 02-01
    provides: MainDispatcher qualifier and dispatcher qualifiers for main-thread enforcement

provides:
  - SpeechRecognizerManager: main-thread-safe STT wrapper with all error codes handled
  - TextToSpeechManager: lifecycle-safe TTS with observable isSpeaking StateFlow
  - AudioFocusManager: AUDIOFOCUS_GAIN_TRANSIENT focus request/abandon for TTS sessions
  - SpeechResult sealed interface: Success and Error subtypes for callback-based STT results

affects:
  - 02-07-voice-viewmodel
  - 02-08-voice-screen

# Tech tracking
tech-stack:
  added: []
  patterns:
    - withContext(mainDispatcher) to enforce main-thread SpeechRecognizer creation
    - StateFlow for observable TTS isSpeaking state
    - Callback-based RecognitionListener bridged to Kotlin lambdas
    - AudioFocusRequest.Builder with AUDIOFOCUS_GAIN_TRANSIENT for transient TTS focus

key-files:
  created:
    - core/audio/src/main/kotlin/com/example/aicompanion/core/audio/model/SpeechResult.kt
    - core/audio/src/main/kotlin/com/example/aicompanion/core/audio/speech/SpeechRecognizerManager.kt
    - core/audio/src/main/kotlin/com/example/aicompanion/core/audio/tts/TextToSpeechManager.kt
    - core/audio/src/main/kotlin/com/example/aicompanion/core/audio/focus/AudioFocusManager.kt
  modified:
    - core/audio/src/main/kotlin/com/example/aicompanion/core/audio/di/AudioModule.kt
    - core/audio/build.gradle.kts

key-decisions:
  - "SpeechRecognizerManager uses withContext(mainDispatcher) on every API call — enforces Looper.getMainLooper() assertion at runtime"
  - "TextToSpeechManager.speak() gated by initialized flag from onInit callback — never speaks before TTS engine is ready"
  - "AudioFocusManager uses USAGE_ASSISTANT + CONTENT_TYPE_SPEECH + AUDIOFOCUS_GAIN_TRANSIENT for correct audio session classification"
  - "AudioModule stays as empty object — all 3 managers use @Inject constructor + @Singleton, so Hilt provisions them automatically without explicit @Provides"

patterns-established:
  - "Pattern 1: All SpeechRecognizer API calls wrapped in withContext(mainDispatcher) with runtime Looper assertion"
  - "Pattern 2: TTS lifecycle guarded by initialized flag; UtteranceProgressListener updates StateFlow for UI reactivity"
  - "Pattern 3: Audio focus requested before TTS speak, abandoned in onDone/onError callbacks"

requirements-completed: [VOIC-01, VOIC-03, VOIC-05, VOIC-06, TTS-01, TTS-02, TTS-03]

# Metrics
duration: 12min
completed: 2026-03-19
---

# Phase 02 Plan 04: Audio Pipeline Summary

**Main-thread-enforced SpeechRecognizerManager, observable-state TextToSpeechManager, and AUDIOFOCUS_GAIN_TRANSIENT AudioFocusManager built as Hilt-injectable singletons completing the Android audio pipeline**

## Performance

- **Duration:** 12 min
- **Started:** 2026-03-19T00:20:10Z
- **Completed:** 2026-03-19T00:32:00Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments

- Built SpeechRecognizerManager with Looper.getMainLooper() assertion and all 9 error code handlers, using withContext(mainDispatcher) in every SpeechRecognizer API call
- Built TextToSpeechManager with isSpeaking StateFlow, initialized guard on speak(), QUEUE_FLUSH for interrupt support (VOIC-06), and getAvailableVoices/setVoice for SETT-03
- Built AudioFocusManager with AUDIOFOCUS_GAIN_TRANSIENT, USAGE_ASSISTANT, CONTENT_TYPE_SPEECH, and proper abandonFocus() for TTS session lifecycle
- Deleted CoreAudio.kt placeholder; all 3 managers auto-provisioned via Hilt @Inject constructor

## Task Commits

Each task was committed atomically:

1. **Task 1: Create SpeechRecognizerManager and SpeechResult model** - `011961c` (feat)
2. **Task 2: Create TextToSpeechManager, AudioFocusManager, and update AudioModule** - `f086a22` (feat)

**Plan metadata:** _(docs commit to follow)_

## Files Created/Modified

- `core/audio/src/main/kotlin/com/example/aicompanion/core/audio/model/SpeechResult.kt` - Sealed interface with Success(text) and Error(code, message) subtypes
- `core/audio/src/main/kotlin/com/example/aicompanion/core/audio/speech/SpeechRecognizerManager.kt` - Main-thread SpeechRecognizer wrapper with Looper assertion and full error handling
- `core/audio/src/main/kotlin/com/example/aicompanion/core/audio/tts/TextToSpeechManager.kt` - TTS lifecycle manager with observable isSpeaking StateFlow and speak() initialization guard
- `core/audio/src/main/kotlin/com/example/aicompanion/core/audio/focus/AudioFocusManager.kt` - Audio focus acquisition with AUDIOFOCUS_GAIN_TRANSIENT and USAGE_ASSISTANT
- `core/audio/src/main/kotlin/com/example/aicompanion/core/audio/di/AudioModule.kt` - Updated comment explaining auto-provision via @Inject constructor
- `core/audio/build.gradle.kts` - Already had :core:domain dependency (pre-existing)

## Decisions Made

- AudioModule remains an empty object — all 3 managers use @Inject constructor + @Singleton, making explicit @Provides unnecessary. Retained as extension point for future bindings.
- SpeechRecognizerManager does not hold a @Singleton annotation — it is not a singleton because it can be destroyed and recreated across voice sessions. TextToSpeechManager and AudioFocusManager are @Singleton.

## Deviations from Plan

None - plan executed exactly as written. SpeechResult.kt and SpeechRecognizerManager.kt were already partially scaffolded as untracked files; verified against acceptance criteria before committing.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- All 3 audio managers are ready for VoiceViewModel (Plan 07) to orchestrate
- SpeechRecognizerManager.initialize() must be called before startListening() — caller responsibility
- TextToSpeechManager.initialize(onReady) must be called before speak() — enforced by initialized guard
- AudioFocusManager.requestFocusForTts() must be called before speak() and abandonFocus() after onDone/onError

---
*Phase: 02-voice-home-control-mvp*
*Completed: 2026-03-19*
