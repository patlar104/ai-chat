# Feature Research

**Domain:** Android voice-first AI companion app (personal use, smart-home control, background automation)
**Researched:** 2026-03-18
**Confidence:** MEDIUM — based on training knowledge through Aug 2025 of the Android assistant / AI companion space (Google Assistant, Bixby, Alexa, Replika, Pi, Character.AI, Home Assistant Companion). WebSearch unavailable; findings cross-checked against PROJECT.md requirements and established Android development patterns.

---

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these = product feels broken or unfinished.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Button-to-talk voice activation | Every voice assistant works this way; hold-to-talk or tap-to-talk is the universal input model | LOW | Push-to-talk avoids always-on battery drain. Android AudioRecord + SpeechRecognizer. Must feel instant — > 300ms button lag destroys UX |
| Speech-to-text (STT) | Core of any voice interface; typing defeats the purpose | LOW | Android platform SpeechRecognizer is baseline. Works without extra setup. Offline via on-device models (limited vocabulary) |
| Text-to-speech (TTS) reply | Users expect spoken answers, not just text on screen | LOW | Android TextToSpeech is baseline (robotic but functional). Cloud TTS (ElevenLabs, Google Neural TTS) needed for companion feel |
| Natural language command parsing | "Turn off the living room lights" must work; rigid command syntax kills trust | MEDIUM | Hybrid: deterministic parser for known commands (fast, offline), LLM fallback for ambiguous input |
| Visual feedback of assistant state | Idle / listening / thinking / speaking / error states — users need to know when it's paying attention | LOW | State machine with UI indicators. Avatar or ring animation. Critical for voice sessions where there's no physical feedback |
| Spoken confirmation of actions | "Done, turning off the lights" closes the feedback loop users expect | LOW | TTS reply + action result. Short confirmations use local TTS for speed |
| Conversation history / chat log | Users scroll back to see what was said and decided | LOW | Room DB, displayed in reverse-chron chat UI. Table stakes for any chat-adjacent interface |
| Basic reminders and alarms | Everyone expects "remind me at 3pm to call mom" | MEDIUM | AlarmManager + notification. Integration with conversation flow. On-device, no cloud required |
| Settings and personalization | Name, voice preference, notification prefs, privacy toggles | LOW | DataStore for user prefs. Standard Android settings structure |
| Offline degraded mode | When network is unavailable, core commands should still work | MEDIUM | Local TTS, local STT (limited), local command parser, cached memory. Graceful "I'm offline" messaging for cloud-only ops |
| Notification for background events | Briefings, reminders, and automation results need a delivery channel | LOW | Android NotificationManager, NotificationChannel setup. Required for background-to-foreground communication |

### Differentiators (Competitive Advantage)

