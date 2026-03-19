---
phase: 02-voice-home-control-mvp
plan: 07
subsystem: ui
tags: [compose, hilt, mvi, speech-recognition, tts, voice, animation]

# Dependency graph
requires:
  - phase: 02-01
    provides: VoiceState/VoiceEvent/VoiceUiEffect domain models, repository interfaces
  - phase: 02-04
    provides: SpeechRecognizerManager, TextToSpeechManager, AudioFocusManager
  - phase: 02-05
    provides: CommandParser, HomeAssistantRepository
  - phase: 02-06
    provides: ReminderScheduler
provides:
  - VoiceViewModel: complete MVI state machine (STT -> parse -> execute -> TTS pipeline)
  - VoiceScreen: animated mic button + last reply card composable with permission handling
  - AnimatedMicButton: state-driven animated FAB with pulse/progress/waveform animations
  - LastReplyCard: animated visibility card showing last assistant reply
affects: [app, navigation, 03-conversational-ai, integration-tests]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - MVI state machine with StateFlow + Channel for effects
    - rememberInfiniteTransition for looping animations (pulse, waveform)
    - animateColorAsState for smooth state-driven color transitions
    - AnimatedVisibility for conditional composable appearance

key-files:
  created:
    - feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceViewModel.kt
    - feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/component/AnimatedMicButton.kt
    - feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/component/LastReplyCard.kt
  modified:
    - feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceScreen.kt
    - feature/voice/build.gradle.kts

key-decisions:
  - "VoiceViewModel uses StateFlow for state + Channel(BUFFERED) for one-shot UI effects — ensures effects are delivered exactly once without loss"
  - "ListeningPulseContent/SpeakingWaveContent extracted as private composables — keeps AnimatedMicButton body readable"
  - "AnimatedMicButton uses Box+clip(CircleShape)+clickable rather than FloatingActionButton — full control over content alignment for all 6 states"
  - "LastReplyCard uses reply.orEmpty() to satisfy non-null Text contract while AnimatedVisibility(visible = reply != null) controls visibility"

patterns-established:
  - "Pattern 1: MVI ViewModel — sealed interface state sealed interface events sealed interface effects, StateFlow/Channel, viewModelScope.launch in onEvent"
  - "Pattern 2: Animated state button — animateColorAsState for background, per-state inner content, semantics contentDescription per state"
  - "Pattern 3: Permission-gated feature — check ContextCompat.checkSelfPermission before first use, rememberLauncherForActivityResult for runtime request"

requirements-completed: [VOIC-01, VOIC-02, VOIC-03, VOIC-04, VOIC-05, VOIC-06, TTS-01, TTS-02, TTS-03, HOME-01, HOME-02, HOME-05, HOME-07]

# Metrics
duration: 3min
completed: 2026-03-19
---

# Phase 02 Plan 07: Voice Interaction Pipeline Summary

**MVI VoiceViewModel wiring STT->CommandParser->HomeAssistantRepository->TTS with animated 6-state mic button composable and permission-gated VoiceScreen**

## Performance

- **Duration:** 3 min
- **Started:** 2026-03-19T13:30:30Z
- **Completed:** 2026-03-19T13:33:30Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- VoiceViewModel: full MVI state machine with all 6 VoiceState transitions, 8s recognition timeout, VOIC-06 TTS interrupt on mic tap during Speaking, ERROR_RECOGNIZER_BUSY retry, audio focus acquire/release, message + audit log persistence
- AnimatedMicButton: state-driven animated FAB — pulse scale animation for Listening, CircularProgressIndicator for Transcribing/Processing, staggered waveform bars for Speaking, error color for Error, contentDescription per state
- VoiceScreen: hiltViewModel() + collectAsStateWithLifecycle, RECORD_AUDIO runtime permission request, effects channel collector (Toast + Vibrate), Spacing.lg gap between mic and reply card

## Task Commits

Each task was committed atomically:

1. **Task 1: Create VoiceViewModel MVI state machine** - `0cc7018` (feat)
2. **Task 2: Create VoiceScreen with AnimatedMicButton and LastReplyCard** - `9a15026` (feat)

**Plan metadata:** TBD (docs: complete plan)

## Files Created/Modified
- `feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceViewModel.kt` - MVI ViewModel with full STT->parse->execute->TTS pipeline, error handling, message + audit log persistence
- `feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/component/AnimatedMicButton.kt` - State-driven animated FAB with 6 visual states
- `feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/component/LastReplyCard.kt` - AnimatedVisibility card with displaySmall typography
- `feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceScreen.kt` - Full implementation replacing placeholder
- `feature/voice/build.gradle.kts` - Added core:audio, core:ai, core:data, core:network, core:automation deps

## Decisions Made
- VoiceViewModel uses StateFlow for state + Channel(BUFFERED) for one-shot UI effects — ensures effects are delivered exactly once without loss
- AnimatedMicButton uses Box+clip(CircleShape)+clickable rather than FloatingActionButton — full control over content alignment for all 6 states
- ListeningPulseContent and SpeakingWaveContent extracted as private composables — keeps AnimatedMicButton body readable

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Restored accidentally deleted SettingsRepositoryImpl.kt**
- **Found during:** Pre-task check (git status showed D core/data/.../SettingsRepositoryImpl.kt)
- **Issue:** SettingsRepositoryImpl.kt was deleted from working tree in a prior session; VoiceViewModel depends on SettingsRepository binding
- **Fix:** git checkout -- to restore the file from last commit
- **Files modified:** core/data/src/main/kotlin/com/example/aicompanion/core/data/repository/SettingsRepositoryImpl.kt
- **Verification:** git status shows file no longer deleted
- **Committed in:** Not committed separately (file was already in git history; restore was a working-tree-only operation)

---

**Total deviations:** 1 auto-fixed (blocking pre-existing deletion)
**Impact on plan:** Restoration of pre-existing file required for correct DI graph. No scope creep.

## Issues Encountered
- VoiceViewModel.kt already existed as an untracked file from a prior incomplete session — the file content matched the plan exactly, so it was staged and committed as Task 1 with no changes needed.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Full voice interaction pipeline is complete end-to-end: mic tap -> STT -> CommandParser -> HA/Reminder execution -> TTS reply
- VoiceScreen replaces the placeholder and is nav-ready via VOICE_ROUTE constant
- All 13 requirements (VOIC-01 through VOIC-06, TTS-01 through TTS-03, HOME-01, HOME-02, HOME-05, HOME-07) are implemented
- Ready for Phase 03 conversational AI integration

## Self-Check: PASSED

All created files verified on disk. All task commits verified in git log.

---
*Phase: 02-voice-home-control-mvp*
*Completed: 2026-03-19*
