# VIAN BOARD — SECURITY VAULT SPECIFICATION & KDBX ENGINE
**Module Specification:** `SECURITY_VAULT_COMPONENT.md`  
**Reference Architecture:** KeePassDX (`Kunzisoft/KeePassDX` KDBX 3.1 / 4.0 Standard)  
**Core Principles:** 
1. **100% In-Keyboard Read-Only Scope**: Unlocks, navigates folders/entries, and pastes fields strictly within keyboard height without full-screen interruptions.
2. **Zero-Leak Sandboxing**: The `.kdbx` file resides strictly in app-private storage (`/files/vault/security_vault.kdbx`) and cannot be touched by other modules.
3. **No Search Bar in Keyboard**: Pure hierarchical, scrollable folder/file explorer.
4. **Clean Hidden Visuals**: Password and OTP values are NOT rendered on screen—action button icons are displayed with live circular timer on the OTP card.
5. **Settings Admin Suite**: Dedicated settings management page with separate authentication, full CRUD, strong customizable password generator, and real-time password strength / entropy meter.

---

## 1. IN-KEYBOARD READ-ONLY SCOPE & SANDBOXED STORAGE

```
┌──────────────────────────────────────────────────────────────────────────┐
│              APP-PRIVATE STORAGE SANDBOX (Zero External Access)          │
│  Path: /data/user/0/<app_id>/files/vault/security_vault.kdbx             │
│  - Mode: Context.MODE_PRIVATE (Encrypted AES-256 / ChaCha20)             │
│  - No cloud sync, no public storage access, no external socket read      │
├──────────────────────────────────────────────────────────────────────────┤
│           ON-DEMAND MEMORY LIFECYCLE (2-Minute Active Session)           │
│  - Master keys and nodes exist in memory ONLY while unlocked             │
│  - Auto-locks on: 120s timer expiry, keyboard dismiss, or [🔒 Lock]      │
│  - Zero-Trace RAM Scrubbing: byte arrays overwritten with 0x00 on lock   │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 2. IN-KEYBOARD AUTHENTICATION & SUGGESTION PILLS

### A. In-Keyboard Pattern Lock Sheet (Within Keyboard Height)
* Unlocked via a **9-Dot Pattern Sheet** rendered directly within the keyboard's height bounds (no full-screen activity or dialog popup).
* **Fallbacks**:
  1. Biometric Authentication (`BiometricPrompt` for Fingerprint / Face).
  2. Device System Lock (`KeyguardManager` for Device PIN / Password).

### B. Input Recognition & Password Suggestion Pill
* When the keyboard detects a login field or shortcut trigger:
  * An inline suggestion pill displays the **Password Shortcut** icon & trigger.
  * Tapping the pill:
    * If locked $\to$ Prompts in-keyboard 9-dot pattern $\to$ Commits decrypted password directly to `InputConnection`.
    * If already in active 2-minute session $\to$ Instantly commits password.

---

## 3. IN-KEYBOARD TWO-STAGE NAVIGATION

```
┌────────────────────────────────────────────────────────────────────────┐
│                   STAGE 1: FOLDER & ENTRY EXPLORER                     │
│ [ 🔐 Security Vault ]                  [ ⏱️ 1:52 ]         [ 🔒 Lock Now ]│
├────────────────────────────────────────────────────────────────────────┤
│  ▼ 📁 Personal                                                         │
│     ├── 📄 GitHub (schuyler_dev)                                       │
│     └── 📄 ProtonMail (vian@pm.me)                                     │
│  ▶ 📁 Work & Servers (4 items)                                         │
│  ▼ 📁 Banking & Crypto                                                 │
│     └── 📄 Chase Bank (schuyler_l)                                     │
├────────────────────────────────────────────────────────────────────────┤
│  [  ABC  ]      [          SPACE          ]      [  ⌫  ]      [  ↵  ]  │
└────────────────────────────────────────────────────────────────────────┘
```
*(Tap entry $\to$ Transitions to Stage 2 Selected Entry View)*

```
┌────────────────────────────────────────────────────────────────────────┐
│                   STAGE 2: SELECTED ENTRY DETAIL VIEW                  │
│ [ ◀ Back to Vault ]        [ 🏷️ GitHub ]           [ 🔒 Lock Now ]      │
├────────────────────────────────────────────────────────────────────────┤
│  ╭──────────────────────────╮      ╭──────────────────────────╮        │
│  │ 👤 Username              │      │ 🔑 Password              │        │
│  │ schuyler_dev             │      │ [ 🔑 Paste Password ]    │        │
│  │ [ 📄 Paste User ]        │      │ *(Hidden Value)*         │        │
│  ╰──────────────────────────╯      ╰──────────────────────────╯        │
│  ╭──────────────────────────╮      ╭──────────────────────────╮        │
│  │ ⏱️ Authenticator (OTP)   │      │ 📎 Attachments           │        │
│  │   ╭──╮                   │      │ 📄 recovery_keys.txt (1) │        │
│  │   │24│ (Live Circle)     │      │                          │        │
│  │   ╰──╯                   │      │ [ 👁️ View / Paste ]      │        │
│  │ [ ⏱️ Paste OTP ]         │      │                          │        │
│  ╰──────────────────────────╯      ╰──────────────────────────╯        │
├────────────────────────────────────────────────────────────────────────┤
│  [  ABC  ]      [          SPACE          ]      [  ⌫  ]      [  ↵  ]  │
│                     (Unified 4-Button Bottom Bar)                      │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. SELECTED ENTRY COMPONENT SPECIFICATIONS

