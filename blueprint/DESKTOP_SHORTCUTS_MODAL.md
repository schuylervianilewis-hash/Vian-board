# Blueprint: Desktop Shortcuts Modal & Navigation Component

## 1. Overview & Access Paths

The **Desktop Shortcuts Modal** provides desktop PC-level text editing, cursor navigation, selection, and clipboard manipulation directly within the Android IME window without switching apps.

### Access Trigger
- **Long-Press Comma (`[,]`)**: Opens the horizontal Quick Switcher Popup containing:
  1. `[😊 Emoji Modal]`
  2. `[📋 Clipboard History]`
  3. `[💬 Prompt List]`
  4. `[🔒 Security Vault]`
  5. `[🖥️ Desktop Shortcuts]` (Launches this modal)
  6. `[⚙️ Settings Shortcut]`
- **Toolbar Quick Action**: Configurable icon on the top suggestion strip (`ic_desktop_shortcuts`).

---

## 2. On-Screen Layout Architecture

The modal replaces the alpha key canvas while retaining the standardized bottom 4-button navigation bar.

```
┌───────────────────────────────────────────────────────────┐
│ [Custom Slot 1] [Custom Slot 2] │      [  ▲ Up (Fat)  ]   │
│ [Custom Slot 3] [Custom Slot 4] │ [◀ Left] [▼ Down] [▶ Right]
│ [Custom Slot 5 / Action More..] │                         │
├───────────────────────────────────────────────────────────┤
│ [  ABC  ]    [       SPACE       ]    [  ⌫ Del  ]   [ ↵ ] │
└───────────────────────────────────────────────────────────┘
```

### Zone Breakdown

### 1. Right Side — Fat Directional D-Pad (Cursor Navigation)
- **Oversized Touch Targets**: $\ge 48\text{dp} \times 48\text{dp}$ touch targets with rounded button cards.
- **Directional Actions**:
  - `[▲ Up]`: `KeyEvent.KEYCODE_DPAD_UP` (or `setSelection` previous line)
  - `[▼ Down]`: `KeyEvent.KEYCODE_DPAD_DOWN` (or `setSelection` next line)
  - `[◀ Left]`: `KeyEvent.KEYCODE_DPAD_LEFT` (cursor back 1 char; hold for continuous)
  - `[▶ Right]`: `KeyEvent.KEYCODE_DPAD_RIGHT` (cursor forward 1 char; hold for continuous)

### 2. Left Side — 5 Customizable Shortcut Slots (Ranked Selection)
- Displays 5 primary action buttons arranged in a clean grid ($2 \times 2 + 1$ layout).
- Mapped dynamically to the user's top-5 ranked shortcuts chosen in **Settings -> Desktop Shortcuts**.
- **Available Actions in Catalog**:
  - `Copy`: `inputConnection.performContextMenuAction(android.R.id.copy)`
  - `Paste`: `inputConnection.performContextMenuAction(android.R.id.paste)`
  - `Cut`: `inputConnection.performContextMenuAction(android.R.id.cut)`
  - `Select All`: `inputConnection.performContextMenuAction(android.R.id.selectAll)`
  - `Replace`: Opens search/replace text injection dialog
  - `Delete Word`: Deletes previous word up to token boundary
  - `Delete All`: Clears entire active text field (`inputConnection.deleteSurroundingText(...)` or selectAll + delete)
  - `Undo`: `inputConnection.performContextMenuAction(android.R.id.undo)` / `Ctrl+Z`
  - `Redo`: `inputConnection.performContextMenuAction(android.R.id.redo)` / `Ctrl+Y`
  - `Home`: Cursor to beginning of line (`KeyEvent.KEYCODE_MOVE_HOME`)
  - `End`: Cursor to end of line (`KeyEvent.KEYCODE_MOVE_END`)
  - `Tab`: `inputConnection.commitText("\t", 1)`
  - `Escape`: `inputConnection.sendKeyEvent(KeyEvent(ACTION_DOWN, KEYCODE_ESCAPE))`

### 3. Bottom 4 Navigation & Desktop Action Bar
- `[ABC]`: Dismisses modal and returns instantly to standard QWERTY/Alpha layout.
- `[SPACE]`: Physical space commit (`inputConnection.commitText(" ", 1)`).
- `[⌫ Del]`: Desktop forward delete (`KEYCODE_FORWARD_DEL`) or backspace according to active modifier state.
- `[↵ Enter]`: Desktop carriage return / newline commit (`KEYCODE_ENTER`).

---

## 3. Settings & Customization Page (`DesktopShortcutsSettings`)

Each module has its dedicated sub-page reachable from the main Settings screen:

- **Shortcut Ranker & Reorderable List**:
  - Drag-and-drop or rank list of all available desktop actions.
  - The top 5 items in the list automatically populate the 5 quick-slots on the Left panel of the modal.
- **D-Pad Sensitivity & Repeat Rate**:
  - Initial hold delay (e.g., 250ms) and autorepeat rate (e.g., 50ms per step).
- **Default Bottom Delete Behavior**:
  - Toggle between `Backspace` (delete previous character) and `Forward Delete` (delete next character).

---

## 4. Telemetry & Log Keeper Isolation

- **Mandate 17 Compliance**:
  - `LogKeeper` records ONLY component transitions and action codes:
    - Example: `LogTag.IME: "Desktop Shortcuts modal opened via LongPressComma"`
    - Example: `LogTag.IME: "Action executed: ACTION_SELECT_ALL"`
  - **Zero PII**: No text surrounding the cursor, no copied text, and no clipboard contents are ever logged.
