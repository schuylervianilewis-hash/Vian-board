# On-Screen Layout & Modals Specification (Vian Board)

This document defines the complete on-screen keyboard structure, layout matrices, modal overlay systems, and their direct customization connections to the Material Design 3 Settings suite.

---

## 1. On-Screen Main Keyboard View Engine

```
┌────────────────────────────────────────────────────────────────────────┐
│                      MAIN KEYBOARD VIEW HIERARCHY                      │
├────────────────────────────────────────────────────────────────────────┤
│ [>]  │    Got    │    To    │   Good   │  [🔲 Select] [📋 Copy] [📄 Paste]│  <- Suggestion & Toolbar Strip
├──────┴───────────┴──────────┴──────────┴───────────────────────────────┤
│ [1¹] [2²] [3³] [4⁴] [5⁵] [6⁶] [7⁷] [8⁸] [9⁹] [0⁰]                      │  <- Dedicated Top Number Row
│ [q%] [w'] [e~] [r=] [t[] [y]] [u*] [i!] [o-] [p;]                      │  <- Row 1 + Secondary Hints
│ [a@] [s#] [d₹] [f-] [g&] [h-] [j+] [k(] [l)]                           │  <- Row 2 + Secondary Hints
│ [ ⇧ ] [z*] [x"] [c'] [v:] [b;] [n!] [m?] [ ⌫ ]                         │  <- Row 3 + Hints + Backspace
│ [?123]  [ , ]  [             SPACEBAR             ]  [ . ]  [ ↵ Enter ]│  <- Bottom Control Row
└────────────────────────────────────────────────────────────────────────┘
```

### A. Component Breakdown
1. **Top Suggestion & Action Strip (`SuggestionsView`)**:
   * **Left Expander (`>`)**: Toggles secondary toolbar actions.
   * **Center Prediction Zone**: 3-candidate live word prediction strip (`Got`, `To`, `Good`).
   * **Right Quick Actions**: Dotted Selection Box (`🔲`), Copy (`📋`), Paste (`📄`).
2. **Dedicated Top Number Row**:
   * Fixed 10-key digit row (`1` through `0`) with superscript indices for fast digit entry without switching pages.
3. **QWERTY Matrix with Integrated Hint Glyphs**:
   * Hardware-accelerated Canvas rendering of character keys with secondary symbols in the top-right corner (`d^₹`, `a^@`, `s^#`, `q^%`, etc.).
   * Long-press commits the hint symbol directly.
4. **Bottom Control Row & Routing**:
   * `[?123]`: Short tap $\to$ Symbols/Numbers; Long-press ($\ge 300\text{ms}$) $\to$ Security Vault trigger.
   * `[ , ]`: Short tap $\to$ `,`; Long-press / slide $\to$ Quick Action popup hub (Prompts, Desktop Nav, Clipboard, Settings).
   * `[ SPACEBAR ]`: Center pill; horizontal slide ($\Delta X \ge 12\text{dp}$) $\to$ cursor move; static hold ($\ge 300\text{ms}$) $\to$ language picker.
   * `[ . ]`: Period with punctuation popup.
   * `[ ↵ Enter ]`: Dynamic IME action (Search, Done, Next, Return).

---

### B. Secondary Keyboard Pages & Number Pads

```
┌────────────────────────────────────────────────────────────────────────┐
│ 1. SYMBOLS PAGE 1 (Triggered via `?123` Short Tap)                     │
├────────────────────────────────────────────────────────────────────────┤
│ [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ] [ 6 ] [ 7 ] [ 8 ] [ 9 ] [ 0 ]            │
│ [ @ ] [ # ] [ $ ] [ _ ] [ & ] [ - ] [ + ] [ ( ] [ ) ] [ / ]            │
│ [=\<] [ * ] [ " ] [ ' ] [ : ] [ ; ] [ ! ] [ ? ] [ ⌫ ]                  │
│ [ ABC ]   [ , ]   [             SPACE             ]   [ . ]  [ ↵ Enter]│
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ 2. SYMBOLS PAGE 2 / MORE SYMBOLS (Triggered via `=\<` Tap)             │
├────────────────────────────────────────────────────────────────────────┤
│ [ ~ ] [ ` ] [ | ] [ • ] [ √ ] [ π ] [ ÷ ] [ × ] [ ¶ ] [ ∆ ]            │
│ [ £ ] [ ¥ ] [ $ ] [ ¢ ] [ ^ ] [ ° ] [ = ] [ { ] [ } ] [ \ ]            │
│ [?123] [ % ] [ © ] [ ® ] [ ™ ] [ ✓ ] [ [ ] [ ] ] [ ⌫ ]                 │
│ [ ABC ]   [ < ]   [             SPACE             ]   [ > ]  [ ↵ Enter]│
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ 3. DEDICATED NUMERIC / PHONE DIALPAD (Triggered by Number/Phone Fields)│
├────────────────────────────────────────────────────────────────────────┤
│ ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ ┌──────────────┐ │
│ │       1       │ │       2       │ │       3       │ │      ⌫       │ │
│ ├───────────────┼───────────────┼───────────────┼──────────────┤ │
│ │       4       │ │       5       │ │       6       │ │      -       │ │
│ ├───────────────┼───────────────┼───────────────┼──────────────┤ │
│ │       7       │ │       8       │ │       9       │ │      ,       │ │
│ ├───────────────┼───────────────┼───────────────┼──────────────┤ │
│ │     * / +     │ │       0       │ │       #       │ │   ↵ Enter    │ │
│ └───────────────┘ └───────────────┘ └───────────────┘ └──────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

