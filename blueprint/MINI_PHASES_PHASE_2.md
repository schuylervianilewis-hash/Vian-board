# Phase 2: Mini-Phases Execution Plan (Layouts, Popups & Gestures)

## Overview
This document defines the atomic, modular mini-phases for Phase 2 of Vian Board. Each mini-phase is designed to be self-contained, easily testable on-device, and robust.

---

## 🔹 Mini-Phase 2.1: Multi-Layer Symbol Engine & Shift Lock
- **Objective**: Complete keyboard layout switching across all 4 modes with Caps Lock support.
- **Components & Details**:
  1. `KeyboardLayoutBuilder.kt`:
     - Standard full layout definitions for:
       - `ALPHABET_LOWER` (Standard QWERTY lowercase)
       - `ALPHABET_UPPER` (Standard QWERTY uppercase)
       - `SYMBOLS_PAGE_1` (`?123` layer: numbers, basic math, quotes, common punctuation)
       - `SYMBOLS_PAGE_2` (`=\<` layer: brackets, math symbols, alternate currency, rare symbols)
  2. Mode Switching Logic:
     - `?123` key toggles to `SYMBOLS_PAGE_1`
     - `=\<` key toggles to `SYMBOLS_PAGE_2`
     - `ABC` key returns to `ALPHABET_LOWER`
     - `Shift` single-tap toggles between lowercase and uppercase (auto-reverting to lowercase after typing a character)
     - `Shift` **long-press (or double-tap)** locks uppercase (Caps Lock mode) with distinct visual state (e.g. underline/fill indicator) until `Shift` is tapped again to unlock.
- **Verification**:
  - Tap `?123` -> type numbers & symbols -> tap `=\<` -> type brackets -> tap `ABC` -> returns to letters.
  - Long press `Shift` -> type multiple capital letters continuously without reverting -> tap `Shift` -> unlocks back to lowercase.

---

## 🔹 Mini-Phase 2.2: Long-Press "More Keys" Accent & Symbol Popup
- **Objective**: Holding down keys displays a floating accent/alternate preview popup; sliding onto an alternate selects and commits it upon release.
- **Components & Details**:
  1. `MoreKeySpec.kt` / `MainKeyboardView.kt`:
     - Configurable long-press timer (~350ms).
     - Floating popup overlay positioned above the held key.
     - Rich character mappings:
       - Vowels: `a` (`á, à, â, ä, ã, å, æ`), `e` (`é, è, ê, ë, ē`), `i` (`í, ì, î, ï`), `o` (`ó, ò, ô, ö, õ, ø, œ`), `u` (`ú, ù, û, ü, ū`)
       - Consonants: `c` (`ç`), `n` (`ñ`), `s` (`ß, ś, š`), `y` (`ý, ÿ`)
       - Currency: `$` (`€, £, ¥, ₹, ¢, ₽, ₩`)
       - Punctuation: `.` (`!, ?, ,, :, ;, -, /, @`)
  2. Touch Release Handling:
     - Sliding finger highlights candidate key; releasing commits the selected character to `RichInputConnection`.
- **Verification**:
  - Long-press `e`, drag to `é`, release -> `é` is typed.
  - Long-press `$`, drag to `€`, release -> `€` is typed.

---

## 🔹 Mini-Phase 2.3: Tactile Gestures (Spacebar Cursor Scrubber & Backspace Swipe-to-Delete)
- **Objective**: Gesture-driven cursor positioning and word deletion directly on keyboard surface.
- **Components & Details**:
  1. **Spacebar Cursor Scrubber**:
     - Touch horizontal drag on spacebar (threshold $\Delta X > 15\text{dp}$).
     - Smoothly translates finger travel distance into character steps.
     - Calls `RichInputConnection` to reposition cursor left/right in real-time.
  2. **Backspace Swipe-to-Delete**:
     - Touch drag leftward starting from Backspace key (threshold $\Delta X < -20\text{dp}$).
     - Dynamically selects previous words according to drag distance.
     - Lifting finger deletes the highlighted range.
     - Sliding back to the right before releasing aborts/cancels deletion without losing text.
- **Verification**:
  - Type text, drag spacebar left/right -> cursor scrubs through text cleanly.
  - Drag left from backspace -> selects words backwards; release -> deletes selected words. Slide back right -> cancels.
