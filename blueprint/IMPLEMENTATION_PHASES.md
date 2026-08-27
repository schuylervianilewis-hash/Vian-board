# 📋 Implementation Phases: 10-Phase On-Device Rebuild Plan

## Strategy Overview
Every single phase produces a clean, compiling, and fully functional Android APK with GitHub Actions CI. 
**Log Keeper** is built in Phase 1 and deeply integrated into every subsequent phase to record clean, zero-PII diagnostic traces across all features.

---

## 🟢 Phase 1: Welcome Onboarding Screen & Full Log Keeper Subsystem (APK #1)
* **Scope**:
  - **One-Time Welcome Screen (First-Launch Only)**:
    - Step 1: Enable VianBoard in Android System Settings (`Settings.ACTION_INPUT_METHOD_SETTINGS`).
    - Step 2: Select VianBoard as the Active/Default Input Method (`InputMethodManager.showInputMethodPicker()`).
    - Step 3: Direct action button to open the **Log Keeper Screen** for immediate diagnostic verification.
    - Memorizes completion state in `SharedPreferences` so onboarding is skipped on future launches.
  - **Complete Log Keeper UI & Engine (Matching Screenshot)**:
    - 200-slot volatile circular memory buffer (zero continuous disk I/O).
    - Strict zero-PII security scrubber firewall.
    - Top Bar: `[← Back]`, `Log Keeper` bold title, **Master ON/OFF Switch**, `[📋 Copy]`, `[📥 Download .txt]`.
    - Segmented Time Filter Bar: `[ 6h ]` | `[ 12h ]` | `[ 24h ]` | `[ All ]`.
    - Rounded `#EAF0F6` card stream with timestamp, component tag, and message summary.
* **Log Keeper Tags**: `LogTag.SYSTEM`, `LogTag.NAVIGATION`.
* **On-Device Verification**: Install APK, complete onboarding steps, open Log Keeper, test Master Switch, test 4 time tabs, test copy and download `.txt`.

---

## 🟢 Phase 2: Core IME Canvas & Responsive Matrix (APK #2)
* **Scope**:
  - High-performance single-pass `Canvas` keyboard renderer.
  - Standard QWERTY layout, Dedicated Number Row toggle, Spacebar, Backspace, and Enter key.
  - Visual themes: Pitch Black AMOLED, Dracula, Monokai, Nord, Material You.
  - Standard key click audio & haptic feedback integration.
* **Log Keeper Tags**: `LogTag.IME` (`onStartInputView`, `onFinishInputView`, canvas layout measured).
* **On-Device Verification**: Open keyboard in any text field (e.g. Notes), verify crisp touch responsiveness, dedicated number row, and theme appearance.

---

## 🟢 Phase 3: Dynamic Input Adaptations & Gestures (APK #3)
* **Scope**:
  - **Dynamic Input Adaptations**:
    - Automatic **Incognito Mode** on password fields (`TYPE_TEXT_VARIATION_PASSWORD`).
    - URI / Web layout (`/` and `.com` / `www.` keys, `[➔ Go]` action key).
    - Email layout (`@` and `.` keys).
    - Number Pad only mode (`TYPE_CLASS_NUMBER` / `TYPE_CLASS_PHONE`).
    - Symbol Pages (`?123` & `=\<`).
  - **Gesture Pipeline**:
    - Spacebar cursor gliding ($\ge 12\text{dp}$ threshold).
    - Swipe-left backspace word deletion.
    - Smart multiply substitution (`5x5` $\to$ `5×5`).
    - Double-space period shortcut (`. `).
* **Log Keeper Tags**: `LogTag.IME` (input type detected, spacebar drag, swipe backspace).
* **On-Device Verification**: Test password field (incognito badge), browser URL bar, email field, number dialer, glide spacebar to move cursor, and swipe backspace.

---

## 🟢 Phase 4: Suggestion Bar & HeliBoard Extended Toolbar (APK #4)
* **Scope**:
  - **Suggestion Bar**: 3-candidate prediction slot + clipboard paste pills.
  - **Left Expandable Chevron Drawer**:
    - `[🕵️ Incognito (tap toggle / long-press 1m)]`
    - `[🎤 Voice Input]`
    - `[↩️ Undo]`
    - `[↪️ Redo]`
    - `[↗️ Go Right Up]`
    - `[↘️ Go Right Down]`
    - `[🗄️ Personal Vault]`
    - `[⚙️ Settings]`
  - **Right Pinned Trio**:
    - `[⬚ Select Word / Long-press Select All]`
    - `[📄 Copy / Long-press Prompts]`
    - `[📋 Paste / Long-press Clipboard]`
  - **Long-Press Switchers**:
    - Long-press Comma (`[,]`) 7-item floating switcher popup (`[Emoji][Clip][Prompts][Vault][Desktop][Settings][Logs]`).
    - Long-press `[?123]` quick vault shortcut trigger.
* **Log Keeper Tags**: `LogTag.TOOLBAR` (drawer expanded, 1m incognito timer started, pinned tool triggered).
* **On-Device Verification**: Open toolbar drawer, test 1-min temporary incognito, test long-press on Copy/Paste/Select, and test comma popup.

---

