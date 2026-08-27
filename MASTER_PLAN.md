# 🏛️ Master UI & System Architecture Blueprint

## 1. Top Suggestion Bar & Toolbar Architecture (HeliBoard Extended)

### A. Layout Structure & Pinned Layout
The suggestion bar sits directly above the keyboard matrix and dynamically transitions between typing candidates and the extended toolbar.

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ [ < Chevron ] [ 1. Candidate / Context Pill ] [ 2. Main Candidate ] [ 3. Cand ] │ [ ⬚ Select ] [ 📄 Copy ] [ 📋 Paste ] │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

#### 1. Left Side — Expandable Tool Chevron (`[ < ]`):
- Tapping opens the horizontal scrollable drawer containing the **Default Toolbar Tools**:
  1. `[ 🕵️ Incognito ]` (Single tap = toggle ON/OFF; **Long-press = Temporary 1-minute auto-revert Incognito mode**)
  2. `[ 🎤 Voice Input ]` (Triggers dedicated Voice Input Modal with Live RMS Glow)
  3. `[ ↩️ Undo ]` (Triggers `inputConnection.performContextMenuAction(android.R.id.undo)` / `Ctrl+Z`)
  4. `[ ↪️ Redo ]` (Triggers `inputConnection.performContextMenuAction(android.R.id.redo)` / `Ctrl+Y`)
  5. `[ ↗️ Go Right Up ]` (Desktop cursor maneuver / line up navigation)
  6. `[ ↘️ Go Right Down ]` (Desktop cursor maneuver / line down navigation)
  7. `[ 🗄️ Personal Vault ]` (Direct shortcut to Personal Vault settings & phrase management)
  8. `[ ⚙️ Settings ]` (Direct shortcut to main Settings root page)

#### 2. Right Side — Pinned Quick Action Trio (Fixed Order):
- **Pin 1: `[ ⬚ Select Word ]`** (Single tap = Select Word; **Long-press = Select All**)
- **Pin 2: `[ 📄 Copy ]`** (Single tap = Copy selection; **Long-press = Open Prompt List Modal**)
- **Pin 3: `[ 📋 Clipboard / Paste ]`** (Single tap = Paste; **Long-press = Open Clipboard History Modal**)

#### 3. Center — Dynamic Suggestion & Smart Context Pills (HeliBoard Behavior):
- **Standard Autocomplete**: Displays 3 ranked dictionary predictions (Primary + Secondary language models).
- **Clipboard Suggestion Pills**: Displays newly copied text snippet pill for 1-tap fast pasting (like HeliBoard).
- **Security Vault Domain Match Pills**:
  - If the active input field URL or app package matches a stored KeePass entry, a dedicated credential pill appears (e.g. `[ 🔑 github.com ]`).
  - Tapping prompts the in-keyboard 9-dot pattern unlock (unless already within the 5-minute active unlock period), then auto-fills username/password directly.
- **Personal Vault Masked Suggestion Pills**:
  - Typing first few letters shows masked pill `[ c•••••••••••2 ]`.
  - Tapping unlocks via 9-dot pattern (if locked) and commits full phrase with 5-minute session memory.

---

## 2. Dynamic Keyboard Layouts & Input Adaptation

The keyboard automatically adapts its layout, action key, and privacy mode based on `EditorInfo.inputType`:

1. **Password / Sensitive Fields**:
   - Automatically forces **Incognito Mode** (incognito badge displays on spacebar, disables dictionary learning, suppresses clipboard logging).
2. **Web / URL Fields (`TYPE_TEXT_VARIATION_URI`)**:
   - Spacebar shrinks; dedicated `[/]` and `[.com]` / `[www.]` keys appear beside the spacebar. Action key becomes `[ ➔ Go ]`.
3. **Email Fields (`TYPE_TEXT_VARIATION_EMAIL_ADDRESS`)**:
   - Dedicated `[@]` and `[.]` keys appear flanking the spacebar.
4. **Number Pad Only Mode (`TYPE_CLASS_NUMBER` / `TYPE_CLASS_PHONE`)**:
   - Switches to 3x4 large-format numeric dialpad with `[#]`, `[*]`, `[+]`, `[-]`, and standard `[⌫]` / `[↵]`.
