# Vian Board — Comprehensive Technical Blueprint & System Architecture

**Base Repository / Upstream Reference:** https://github.com/HeliBorg/HeliBoard

---

## 1. Project Identity, Legal & Process Isolation

- **App Name:** `Vian Board`
- **Application ID:** `com.shura.vianboard`
- **Base Upstream Project:** [HeliBoard (GitHub)](https://github.com/HeliBorg/HeliBoard)
- **Primary License:** **GNU General Public License v3.0 (GPL-3.0)** (Standard copyleft license in `/LICENSE`).
- **Attributions & Third-Party Credits:** Included in **Settings → About → Open Source Licenses & Credits**:
  - *HeliBoard & OpenBoard Contributors* (Core IME engine, layout matrices, key popups, dictionary structures, toolbar/suggestion frameworks).
  - *Android Open Source Project (AOSP)* (LatinIME foundation).
  - *KeePassJava2 / KeePassDX Core* (Encrypted `.kdbx` database parser, crypto primitives).
  - *Offline Neural Runtime & Voice Models Engine* (ONNX Runtime, GGUF / Whisper.cpp, Vosk `.bin`).
- **Process Isolation Model:**
  - `com.shura.vianboard:ime` — Dedicated lightweight process strictly for `VianBoardService` (IME lifecycle, keyboard rendering, low-latency key events, `mmap` binary dictionary access). Zero UI framework bloat.
  - `com.shura.vianboard` — Main app process for MD3 Settings Activity, full-screen editors, backup/restore managers, file import pickers, Log Keeper diagnostics, and configuration screens. Reclaimable by the OS when closed without interrupting the active keyboard.

---

## 2. Key Mapping, Long-Press & Gesture Routers

```
┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                                 KEY GESTURE & POPUP ROUTING                              │
├──────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. SPACEBAR GESTURES                                                                     │
│    • Touch Down & Slide Horizontal (ΔX ≥ 12dp) ──► PRECISION CURSOR MOVE                 │
│      - Sliding Left: Cursor moves left with haptic micro-ticks                           │
│      - Sliding Right: Cursor moves right with haptic micro-ticks                         │
│    • Static Long-Press (Hold ≥ 300ms, no slide) ──► ALL LANGUAGES / SUBTYPES SWITCHER    │
│                                                                                          │
│ 2. NUMBER & SYMBOL SWITCH KEY (?123)                                                     │
│    • Short Tap ──────────────────────────────────► TOGGLE NUMBERS & SYMBOLS PAGE         │
│    • Static Long-Press (Hold ≥ 300ms) ───────────► SECURITY VAULT UNLOCK (PIN/KeePass)   │
│                                                                                          │
│ 3. COMMA KEY ( , ) POPUP (HeliBoard-Style Quick Menu with Extensions)                    │
│    • Short Tap ──────────────────────────────────► INSERT COMMA ( , )                    │
│    • Long-Press / Slide Popup Menu ──────────────► QUICK ACTION POPUP HUB:               │
│         ├── 📝 Prompt List Modal                                                         │
│         ├── 🖥️ Desktop Keyboard / Shortcuts Modal (DesktopKeyboardView)                 │
│         ├── 📋 Clipboard History Modal                                                   │
│         ├── 🖐️ One-Handed / Floating Mode                                                │
│         └── ⚙️ Open Vian Board Settings                                                  │
└──────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Desktop Keyboard Mode (`DesktopKeyboardView`)

Traditional mobile "PC layouts" compress 60–104 keys into tiny 20dp buttons, causing constant miss-taps. `DesktopKeyboardView` replaces this with a spacious, ergonomic **3-row layout with large, fat, tactile buttons**.

```
┌──────────────────────────────────────────────────────────────────────────────────────────┐
│ 🖥️ DESKTOP KEYBOARD (DesktopKeyboardView) — SPACIOUS 3-ROW FAT-KEY NAVIGATION PAD        │
├──────────────────────────────────────────────────────────────────────────────────────────┤
│ Row 1: [ HOME ]  [  ▲ UP  ]  [  END  ]  [ PG UP ]  [ Custom Slot 1 ]  [ Custom Slot 2 ] │
│ Row 2: [ ◀ LEFT] [ ▼ DOWN ]  [ ▶ RIGHT] [ PG DN ]  [ Custom Slot 3 ]  [ Custom Slot 4 ] │
│ Row 3: [ ABC ]   [   SEL  ]  [  SPACE  ]   [ Custom Slot 5 ]   [  ⌫ DEL  ]   [  ↵ ENTER ]│
└──────────────────────────────────────────────────────────────────────────────────────────┘
```

### A. Core Default (Pure Arrows & Navigation Preset)
- Out of the box, the navigation pad displays oversized directional arrow keys (`▲`, `▼`, `◀`, `▶`), `HOME`, `END`, `PG UP`, `PG DN`, `ABC` (return to standard keyboard), `SEL` (shift-selection toggle mode), `SPACE`, `⌫` (Delete), and `↵` (Enter).

### B. Custom Shortcuts & Strict 5-Key Limit
- When custom shortcuts are enabled, they seamlessly sit alongside the navigation cluster while preserving large, comfortable touch targets (≥48dp minimum touch target).
- **Strict Limit (Max 5 Shortcuts):** Limits extra shortcuts to preserve large, fat button sizes without crowding.
- **Library of Predefined Essential Actions:**
  1. `Select All` (`Ctrl+A` / `performContextMenuAction`)
  2. `Copy` (`Ctrl+C`)
  3. `Paste` (`Ctrl+V`)
  4. `Cut` (`Ctrl+X`)
  5. `Undo` (`Ctrl+Z`)
  6. `Redo` (`Ctrl+Y`)
  7. `Home` / `End` / `Page Up` / `Page Down`
  8. `Tab` (`KeyEvent.KEYCODE_TAB`)
  9. `Forward Delete` (`KeyEvent.KEYCODE_FORWARD_DEL`)
  10. `Escape` (`KeyEvent.KEYCODE_ESCAPE`)
  11. `Select Word` (word-boundary selection expansion)
  12. `Find / Replace` (`Ctrl+F`)
  13. `Save` (`Ctrl+S`)

### C. Dedicated Settings Page
- **Reorder Active Keys:** Reorder active shortcuts using up/down arrow controls.
- **Add & Remove Shortcuts:** Toggle any item from the essential action library (capped at 5).
- **Reset Button:** One-tap reset back to the pure arrows preset.

---

## 4. Log Keeper Architecture (Built from Day 1 Foundation)

Log Keeper is the diagnostic bedrock of Vian Board, active from Phase 0 to monitor the `:ime` service, touch latencies, dictionary memory mappings, and vault sanitization without polluting system `logcat` with sensitive user data.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        LOG KEEPER SUBSYSTEM                            │
├────────────────────────────────────────────────────────────────────────┤
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │ In-Memory Non-Blocking Ring Buffer (Capacity: 5,000 log events)    │ │
│ └──────────────────────────────────┬─────────────────────────────────┘ │
│                                    │                                   │
│           ┌────────────────────────┼────────────────────────┐          │
│           ▼                        ▼                        ▼          │
│   [ Component Tagging ]  [ Privacy Sanitizer ]    [ Crash Handler ]    │
│   • [IME] Lifecycle      • Redacts Passwords      • Uncaught exception │
│   • [TOUCH] Gestures     • Strips PIN/OTP fields  • Stack trace dump   │
│   • [DICT] mmap lookups  • Masks Vault payloads   • Auto-recovery      │
│   • [VAULT] Auth status  • Zero Disk I/O on type                       │
│   • [DESKTOP] Shortcuts                                                │
└────────────────────────────────────────────────────────────────────────┘
```

- **Zero-Latency In-Memory Ring Buffer**: Writes are non-blocking circular memory appends with zero disk I/O on the active typing thread.
- **Privacy Sanitizer**: Automatic masking of all password fields, PIN inputs, and encrypted vault payloads.
- **UI Diagnostics in Settings**:
  - Master logging toggle.
  - Time-range filtering: `[ 6h ]` `[ 12h ]` `[ 24h ]` `[ All ]`.
  - Monospace timestamped event cards with colored component badges.
  - One-tap "Copy All Logs" and "Export .txt" actions.
  - One-tap "Clear Buffer" action.

---

## 5. Dedicated Offline Voice Input Modal

```
┌────────────────────────────────────────────────────────────────────────┐
│ 🎤 OFFLINE VOICE INPUT MODAL (Dedicated Staging View)                  │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │ "Recognized text streams here first in real-time..."               │ │ ◄── In-Modal Preview Box
│ └────────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│               (( 🟢 LIVE AUDIO PULSE CIRCLE / WAVEFORM ))             │ ◄── Color-Coded Audio Meter
│                     (Green = Speaking | Blue = Idle)                   │
│                                                                        │
├────────────────────────────────────────────────────────────────────────┤
│ ┌──────────┐ ┌───────────────────┐ ┌──────────┐ ┌────────────────────┐ │
│ │  CANCEL  │ │   CLEAR PREVIEW   │ │    ⌫     │ │  INSERT INTO FIELD │ │
│ └──────────┘ └───────────────────┘ └──────────┘ └────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

- **Color-Coded Status Feedback**:
  - 🟢 **Vibrant Green**: Active speech detected (PCM RMS power threshold met).
  - 🔵 **Muted Blue**: Microphone active, listening / idle background.
  - 🔴 **Amber / Red**: Speech timeout / silence threshold reached.
- **Two-Stage Text Commitment**:
  - Streamed tokens from the `.bin` / `.onnx` / `.gguf` neural decoder populate the in-modal preview box first.
  - Users can review, edit with backspace, or tap **"Insert Into Field"** to commit to the active app.
- **On-Demand Memory Release**:
  - Closing the modal stops the `AudioRecord` thread and purges neural tensors from RAM immediately.

---

## 6. Dictionary, Autocorrect & Multilingual Prediction Architecture

```
                               ┌──────────────────────────────────────────────┐
                               │            Word / Context Stream             │
                               │  ("meet me at" / "call" / "password123")     │
                               └──────────────────────┬───────────────────────┘
                                                      │
                                                      ▼
                               ┌──────────────────────────────────────────────┐
                               │           Suggest / Candidate Router         │
                               └──────┬───────────────┬───────────────┬───────┘
                                      │               │               │
                 ┌────────────────────┴──┐            │        ┌──────┴────────────────────┐
                 ▼                       ▼            ▼        ▼                           ▼
        ┌──────────────────┐  ┌──────────────────┐  ┌─────┐  ┌──────────────────┐ ┌──────────────────┐
        │ Primary .dict    │  │ Secondary .dict  │  │User │  │ Personal Learned │ │ Dynamic Bigram / │
        │ (mmap Zero-Heap) │  │ (mmap Zero-Heap) │  │Dict │  │ Number Trie      │ │ Context History  │
        │ e.g. English     │  │ e.g. Spanish     │  │(Room│  │ (Frequent digits)│ │ (Previous Word)  │
        └──────────────────┘  └──────────────────┘  └─────┘  └──────────────────┘ └──────────────────┘
                                      │               │               │                    │
                                      └───────┬───────┴───────┬───────┴────────────────────┘
                                              ▼               ▼
                               ┌──────────────────────────────────────────────┐
                               │      Weighted Scorer & Autocorrect Filter    │
                               │   (Frequency + Edit Distance + Bigram Prob)  │
                               └──────────────────────┬───────────────────────┘
                                                      ▼
                               ┌──────────────────────────────────────────────┐
                               │               Suggestion Bar                 │
                               │        [ Left ]  [ Center (Bold) ]  [ Right ]│
                               └──────────────────────────────────────────────┘
```

- **Dual-Language Engine**: Both primary and secondary `.dict` files mapped via Linux `mmap()` for simultaneous typing without manual switching.
- **Number Learning & Prediction**:
  - Learns frequent numeric sequences (e.g. `2026`, `1080`, `4k`, `101`).
  - Contextual number bigrams (e.g. `meet at` → `5`, `6`, `7`; `room` → `101`, `204`).
  - Strict privacy filter excluding OTP/PINs, password fields, and credit cards (Luhn algorithm check).

---

## 7. Toolbar & Suggestion Bar Engine (Full HeliBoard Import + Extensions)

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        Suggestion Bar & Toolbar Hybrid Strip                           │
│                                                                                        │
│ [>] [ 📋 Paste Pill: "Recently copied text..." ]  [ 🔤 Select Word ] [ 📋 Copy ] [ 📄 Paste ] │
│  │   (or Word Suggestions / Vault Credential Pills)                                    │
│  └─► Expandable for Extra Tools: [ 📝 Prompts ] [ 🔐 Vault ] [ 🎤 Voice ] [ ⚙️ Settings ] │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### A. Suggestion Bar & Dynamic Pills
1. **HeliBoard Paste Pill**: Copied text creates a tap-to-paste pill in the suggestion bar.
2. **KeePass Security Vault Pills**: Recognized login/URL fields trigger credential pills → Tap → PIN/Pattern → Paste.
3. **Word Candidates & Number Predictions**: Real-time display of unigrams, learned numbers, and next-word bigram candidates.
4. **Prompt / Quick Phrase Expansions**: Typing shortcut triggers displays matching prompt expansions.

### B. Toolbar Default Pinned Tools (Right Side)
1. 🔤 **Select Word**: Short tap → selects word under cursor. **Long-press → Select All** (desktop `Ctrl+A`).
2. 📋 **Copy**: Short tap → copies current selection. **Long-press → Opens Prompt List Modal**.
3. 📄 **Paste**: Short tap → pastes clipboard. **Long-press → Opens Clipboard Modal**.
- **Optional Tools (Configurable via Toolbar Settings)**: 📝 Prompt List, 🖥️ Desktop Keyboard, 🔐 Security Vault, 🎤 Offline Voice-to-Text, ⚙️ Settings, 🎯 Cursor Pad, ✂️ Cut.

---

## 8. Keyboard Layouts, Modals & Unified 4-Key Bottom Bar

### A. Layouts & Modals Hierarchy
1. **Alphabetical Main Layouts**: Full direct HeliBoard import (QWERTY, QWERTZ, AZERTY, Dvorak, Colemak, Workman, PC layout, BÉPO).
2. **Numbers & Symbols (`?123` & `=\<`)**: Full two-page symbols and numeric layouts.
3. **Desktop Keyboard (`DesktopKeyboardView`)**: Spacious 3-row fat-button navigation pad with up to 5 custom shortcuts.
4. **Emoji Modal**: Categorized tabs, dense grid, **no search bar** (zero memory index overhead).
5. **Clipboard Modal**: Ephemeral history + pinned clips. Short-tap item → paste. Long-press item → Tiny popup with 3 micro-actions: `[ 📌 Pin / Unpin ]`, `[ 🗑️ Delete ]`, `[ 📝 Move to Prompt List ]`.
6. **Prompt List Modal**: Quick-tap prompt cards to insert. Long-press prompt card → Tiny popup with 2 micro-actions: `[ 📌 Pin / Unpin ]` (keeps prompt sticky at top) and `[ 🗑️ Delete ]`.

### B. Unified Bottom Control Bar (for Clipboard, Emoji, Desktop, and Prompt List)

```
┌────────────────────────────────────────────────────────────────────────┐
│          MODAL CONTENT (Emoji / Clipboard / Prompt / Desktop)          │
├────────────────────────────────────────────────────────────────────────┤
│ ┌──────────┐ ┌──────────────────────────────────┐ ┌──────────┐ ┌─────┐ │
│ │   ABC    │ │              SPACE               │ │    ⌫     │ │  ↵  │ │
│ │ (Letters)│ │                                  │ │(Backspace│ │Enter│ │
│ └──────────┘ └──────────────────────────────────┘ └──────────┘ └─────┘ │
└────────────────────────────────────────────────────────────────────────┘
```
- `[ ABC ]` (Far Left): Exits modal back to letters.
- `[ SPACE ]` (Center): Proportional width, shortened to fit controls.
- `[ ⌫ ]` (Backspace): Deletes last character directly.
- `[ ↵ ]` (Enter / Next Line, Far Right): Inserts newline or executes action directly.

---

## 9. Security Vault (.kdbx + 2FA) & Personal Privacy Vault

### A. Security Vault (KeePass DX .kdbx + 2FA)
- **Trigger**: Long-press `?123` or select from Toolbar / Quick Menu.
- **Hierarchical Folder Tree Navigation**: Scrollable, sortable tree (A-Z, Z-A, Date Modified) with **no search bar**.
- **Authentication**: In-keyboard **9-Dot Pattern Lock** (with remember option), Fingerprint, or PIN fallback.
- **2FA TOTP Generator**: Computes RFC 6238 6-digit codes with live countdown timer ring.

### B. Personal Privacy Vault (Full-Screen Quick Phrases UI)
- **Dedicated Full-Screen Manager**: Formatted like a comprehensive Quick Phrases editor with shortcut bindings and category tags.
- **PIN Protected**: Accessing this screen from Settings **strictly requires PIN/Pattern authentication**.
- **Masked Inline Suggestions**: Sensitive items (Address, Phone, Email, DOB) appear masked in the suggestion bar (e.g. `123***NY`). Tapping prompts pattern verification and commits the full text.

---

## 10. Backup & Restore Architecture

```
Vian Board Backup Manager
├── 📤 Create Backup (.vianbackup)
│    ├── Settings & Custom Layout Profiles
│    ├── Desktop Shortcuts & Key Configurations
│    ├── Prompts & Quick Phrases
│    ├── Personal Word Dictionaries & Learned Number Bigrams
│    └── Encrypted Privacy Vault (Protected by Master Backup Password)
│
└── 📥 Restore Backup
     ├── HeliBoard Backup Importer (.zip / .json archive parser)
     └── Native Vian Board Restore (.vianbackup)
```

---

## 11. Complete Settings Architecture & Subpages

Built with Material Design 3, providing a dedicated page for every setting group:

```
Vian Board Settings
├── 🔤 Layout & Languages
│    ├── Primary & Secondary Language Selector
│    ├── Spacebar Long-Press Language Switcher Configuration
│    ├── Keyboard Height (70% - 130%) & Bottom Padding Sliders
│    ├── Key Long-Press Duration (100ms - 500ms)
│    ├── Comma Popup Options Customization
│    └── One-Handed & Floating Modes
│
├── 🖥️ Desktop Shortcuts & Navigation Settings
│    ├── Active 3-Row Layout Live Preview
│    ├── Add & Remove Shortcuts from Essential Library (Max 5)
│    ├── Reorder Active Keys (Up / Down controls)
│    └── Reset to Pure Arrows Preset Button
│
├── 🎨 Themes & Styling
│    ├── Dynamic Material You Color Scheme
│    ├── Pure OLED Black (Battery Optimization)
│    ├── Light / Dark Manual Overrides
│    └── Key Borders, Corner Radius & Touch Ripple Styles
│
├── 📖 Dictionaries & Prediction
│    ├── Main & Secondary Binary Dictionaries (mmap zero-heap)
│    ├── Personal User Dictionary (Add / Edit / Remove custom words)
│    ├── Accidental Number Typo Auto-Correction (Non-breaking continuous suggestions)
│    ├── Number Learning & Prediction Toggle
│    ├── Quick Phrases & Abbreviation Expander
│    └── Next-Word Prediction & Autocorrect Toggles
│
├── 🎤 Offline Voice-to-Text
│    ├── AI Model Manager (Import .bin, .onnx, .gguf model files)
│    ├── Active Model Details (Format, Quantization, Language)
│    ├── Microphone Sensitivity & Silence Threshold
│    └── On-Demand Model Unload Timeout
│
├── 🔐 Security Vault (KeePass / 2FA)
│    ├── Number/Symbol Switch (?123) Long-Press Trigger Toggle
│    ├── Link / Select .kdbx File from Storage
│    ├── Folder Tree Display & Sort Options (A-Z, Z-A, Date Modified)
│    ├── 9-Dot Pattern Setup & Remember Toggle
│    ├── Biometric Fingerprint & PIN Fallback Options
│    ├── Auto-Lock Inactivity Timer (Immediate, 1 min, 5 min)
│    └── 2FA TOTP RFC 6238 Options
│
├── 🛡️ Personal Privacy Vault (PIN-Protected Full-Screen UI)
│    ├── Manage Sensitive Entries (Address, Phone, Email, DOB)
│    ├── Shortcut Configuration (Quick-phrase style abbreviations)
│    ├── Pattern Authentication Settings for Privacy
│    └── Masking Display Rules
│
├── 📋 Clipboard & Prompts
│    ├── Paste Pill in Suggestion Bar Toggle
│    ├── History Retention Rules (Auto-clear duration)
│    ├── Full-Screen Prompt List Manager (Edit text, shortcuts, categories)
│    └── Long-Press Action Customization (Pin, Delete, Add to Prompts)
│
├── 💾 Backup & Restore
│    ├── Export Native Backup (.vianbackup)
│    ├── Restore Native Backup (.vianbackup)
│    └── Import HeliBoard Backup (.zip / .json)
│
├── 📊 Log Keeper (Matching Exact UI Reference)
│    ├── Master Logging Switch (On / Off)
│    ├── Time Filter Tabs: [6h] [12h] [24h] [All]
│    ├── Timestamped Event Cards (Monospace timestamps + Category badges)
│    ├── Copy All Logs Action
│    ├── Export .txt File Action
│    └── Clear Buffer Action
│
└── ℹ️ About & Credits
     ├── Vian Board Version & GitHub Action CI Build Status
     └── 📜 Open Source Licenses & Credits
          ├── GNU General Public License v3.0 (GPL-3.0) Full Notice
          ├── HeliBoard & OpenBoard Authors & Contributors Attribution
          ├── Android Open Source Project (AOSP LatinIME)
          ├── KeePass Crypto & Decryption Libraries
          └── Offline Speech Inference Engines (ONNX Runtime, GGUF / Whisper.cpp, Vosk .bin)
```

---

## 12. Phased Build Sequence & Roadmap

```
[Phase 0: Log Keeper & Core Contracts] (MANDATORY Foundation)
               │
               ▼
[Phase 1: Minimal IME Service & Input Connection]
               │
               ▼
[Phase 2: Touch Dispatcher, Layout Matrix & Gesture Engine]
               │  (Spacebar slide/hold, ?123 vault trigger, Comma popup)
               ▼
[Phase 3: Suggestion Strip & Dual-Language Dictionary]
               │
               ▼
[Phase 4: DesktopKeyboardView & Zero-Search Modals]
               │  (3-Row Fat-Key Nav Pad, Emoji, Clipboard, Voice)
               ▼
[Phase 5: Security Vaults (KeePass, 2FA/TOTP, Privacy Pills)]
               │
               ▼
[Phase 6: MD3 Settings Suite, Desktop Shortcuts Manager & HeliBoard Backup Importer]
```

---

## 13. Memory Lifecycle & CI Pipeline

- **Memory Eviction (`onTrimMemory(TRIM_MEMORY_UI_HIDDEN)`):**
  - Purges emoji preview buffers, decrypted KeePass trees, 2FA instances, desktop shortcut caches, and temporary audio arrays when keyboard closes.
- **GitHub Actions CI Pipeline (`.github/workflows/build.yml`):**
  - **JDK:** Eclipse Temurin 17 | **Gradle:** 9.3.1
  - **Task:** `gradle assembleDebug`
  - **Output Artifact:** `app/build/outputs/apk/debug/app-debug.apk` under package `com.shura.vianboard`
