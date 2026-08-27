# VIAN BOARD — HELIBOARD FOLDER-BY-SUBFOLDER BATCH IMPORT PLAN
**Specification Document:** `blueprint/BATCH_IMPORT_PLAN.md`  
**Reference Upstream:** HeliBoard (`github.com/HeliBorg/HeliBoard`)  
**Core Strategy:** Sequential, layer-by-layer subfolder import to guarantee zero missing class cascades, strict compilation verification at every stage, and clean modular boundaries.

---

## 1. BATCH TOPOLOGY & ROADMAP OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 1: com.example.foundation                                             │
│ - Constants, Common, LocaleUtils, UnicodeUtils, CollectionUtils             │
│ - CoordinateUtils, DeviceUtils, ResourceUtils, SubtypeLocaleUtils           │
│ - ByteArrayDictBuffer, RingCharBuffer, Subtype data structures              │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 2: com.example.engine.dict                                            │
│ - FormatSpec, BinaryDictDecoder, Probability, FusionDictionary              │
│ - BinaryDictionary, Dictionary, DictionaryGroup, DictionaryCollection       │
│ - Zero-heap mmap FileChannel loader, asset & user .dict importer            │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 3: com.example.engine.core                                            │
│ - Suggest, SuggestedWords, SuggestedWordInfo                                │
│ - WordComposer (with Number-Aware Typo Proximity Engine)                    │
│ - UserDictionary, DictionaryFacilitator, PersonalPhraseEntity (Room)        │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 4: com.example.keyboard.internal                                      │
│ - Key, KeyboardParams, KeyboardRow, KeySpecParser, MoreKeySpec              │
│ - MainKeyboardView (Canvas rendering, touch dispatch, 120 FPS target)      │
│ - PointerTracker (Multi-touch), MoreKeysKeyboardView (Accent bubbles)       │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 5: com.example.ime & modals                                           │
│ - VianBoardService, RichInputConnection, SubtypeSwitcher                    │
│ - SuggestionStripView (Inline raw/correct/next candidates + paste pills)    │
│ - Modals Manager: Clipboard, Prompt List, Navigation Pad, Emoji, Vault Sheet│
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 6: com.example.settings & backup                                      │
│ - Material Design 3 Settings Activities and Fragments                       │
│ - Custom Shortcuts Manager (Desktop Nav Pad slot configurator)              │
│ - HeliBoard Backup Importer (.zip/.json parser) & .vianbackup exporter      │
│ - Voice & Security Vault admin controllers                                  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 7: com.example.voice (Offline Raw Audio & Neural Voice Engine)        │
│ - RawAudioRecorder (16kHz 16-bit Mono PCM, zero-GC ring buffer)             │
│ - Silero VAD (Real-time speech probability & silence detection)             │
│ - Whisper/GGML Inference Engine Runner (whisper.cpp JNI interface)          │
│ - VoiceModelManager (External SAF model importer & downloader, no models in APK)│
│ - In-Keyboard Voice Overlay View & Decibel Waveform Visualizer               │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ BATCH 8: com.example.vault (In-Keyboard Security Vault & KeePassDX Bridge)  │
│ - Zero-Clipboard Secret Injection into InputConnection                      │
│ - AES-256-GCM Cryptographic Vault & Memory Zero-Wipe Security               │
│ - KeePassDX .kdbx File Reader & SAF Importer                                │
│ - RFC 6238 TOTP (Time-based One-Time Password) Authenticator Generator      │
│ - In-Keyboard Vault Overlay View with Search & Quick-Fill Pills             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. DETAILED BATCH SPECIFICATIONS

### BATCH 1: `com.example.foundation` (Base Data Structures & Utilities)
* **Goal**: Establish the zero-dependency base utilities, coordinate math, locale handling, unicode processors, and byte buffers used throughout the keyboard and dictionary engines.
* **Target Package**: `com.example.foundation`
* **Sub-packages**:
  * `com.example.foundation.common`: Constants, LocaleUtils, CollectionUtils, UnicodeUtils.
  * `com.example.foundation.utils`: CoordinateUtils, DeviceUtils, ResourceUtils, SubtypeLocaleUtils, ByteArrayDictBuffer, RingCharBuffer, Subtype, RichInputMethodSubtype.