5. **Symbol Page 1 (`?123`) & Symbol Page 2 (`=\<`)**:
   - **Page 1 (`?123`)**: Standard numeric row + primary symbols (`@`, `#`, `$`, `%`, `&`, `-`, `+`, `(`, `)`).
   - **Page 2 (`=\<`)**: Mathematical, bracket, and currency operators (`~`, `\`, `|`, `<`, `>`, `{`, `}`, `€`, `£`, `¥`).
   - **Long-Press `[?123]`**: Instant in-keyboard **Security Vault Quick Entry**.

---

## 3. Comprehensive In-Keyboard Overlays & Modals

All in-keyboard overlays adopt the standardized **Bottom 4 Navigation Bar**:
`[  ABC  ]    [       SPACE       ]    [  ⌫ Del  ]   [ ↵ ]`

### A. Desktop Shortcuts Modal
- **Location & Settings**: Customization page lives under **Settings -> Gestures & Typing Behavior**.
- **Right Side**: Fat Directional D-Pad (`[▲ Up]`, `[◀ Left]`, `[▼ Down]`, `[▶ Right]`) with oversized touch targets ($\ge 48\text{dp}$).
- **Left Side**: 5 Customizable Shortcut Slots (e.g. `Copy`, `Paste`, `Cut`, `Select All`, `Delete Word`, `Delete All`, `Undo`, `Redo`, `Home`, `End`, `Tab`, `Esc`).
- **Bottom Bar**: `[ABC]`, `[SPACE]`, `[⌫ Del]` (Forward Delete / Backspace), `[↵ Enter]`.

### B. Clipboard History Modal
- Displays scrollable list of recent clipboard items, pinned clips, and clear-all action.
- Tapping any clip immediately injects it into the active input.

### C. Prompt List Modal (Triggered via Long-Press Copy / Toolbar)
- Vertical scrollable prompt cards. Tap to inject template directly.

### D. Security Vault In-Keyboard Modal (Triggered via Long-Press `[?123]` / Toolbar)
- **Vault List View**: Filter & Sort controls (no search bar in IME view).
- **Chosen Entry Modal**:
  - Top Action Header: `[🔒 Lock]` | `[🔄 Choose Another]`
  - Action Row: `[👤 Username]` | `[🔑 Password]` (Masked / hidden) | `[⏱️ TOTP Authenticator]` (Live circle countdown) | `[📎 Attachments Drop-up]`

### E. Voice Input Modal (Triggered via Toolbar `[🎤 Voice]`)
- **Live Pulse Glow Dynamics**:
  - Speaking (RMS > 0.05): Electric Indigo halo `Color(0xFF6366F1)` ($\alpha = 0.45\text{f} \to 0.05\text{f}$) expanding $56\text{dp} \to 110\text{dp}$. FAB in Deep Indigo `Color(0xFF4F46E5)`.
  - Mic Active Silence: Crimson Red FAB `Color(0xFFDC2626)` with soft red halo `Color(0xFFEF4444)` at $\alpha = 0.15\text{f}$.
  - Stopped / Standby: M3 `primaryContainer` theme neutral.
- **Waveform Gradient**: Horizontal spectrum `Color(0xFF6366F1)` (Indigo) $\to$ `Color(0xFFEC4899)` (Pink) $\to$ `Color(0xFFF59E0B)` (Amber).

---

## 4. Log Keeper UI (Matching Uploaded Screenshot Specification)

- **Access Point**: Long-Press Comma (`[,]`) switcher shortcut or Settings.
- **Top Bar**: `[← Back]` | `Log Keeper` | **Master Toggle Switch** (`[ON/OFF]`) | `[📋 Copy]` | `[📥 Download .txt]`.
- **Time Tabs**: `[ 6h ]` | `[ 12h ]` | `[ 24h ]` | `[ All ]`.
- **Stream Cards**: Rounded `#EAF0F6` cards showing `Timestamp` on left, `Component Tag` on right, zero-PII event description below.
- **Firewall Policy**: Zero visibility into passwords, PINs, KeePass data, or personal vault contents.

---

## 5. Master Settings & Modular Navigation Structure

```
Settings (Master Root)
├── 🎨 Keyboard Layout & Themes
│   ├── Theme Selector (Pitch Black AMOLED, Dracula, Monokai, Nord, Material You)
│   ├── Toolbar Customizer (Reorder & Pin HeliBoard + Extended tools)
│   ├── Dedicated Number Row Toggle
│   └── Key Heights & Secondary Hints
│
├── 👆 Gestures & Typing Behavior
│   ├── 🖥️ Desktop Modal Customizable Buttons & Rankings (5 Left-Panel Slots)
│   ├── Spacebar Cursor Sensitivity (≥12dp)
│   ├── Long-Press [?123] Vault Shortcut Toggle
│   ├── Long-Press Toolbar Actions (Copy→Prompts, Paste→Clipboard, Select→SelectAll)
│   ├── Swipe-Left Backspace Word Deletion
│   └── Double-Space Period & Smart Multiply ('x' → '×')
│
├── 💬 Prompt List Manager
│   └── Prompt Template Editor (Create, Edit, Delete Snippets)
│
├── 🗄️ Personal Vault (Secure Non-Learning Lexicon)
│   ├── Manual Phrases & Masked Item Editor
│   └── 5-Minute Inactivity Unlock Settings
│
├── 🔒 Security Vault (Full Database Suite)
│   ├── Search Bar & Filter/Sort Controls
│   ├── Full CRUD: Create, Edit, Delete, Move, Create Folders
│   ├── KeePass .kdbx File Linking & Sync
│   └── Master Unlock Mode (9-Dot Pattern vs PIN vs Password)
│
├── 📖 Dictionary & Prediction Engine
│   ├── Primary & Secondary Language Selection (HeliBoard dual-model)
│   ├── HeliBoard Binary .dict Importer
│   └── User Dictionary Editor
│
├── 🎙️ Voice Input (FUTO Whisper / Silero VAD)
│   ├── Audio Glow & Waveform Visualizer Settings
│   ├── Whisper Model File Selector (.bin/.gguf via SAF)
│   └── VAD Silence Threshold (ms) & Thread Allocation
│
├── 📦 Backup & Restore
│   ├── Export Encrypted VianBoard ZIP
│   ├── Import VianBoard Backup
│   └── Import Legacy HeliBoard Backup Archive
│
└── 📋 Log Keeper & Diagnostics
    ├── Master On/Off Switch
    ├── Live Log Viewer ([6h][12h][24h][All] UI)
    ├── Download / Export Logs (.txt to device)
    └── Clear Memory Ring Buffer
```

---

## 6. Gesture Mechanics, Precision Rules & Performance Constraints

### A. Advanced Gesture Pipeline
1. **Spacebar Cursor Gliding**:
   - Touch drag on spacebar with horizontal sensitivity $\ge 12\text{dp}$ moves text cursor character-by-character with tactile accuracy.
2. **Swipe-Left Backspace Word Deletion**:
   - Sliding left from the `[⌫]` key deletes preceding tokens/words based on swipe distance.
3. **Smart Multiply Substitution**:
   - Typing character `x` between two numeric digits automatically transforms to the mathematical multiplication sign `×` (e.g., `5x5` $\to$ `5×5`).
4. **Double-Space Period Shortcut**:
   - Tapping the spacebar twice after a word automatically commits `. ` (period + space).

### B. Ultra-Lightweight Memory & CPU Constraints
1. **On-Demand Lazy Instantiation**:
   - **KeePass / Vault Cipher**: Zero memory footprint until pattern unlock; decrypted records and keys are aggressively purged from RAM after 5 minutes of inactivity.
   - **Whisper Speech Native Library**: `WhisperEngine` native binaries and buffers are never allocated at startup; loaded strictly on-demand when the microphone tool is pressed.
2. **Zero-Overhead Log Keeper Ring Buffer**:
   - Maximum 200 in-memory diagnostic slots operating as a volatile circular buffer.
   - Zero continuous disk I/O; writes to disk only when user explicitly taps `[📥 Download]`.
3. **Zero Background CPU**:
   - When the keyboard window is hidden (`onFinishInputView`), all coroutine loops, audio listeners, and Trie decoders enter deep sleep immediately.