1. **Top Header**:
   * `[ ◀ Back to Vault ]`: Returns to Stage 1 Folder Tree without locking session.
   * `[ 🏷️ Title ]`: Displays entry label.
   * `[ 🔒 Lock Now ]`: Manually locks session and wipes RAM immediately.

2. **The 4 Modular Action Cards**:
   * **👤 Username Card**: Displays title/username text + `[ 📄 Paste User ]` action button.
   * **🔑 Password Card**: Displays clean key icon & `[ 🔑 Paste Password ]` button. The password string itself is **NEVER displayed on screen**.
   * **⏱️ Authenticator (OTP) Card**: Displays a live circular countdown progress ring (0–30s remaining) + `[ ⏱️ Paste OTP ]` button. Raw 6-digit OTP code is **NOT printed in plain text**.
   * **📎 Attachments Card & Popup**: Lists embedded files stored inside the KDBX entry. Tapping opens an in-keyboard popup sheet with attachment preview and `[ 📄 Paste Attachment ]` button.

3. **Unified 4-Button Bottom Control Bar**:
   * `[ABC] [SPACE] [⌫] [↵]` maintains full typing flow at all times.

---

## 5. SETTINGS PAGE: SECURITY VAULT ADMIN SUITE

Full CRUD operations (Add, Edit, Delete, Import, Export) are separated into a dedicated **Settings $\to$ Security Vault** page.

### A. Independent Authentication
* Requires separate Master Password / Pattern, Biometric, or Phone Lock before opening.

### B. Professional Password Generator
* **Length Slider**: 8 to 64 characters (default: 20 characters).
* **Character Set Toggles**:
  * Uppercase (`A-Z`)
  * Lowercase (`a-z`)
  * Digits (`0-9`)
  * Special Characters (`!@#$%^&*()_+-=[]{}|;:,.<>?`)
  * Avoid Ambiguous Characters (`0`, `O`, `l`, `1`, `I`)
* **Memorable Passphrase Mode**: Diceware multi-word generator (e.g. `correct-horse-battery-staple`).

### C. Live Password Strength & Entropy Measurer
* **Entropy Gauge**: Calculates bit entropy:
  $$E = L \times \log_2(R)$$
  *(where $L$ = password length, $R$ = character pool size)*.
* **Visual Strength Meter**:
  * **Red (Weak, $< 40\text{ bits})$**
  * **Yellow (Fair, $40 - 64\text{ bits})$**
  * **Green (Strong, $65 - 80\text{ bits})$**
  * **Cyan / Emerald (Very Strong / Military Grade, $> 80\text{ bits})$**
* **Instant Action**: `[ 📋 Copy ]` and `[ 💾 Save to Entry ]`.

---

## 6. DIAGNOSTICS & PRIVACY SANITIZATION (`LogKeeper`)

* **Tag**: `[VAULT]` / `LogTag.VAULT`.
* **Tracked Telemetry**:
  * In-keyboard pattern verification outcomes.
  * Session duration and countdown triggers.
  * Node navigation and folder expand/collapse events.
* **Strict Redaction**:
  * **ZERO passwords, ZERO usernames, ZERO OTP seeds, and ZERO attachment contents are written to `LogKeeper`.**