* **Verification Gate**: Kotlin compilation check via `compile_applet`.

---

### BATCH 2: `com.example.engine.dict` (Binary Dictionary & mmap Storage)
* **Goal**: Port the zero-copy binary `.dict` parser and file loader.
* **Target Package**: `com.example.engine.dict`
* **Key Components**:
  * `FormatSpec`: AOSP and HeliBoard v2/v4 binary dictionary format specifications.
  * `BinaryDictDecoder`: Memory-mapped binary header, trie node traversing, and bigram frequency decoders.
  * `BinaryDictionary`: Fast in-memory / mmap query engine for exact matches and prefix searches.
  * `DictionaryCollection`: Multi-dictionary aggregation (Main Language + Secondary Language + Contacts).
* **Verification Gate**: Unit/Robolectric tests for dictionary node traversal and asset loading.

---

### BATCH 3: `com.example.engine.core` (Suggestion Core & Proximity Scorer)
* **Goal**: Port the suggestion pipeline and implement the number-aware typo proximity engine.
* **Target Package**: `com.example.engine.core`
* **Key Components**:
  * `WordComposer`: Touch coordinate tracking and non-breaking continuous composition for numbers (`hell8` $\to$ `hello`).
  * `Suggest`: Scoring algorithms, auto-correction thresholds, frequency weighting, and candidate ranking.
  * `SuggestedWords` & `SuggestedWordInfo`: Representation of top 3 suggestion strip slots and raw override tags.
  * `DictionaryFacilitator`: Simultaneous dual-language facilitator and language weight blender.
* **Verification Gate**: Compilation and prediction verification tests.

---

### BATCH 4: `com.example.keyboard.internal` (Layout, Keys & Hardware Canvas)
* **Goal**: Implement high-performance Canvas-based keyboard rendering and multi-touch pointer tracking.
* **Target Package**: `com.example.keyboard.internal`
* **Key Components**:
  * `Key` & `KeyboardRow`: Geometric key definitions, touch bounds, code points, and top-right hint glyph coordinates.
  * `KeySpecParser`: XML / JSON layout parser for QWERTY, Symbols 1/2, Number row, and Accents.
  * `MoreKeySpec`: Long-press popup definitions for international characters (`ē`, `ū`, `ñ`, etc.) and currencies (`₹`, `$`, `€`).
  * `PointerTracker`: Zero-latency multi-touch pointer tracker and micro-gesture detector (Spacebar glide $\Delta X$, Backspace swipe $\Delta X$).
  * `MainKeyboardView`: Hardware-accelerated `onDraw()` canvas key renderer with zero GC allocations in hot paths.
* **Verification Gate**: Layout rendering and touch event dispatch verification.

---

### BATCH 5: `com.example.ime` (IME Service, Lifecycle & Unified Modals)
* **Goal**: Connect Android InputMethodService with the Canvas View, Suggestion Bar, and Modal Sheets.
* **Target Package**: `com.example.ime`
* **Key Components**:
  * `VianBoardService`: InputMethodService lifecycle, `EditorInfo` variation inspection, batch text edits.
  * `RichInputConnection`: Robust text manipulation, cursor position tracking, word deletions, and token commits.
  * `SuggestionStripView`: Top bar rendering for raw, auto-correct, and next-word pills + fast action buttons (`Select`, `Copy`, `Paste`).
  * `ModalOverlayManager`: Unified 4-button modal host for Clipboard, Prompt List, Desktop Nav Pad, Emoji, and Security Vault.
* **Verification Gate**: Full end-to-end typing, suggestion commit, and modal transition test.

---

