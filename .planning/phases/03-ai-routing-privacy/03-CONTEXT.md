# Phase 3: AI Routing + Privacy - Context

**Gathered:** 2026-03-19
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase establishes the central AI routing logic and enforces privacy at the network level. It bridges the gap between the simple deterministic parser and the cloud-based LLM reasoning. Key deliverables include a central `AiRouter`, a privacy-enforcing network interceptor, and UI indicators for the current connectivity/privacy state.

</domain>

<decisions>
## Implementation Decisions

### AI Routing Strategy (ROUT-01, ROUT-02, ROUT-03)
- **Deterministic First:** All user transcripts are first processed by the `CommandParser` (Regex-based). If a match is found, the intent is executed immediately without any AI model involvement.
- **Cloud Fallback:** If `CommandParser` returns `Unknown`, the request is routed to the Cloud AI tier (e.g., Gemini API via Vertex AI or similar).
- **Local AI (Future-proofing):** The architecture must support a `LocalAi` tier (ROUT-V2-01), but for Phase 3, this tier will return `Unknown` and trigger the Cloud fallback.
- **Tier Logging:** Every turn must record which tier handled it in the `Message` model (`source_type` field: `DETERMINISTIC`, `CLOUD`, `UNKNOWN`).

### Privacy Mode Enforcement (PRIV-01, PRIV-02)
- **Interceptor-level Lockout:** Privacy mode is NOT just a UI toggle. A dedicated `PrivacyInterceptor` in `:core:network` will intercept all outbound requests.
- **Whitelist Approach:** While Privacy Mode is ON, only requests to the configured Home Assistant Local URL are allowed. All requests to cloud AI domains (e.g., `generativelanguage.googleapis.com`) or cloud TTS domains must be blocked (return a synthetic "Privacy Mode Active" error).
- **Persistence:** Privacy Mode state is stored in `DataStore` and observed by the interceptor.

### Degraded Mode UI & Communication (PRIV-03, PRIV-06, ROUT-04)
- **Visual Badge:** The `VoiceScreen` and `ChatScreen` must display a subtle but clear "Privacy Mode" or "Offline" indicator when those states are active.
- **Spoken Feedback:** If a user issues a command that requires Cloud AI while Privacy Mode or Offline mode is active, the assistant should reply with a specific local TTS message: "I can't answer that right now because [Privacy Mode / Offline mode] is active. I can still control your home and set reminders."
- **Audit Log:** Blocks caused by Privacy Mode are recorded in the Audit Log as "Blocked by Privacy Mode".

### Message Metadata (ROUT-05)
- **Chat Badges:** The `MessageBubble` in `ChatScreen` is updated to show a badge for `CLOUD` messages, distinguishing them from `DETERMINISTIC` ones.

### Claude's Discretion
- Exact naming of the `AiRouter` and its internal state machine/flow.
- The visual design of the "Privacy Mode" indicator (e.g., icon in the top bar vs badge near the mic).
- The specific error handling flow when the Cloud AI returns a timeout or rate-limit error.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Requirements
- `.planning/REQUIREMENTS.md` — ROUT-01 through ROUT-05 and PRIV-01 through PRIV-06.

### Existing Logic
- `core/ai/src/main/kotlin/com/example/aicompanion/core/ai/parser/CommandParser.kt` — The existing deterministic parser.
- `core/network/src/main/kotlin/com/example/aicompanion/core/network/ha/HaAuthInterceptor.kt` — Example of an existing OkHttp interceptor.
- `core/domain/src/main/kotlin/com/example/aicompanion/core/domain/model/ParsedIntent.kt` — The intent model to be extended or used by the router.

### Research
- `.planning/research/STACK.md` — Check for ML Kit GenAI or Vertex AI library versions.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `HaAuthInterceptor` pattern can be reused for `PrivacyInterceptor`.
- `CommandParser` is the first stop in the `AiRouter`.

### Integration Points
- `VoiceViewModel`: Currently calls the parser directly. Needs to be updated to call the `AiRouter`.
- `NetworkModule`: Where the new `PrivacyInterceptor` will be registered in the OkHttpClient.

</code_context>

<specifics>
## Specific Ideas
- The `AiRouter` should be a clean interface in `:core:ai`, with its implementation having access to the `CommandParser` and the `CloudAiService`.
- Use a `Flow` to observe the Privacy Mode state from `DataStore` in the `PrivacyInterceptor`.

</specifics>

<deferred>
## Deferred Ideas
- On-device AI (Gemini Nano) — Deferred to Phase 3.5 or v2 (ROUT-V2-01).
- Advanced privacy auditing (logging exactly which data was sent to cloud) — Deferred to Phase 6.

</deferred>

---

*Phase: 03-ai-routing-privacy*
*Context gathered: 2026-03-19*
