# VIAN BOARD — EMBEDDED VOICE INPUT ARCHITECTURE & INTEGRATION SPECIFICATION
**Module:** `VOICE_INPUT_COMPONENT.md`  
**Base Architecture:** FUTO Voice Input (`futo-org/voice-input` & `futo-org/android-keyboard`)  
**Core Directive:** **100% On-Demand Resource Lifecycle**. Native libraries, models, audio recorders, and threads are allocated ONLY when the user actively triggers voice dictation and are completely shut down, freed from RAM, and destroyed immediately upon exit.

---

## 1. ON-DEMAND LIFECYCLE & ZERO-IDLE FOOTPRINT DIRECTIVE

To maintain Vian Board's sub-millisecond typing responsiveness and low memory footprint:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   STATE 0: DORMANT / SHUT DOWN (Default)                 │
│  - 0 KB RAM allocated for audio/neural buffers                           │
│  - 0 Native Whisper/VAD model handles in memory                          │
│  - 0 Background audio threads or polling services running                │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                 [User Taps 🎤 Voice / Long-Press Comma]
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                   STATE 1: INSTANT ON-DEMAND WAKEUP                      │
│  1. Check Audio Permission -> Prompt if needed                           │
│  2. Verify Imported Model in App Sandbox (/files/models/)                │
│  3. Initialize Silero VAD (ONNX) + Multi-Threaded whisper.cpp (JNI)      │
│  4. Allocate Thread-Safe Circular PCM Ring Buffer                        │
│  5. Start AudioRecord (16 kHz, 16-bit Mono PCM)                          │
│  6. Transition Keyboard View into Voice Staging Modal                    │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                   [User Finishes / Taps ⏹️ / Switches to [ABC]]
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                   STATE 2: IMMEDIATE SHUTDOWN & CLEANUP                  │
│  1. AudioRecord.stop() & AudioRecord.release()                           │
│  2. Drain & Deallocate Circular Ring Buffer                              │
│  3. Native freeContext() on Whisper & Silero VAD handles                 │
│  4. Terminate CPU worker threads                                         │
│  5. Force System.gc() hint -> Return keyboard to 100% dormant state      │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 2. FUTO'S PRODUCTION VOICE PIPELINE

```
┌────────────────────────────────────────────────────────────────────────┐
│                        1. HIGH-SPEED AUDIO CAPTURE                     │
│  - AudioRecord: 16 kHz, 16-bit Mono PCM (512-sample frames)            │
│  - Real-time RMS extraction -> Drives Live Colored Visual Pulse Canvas │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    2. THREAD-SAFE CIRCULAR RING BUFFER                 │
│  - Lock-free / atomic ring buffer holding rolling raw PCM              │
│  - Decouples high-priority audio acquisition from heavy neural workers │
│  - Zero buffer overflows and zero audio stutter                        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      3. SILERO NEURAL VAD ENGINE                       │
│  - Quantized Silero Voice Activity Detector (ONNX / C++ runtime)       │
│  - Evaluates 32ms / 512-sample windows with high confidence ($P > 0.5$)│
│  - Accurately detects speech start, natural breath pauses, and speech  │
│    termination (400ms - 700ms silence threshold)                       │
│  - Filters out background cafe/traffic noise; prevents Whisper         │
│    hallucinations on silence                                           │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│         4. MULTI-THREADED WHISPER.CPP / SHERPA-ONNX INFERENCE          │
│  - Multi-threaded native C++ JNI (`libwhisper.so` / `libsherpa-onnx.so`)│
│  - SIMD acceleration: ARM NEON + FP16 on arm64-v8a                     │
│  - Quantized GGML model execution (`Q4_0`, `Q5_1`, `Q8_0`, `INT8`)     │
│  - Segment decoding with context token preservation across sentences   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 5. TEXT FORMATTING & COMMIT ENGINE                     │
│  - Auto-capitalization, smart spacing, and punctuation normalization   │
│  - Live partial preview strip + atomic commit to InputConnection       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. LIVE VISUAL PULSE & DYNAMIC COLOR BLOOM

The Voice Modal features a hardware-accelerated Canvas with real-time breathing visuals driven directly by the audio buffer:

```
┌────────────────────────────────────────────────────────────────────────┐
│ [ 📁 Import Model ]      [ ⚙️ Settings ]                     [ ✕ Close ]│
│                                                                        │
│                         ╭───────────────╮                              │
│                         │   (( 🎤 ))    │   <-- Dynamic Radial Glow   │
│                         ╰───────────────╯                              │
│             | | || |||| |||||||||||||||| |||| || | |                   │
│             <-- Live Symmetrical Equalizer Waveform -->                │
│                                                                        │
│  "Recognized speech preview appears here in real time..."              │
│                                                                        │
│                    [ ⏸️ Pause ]       [ ⏹️ Done ]                       │
├────────────────────────────────────────────────────────────────────────┤
│ [  ABC  ]    [       SPACE       ]    [  ⌫  ]   [  ↵  ] (Unified Bar)  │
└────────────────────────────────────────────────────────────────────────┘
```

### Visual Themes & States
1. **Material You Dynamic (M3)**: Center glow uses `primaryContainer`, outer frequency bars pulse from `tertiary` to `primary`.
2. **OLED Pure Black**: Glowing neon cyan-to-emerald gradient (`#00E5FF` $\to$ `#00E676`) on pitch black background.
3. **Slate Dark / Crisp Light**: Indigo-to-violet gradient (`#6366F1` $\to$ `#8B5CF6`).
4. **State Transitions**:
   * *Listening*: Gentle undulating breathing pulse.
   * *Speech Detected*: Dynamic multi-bar equalizer expanding symmetrically with audio amplitude.
   * *Processing / Transcribing*: Smooth rotating aura around the central microphone orb.