---

### C. Context-Aware Adaptive Layout Morphing

The keyboard dynamically adapts its bottom row and strip layout based on the active target application's `EditorInfo` (`inputType` & `imeOptions`):

| Target Input Type / Field | On-Screen Dynamic Morph | Functional Behavior |
| :--- | :--- | :--- |
| **Web Browser / URL Field** (`TYPE_TEXT_VARIATION_URI`) | Comma key `[ , ]` morphs to `[ / ]` | Instant path & URL slash typing without switching symbol pages |
| **Web Browser / URL Field** (`TYPE_TEXT_VARIATION_URI`) | Period key `[ . ]` popup | Long-press expands top TLD shortcuts (`.com`, `.org`, `.net`, `.io`, `.gov`) |
| **Email Address Field** (`TYPE_TEXT_VARIATION_EMAIL_ADDRESS`) | Dedicated `[ @ ]` and `[ . ]` keys | Surfaced directly on bottom row flanking the spacebar |
| **Password Field** (`TYPE_TEXT_VARIATION_PASSWORD`) | Top Suggestion Bar hidden | Completely conceals word predictions and disables dictionary learning |
| **Numeric Password / PIN** (`TYPE_NUMBER_VARIATION_PASSWORD`) | Full 3x4 Pin Pad | Direct, large numeric buttons with no letter clutter |
| **Action Key Semantics** (`imeOptions`) | Dynamic `[ ↵ ]` Icon | Morphs to Search `🔍`, Send `✈️`, Next `⏭️`, Done `✔️`, or Go `➡️` |

---

### D. Smart Typing Rules & Micro-Transformations

HeliBoard's intelligent keystroke pipeline automatically handles clean typography and formatting:

1. **Smart Multiply (`x` between digits)**:
   * Typing digits followed by `x` and another digit (e.g., `1920` + `x` + `1080` or `4` + `x` + `4`) automatically converts `x` into the mathematical multiplication sign `×` (`1920×1080`, `4×4`).
2. **Double Space $\to$ Period + Space (`. `)**:
   * Tapping spacebar twice consecutively converts the trailing space into `. ` and auto-shifts the next character to uppercase.
3. **Punctuation Space-Eating & Auto-Spacing**:
   * **Space-Eating**: If a trailing space exists before a punctuation key, typing `, . ! ? ; :` automatically removes the preceding space and snaps the punctuation directly to the previous word.
   * **Auto-Space**: Automatically appends a trailing space after committing punctuation marks (`,`, `.`, `!`, `?`).
4. **Fraction & Superscript Glyphs**:
   * Typing standard fractions (`1/2`, `1/4`, `3/4`) or long-pressing number keys triggers instant fraction glyphs (`½`, `¼`, `¾`, `¹`, `²`, `³`).

---

## 2. Modal Panels & Unified 4-Button Bottom Bar

