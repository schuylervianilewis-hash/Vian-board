# VIAN BOARD — PROMPT LIST & PERSONAL VAULT ARCHITECTURE
**Module Specification:** `PROMPT_LIST_AND_PERSONAL_VAULT.md`  
**Core Purpose:** Defines the unified data model and user experience for the two primary Personal Dictionary / Quick Phrases variants: **Prompt List** (rich clipboard-sourced template modal) and **Personal Vault** (masked, pattern/biometric-locked secure phrases).

---

## 1. UNIFIED ARCHITECTURAL FOUNDATION

Rather than maintaining separate, heavy storage layers, all personal words, canned prompts, and secure credentials share a **single, unified Room/SQLite entity** with distinct security and display policies:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   UNIFIED PHRASE ENTITY (Room Database)                  │
├──────────────────────────────────────────────────────────────────────────┤
│ • id: Long (Primary Key, Auto-increment)                                 │
│ • title: String (e.g. "Home Address", "Passport No", "Code Review")      │
│ • triggerShortcut: String? (e.g. "addr", "idnum", "c_rev")               │
│ • content: String (Plaintext OR AES-256-GCM Encrypted Payload)           │
│ • category: String? (e.g. "Work", "AI Prompts", "Secure", "Replies")     │
│ • isPinned: Boolean (Default: false)                                     │
│ • variant: Enum [ STANDARD_DICTIONARY, PROMPT_LIST, PERSONAL_VAULT ]     │
│ • isMasked: Boolean (Default: true for VAULT, false for others)          │
│ • createdAt, updatedAt: Long (Timestamps)                                │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 2. PROMPT LIST MODAL (CLIPBOARD-SOURCED SNIPPETS & TEMPLATES)

### A. Core Concept & Ingestion Flow
The Prompt List is a permanent, 2-column modal housing reusable prompt templates, AI system prompts, and frequently typed paragraphs. It seamlessly integrates with the clipboard history:

```
┌───────────────────────────┐         Long-Press Card
│      CLIPBOARD MODAL      │  ─────────────────────────────►  [ Micro-Popup: 3 Actions ]
│ (Ephemeral & Pinned Clips)│                                   ├── [ 📌 Pin / Unpin ]
└───────────────────────────┘                                   ├── [ 🗑️ Delete ]
                                                                └── [ 📝 Move to Prompt List ]
                                                                               │
                                                                               ▼
                                                                Promotes snippet to permanent
                                                                Prompt List & evicts from FIFO
```

### B. Trigger & Navigation
* **Primary Toolbar Trigger**: **Long-press on the Copy button `[ 📋 ]`** in the top toolbar.
* **Secondary Trigger**: Select `[ 📝 Prompt List ]` from the expanded toolbar drawer (`[ > ]`).

### C. Modal Layout & Interactions
```
┌──────────────────────────────────────────────────────────────────────────┐
│ [ 🔍 Filter Prompts... ]    [ All | AI | Work | Code ]    [ + New Prompt]│
├──────────────────────────────────────────────────────────────────────────┤
│  ╭──────────────────────────╮      ╭──────────────────────────╮          │
│  │ 📌 System Architecture   │      │ 📌 Code Review Template  │          │
│  │ "Act as Senior Android..."│      │ "Please review PR for..."│          │
│  ╰──────────────────────────╯      ╰──────────────────────────╯          │
│  ╭──────────────────────────╮      ╭──────────────────────────╮          │
│  │ Customer Support Reply   │      │ Standard Meeting Notes   │          │
│  │ "Thank you for reaching.."│      │ "Attendees: \nAction..." │          │
│  ╰──────────────────────────╯      ╰──────────────────────────╯          │
├──────────────────────────────────────────────────────────────────────────┤
│  [  ABC  ]      [          SPACE          ]      [  ⌫  ]      [  ↵  ]    │
│                     (Unified 4-Button Bottom Bar)                        │
└──────────────────────────────────────────────────────────────────────────┘
```

* **Short Tap on Prompt Card**: Pastes the prompt content directly into the editor at cursor position and closes the modal.
* **Long-Press on Prompt Card**: Opens a compact micro-popup with 2 actions:
  1. `[ 📌 Pin / Unpin ]`: Toggles sticky status (pinned prompts stay at the top of the grid).
  2. `[ 🗑️ Delete ]`: Removes the prompt from storage.

---

## 3. PERSONAL VAULT (MASKED & PATTERN-LOCKED QUICK PHRASES)

### A. Core Concept
The Personal Vault is **a protected personal dictionary of sensitive quick phrases** (passwords, PINs, bank accounts, passport numbers, API tokens) that are:
1. **Masked visually** on screen (`••••••••` / `123***NY`).
2. **Locked behind an in-keyboard 9-dot Pattern Lock** (with biometric and system lock fallbacks).
3. **Encrypted locally** using AES-256-GCM backed by Android Keystore.

### B. Authentication Hierarchy
```
┌──────────────────────────────────────────────────────────────────────────┐
│                        1. PRIMARY IN-KEYBOARD GATE                       │
│      - 9-Dot Pattern Sheet rendered directly inside keyboard view        │
│      - Zero app switching; muscle-memory 1-second unlock                │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ (Fallback / User Preference)
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     2. HARDWARE BIOMETRIC FALLBACK                       │
│      - BiometricPrompt (Fingerprint / Face Unlock)                       │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ (Secondary Fallback)
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     3. DEVICE SYSTEM LOCK FALLBACK                       │
│      - KeyguardManager (Device PIN / Password / Pattern)                 │
└──────────────────────────────────────────────────────────────────────────┘
```