---

## 4. POST-INSTALL MODEL IMPORT (PATH A: LOCAL FILE IMPORT)

To keep the base keyboard APK tiny ($< 5\text{MB}$), zero model weights are bundled in the installer.

### Import Flow
1. **Trigger**: Tap `[ 📁 Import Model ]` in Voice Modal or navigate to `Settings -> Voice Input -> Model Manager`.
2. **Storage Access Framework (SAF)**: Launches Android system document picker (`Intent.ACTION_OPEN_DOCUMENT`) with MIME filter `*/*` (targeting `.bin`, `.gguf`, `.onnx`).
3. **Sandboxed Copy**: Copies selected model into internal private storage:
   * Target: `/data/user/0/<app_package>/files/models/whisper-active.bin`.
4. **Validation & Magic Header Check**:
   * Inspects binary header (`0x67676d6c` for GGML / `0x47475546` for GGUF / ONNX).
   * Validates quantization format (`Q4_0`, `Q5_1`, `Q8_0`) and parameter sizes.
   * Displays model details (size, quantization, language capabilities) upon successful verification.

---

## 5. IME & MODAL INTEGRATION

* **Activation Triggers**:
  * Tap `[ 🎤 Voice ]` button in the top toolbar drawer.
  * Long-press comma key `,` and slide to `🎤`.
* **Unified 4-Button Bottom Control Bar**:
  * `[ABC]`: Instantly shuts down voice engine and returns to QWERTY typing.
  * `[SPACE]`: Inserts space into active editor.
  * `[⌫]`: Deletes previous word/character.
  * `[↵]`: Commits final text and triggers editor action (Enter/Send/Search).

---

## 6. DAY-1 DIAGNOSTICS & PRIVACY SANITIZATION (`LogKeeper`)

* **Tag**: `[VOICE]` / `LogTag.VOICE`.
* **Tracked Telemetry**:
  * Engine startup time (ms).
  * VAD speech probability and segment boundary triggers.
  * Inference Real-Time Factor (RTF, e.g. `0.18x`) and CPU thread utilization.
  * Instant shutdown confirmation and memory deallocation status.
* **Strict Privacy Masking**:
  * **ZERO audio data, ZERO audio waveforms, and ZERO transcribed words are written to `LogKeeper`**.
  * Example Log Output:
    ```
    [VOICE] 14:02:11.104 | Engine initialized on-demand (threads=4, model=whisper-tiny-q4.bin)
    [VOICE] 14:02:13.450 | Speech segment detected (vad_conf=0.92, duration=2400ms)
    [VOICE] 14:02:13.890 | Segment inference completed in 440ms (rtf=0.18x, tokens=14)
    [VOICE] 14:02:16.120 | Shutdown triggered -> AudioRecord released, models freed, memory 100% reclaimed
    ```

---

## 7. OPEN SOURCE LICENSING & ATTRIBUTIONS

* **whisper.cpp**: Licensed under **MIT License** (Copyright © 2023-2026 Georgi Gerganov).
* **FUTO Voice Input / FUTO Keyboard**: Licensed under **GNU General Public License v3.0 (GPL-3.0)** (Copyright © FUTO Holdings).
* **Silero VAD**: Licensed under **MIT License** (Copyright © Silero Team).
* **Sherpa-ONNX**: Licensed under **Apache License 2.0** (Copyright © Next-gen Kaldi).
* All licenses and attributions are surfaced cleanly in Vian Board's `Settings -> About -> Open Source Licenses`.
