# Gemini Video & Audio Transcription Resilience, Quota Defense & Diagnostics Plan

An updated and comprehensive engineering plan to diagnose, log, and recover from all failure modes in `GeminiVideoAudioTranscriptionService`—specifically targeting HTTP 429 (Resource Exhausted / Quota Limit), HTTP 503 (Model Overloaded), network timeouts, and missing API credentials.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Fallback Mechanism (Confirmed)**: When Gemini audio transcription is unavailable or quota is exceeded (HTTP 429), the service will automatically engage the on-device Android SpeechRecognizer and acoustic timeline synthesis to produce synchronized dialogue lines without blocking the user.
> - **Telemetry & Diagnostics (Confirmed)**: Both an in-app expandable diagnostics drawer and system Logcat logging will be implemented, providing clear human-readable explanations (e.g., quota limits, missing keys) and raw technical response details.
> - **Quota & Overload Handling (Added in Revision)**: Intelligent detection of `RESOURCE_EXHAUSTED` and `MODEL_OVERLOADED` errors with immediate fallback activation and guidance on switching keys or waiting for quota reset.

---

## 1. Overview & Core Concept

When processing video audio for automatic dubbing, `GeminiVideoAudioTranscriptionService` communicates with the Gemini REST API. If the user hits rate limits (HTTP 429), encounters transient model overloads (HTTP 503), or has an invalid/missing API key, the dubbing workflow previously stopped with a failure message.

This update introduces:
1. **End-to-End Diagnostic Logger**: Captures timestamps, payload size, MIME type, HTTP status code, error body, and parsing results.
2. **Quota & Overload Shield**: Detects HTTP 429 and 503 specifically, logs the exact error details, and gracefully triggers the on-device fallback instead of throwing an unhandled exception.
3. **On-Device SpeechRecognizer & Acoustic Fallback**: Generates localized, synchronized dialogue segments directly on the device using Android's native speech recognition and audio waveform analysis.
4. **In-App Expandable Diagnostics Panel**: Allows users to inspect API status, latency, error reasons, and copy logs directly from the transcription UI.

---

## 2. User Experience & Visual Design

### User Interaction Flow

```
[Import Video & Tap Auto Dubbing]
                 │
                 ▼
[Extract Audio & Call Gemini Transcription]
                 │
                 ├──────────────────────────────────────┐
                 ▼ (Success 200 OK)                     ▼ (Error: 429 Quota / 503 / Offline)
    [Parse Transcribed Segments]               [Log Detailed Error to Telemetry]
                 │                                      │
                 │                                      ▼
                 │                             [Display Friendly Notice:
                 │                              "تم تفعيل الدبلجة الاحتياطية على الجهاز"]
                 │                                      │
                 │                                      ▼
                 │                             [Execute On-Device SpeechRecognizer
                 │                              & Acoustic Timeline Builder]
                 │                                      │
                 └───────────────────┬──────────────────┘
                                     │
                                     ▼
                    [Dialogue Review & Diagnostics Screen]
                    • Interactive Segment Cards (Edit text, adjust timings)
                    • Expandable "سجل تشخيص الذكاء الاصطناعي" Card
                    • Indicator Badge: "Gemini AI سحابي" vs "محلي على الجهاز"
```

### Visual Styling & Components
- **Status Indicator**: Dynamic badge indicating "سحابي (Gemini)" in Emerald or "محلي (بدون إنترنت)" in Amber.
- **Diagnostics Drawer / Card**: Collapsible Material 3 card displaying request duration (ms), payload size (KB), HTTP code, and formatted log entries.
- **Copy Logs Button**: Single-tap button to copy diagnostic logs for easy troubleshooting.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Zero-Disruption Fallback on Quota Exhaustion**
  - *Chosen Approach*: When HTTP 429 occurs, instead of terminating the pipeline, immediately switch to the on-device fallback and notify the user that local processing was used due to API quota limits.
  - *Why*: Ensures the user can always create and export dubbed videos regardless of cloud API status.

- **Decision 2: Dual Log Streaming (Logcat + StateFlow)**
  - *Chosen Approach*: Maintain a ring buffer of recent diagnostic events in a `StateFlow<List<TranscriptionLogItem>>` while simultaneously logging to Logcat with tag `GeminiTranscription`.
  - *Why*: Gives both normal users and developers instant insight without requiring ADB terminal access.

- **Decision 3: Android SpeechRecognizer with Native Segment Alignment**
  - *Chosen Approach*: Leverage Android's `SpeechRecognizer` API for offline speech recognition, paired with duration-based timestamp alignment.
  - *Why*: Provides real speech recognition capability completely on-device without cloud dependency.

---

## 4. Technical Architecture & Component Mapping

### System Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        User Interface Layer                            │
│  ┌───────────────────────────────┐  ┌────────────────────────────────┐ │
│  │ VideoSpeechToTextComponent    │  │ TranscriptionDiagnosticsCard   │ │
│  │ • Segment List & Review       │  │ • Real-time Log Stream         │ │
│  │ • Fallback Mode Indicator     │  │ • HTTP Status & Error Details  │ │
│  └───────────────┬───────────────┘  └───────────────▲────────────────┘ │
└──────────────────┼──────────────────────────────────┼──────────────────┘
                   │                                  │
                   ▼                                  │ StateFlow<List<LogItem>>
┌─────────────────────────────────────────────────────┴──────────────────┐
│               GeminiVideoAudioTranscriptionService                     │
│  ┌─────────────────────────────┐   ┌─────────────────────────────────┐ │
│  │ Audio Track Extraction      │   │ Diagnostic & Telemetry Logger   │ │
│  │ (MediaExtractor/MediaMuxer) │   │ • Request / Response intercept  │ │
│  └──────────────┬──────────────┘   │ • HTTP 429 / 503 error decoder  │ │
│                 │                  └─────────────────────────────────┘ │
│                 ▼                                   ▲                  │
│  ┌─────────────────────────────┐                    │                  │
│  │ OkHttp Gemini Multimodal    ├────────────────────┘                  │
│  │ Client (gemini-2.5-flash)   │                                       │
│  └──────────────┬──────────────┘                                       │
│                 │ (On Quota/Error/Offline)                             │
│                 ▼                                                      │
│  ┌─────────────────────────────┐                                       │
│  │ On-Device Fallback Engine   │                                       │
│  │ • Android SpeechRecognizer  │                                       │
│  │ • Acoustic Segment Splitter │                                       │
│  └─────────────────────────────┘                                       │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Structures & State
1. **`TranscriptionLogItem`**:
   - `timestamp`: Long
   - `level`: `INFO`, `WARN`, `ERROR`
   - `stage`: `AUDIO_EXTRACTION`, `API_REQUEST`, `RESPONSE_PARSING`, `FALLBACK_TRIGGERED`
   - `message`: String
   - `details`: String? (e.g., HTTP status, response snippet)
2. **`VideoAudioTranscriptionResult`**:
   - `isFromLiveGeminiApi`: Boolean
   - `isFallbackUsed`: Boolean
   - `httpStatusCode`: Int?
   - `diagnosticLogs`: List<TranscriptionLogItem>
   - `detectedLanguage`: String
   - `segments`: List<VideoAudioTranscriptionSegment>
