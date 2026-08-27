# Themes, Gestures, Toolbar & Layout Dimensions Specification (Vian Board)

This document specifies the remaining subsystem imports from HeliBoard, covering Backspace Swipe Gestures, Themes and Styling, Keyboard Dimensions & Insets, Comprehensive Suggestion & Toolbar Behaviors (including Paste Pills and Incognito Mode), and Open-Source Licensing Attributions.

---

## 1. Backspace Swipe-to-Delete Gesture Engine

```
┌────────────────────────────────────────────────────────────────────────┐
│                   BACKSPACE SWIPE-TO-DELETE GESTURE                    │
├────────────────────────────────────────────────────────────────────────┤
│  • Touch Down on [ ⌫ ] and Slide Left:                                │
│    - Distance-proportional selection of previous words in real-time.   │
│    - Visual highlight applied to words marked for deletion.            │
│                                                                        │
│  • Release Finger (Touch Up):                                          │
│    - Instantly deletes all highlighted words in one atomic operation.  │
│                                                                        │
│  • Slide Back to the Right (Before Release):                           │
│    - Safely cancels the deletion; unselects words without deleting.    │
└────────────────────────────────────────────────────────────────────────┘
```

### Source Files in HeliBoard:
* `latin/touch/PointerTracker.java` (Swipe trajectory & threshold tracking)
* `latin/LatinIME.java` (`handleBatchDelete()` / atomic deletion dispatch)

---

## 2. Themes, Colors & Visual Styling Engine

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VISUAL THEME ARCHITECTURE                       │
├────────────────────────────────────────────────────────────────────────┤
│ 1. Dynamic Material You / Monet (Android 12+):                         │
│    - Automatically extracts accent and surface tones from wallpaper.   │
│                                                                        │
│ 2. Pure OLED Black (#000000):                                          │
│    - Deep true-black canvas for maximum battery savings on AMOLED.     │
│                                                                        │
│ 3. Key Borders & Geometry:                                             │
│    - Toggleable key background borders for high tactile contrast.      │
│    - Adaptive corner radius for keys and modal containers.             │
│                                                                        │
│ 4. Day / Night Automatic Theme Following:                              │
│    - Seamlessly transitions between Light and Dark mode with OS.       │
└────────────────────────────────────────────────────────────────────────┘
```

### Source Files in HeliBoard:
* `latin/settings/ColorTheme.kt`
* `latin/settings/ThemeSettingsFragment.kt`
* `res/values/themes.xml` & `res/values/colors.xml`

---

## 3. Keyboard Dimensions, Sizing & Insets Engine

```
┌────────────────────────────────────────────────────────────────────────┐
│                    KEYBOARD SIZING & INSET CONTROLS                    │
├────────────────────────────────────────────────────────────────────────┤
│ 1. Keyboard Height Scale Slider:                                       │
│    - Continuous adjustment from 70% (compact) to 130% (tall).          │
│                                                                        │
│ 2. Bottom Padding & Navigation Bar Insets:                             │
│    - User-adjustable vertical bottom clearance (0dp to 48dp).          │
│    - Prevents accidental triggering of system gesture navigation bars  │
│      (Home pill / Back swipe).                                         │
└────────────────────────────────────────────────────────────────────────┘
```

### Source Files in HeliBoard:
* `latin/KeyboardLayoutSet.java`
* `latin/settings/AppearanceSettingsFragment.kt`

---

## 4. Comprehensive Suggestion Bar, Toolbar & Interactive Behaviors

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        SUGGESTION BAR & TOOLBAR COMPLETE LIFECYCLE                     │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. DEFAULT TYPING STATE                                                                │
│    [ > ]  │    Candidate 1    │  Candidate 2 (Bold)  │    Candidate 3    │ [🔲] [📋] [📄] │
│                                                                                        │
│ 2. CLIPBOARD PASTE PILL DETECTED (New clip copied in last 60s)                         │
│    [ > ]  │ 📋 Paste: "Recent copied snippet..."                           │ [🔲] [📋] [📄] │
│                                                                                        │
│ 3. INCOGNITO / PRIVATE BROWSING / PASSWORD FIELD                                       │
│    [ 🕶️ Incognito Active — No Learning / No History Recorded ]             │ [🔲] [📋] [📄] │
│                                                                                        │
│ 4. TEXT SELECTION ACTIVE (User selects text in editor)                                 │
│    [ ◀ ] [ ▶ ]  │  [ ✂ Cut ]  │  [ 📋 Copy ]  │  [ 📄 Paste ]  │  [ 🔲 Select All ]    │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### A. Candidate Tap & Long-Press Behaviors
* **Center Candidate (Auto-Correction)**:
  * **Short Tap**: Inserts candidate + appends trailing space.
  * **Spacebar Tap**: Automatically commits this bold candidate when confidence threshold is satisfied.
  * **Long-Press on Candidate**:
    * `[ 🗑️ Remove from Learned History ]`: Purges the word from `UserHistoryDictionary`.
    * `[ 📌 Add to Personal Dictionary ]`: Permanently pins the word to prevent auto-correction.

### B. Inline Clipboard Paste Pill
* **Detection Trigger**: Detects new clipboard content copied within the last 60 seconds.
* **Display**: Temporarily morphs the suggestion strip into a high-visibility **Inline Paste Pill** with icon (`📋`) and snippet preview.
* **Tap Action**: Instantly commits the clipboard text into the active field.
* **Auto-Dismiss**: Typing any key or 5-second inactivity timeout dismisses the pill back to word predictions.

### C. Incognito & Password Privacy Safeguards
* **Active When**:
  * `EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING` is set.
  * `InputType.TYPE_TEXT_VARIATION_PASSWORD` / `TYPE_NUMBER_VARIATION_PASSWORD`.
  * Browser Private / Incognito browsing tabs.
* **Safeguards**:
  * Strictly disables dictionary learning (zero writes to user history).
  * Hides predictions entirely in password fields or shows private mask badge (`🕶️`).
  * Pauses automatic clipboard history capture during password entry.

### D. Toolbar Expand (`>`) & Fast-Action Tools
* **Expand Toggle (`>`)**:
  * Tap reveals secondary tools drawer: Voice Input, Modal Launchers (Clipboard, Prompts, Desktop Nav), Settings shortcut, One-Handed toggle.
* **Right-Side Fast Actions**:
  * `[ 🔲 Select Word ]`: Short tap selects word under cursor; **Long-press selects all** (`Ctrl+A`).
  * `[ 📋 Copy ]`: Copies active text selection.
  * `[ 📄 Paste ]`: Short tap pastes clipboard; **Long-press opens Clipboard Modal**.

---

## 5. Upstream Open-Source Licensing & Legal Attributions

To ensure 100% compliance with the GNU General Public License (GPL-3.0), a dedicated settings subpage provides full legal credits:

```
┌────────────────────────────────────────────────────────────────────────┐
│                    OPEN-SOURCE LICENSES & ATTRIBUTIONS                 │
├────────────────────────────────────────────────────────────────────────┤
│ • HeliBoard (GPL-3.0) — Core layout engine, suggestions & dictionaries │
│ • OpenBoard (GPL-3.0) — Layout assets, key rendering & touch tracking  │
│ • AOSP LatinIME (Apache-2.0) — InputMethodService lifecycle & base IME │
│ • KeePassDX / KeePassJava2 (GPL-3.0) — .kdbx format & cryptographic vlt│
└────────────────────────────────────────────────────────────────────────┘
```

### Source Files in HeliBoard:
* `latin/settings/AboutFragment.kt`
* `NOTICE` & `LICENSE`