All secondary modal panels share a standardized bottom control bar to preserve typing muscle memory:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        UNIFIED 4-BUTTON BOTTOM BAR                     │
├────────────────────────────────────────────────────────────────────────┤
│ ┌──────────┐  ┌──────────────────────────────────┐  ┌──────────┐ ┌───┐ │
│ │   ABC    │  │              SPACE               │  │    ⌫     │ │ ↵ │ │
│ └──────────┘  └──────────────────────────────────┘  └──────────┘ └───┘ │
└────────────────────────────────────────────────────────────────────────┘
```

---

### A. Modal 1: Clipboard History Modal

```
┌────────────────────────────────────────────────────────────────────────┐
│ [ ▲ ] [ ▼ ] [ ◀ ] [ ▶ ] [ ↶ ] [ ↷ ] [ ✂ ] [ 📋 ] [ 📄 ] [ 🔲 ] [ ✕ ]   │  <- Navigation & Edit Bar
├────────────────────────────────────────────────────────────────────────┤
│ ┌───────────────────────────────┐ ┌──────────────────────────────────┐ │
│ │ 📌 just discuss no coding or  │ │ 📌 Check current repo against    │ │
│ │    building or updating...    │ │    planned fixes. Check if...    │ │
│ └───────────────────────────────┘ └──────────────────────────────────┘ │  <- 2-Column Responsive Grid
│ ┌───────────────────────────────┐ ┌──────────────────────────────────┐ │
│ │ 📌 Unacceptable. You edited   │ │ 📌 9:16 aspect ratio tik tok     │ │
│ │    files but pasted no...     │ │    style. Hyper realistic.       │ │
│ └───────────────────────────────┘ └──────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────┤
│ [ ABC ]     [                     SPACE                     ]    [ ⌫ ] │  <- Unified Bottom Bar
└────────────────────────────────────────────────────────────────────────┘
```

* **Top Toolbar**: Full directional pad (`▲`, `▼`, `◀`, `▶`) + edit tools (`Undo`, `Redo`, `Cut`, `Copy`, `Paste`, `Select All`, `Close`).
* **Content**: 2-column scrollable grid with pin indicators (`📌`). Tap $\to$ paste; Long-press on card $\to$ tiny popup with 3 micro-actions: `[ 📌 Pin / Unpin ]`, `[ 🗑️ Delete ]`, `[ 📝 Move to Prompt List ]`.
* **Bottom Bar**: Unified 4-button control bar.

---

### B. Modal 2: Emoji Palette Modal
* **Category Tabs**: Smileys & Emotion, People, Nature, Food, Activities, Travel, Objects, Symbols, Flags.
* **Content**: Dense emoji grid with direct tap-to-insert (zero dynamic search overhead for low memory footprint).
* **Bottom Bar**: Unified 4-button control bar (`[ABC] [SPACE] [⌫] [↵]`).

---

### C. Modal 3: Prompt List Modal (Placeholder Stub)
* **Top Bar**: Search filter stub + `[+ New Prompt]` action.
* **Content**: 2-column card grid displaying categorized prompt templates and quick phrases.
* **Actions**: Short tap on prompt $\to$ paste; Long-press on prompt card $\to$ tiny popup with 2 micro-actions: `[ 📌 Pin / Unpin ]` (sticky to top) and `[ 🗑️ Delete ]`.
* **Bottom Bar**: Unified 4-button control bar (`[ABC] [SPACE] [⌫] [↵]`).

---

### D. Modal 4: Desktop Keyboard Modal (`DesktopKeyboardView` Placeholder Stub)
* **Key Matrix**: Spacious 3-row fat-button layout:
  * **Row 1**: `[ HOME ]` `[ ▲ UP ]` `[ END ]` `[ PG UP ]` + Custom Action Slots
  * **Row 2**: `[ ◀ LEFT ]` `[ ▼ DOWN ]` `[ ▶ RIGHT ]` `[ PG DN ]` + Custom Action Slots
  * **Row 3**: `[ ABC ]` `[ SEL ]` `[ SPACE ]` + Custom Action Slot + `[ ⌫ ]` `[ ↵ ]`
* **Bottom Bar**: Unified 4-button control bar.

---

### E. Modal 5: Security Vault Modal (Placeholder Stub)
* **Auth Layer**: In-keyboard authentication sheet (9-Dot Pattern / PIN / Biometric).
* **Content**: KeePass `.kdbx` folder hierarchy tree + RFC 6238 2FA token list with live countdown indicators.
* **Bottom Bar**: Unified 4-button control bar.

---

## 3. Settings Integration & Customization Bridge

Every on-screen layout component and modal connects directly to a dedicated subpage in the Material Design 3 Settings suite:

| On-Screen Component | Target Settings Subpage | Available Customization Options |
| :--- | :--- | :--- |
| **Main Keyboard Matrix** | `🔤 Layout & Languages Settings` | • Toggle Dedicated Number Row<br>• Toggle Hint Symbols on Keys<br>• Keyboard Height Slider (70% to 130%)<br>• Bottom Padding & Inset Adjuster<br>• Key Long-Press Delay (100ms to 500ms)<br>• Spacebar Language Switcher Toggle |
| **Comma Popup Hub** | `🔤 Comma Popup Customization` | • Select items visible in Comma popup (Prompts, Desktop Nav, Clipboard, One-Handed, Settings)<br>• Order of popup shortcuts |
| **Desktop Nav Pad** | `🖥️ Desktop Shortcuts Settings` | • Live 3-Row Layout Preview<br>• Add/Remove shortcuts from Essential Library (Strict Max 5)<br>• Reorder Active Keys (Up/Down arrows)<br>• One-Tap "Reset to Pure Arrows" Preset |
| **Toolbar Strip** | `🛠️ Toolbar & Actions Settings` | • Reorder right-side fast action buttons<br>• Configure tools inside the left `>` expander menu |
| **Clipboard Modal** | `📋 Clipboard Settings` | • Clipboard retention duration<br>• Auto-clear sensitive clips timer<br>• Maximum pinned clips limit |
| **Prompt List Modal** | `📝 Prompts & Templates Settings` | • Create, edit, and organize prompt cards into categories<br>• Import/Export prompt library |
| **Security Vault** | `🔐 Security Vault Settings` | • Toggle `?123` long-press trigger<br>• Link `.kdbx` database file<br>• Configure 9-Dot Pattern / PIN<br>• Manage Personal Privacy Vault masked pills |
| **Visual Styling** | `🎨 Themes & Styling Settings` | • Dynamic Material You / Monet theme<br>• Pure OLED Black mode<br>• Custom Accent Colors and Key Borders |
| **Diagnostics** | `📊 Log Keeper Settings` | • Master logging toggle<br>• Time-range filter (`6h`, `12h`, `24h`, `All`)<br>• Copy/Export diagnostic log buffer |