### C. Session & Auto-Lock Security Rules
* **Auto-Lock on Keyboard Hide**: When `onFinishInputView()` triggers, the decrypted session key is immediately purged from RAM.
* **Inactivity Timeout**: Auto-locks after 60 seconds of idle keyboard state.
* **Failed Attempt Limit**: 5 consecutive incorrect pattern attempts enforce a 30-second lockout timer.

### D. In-Keyboard Vault Modal & Masked Suggestions
* **Access Triggers**:
  * Long-press on symbol toggle `[?123]` on main keyboard.
  * Tap `[ 🔐 Vault ]` from the expanded toolbar drawer.
* **Suggestion Bar Integration**:
  * Typing a registered trigger shortcut (e.g. typing `ssn` or `wifi`) displays a masked suggestion pill: `[ 🔐 ***-**-4521 ]`.
  * Tapping the pill triggers the in-keyboard pattern sheet $\to$ on success, commits the decrypted secret $\to$ relocks.
* **Vault Modal Layout**:
```
┌──────────────────────────────────────────────────────────────────────────┐
│ [ 🔐 Personal Vault ]             [ 🔒 Lock Now ]           [ + Add Secret]│
├──────────────────────────────────────────────────────────────────────────┤
│  ╭──────────────────────────╮      ╭──────────────────────────╮          │
│  │ 📌 Wi-Fi (Home 5G)       │      │ 📌 Primary Bank IBAN     │          │
│  │ [ •••••••••••••••• ]     │      │ [ •••••••••••••••• ]     │          │
│  ╰──────────────────────────╯      ╰──────────────────────────╯          │
│  ╭──────────────────────────╮      ╭──────────────────────────╮          │
│  │ Passport ID Number       │      │ Backup Recovery Key      │          │
│  │ [ •••••••••••••••• ]     │      │ [ •••••••••••••••• ]     │          │
│  ╰──────────────────────────╯      ╰──────────────────────────╯          │
├──────────────────────────────────────────────────────────────────────────┤
│  [  ABC  ]      [          SPACE          ]      [  ⌫  ]      [  ↵  ]    │
│                     (Unified 4-Button Bottom Bar)                        │
└──────────────────────────────────────────────────────────────────────────┘
```
* **Short Tap on Vault Card**: Prompts Pattern/Biometric $\to$ Pastes decrypted secret $\to$ Resets auth state.
* **Long-Press on Vault Card**: Opens micro-popup: `[ 📌 Pin / Unpin ]`, `[ 👁️ Reveal / Copy ]`, `[ 🗑️ Delete ]`.

---

## 4. COMPARISON MATRIX: THE 3 DICTIONARY VARIANTS

| Attribute | Standard User Dictionary | Prompt List Modal | Personal Vault |
| :--- | :--- | :--- | :--- |
| **Primary Use Case** | Single words, names, jargon | Canned templates, notes, AI prompts | Sensitive phrases, PINs, accounts |
| **Payload Format** | Plaintext short string | Plaintext multi-line text | AES-256-GCM encrypted string |
| **Visual Display** | Standard suggestion bar | 2-column card grid in modal | Masked card grid (`••••`) in modal |
| **Security Gate** | None | None | 9-Dot Pattern / Biometric / Device PIN |
| **Creation Sources** | Settings / Auto-learning | Manual / **Moved from Clipboard** | Manual entry (In-Keyboard or Settings) |
| **Primary Trigger** | Inline typing auto-suggest | **Long-press `[ 📋 ]` (Copy button)** | **Long-press `[?123]`** or Drawer `[ 🔐 ]` |
| **Bottom Control Bar**| Standard Keyboard Matrix | Unified 4-Button Bar (`[ABC][SPACE][⌫][↵]`)| Unified 4-Button Bar (`[ABC][SPACE][⌫][↵]`)|

---

## 5. DIAGNOSTICS & PRIVACY SANITIZATION (`LogKeeper`)

* **Component Tag**: `[VAULT]` and `[MODAL]` in `LogKeeper`.
* **Telemetry Recorded**:
  * Auth state transitions (`AUTH_SUCCESS`, `AUTH_LOCKED`, `TIMEOUT_LOCK`).
  * Item count and category filtering metrics.
  * Clipboard $\to$ Prompt List migration events.
* **Strict Privacy Masking**:
  * **ZERO plaintext vault contents, ZERO passwords/PINs, and ZERO prompt text are written to logs.**
  * Example Log Output:
    ```
    [MODAL] 15:10:02.100 | Prompt list modal opened (item_count=18, filter=ALL)
    [MODAL] 15:10:05.420 | Clip migrated to Prompt List (id=142, size=312_bytes)
    [VAULT] 15:11:14.200 | Pattern auth prompt triggered (auth_type=PATTERN_9DOT)
    [VAULT] 15:11:15.850 | Auth verified successfully (session_timer=60s)
    [VAULT] 15:11:17.300 | Masked snippet committed to InputConnection (is_redacted=true)
    [VAULT] 15:12:17.300 | Session expired -> Keys purged from RAM, state=LOCKED
    ```
