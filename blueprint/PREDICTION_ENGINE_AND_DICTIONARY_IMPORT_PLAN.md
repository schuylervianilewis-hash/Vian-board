# Prediction Engine, Dual-Language & Dictionary Import Plan (Vian Board)

This document provides a detailed, file-by-file import and integration plan for the Autocorrect & Suggestion Engine, Simultaneous Dual-Language System, Binary `.dict` Importers, Personal Dictionary Variants, Settings Subpages, and HeliBoard Backup Migration subsystem.

---

## 1. Engine Architecture & Dataflow

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        HELIPROJECT DICTIONARY & SUGGESTION PIPELINE                    │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                                  TOUCH KEY EVENT STREAM                                │
│                                            │                                           │
│                                            ▼                                           │
│                        [ WordComposer / InputPointers ]                                │
│                     (Collects typed chars + touch proximity)                           │
│                                            │                                           │
│                       ┌────────────────────┴────────────────────┐                      │
│                       ▼                                         ▼                      │
│             [ PRIMARY DICTIONARY ]                    [ SECONDARY DICTIONARY ]         │
│           (e.g., main_en.dict mmap)                 (e.g., secondary_es.dict mmap)     │
│           (Weight Multiplier: 1.0x)                 (Weight Multiplier: 0.85x)         │
│                       │                                         │                      │
│                       ├───────────────┬─────────────────────────┤                      │
│                       ▼               ▼                         ▼                      │
│                 [ UserHistory ]  [ ContactsDict ]        [ Personal/UserDict ]         │
│                 (Learned n-gram) (Device names)          (Custom user words)           │
│                       │               │                         │                      │
│                       └───────────────┼─────────────────────────┘                      │
│                                       ▼                                                │
│                          [ SuggestionStrip / Ranking ]                                 │
│                   - Unigram Frequency Score + Bigram Boost                             │
│                   - Spatial Proximity Distance (Typo penalty)                          │
│                   - Auto-Correction Threshold Evaluator                                │
│                                       │                                                │
│                                       ▼                                                │
│                    [ 3 Candidates to SuggestionsView ]                                 │
│                    Left: Raw/Previous | Center: Auto-Correct (Bold) | Right: Next-Word │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. File-by-File Import Mapping Table

| Module Group | HeliBoard Source Files | Target Package (Vian Board) | Core Responsibility |
| :--- | :--- | :--- | :--- |
| **Suggestion Core** | `latin/Suggest.java`<br>`latin/SuggestedWords.java`<br>`latin/WordComposer.java`<br>`latin/Dictionary.java`<br>`latin/DictionaryGroup.java`<br>`latin/SuggestedWordInfo.java` | `com.example.engine.core` | Evaluates touch proximity, scores word frequencies, handles auto-correct decisions, and generates top 3 candidates. Includes number-aware typo proximity to prevent accidental digits from dropping suggestion candidates. |
| **Dual-Language & Switcher** | `latin/DictionaryFacilitator.java`<br>`latin/Subtype.java`<br>`latin/RichInputMethodSubtype.java`<br>`latin/SubtypeSwitcher.java`<br>`latin/utils/SubtypeLocaleUtils.java` | `com.example.engine.lang` | Manages simultaneous dual-language dictionaries in memory and handles the Spacebar long-press ($\ge 300\text{ms}$) language switcher dialog. |
| **Binary .dict Loader** | `latin/BinaryDictionary.java`<br>`latin/makedict/FormatSpec.java`<br>`latin/utils/DictionaryUtils.kt`<br>`latin/utils/DictionaryInfoUtils.java`<br>`latin/DictionaryCollection.java` | `com.example.engine.dict` | Zero-heap, zero-copy binary `.dict` reader using Linux `mmap()`; handles importing external `.dict` files via `Intent.ACTION_OPEN_DOCUMENT`. |
| **Personal Dictionaries** | `latin/userdictionary/UserDictionary.java`<br>`latin/personalization/UserHistoryDictionary.java`<br>`com.example.engine.user.PersonalVaultDictionary`<br>`com.example.engine.user.PromptListDictionary` | `com.example.engine.user` | Manages the 3 Personal Dictionary variants: Standard User Words, Encrypted Personal Vault Shortcuts (placeholder), and Prompt Templates (placeholder). |
| **Engine Settings** | `latin/settings/CorrectionSettingsFragment.kt`<br>`latin/settings/CustomDictionarySettingsFragment.kt`<br>`latin/userdictionary/UserDictionaryList.kt` | `com.example.settings.screens` | Material Design 3 settings screens for Text Correction, Dictionary Manager, and the Personal Dictionary Suite. |
| **HeliBoard Backup** | `latin/settings/BackupRestoreUtils.kt`<br>`latin/utils/JsonUtils.kt`<br>`latin/settings/BackupSettingsFragment.kt` | `com.example.backup` | Lossless importer for legacy HeliBoard `.zip` and `.json` backup archives. |

---