### BATCH 6: `com.example.settings` & `com.example.backup` (Settings, Personal Vault & Modular ZIP Backup)
* **Goal**: Modern list-based preferences UI with dedicated sub-pages, text shortcuts palette, personal vault placeholder, and modular ZIP backup engine with password-gated security vault protection & HeliBoard archive migration.
* **Target Package**: `com.example.settings`, `com.example.backup`, `com.example.vault.personal`
* **Key Components**:
  * `SettingsSubPage`: Minimalist list-based hierarchical navigation where each category opens its dedicated sub-page with a top back button.
  * `KeyboardSettings`: Data model for layout heights, insets, haptics, sounds, number row, smart multiply morph (`x` -> `×`), and double-space period.
  * `CustomShortcutsManager`: Configurable 5-slot action bar editor for Desktop Navigation Pad.
  * `PersonalVaultPlaceholderTab`: Dedicated placeholder interface for private notes, custom encrypted scratchpads, and user data.
  * `VianBackupManager`: Modular `.zip` backup generator with granular toggles, password-gated `security_vault.enc` encryption, and automatic HeliBoard backup signature detection & migration.
* **Verification Gate**: Settings persistence, list navigation, and modular ZIP backup import/export verification.

---

### BATCH 7: `com.example.voice` (Offline Raw Audio & Neural Voice Engine)
* **Goal**: Build a 100% offline speech recognition pipeline modeled on FUTO Voice Input, recording raw audio directly and running local neural models (Whisper/GGML + Silero VAD) loaded dynamically after app install (zero bundled models in the APK).
* **Target Package**: `com.example.voice`
* **Sub-packages & Components**:
  * `com.example.voice.audio`:
    * `RawAudioRecorder`: Low-latency `AudioRecord` engine capturing 16kHz, 16-bit Mono PCM into a zero-allocation ring buffer.
  * `com.example.voice.vad`:
    * `SileroVadDetector`: Real-time voice activity detector computing speech probability to trim silence without battery drain.
  * `com.example.voice.engine`:
    * `WhisperEngine`: JNI inference binding for `whisper.cpp` / GGML supporting dynamic model execution on CPU/NPU.
  * `com.example.voice.model`:
    * `VoiceModelManager`: SAF file picker, SHA-256 integrity verifier, model directory manager, and remote model downloader (no models bundled in APK).
  * `com.example.voice.ui`:
    * `VoiceInputOverlayView`: Real-time RMS decibel waveform visualizer and listening sheet with Unified 4-Button bottom bar.
    * `VoiceModelSettingsView`: Settings UI for downloading, importing, and selecting active Whisper and VAD models.
* **Verification Gate**: Kotlin compilation check, model file verification logic, and audio buffer test.

---

### BATCH 8: `com.example.vault` (In-Keyboard Security Vault & KeePassDX Bridge)
* **Goal**: Build an in-keyboard password vault and KeePassDX `.kdbx` reader that injects credentials (usernames, passwords, 2FA tokens) directly via `RichInputConnection.commitText()`, bypassing the Android OS clipboard completely.
* **Target Package**: `com.example.vault`
* **Sub-packages & Components**:
  * `com.example.vault.model`:
    * `VaultEntry`: Data model for encrypted records (ID, Title, Username, Password, URL/Package, TOTP Secret, Custom Fields, Notes, Last Modified).
    * `VaultDatabase`: In-memory container holding unlocked credentials.
  * `com.example.vault.crypto`:
    * `VaultCipher`: AES-256-GCM authenticated cipher with PBKDF2/Argon2 key derivation and zero-fill memory wiping on lock.
  * `com.example.vault.kdbx`:
    * `KdbxParser`: External KeePass 2.x (.kdbx) database loader and parser supporting master password and keyfile authentication via SAF.
  * `com.example.vault.totp`:
    * `TotpGenerator`: RFC 6238 time-based one-time password (TOTP) engine generating live 6-digit verification codes.
  * `com.example.vault.storage`:
    * `VaultRepository`: File manager for app-private encrypted storage, lock state tracker, and auto-lock timeout scheduler.
  * `com.example.vault.ui`:
    * `VaultOverlayView`: In-keyboard interactive sheet for filtering entries, quick-injecting `[User]`, `[Pass]`, and `[TOTP]` pills directly into the active field, and Unified 4-Button bottom bar.
    * `VaultSettingsTab`: Compose settings interface for master password configuration, `.kdbx` database import, entry creation/editing, and auto-lock timer preferences.
* **Verification Gate**: Kotlin compilation check, cryptographic round-trip tests, TOTP generation verification, and IME injection test.