## 🟢 Phase 5: Desktop Shortcuts Modal (APK #5)
* **Scope**:
  - Standard bottom 4-button navigation bar (`[ABC] [SPACE] [⌫ Del] [↵ Enter]`).
  - **Right Side**: Fat Directional D-Pad (`[▲ Up]`, `[◀ Left]`, `[▼ Down]`, `[▶ Right]`) with $\ge 48\text{dp}$ touch targets.
  - **Left Side**: 5 Customizable Shortcut Slots (Copy, Paste, Cut, Select All, Delete Word, Undo, Redo, Home, End, Tab, Esc).
* **Log Keeper Tags**: `LogTag.MODAL` (Desktop Shortcuts opened, D-pad directional action executed, custom slot action performed).
* **On-Device Verification**: Open Desktop modal via long-press comma or toolbar, maneuver cursor with large fat arrow keys, test 5 shortcut action buttons, and return via `[ABC]`.

---

## 🟢 Phase 6: Clipboard History & Prompt List Modals (APK #6)
* **Scope**:
  - **Clipboard History Modal**:
    - In-keyboard list of recent clipboard items, pin items, clear-all. Tap clip to inject.
  - **Prompt List Modal**:
    - Vertical scrollable prompt cards. Tap to inject prompt template into active field.
* **Log Keeper Tags**: `LogTag.MODAL`, `LogTag.CLIPBOARD` (clip selected, prompt injected). *Zero text content logged.*
* **On-Device Verification**: Copy multiple text snippets, open Clipboard modal via long-press paste, inject clip; open Prompt modal via long-press copy, inject prompt.

---

## 🟢 Phase 7: Personal Vault (Non-Learning Lexicon & Pattern Unlock) (APK #7)
* **Scope**:
  - Manual phrase data store (never learns from daily typing).
  - First/last masked suggestion pill in suggestion bar (e.g. typing `c-a-r` $\to$ `[ c•••••••••••2 ]`).
  - In-keyboard **9-Dot Pattern Canvas** to unlock, commit full phrase, and hold 5-minute active unlock session.
* **Log Keeper Tags**: `LogTag.VAULT` (phrase matched, pattern unlock success/fail, 5m session timeout). *Zero phrases or pattern sequences logged.*
* **On-Device Verification**: Add sample phrase in Personal Vault, type first letters in text field, tap masked pill, draw 9-dot pattern, observe text insertion and 5-minute unlock timer.

---

## 🟢 Phase 8: Security Vault (KeePass, TOTP & Chosen Entry Modal) (APK #8)
* **Scope**:
  - AES-256-GCM cipher, KeePass KDBX SAF reader, RFC 6238 TOTP generator.
  - Long-press `[?123]` quick vault entry.
  - In-keyboard Filter & Sort list (no search bar in IME).
  - **Chosen Entry Modal**:
    - Top Header: `[🔒 Lock]` | `[🔄 Choose Another]`
    - Action Row: `[👤 Username]` | `[🔑 Password]` (masked) | `[⏱️ TOTP]` (live circle countdown) | `[📎 Attachments drop-up]`
  - Auto-domain match suggestion pills (`[🔑 domain.com]`).
* **Log Keeper Tags**: `LogTag.VAULT` (KDBX loaded, entry count, domain matched, TOTP generated). *Zero credentials logged.*
* **On-Device Verification**: Long-press `[?123]` to open vault, select account, tap Username/Password/TOTP to insert, test 30-second TOTP countdown ring.

---

## 🟢 Phase 9: Voice Input Engine (FUTO Whisper / Silero VAD) (APK #9)
* **Scope**:
  - On-demand 16kHz audio capture; zero-background CPU until mic is tapped.
  - Silero VAD silence detection and auto-endpointing.
  - **Live Dynamic RMS Pulse Glow**:
    - Electric Indigo halo (`0xFF6366F1`) expanding $56\text{dp} \to 110\text{dp}$.
    - Crimson Red listening silence state (`0xFFDC2626`).
    - Standby theme neutral.
    - Waveform gradient: Indigo $\to$ Pink $\to$ Amber.
* **Log Keeper Tags**: `LogTag.VOICE` (mic active, VAD silence triggered, audio stopped). *Zero audio/transcripts logged.*
* **On-Device Verification**: Tap mic in toolbar, speak into phone, watch Indigo pulse react dynamically to voice volume, verify Crimson Red silence indicator, and inject text.

---

## 🟢 Phase 10: HeliBoard Binary Dictionary, Backup/Restore & Master Settings (APK #10)
* **Scope**:
  - **HeliBoard Dual Binary Dictionary**: Streaming Trie parser for Primary + Secondary language `.dict` files + User Dictionary editor.
  - **Backup & Restore**: Encrypted AES-ZIP export/import + legacy HeliBoard backup importer.
  - **Master Settings Screen**: Complete M3 list navigation with dedicated sub-pages for all modules (Themes, Gestures & Desktop 5-slot ranking, Prompts, Personal Vault, Security Vault full CRUD/search/folders, Voice, Dictionary, Backup, Log Keeper).
* **Log Keeper Tags**: `LogTag.ENGINE`, `LogTag.BACKUP`, `LogTag.SETTINGS`.
* **On-Device Verification**: Customize theme and key heights, rank Desktop shortcuts in Gestures settings, export/import encrypted backup, import HeliBoard backup, verify full Log Keeper diagnostic stream.