## 3. The 3 Personal Dictionary Variants

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        3 PERSONAL DICTIONARY / SHORTCUT VARIANTS                       │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. Standard User Words & Shortcuts (Direct from HeliBoard)                             │
│    • Plaintext custom words, technical terms, and classic text expansion shortcuts.   │
│    • Always active; suggestions appear as normal unmasked word pills.                 │
│                                                                                        │
│ 2. Personal Vault Shortcuts (Placeholder Stub)                                         │
│    • Sensitive personal data expansions (Addresses, Phone Numbers, Tax/ID Numbers).    │
│    • Protected & masked: Renders in suggestion strip as privacy pills (e.g., 123***NY).│
│    • Requires Auth (PIN/Pattern/Biometric) to decrypt and expand into the editor.      │
│                                                                                        │
│ 3. Prompt List & Phrase Templates (Placeholder Stub)                                   │
│    • Multi-line prompts, structured snippets, and long-form responses.                 │
│    • Surfaced via the Comma popup hub or Toolbar icon into a 2-column card picker.     │
│    • Expandable into full-screen editor with category tags.                            │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Number-Aware Proximity & Non-Breaking Tokenization Engine

In legacy AOSP / HeliBoard LatinIME, typing an accidental digit (e.g. typing `hell8` instead of `hello` because `8` is adjacent to `i`/`o`) immediately breaks word composition: the engine treats the number as a hard delimiter, aborts dictionary trie traversal, and empties the suggestion strip.

Vian Board resolves this with **Continuous Composition & Number-Adjacent Typo Proximity**:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│             CONTINUOUS COMPOSITION & NUMBER-ADJACENT TYPO PROXIMITY                    │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. Number-Adjacent Spatial Proximity Map:                                              │
│    • Key 1 ➔ [q, w]    • Key 2 ➔ [w, e]    • Key 3 ➔ [e, r]    • Key 4 ➔ [r, t]        │
│    • Key 5 ➔ [t, y]    • Key 6 ➔ [y, u]    • Key 7 ➔ [u, i]    • Key 8 ➔ [i, o]        │
│    • Key 9 ➔ [o, p]    • Key 0 ➔ [p]                                                   │
│                                                                                        │
│ 2. Dual-Branch Lookup on Digit Touches:                                                │
│    • Branch A (Proximity Correction): Evaluates digit as adjacent letter with standard │
│      proximity penalty score. 'hell8' ➔ Matches 'hello' (High Confidence Score).       │
│    • Branch B (Literal / Alphanumeric): Retains 'hell8' as fallback raw candidate.     │
│                                                                                        │
│ 3. Continuous Suggestion Strip Output:                                                 │
│    • Left Candidate: 'hell8' (Literal override)                                        │
│    • Center Candidate (Bold Auto-Correct): 'hello' (Tapping space auto-fixes typo!)     │
│    • Right Candidate: 'helps' / 'hell' (Next-word / alternative prediction)            │
│                                                                                        │
│ 4. Mixed Token & Delimiter Handling:                                                   │
│    • Digits do NOT trigger hard word termination unless followed by whitespace.        │
│    • Intentional alphanumeric codes (MP4, 4K, COVID19) preserved via user dictionary.  │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Settings Subpages Integration

Every engine configuration connects directly to a dedicated Material Design 3 screen:

```
Vian Board Settings
│
├── 🔤 Text Correction & Prediction Settings (Full HeliBoard Suite)
│    ├── Primary Language Selector & Spacebar Subtype Switcher Toggle
│    ├── Secondary Language Selector (for simultaneous dual-typing)
│    ├── Auto-Correction Sensitivity (Off, Modest, Aggressive, Very Aggressive)
│    ├── Accidental Number Typo Auto-Correction Toggle
│    ├── Next-Word Prediction & Context Bigrams Toggle
│    ├── Suggest Contact Names Toggle
│    ├── Block Offensive Words Toggle
│    ├── Double-Space Period & Smart Multiply (x ➔ ×)
│    └── Personalized History Learning & Clear History Data
│
├── 📚 Dictionary Manager Page
│    ├── Primary & Secondary .dict Slot Manager (Status, Word Count, Version)
│    ├── Import External .dict File (Storage file picker)
│    ├── Export / Delete Installed Dictionaries
│    └── Simultaneous Dual-Language Scoring Bias Slider (0.7x – 1.0x)
│
├── 📖 Personal Dictionary Suite
│    ├── 1. Standard User Words & Custom Shortcuts Editor
│    ├── 2. Personal Vault Entries (PIN/Pattern Protected Placeholder)
│    └── 3. Prompts & Templates Manager (2-Column Card Organizer Placeholder)
│
└── 🔄 HeliBoard Backup & Migration Hub
     ├── Import Legacy HeliBoard Backup (.zip / .json archive picker)
     ├── Export Native Vian Backup (.vianbackup)
     └── Selective Restore Checklist (Settings, Dictionaries, User Words, Custom Layouts)
```

---

## 6. Phased Execution Order

```
[Step 1: Binary .dict Loader & Memory Mapper (FormatSpec, DictionaryUtils)]
                             │
                             ▼
[Step 2: Core Suggestion & Proximity Scorer + Number Proximity (Suggest, WordComposer)]
                             │
                             ▼
[Step 3: Dual-Language Facilitator & Spacebar Switcher (DictionaryFacilitator, Subtype)]
                             │
                             ▼
[Step 4: Personal Dictionary Suite (Standard UserDict, Vault Stub, Prompt Stub)]
                             │
                             ▼
[Step 5: Engine Settings Subpages (Text Correction, Dict Manager, Personal Suite)]
                             │
                             ▼
[Step 6: HeliBoard Backup Importer & Migration Engine (BackupRestoreUtils)]
```