Features that set this product apart. Not universally expected, but create strong retention when done well.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Expressive 2D avatar with voice-sync | Presence creates emotional connection that pure voice lacks; avatar states (listening, thinking, speaking) make the AI feel alive without 3D complexity | MEDIUM | Jetpack Compose animation or Lottie for state-driven transitions. Lip-sync approximation via amplitude envelope from TTS audio output. 2D is the right call — 3D adds months, not weeks |
| Hybrid AI routing (deterministic → on-device → cloud) | Most assistants go straight to cloud. Hybrid means home-control commands are instant and private; open-ended questions still get full LLM reasoning | HIGH | Three-tier: regex/rule parser for known intents, on-device model (Gemini Nano / llama.cpp) for offline reasoning, cloud LLM for complex tasks. Requires intent classification layer |
| Home Assistant integration | Broad smart-home device coverage via one integration point; HA is the de facto open standard for home automation | HIGH | Home Assistant REST API + WebSocket for real-time events. Device/room alias resolution. Routine execution. Requires HA server discovery or manual IP entry |
| Persistent user memory with explicit review | "Remember that I prefer morning briefings" + ability to see and delete stored facts — trust through transparency | MEDIUM | Room entity for memory items. Types: preferences, aliases, facts, recurring patterns. User-facing memory management screen is the differentiator (most apps hide or don't offer this) |
| Two-tier TTS (utility vs companion) | Local TTS for fast confirmations ("Done"), neural TTS for conversational replies — right voice for context without sacrificing speed | MEDIUM | Conditional routing based on response type. Local: < 100ms. Cloud neural: 500-1500ms but significantly higher quality. Requires response classification |
| Background briefings via WorkManager | Proactive assistant behavior — morning brief, weather, scheduled summaries — without user initiation | MEDIUM | WorkManager periodic tasks. Briefing assembly from configured sources. Delivered via notification + optionally TTS autoplay. Differentiates from purely reactive assistants |
| Privacy mode (cloud lockout) | Explicit user-controlled toggle that prevents all cloud requests — for sensitive conversations | LOW | Single DataStore boolean checked in AI router before any network call. Simple but high trust value. Visible indicator in UI when active |
| Device and room alias resolution | "Turn off the study" works even though Home Assistant calls it "study_lights_main" — natural language aliasing | MEDIUM | Alias map in Room DB. Fuzzy match on spoken room/device names. Editable by user. Required to make home control feel natural vs brittle |
| Scheduled automations (time/event triggers) | "Every morning at 7, run my wake-up routine" — moves assistant from reactive to proactive | MEDIUM | WorkManager for time-based. Event-based triggers (e.g., arriving home) require geofencing — add complexity. Start with time-only |
| Conversation context window | "Turn it up" referencing the last mentioned device — stateful conversation, not just single-turn commands | MEDIUM | Short-term context buffer (last N turns) passed to intent resolver. Needed for natural multi-turn home control flows |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good on the surface but create significant problems and should be explicitly deferred.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Always-on hotword detection ("Hey Jarvis") | Feels like real sci-fi assistant; no button press needed | Battery drain is severe (continuous mic recording); Android background mic restrictions since API 29 make this unreliable; false positive rate creates trust issues; Play Store compliance risk | Button-to-talk is fast enough for personal use; revisit only after battery baseline is established |
| Autonomous purchasing / high-risk transactions | "Buy more coffee pods when I'm running low" — compelling for automation | No confirmation model built yet; wrong execution = real money spent; requires secure credential handling, OAuth flows, retry logic | Build confirmation-first transaction model in v2; start with read-only integrations (check inventory, price) |
| Full 3D animated avatar | Looks impressive in demos; suggests realism | Adds 2-6 months of art/engineering work; 3D rendering competes with audio processing for CPU/GPU on mid-range Android devices; frame drops during voice sessions destroy UX | 2D expressive states (Lottie or Compose animated) deliver 80% of the emotional value at 10% of the cost |
| Broad cross-app UI automation (accessibility service) | "Send a WhatsApp message to John" is genuinely useful | Android AccessibilityService APIs are unstable across OEM skins; Google Play policy around accessibility APIs is strict and evolving; app-to-app UI scripting breaks with app updates | Use explicit intents and Share APIs for structured cross-app actions; deep integrations (WhatsApp, calendar) via proper APIs where available |
| Multi-user household profiles | "My partner has different preferences" | Massively increases auth and memory complexity; session ownership ambiguity; personal use validated first | Build single-user profile model cleanly so it can be extended; defer multi-user until single-user model is proven |
| Real-time streaming voice conversation | No push-to-talk; continuous duplex listening like a phone call | Requires echo cancellation, VAD (voice activity detection), barge-in handling, streaming STT — each is a significant engineering problem; battery impact multiplies | Button-to-talk is predictable and reliable; streaming is a v2+ enhancement after base interaction model is solid |
| Always-visible floating overlay widget | Assistant always on screen over other apps | Requires SYSTEM_ALERT_WINDOW permission; restricted in Android 12+; creates UX confusion when overlaid on other apps | Notification-based quick access or Android widget on home screen are lower-friction alternatives |
| Fully local LLM inference for complex reasoning | Privacy-maximalist appeal; no cloud dependency | On-device LLMs sufficient for complex reasoning (7B+) require 4-8GB RAM — not available on mid-range Android; inference latency for complex queries is 10-60s on device; quantized small models miss quality bar | Hybrid model is correct: local for known intents and simple ops, cloud LLM for reasoning. Make cloud optional, not absent |

---

## Feature Dependencies

```
[Button-to-Talk Voice Shell]
    └──requires──> [STT (SpeechRecognizer)]
    └──requires──> [Visual State Machine (idle/listen/think/speak/error)]
    └──requires──> [TTS Reply]
                       └──requires──> [Two-Tier TTS Router]

[Natural Language Command Parsing]
    └──requires──> [STT]
    └──requires──> [Intent Classification Layer]
                       └──requires──> [Deterministic Parser]
                       └──requires──> [Hybrid AI Router]
                                          └──requires──> [On-Device AI (optional)]
                                          └──requires──> [Cloud AI Client]
                                          └──requires──> [Privacy Mode Toggle]

[Home Control Execution]
    └──requires──> [Natural Language Command Parsing]
    └──requires──> [Device/Room Alias Resolution]
    └──requires──> [Home Assistant API Client]
    └──enhances──> [Conversation Context Window] (stateful multi-turn: "turn it up")

[Persistent Memory]
    └──requires──> [Room DB schema for memory items]
    └──enhances──> [Natural Language Command Parsing] (preferences inform routing)
    └──enhances──> [Background Briefings] (personalized content)

[Background Briefings / Automations]
    └──requires──> [WorkManager setup]
    └──requires──> [Notification channel]
    └──enhances──> [Persistent Memory] (personalized briefing content)
    └──requires──> [TTS Reply] (optional autoplay on trigger)

[Expressive 2D Avatar]
    └──requires──> [Visual State Machine]
    └──enhances──> [Button-to-Talk Voice Shell] (state-driven visual feedback)
    └──enhances──> [TTS Reply] (lip-sync / speaking animation)

[Conversation History / Chat Log]
    └──requires──> [Room DB]
    └──enhances──> [Conversation Context Window] (context drawn from history)

[Reminders]
    └──requires──> [Natural Language Command Parsing] (parse "remind me at 3pm...")
    └──requires──> [AlarmManager / WorkManager]
    └──requires──> [Notification channel]

[Privacy Mode]
    └──enhances──> [Hybrid AI Router] (blocks cloud leg)
    └──conflicts──> [Cloud Neural TTS] (cloud TTS disabled in privacy mode)
```

### Dependency Notes

- **Voice Shell requires STT before anything else:** All voice features are blocked until SpeechRecognizer is wired and tested. This makes STT/TTS the Phase 1 foundation.
- **Home Control requires Alias Resolution:** Without alias resolution, home control commands fail on natural names. Both must land together.
- **Privacy Mode must be in AI Router, not UI:** Checking privacy mode at the network call site (not just hiding buttons) ensures no accidental cloud leakage.
- **Avatar enhances but doesn't block:** Avatar states can be stubbed with a simple ring/icon in early phases. Real animation is additive, not required for voice function.
- **Background Briefings requires WorkManager + Notification channels:** Both must be set up before briefings can deliver. WorkManager should be scaffolded in the same phase as reminders to reuse infrastructure.
- **Conversation Context Window enhances Home Control:** Needed for natural use but can ship after single-turn home control is working. Don't block home control on context.

---

## MVP Definition

### Launch With (v1)

Minimum viable product — what's needed to validate that the concept works daily.

- [ ] Button-to-talk voice shell with visual state machine — core interaction loop
- [ ] STT (Android SpeechRecognizer) + TTS (local Android TTS, cloud TTS for companion mode) — speech in, speech out
- [ ] Deterministic command parser + Home Assistant integration — execute the main daily use case
- [ ] Device/room alias resolution — makes home control natural, not brittle
- [ ] Hybrid AI router (deterministic → cloud) with privacy mode toggle — handles edge cases, respects privacy
- [ ] Basic reminders via AlarmManager — covers the most common personal assistant request
- [ ] Conversation history persisted in Room — users need to review what happened
- [ ] Persistent memory (preferences + aliases) with review/delete screen — trust through transparency
- [ ] Expressive 2D avatar (state-driven, not full animation) — presence matters from day one
- [ ] Offline degraded mode — local commands and reminders work without network
- [ ] WorkManager background briefings (time-based only) — proactive behavior is a key differentiator

### Add After Validation (v1.x)

Features to add once the core daily loop is working and trusted.

- [ ] Cloud neural TTS upgrade — add after local TTS proves too robotic for companion interactions
- [ ] Conversation context window (multi-turn "it" references) — add once single-turn commands are solid
- [ ] On-device LLM integration (Gemini Nano) — add when offline reasoning quality becomes a complaint
- [ ] Event-based automations (geofence triggers) — add after time-based automations are well-used
- [ ] Enhanced avatar animation (Lottie, lip-sync) — add when basic presence is validated

### Future Consideration (v2+)

Features to defer until personal daily use is proven.

- [ ] Always-on hotword detection — battery baseline must be established first
- [ ] Streaming/duplex voice conversation — major engineering lift; validate button-to-talk UX first
- [ ] Autonomous transactions with confirmation model — needs dedicated security/confirmation architecture
- [ ] Cross-app UI automation — AccessibilityService risk and complexity; defer until explicit need
- [ ] Multi-user household profiles — personal use proven first
- [ ] Full 3D avatar — defer entirely until 2D presence is validated as insufficient

---

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Button-to-talk voice shell | HIGH | LOW | P1 |
| STT (Android SpeechRecognizer) | HIGH | LOW | P1 |
| Local TTS (Android) | HIGH | LOW | P1 |
| Visual state machine (idle/listen/think/speak) | HIGH | LOW | P1 |
| Deterministic command parser | HIGH | MEDIUM | P1 |
| Home Assistant integration | HIGH | HIGH | P1 |
| Device/room alias resolution | HIGH | MEDIUM | P1 |
| Conversation history (Room) | HIGH | LOW | P1 |
| Persistent memory + review UI | HIGH | MEDIUM | P1 |
| Basic reminders | HIGH | MEDIUM | P1 |
| Hybrid AI router + cloud LLM | HIGH | HIGH | P1 |
| Privacy mode toggle | HIGH | LOW | P1 |
| Expressive 2D avatar (state-driven) | MEDIUM | MEDIUM | P1 |
| Offline degraded mode | HIGH | MEDIUM | P1 |
| WorkManager background briefings | MEDIUM | MEDIUM | P1 |
| Cloud neural TTS | MEDIUM | MEDIUM | P2 |
| Conversation context window | MEDIUM | MEDIUM | P2 |
| On-device LLM (Gemini Nano) | MEDIUM | HIGH | P2 |
| Event-based automations (geofence) | LOW | HIGH | P3 |
| Enhanced avatar animation (Lottie) | LOW | MEDIUM | P2 |
| Always-on hotword | MEDIUM | HIGH | P3 |
| Streaming voice conversation | MEDIUM | HIGH | P3 |
| Autonomous transactions | LOW | HIGH | P3 |
| Full 3D avatar | LOW | HIGH | P3 |
| Multi-user profiles | LOW | HIGH | P3 |

**Priority key:**
- P1: Must have for launch — daily use is not valid without it
- P2: Should have, add after core is stable
- P3: Nice to have, future consideration or explicitly deferred

---

## Competitor Feature Analysis

Reference apps analyzed from training knowledge: Google Assistant (Android), Samsung Bixby, Amazon Alexa (Android), Replika, Pi AI, Character.AI, Home Assistant Companion.

| Feature | Google Assistant / Bixby | Alexa Android | Replika / Pi | Our Approach |
|---------|--------------------------|---------------|--------------|--------------|
| Voice activation | Always-on hotword | Always-on hotword | Button-to-talk or text | Button-to-talk (battery-safe personal use) |
| Avatar / presence | None (audio-only) | None | 3D avatar (Replika), minimal (Pi) | 2D expressive states — presence without 3D cost |
| Home control | Deep Google Home / SmartThings | Alexa ecosystem only | None | Home Assistant (vendor-agnostic, broader coverage) |
| Memory | Implicit, opaque, cloud-only | Implicit, cloud-only | Explicit in Replika (relationship memory) | Explicit + user-reviewable + local-first |
| Privacy mode | No explicit toggle | No explicit toggle | No explicit toggle | First-class toggle, cloud lockout enforced in router |
| Offline capability | Partial (basic commands) | Very limited | None | Core commands, local TTS, reminders, memory — offline |
| Background proactive tasks | Limited (reminders only) | Routines (Alexa) | None | WorkManager briefings + time-based automations |
| Conversation context | Yes (Google) | Limited | Strong (Replika) | Context window for multi-turn within session |
| On-device AI | Gemini Nano (Pixel 8+) | No | No | Optional, additive in hybrid router |

**Key gaps we fill that competitors don't:**
- Home Assistant integration = vendor-agnostic home control (vs locked ecosystems)
- Explicit memory review screen (vs opaque cloud memory)
- Privacy mode with hard cloud lockout (vs trust-based)
- 2D avatar presence without 3D complexity (vs no avatar at all)
- Offline-capable core (vs degraded to nothing without network)

---

## Sources

- Training knowledge (confidence: MEDIUM) — Android developer documentation patterns, Google Assistant / Bixby / Alexa feature sets as of Aug 2025
- PROJECT.md requirements and architectural decisions — treated as HIGH confidence ground truth for this project's scope
- Android WorkManager, SpeechRecognizer, AlarmManager, TextToSpeech — established platform APIs, HIGH confidence on capabilities
- Home Assistant REST API and WebSocket — documented open-source project, HIGH confidence on integration pattern
- Replika / Pi / Character.AI — companion app feature analysis from public product descriptions, MEDIUM confidence
- Note: WebSearch unavailable during this research session. Feature categorizations are based on domain knowledge and PROJECT.md alignment, not current web sources. Recommend validating anti-feature list (especially AccessibilityService policy status) against current Google Play policy before v1.x planning.

---
*Feature research for: Android voice-first AI companion app*
*Researched: 2026-03-18*
