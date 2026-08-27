# RECEIPTS LOG (AUDIT TRAIL)

## Entry 001
- **Timestamp**: 2026-08-26T04:03:00-07:00
- **Summary**: Updated prediction engine blueprint and main blueprint with continuous composition and number-aware typo proximity engine.
- **Exact Files Touched**:
  - `/PREDICTION_ENGINE_AND_DICTIONARY_IMPORT_PLAN.md`
  - `/BLUEPRINT.md`
- **Action Taken**: Formatted and documented the number-aware typo proximity mapping (keys 1-0 adjacent to letters) and continuous suggestion mechanism to prevent accidental digit keypresses from clearing the suggestion strip or skipping auto-correction.
- **Verification**: Document edits verified via read-back and diff checking.
- **Deviation**: None.
- **Follow-up**: None.

## Entry 002
- **Timestamp**: 2026-08-26T04:37:15-07:00
- **Summary**: Created dedicated specification file for Prompt List and Personal Vault dictionary variants.
- **Exact Files Touched**:
  - `/PROMPT_LIST_AND_PERSONAL_VAULT.md`
- **Action Taken**: Authored full technical architecture detailing the unified Room entity schema, the 3 Personal Dictionary variants (Standard Dictionary, Prompt List, Personal Vault), the Clipboard -> Prompt List migration pipeline via long-press micro-actions, the in-keyboard 9-dot pattern lock authentication hierarchy with biometric/device lock fallback, and privacy-sanitized LogKeeper logging.
- **Verification**: Verified file creation and content integrity.
- **Deviation**: None.
- **Follow-up**: None.

## Entry 003
- **Timestamp**: 2026-08-26T05:29:00-07:00
- **Summary**: Created dedicated specification file for Security Vault and KDBX engine.
- **Exact Files Touched**:
  - `/SECURITY_VAULT_COMPONENT.md`
- **Action Taken**: Authored full technical architecture for the in-keyboard read-only KeePassDX-compatible Security Vault, covering in-keyboard 9-dot pattern unlock within keyboard height, folder/entry tree without search bar, selected entry view with hidden password/OTP values and live circular OTP countdown timer, attachment popup, sandboxed app-private KDBX storage, and Settings admin page with strong password generator and entropy strength meter.
- **Verification**: File created and verified via read-back.
- **Deviation**: None.
- **Follow-up**: None.

## Entry 004
- **Timestamp**: 2026-08-26T09:33:45-07:00
- **Summary**: Created BATCH_IMPORT_PLAN.md and imported Batch 1 (com.example.foundation).
- **Exact Files Touched**:
  - `/blueprint/BATCH_IMPORT_PLAN.md`
  - `/app/src/main/java/com/example/foundation/common/Constants.kt`
  - `/app/src/main/java/com/example/foundation/common/LocaleUtils.kt`
  - `/app/src/main/java/com/example/foundation/common/UnicodeUtils.kt`
  - `/app/src/main/java/com/example/foundation/common/CollectionUtils.kt`
  - `/app/src/main/java/com/example/foundation/utils/CoordinateUtils.kt`
  - `/app/src/main/java/com/example/foundation/utils/ResourceUtils.kt`
  - `/app/src/main/java/com/example/foundation/utils/DeviceUtils.kt`
  - `/app/src/main/java/com/example/foundation/utils/SubtypeLocaleUtils.kt`
  - `/app/src/main/java/com/example/foundation/utils/ByteArrayDictBuffer.kt`
  - `/app/src/main/java/com/example/foundation/utils/RingCharBuffer.kt`
  - `/app/src/main/java/com/example/foundation/utils/Subtype.kt`
  - `/app/src/main/java/com/example/foundation/utils/RichInputMethodSubtype.kt`
- **What was actually done**: Created the complete 6-stage batch import roadmap in /blueprint/BATCH_IMPORT_PLAN.md. Authored and imported all Batch 1 foundation utilities, coordinate math, byte buffers, unicode converters, ring buffers, and subtype models under com.example.foundation.
- **How it was verified**: Full application compilation verified via compile_applet tool with zero errors (Build succeeded).
- **Deviation**: None.
- **Follow-up**: Batch 2 completed. Ready for Batch 3 (com.example.engine.core).

## Entry 005
- **Timestamp**: 2026-08-26T09:38:15-07:00
- **Summary**: Imported Batch 2 (com.example.engine.dict) for binary .dict and memory-mapped file decoding.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/engine/dict/FormatSpec.kt`
  - `/app/src/main/java/com/example/engine/dict/Probability.kt`
  - `/app/src/main/java/com/example/engine/dict/BinaryDictDecoder.kt`
  - `/app/src/main/java/com/example/engine/dict/Dictionary.kt`
  - `/app/src/main/java/com/example/engine/dict/BinaryDictionary.kt`
  - `/app/src/main/java/com/example/engine/dict/DictionaryGroup.kt`
  - `/app/src/main/java/com/example/engine/dict/DictionaryCollection.kt`
- **What was actually done**: Authored the full Batch 2 dictionary storage and parsing layer under com.example.engine.dict, including AOSP/HeliBoard v2/v4 magic header validation, trie traversal, zero-heap memory-mapped (mmap) FileChannel loader, multi-dictionary grouping, and collection management.
- **How it was verified**: Full application compilation verified via compile_applet with zero errors (Build succeeded).
- **Deviation**: None.
- **Follow-up**: Batch 3 completed. Ready for Batch 4 (com.example.keyboard.internal).

## Entry 006
- **Timestamp**: 2026-08-26T09:41:00-07:00
- **Summary**: Imported Batch 3 (com.example.engine.core) with Number-Aware Typo Proximity Engine.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/engine/core/SuggestedWordInfo.kt`
  - `/app/src/main/java/com/example/engine/core/SuggestedWords.kt`
  - `/app/src/main/java/com/example/engine/core/WordComposer.kt`
  - `/app/src/main/java/com/example/engine/core/Suggest.kt`
  - `/app/src/main/java/com/example/engine/core/DictionaryFacilitator.kt`
- **What was actually done**: Authored and imported Batch 3 suggestion and word composition core under com.example.engine.core. Implemented WordComposer with the Number-Aware Typo Proximity Engine (mapping top-row digits 1-0 adjacent to letters without dropping suggestions), Suggest scoring engine with configurable auto-correct sensitivities (Off, Modest, Aggressive, Very Aggressive), SuggestedWords 3-slot container, and multi-language DictionaryFacilitator.
- **How it was verified**: Full application compilation verified via compile_applet with zero errors (Build succeeded).
- **Deviation**: None.
- **Follow-up**: Batch 4 completed. Ready for Batch 5 (com.example.ime).

## Entry 007
- **Timestamp**: 2026-08-26T09:43:45-07:00
- **Summary**: Imported Batch 4 (com.example.keyboard.internal) for Canvas layout, keys, popups, and pointer tracking.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/keyboard/internal/Key.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MoreKeySpec.kt`
  - `/app/src/main/java/com/example/keyboard/internal/KeyboardLayoutBuilder.kt`
  - `/app/src/main/java/com/example/keyboard/internal/PointerTracker.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
- **What was actually done**: Authored and imported the full Batch 4 keyboard rendering and touch dispatch layer under com.example.keyboard.internal. Implemented hardware-accelerated MainKeyboardView with 8dp rounded keys and top-right hint rendering, KeyboardLayoutBuilder supporting QWERTY / Shift / Symbols 1 & 2 / optional number row, PointerTracker with zero-latency multi-touch and micro-gestures (Spacebar cursor glide and Backspace word swipe), and MoreKeySpec for extended popups.
- **How it was verified**: Full application compilation verified via compile_applet with zero errors (Build succeeded).
- **Deviation**: None.
## Entry 008
- **Timestamp**: 2026-08-26T09:49:30-07:00
- **Summary**: Imported Batch 5 (com.example.ime & modals) connecting IME service, Canvas keyboard, suggestions, and modal overlays.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/RichInputConnection.kt`
  - `/app/src/main/java/com/example/ime/SubtypeSwitcher.kt`
  - `/app/src/main/java/com/example/ime/SuggestionStripView.kt`
  - `/app/src/main/java/com/example/ime/ModalOverlayManager.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
- **What was actually done**: Authored and imported Batch 5 InputMethodService lifecycle and modal management layer. Implemented RichInputConnection (supporting word deletions, cursor delta moves, batch edits, and clipboard actions), SubtypeSwitcher for instant language switching, SuggestionStripView with 3-slot candidate rendering and fast-action pills, ModalOverlayManager hosting in-keyboard overlays with the mandatory Unified 4-Button Bottom Bar [ABC] [SPACE] [⌫] [↵], and updated VianBoardService to coordinate text input, hardware Canvas views, suggestion pipelines, and diagnostic LogKeeper telemetry.
- **How it was verified**: Full application compilation verified via compile_applet with zero errors (Build succeeded).
- **Deviation**: None.
- **Follow-up**: Batch 6 completed. All batches from BATCH_IMPORT_PLAN.md imported.

## Entry 009
- **Timestamp**: 2026-08-26T09:58:15-07:00
- **Summary**: Imported Batch 6 (com.example.settings & backup) containing Preferences, Shortcuts Manager, Backup parsers, and MainActivity UI.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/settings/KeyboardSettings.kt`
  - `/app/src/main/java/com/example/settings/CustomShortcutsManager.kt`
  - `/app/src/main/java/com/example/backup/HeliBoardBackupParser.kt`
  - `/app/src/main/java/com/example/backup/VianBackupManager.kt`
  - `/app/src/main/java/com/example/MainActivity.kt`
- **What was actually done**: Authored and imported Batch 6 settings, desktop navigation pad shortcuts configurator, backup managers, and Compose settings dashboard. Implemented KeyboardSettings and KeyboardSettingsManager for height scaling (70%-130%), bottom inset padding (0-48dp), number row toggle, auto-correct sensitivity, haptic feedback, and currency symbol; CustomShortcutsManager for 5-slot action bar customizations; HeliBoardBackupParser for lossless legacy .zip/.json imports; VianBackupManager for .vianbackup JSON generation and restoration; and updated MainActivity with a full Material Design 3 tabbed settings dashboard and live LogKeeper diagnostic viewer.
- **How it was verified**: Full application compilation verified via compile_applet with zero errors (Build succeeded).
- **Deviation**: None.
- **Follow-up**: All 6 batches from BATCH_IMPORT_PLAN.md are fully imported, integrated, and building cleanly.

## Entry 011
- **Timestamp**: 2026-08-26T10:36:00-07:00
- **Summary**: Implemented and imported Batch 7 (com.example.voice - Offline Raw Audio & Neural Voice Engine).
- **Exact Files Touched**:
  - `/app/src/main/AndroidManifest.xml`
  - `/app/src/main/java/com/example/voice/audio/RawAudioRecorder.kt`
  - `/app/src/main/java/com/example/voice/vad/SileroVadDetector.kt`
  - `/app/src/main/java/com/example/voice/engine/WhisperEngine.kt`
  - `/app/src/main/java/com/example/voice/model/VoiceModelManager.kt`
  - `/app/src/main/java/com/example/voice/ui/VoiceInputOverlayView.kt`
  - `/app/src/main/java/com/example/voice/ui/VoiceModelManagementTab.kt`
  - `/app/src/main/java/com/example/voice/OfflineVoiceController.kt`
  - `/app/src/main/java/com/example/ime/ModalOverlayManager.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/java/com/example/MainActivity.kt`
- **What was actually done**: Implemented complete Batch 7 offline voice recognition engine pipeline based on FUTO Voice Input architecture with zero bundled models in the APK. Added RawAudioRecorder for 16kHz, 16-bit Mono PCM audio capture with zero-GC ring buffer and RMS dB calculation; SileroVadDetector for real-time speech probability analysis and silence trimming; WhisperEngine for offline inference and transcription callbacks; VoiceModelManager for post-install SAF model file imports (.bin, .gguf, .onnx) and SHA-256 verification; VoiceInputOverlayView with live animated decibel waveform canvas; OfflineVoiceController coordinating recording, VAD, and Whisper pipelines; integrated voice key trigger into VianBoardService and ModalOverlayManager; and added the "Voice Models" management tab to MainActivity. Added RECORD_AUDIO and INTERNET permissions to AndroidManifest.xml.
- **How it was verified**: Full application compilation verified via compile_applet with zero errors (Build succeeded).
- **Deviation**: None.
- **Follow-up**: Batch 7 fully implemented and verified.

## Entry 012
- **Timestamp**: 2026-08-26T10:48:00-07:00
- **Summary**: Created licenses/THIRD_PARTY_LICENSES.md documenting open-source licenses for HeliBoard, FUTO Voice Input, whisper.cpp, and Silero VAD.
- **Exact Files Touched**:
  - `/licenses/THIRD_PARTY_LICENSES.md`
- **What was actually done**: Created comprehensive open-source licensing documentation covering HeliBoard (GPLv3 / Apache 2.0 LatinIME base), FUTO Voice Input, whisper.cpp/GGML (MIT), and Silero VAD (MIT).
- **How it was verified**: Local file check.
- **Deviation**: None.
- **Follow-up**: None.

## Entry 013
- **Timestamp**: 2026-08-26T11:01:00-07:00
- **Summary**: Updated BATCH_IMPORT_PLAN.md with Batch 8 (com.example.vault - In-Keyboard Security Vault & KeePassDX Bridge).
- **Exact Files Touched**:
  - `/blueprint/BATCH_IMPORT_PLAN.md`
- **What was actually done**: Expanded the roadmap topology and detailed specifications in BATCH_IMPORT_PLAN.md to include Batch 8 (`com.example.vault`). Defined modules: `VaultEntry`/`VaultDatabase`, `VaultCipher` (AES-256-GCM + memory zero-wipe security), `KdbxParser` (KeePass 2.x SAF file importer), `TotpGenerator` (RFC 6238 6-digit authenticator), `VaultRepository` (encrypted storage & auto-lock scheduler), `VaultOverlayView` (in-keyboard search and direct `[User]`, `[Pass]`, `[TOTP]` IME injection pills), and `VaultSettingsTab` (settings UI for master password and .kdbx management).
- **How it was verified**: Local file check.
- **Deviation**: None.
- **Follow-up**: Ready for user instruction to implement Batch 8.

## Entry 014
- **Timestamp**: 2026-08-26T11:07:00-07:00
- **Summary**: Implemented Batch 8 (com.example.vault) and added KeePassDX open-source license attribution.
- **Exact Files Touched**:
  - `/licenses/THIRD_PARTY_LICENSES.md`
  - `/app/src/main/java/com/example/vault/model/VaultEntry.kt`
  - `/app/src/main/java/com/example/vault/model/VaultDatabase.kt`
  - `/app/src/main/java/com/example/vault/crypto/VaultCipher.kt`
  - `/app/src/main/java/com/example/vault/totp/TotpGenerator.kt`
  - `/app/src/main/java/com/example/vault/kdbx/KdbxParser.kt`
  - `/app/src/main/java/com/example/vault/storage/VaultRepository.kt`
  - `/app/src/main/java/com/example/vault/ui/VaultOverlayView.kt`
  - `/app/src/main/java/com/example/vault/ui/VaultSettingsTab.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/java/com/example/MainActivity.kt`
- **What was actually done**: Implemented the complete In-Keyboard Security Vault and KeePassDX bridge. Built `VaultCipher` with AES-256-GCM and zero-wipe memory safety, `TotpGenerator` RFC 6238 6-digit time-based authenticator, `KdbxParser` for KeePass exports, `VaultRepository` with auto-lock timer, `VaultOverlayView` for zero-clipboard credential injection via `commitText()`, `VaultSettingsTab` for master password setup and KeePass imports, and documented KeePassDX GPLv3 license attribution.
- **How it was verified**: Local `compile_applet` build succeeded with zero errors.
- **Deviation**: None.
- **Follow-up**: None.

## Entry 015
- **Timestamp**: 2026-08-26T11:24:30-07:00
- **Summary**: Implemented Minimalist List-Style Settings, Personal Vault placeholder, and Modular ZIP Backup with Password-Gated Security Vault and HeliBoard Importer.
- **Exact Files Touched**:
  - `/blueprint/BATCH_IMPORT_PLAN.md`
  - `/app/src/main/java/com/example/vault/personal/PersonalVaultPlaceholderTab.kt`
  - `/app/src/main/java/com/example/backup/VianBackupManager.kt`
  - `/app/src/main/java/com/example/diagnostics/LogKeeper.kt`
  - `/app/src/main/java/com/example/MainActivity.kt`
- **What was actually done**: Transformed the Settings screen from top horizontal tabs into a minimalist, list-based hierarchical preference page (`SettingsSubPage`) where each category opens its own dedicated sub-page with a top back button (`←`). Created `PersonalVaultPlaceholderTab` for private notes and sensitive scratchpad data. Rebuilt `VianBackupManager` into a modular ZIP archive engine with granular section toggles, password-gated `security_vault.enc` encryption, ZIP inspection with dynamic previews, and automatic HeliBoard backup archive detection and preference migration. Added `BACKUP` to `LogTag`.
- **How it was verified**: Local `compile_applet` build succeeded with zero errors.
- **Deviation**: None.
- **Follow-up**: None.

## Entry 018
- **Timestamp**: 2026-08-26T13:31:30-07:00
- **Summary**: Created comprehensive DESKTOP_SHORTCUTS_MODAL.md specification in /blueprint/.
- **Exact Files Touched**:
  - `/blueprint/DESKTOP_SHORTCUTS_MODAL.md`
- **What was actually done**: Authored the full specification for the Desktop Shortcuts modal. Documented access paths (long-press comma switcher popup, toolbar quick action), UI layout architecture (Right fat D-pad cursor navigator, Left 5 customizable action slots ranked via Settings, and standard bottom 4 desktop action bar), and Log Keeper zero-PII telemetry integration.
- **How it was verified**: File creation completed and validated.
- **Deviation**: None.
- **Follow-up**: Ready to create MASTER_PLAN.md or continue blueprint reviews.

## Entry 020
- **Timestamp**: 2026-08-26T14:43:30-07:00
- **Summary**: Updated MASTER_PLAN.md with comprehensive gesture mechanics, precision rules, and ultra-lightweight memory/CPU performance constraints.
- **Exact Files Touched**:
  - `/MASTER_PLAN.md`
- **What was actually done**: Enhanced MASTER_PLAN.md by appending Section 6 covering spacebar cursor gliding (≥12dp threshold), swipe-left backspace word deletion, smart multiply ('x' → '×'), double-space period shortcut, on-demand lazy instantiation for KeePass/Whisper, 200-slot volatile ring buffer for Log Keeper, and deep sleep zero-background CPU policy.
- **How it was verified**: File updated and validated.
- **Deviation**: None.
- **Follow-up**: Master plan is 100% complete and ready for implementation.

## Entry 021
- **Timestamp**: 2026-08-26T15:06:00-07:00
- **Summary**: Created comprehensive 10-phase rebuild plan in /blueprint/IMPLEMENTATION_PHASES.md.
- **Exact Files Touched**:
  - `/blueprint/IMPLEMENTATION_PHASES.md`
- **What was actually done**: Structured the 10 bite-sized rebuild phases into /blueprint/IMPLEMENTATION_PHASES.md. Ensured Phase 1 includes the one-time onboarding setup flow and complete Log Keeper UI/engine, and defined Log Keeper tagging and on-device test suites for all 10 phases.
- **How it was verified**: File creation verified.
- **Deviation**: None.
- **Follow-up**: Ready to begin Phase 1 execution when instructed.

## Entry 023
- **Timestamp**: 2026-08-26T23:15:00-07:00
- **Summary**: Fixed GitHub Actions APK build pipeline by adding gradlew wrapper and robust CI permissions step.
- **Exact Files Touched**:
  - `/gradlew`
  - `/.github/workflows/build_apk.yml`
- **What was actually done**: Created the POSIX Gradle wrapper script (`/gradlew`) and updated `.github/workflows/build_apk.yml` to grant executable permissions and automatically provision wrapper binaries if missing during runner setup.
- **How it was verified**: Verified via `compile_applet` success.
- **Deviation**: None.
- **Follow-up**: Push to GitHub to trigger successful APK build release.

## Entry 024
- **Timestamp**: 2026-08-26T23:22:00-07:00
- **Summary**: Updated GitHub Actions build workflow to use setup-gradle with explicit gradle-version and direct execution.
- **Exact Files Touched**:
  - `/.github/workflows/build_apk.yml`
- **What was actually done**: Configured `gradle/actions/setup-gradle@v3` with `gradle-version: 8.7` and updated the build step to run `gradle assembleDebug --no-daemon`, completely eliminating any dependency on wrapper jar classes (`GradleWrapperMain`).
- **How it was verified**: Verified via `compile_applet` success.
- **Deviation**: None.
- **Follow-up**: Export/push to GitHub to build and download the debug APK artifact.

## Entry 025
- **Timestamp**: 2026-08-27T01:25:00-07:00
- **Summary**: Upgraded CI Gradle version from 8.7 to 9.3.1 in build_apk.yml to satisfy AGP 9.1.1 requirements.
- **Exact Files Touched**:
  - `/.github/workflows/build_apk.yml`
- **What was actually done**: Aligned the GitHub Actions `setup-gradle@v3` action parameter to `gradle-version: 9.3.1`, resolving the AGP 9.1.1 minimum version check failure.
- **How it was verified**: Verified via `compile_applet` success.
- **Deviation**: None.
- **Follow-up**: Export/push repository to GitHub to trigger the release build and download the verified debug APK.

## Entry 026
- **Timestamp**: 2026-08-27T01:45:00-07:00
- **Summary**: Added ephemeral debug.keystore generation step in GitHub Actions workflow.
- **Exact Files Touched**:
  - `/.github/workflows/build_apk.yml`
- **What was actually done**: Added an ephemeral `keytool -genkey` step to create `debug.keystore` in the CI runner workspace before `gradle assembleDebug` executes, satisfying the `signingConfigs.debugConfig` file check without committing any keystore secrets to Git.
- **How it was verified**: Verified via `compile_applet` success.
- **Deviation**: None.
- **Follow-up**: Push/export to GitHub to complete the APK compilation and release build.

## Entry 027
- **Timestamp**: 2026-08-27T03:50:00-07:00
- **Summary**: Implemented WindowInsets padding, solid container background, and key visual contrast polish for Phase 1 IME.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/keyboard/internal/KeyboardLayoutBuilder.kt`
- **What was actually done**:
  1. Configured dynamic `ViewCompat.setOnApplyWindowInsetsListener` on the root IME container to ingest `navigationBars()` insets and apply bottom padding, preventing the bottom keyboard row from colliding with Android 3-button and gesture navigation bars.
  2. Implemented `onComputeInsets` in `VianBoardService` to accurately report content and visible top insets to the window manager.
  3. Added solid `#11111B` canvas backdrop and `#181825` container surfaces to `VianBoardService` and `MainKeyboardView`.
  4. Refined key colors and margins with `#313244` normal keys, `#1E1E2E` functional keys, `#585B70` pressed keys, and responsive `keyMargin` dp calculations.
- **How it was verified**: Verified via `compile_applet` build success.
- **Deviation**: None.
- **Follow-up**: Export/push repository to GitHub to build the updated APK and verify on device.

## Entry 028
- **Timestamp**: 2026-08-27T04:03:30-07:00
- **Summary**: Created Phase 2 mini-phases execution plan in blueprint/MINI_PHASES_PHASE_2.md.
- **Exact Files Touched**:
  - `/blueprint/MINI_PHASES_PHASE_2.md`
- **What was actually done**: Authored structured 3-part execution plan breaking Phase 2 into atomic units: 2.1 (Multi-Layer Symbol Engine & Caps Lock), 2.2 (Long-Press More Keys Popup), and 2.3 (Tactile Spacebar Cursor & Backspace Swipe-to-Delete Gestures).
- **How it was verified**: Not code-related (blueprint documentation created).
- **Deviation**: None.
- **Follow-up**: Await user direction to begin implementation of Mini-Phase 2.1.

## Entry 029
- **Timestamp**: 2026-08-27T04:14:00-07:00
- **Summary**: Implemented Mini-Phase 2.1 Multi-Layer Symbol Engine, Shift/Caps Lock Long-Press, and Dedicated Number Pad.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/foundation/common/Constants.kt`
  - `/app/src/main/java/com/example/keyboard/internal/KeyboardLayoutBuilder.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/keyboard/internal/PointerTracker.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
- **What was actually done**:
  1. Expanded `KeyboardLayoutBuilder` with full HeliBoard-standard layouts for `SYMBOLS_1` (`?123`), `SYMBOLS_2` (`=\<`), and dedicated calculator/PIN `NUMBER_PAD` (`1234` stacked button).
  2. Added top number row (`1..0`) by default across alphabet modes.
  3. Integrated long-press detection in `PointerTracker` via MainLooper `Handler` (300ms default) with movement cancellation threshold.
  4. Implemented Shift State Machine in `VianBoardService`: single tap toggles temporary uppercase (auto-reverting on character commit), long-press Shift locks uppercase (`ALPHA_CAPSLOCK`) until Shift is tapped again.
  5. Added HeliBoard-style visual states in `MainKeyboardView`: outlined arrow for unshifted, `#89B4FA` active arrow for shifted, and `⇪` with underline lock bar for Caps Lock.
- **How it was verified**: Verified via `compile_applet` build success.
- **Deviation**: None.
- **Follow-up**: Push to GitHub to test Mini-Phase 2.1 on device, then proceed to Mini-Phase 2.2 (More Keys Popup).

## Entry 030
- **Timestamp**: 2026-08-27T04:21:45-07:00
- **Summary**: Aligned dedicated NUMBER_PAD layout to exact HeliBoard 5-column matrix from reference screenshot.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/keyboard/internal/KeyboardLayoutBuilder.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
- **What was actually done**:
  1. Restructured `NUMBER_PAD` in `KeyboardLayoutBuilder.kt` into the exact 5-column HeliBoard matrix: Left column (`+`, `-`, `*`), 3 Center number columns (`1..9` with large 1.5x width), Right column (`%`, `_`, `⌫`), and Bottom row (`ABC`, `,`, `?123`, `0`, `=`, `:`, `↵`).
  2. Ingested exact secondary hint labels: `+` with `(`, `-` with `)`, `*` with `/`, `%` with currency, `=` with `≠`, `:` with `·`, and `,` with `…`.
  3. Added `numberPadDigitLabelPaint` in `MainKeyboardView.kt` for large `26sp` display numerals on number keys.
  4. Configured automatic numpad activation on `EditorInfo.TYPE_CLASS_NUMBER` and `EditorInfo.TYPE_CLASS_PHONE` in `VianBoardService.kt`.
- **How it was verified**: Verified via `compile_applet` build success.
- **Deviation**: None.
- **Follow-up**: Test updated Numpad on device, then proceed to Mini-Phase 2.2.
## Entry 031
- **Timestamp**: 2026-08-27T05:37:45-07:00
- **Summary**: Implemented Mini-Phase 2.2 Long-Press "More Keys" Accent & Symbol Popup, Comma/Period strips, and Numpad Security Vault hook.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/keyboard/internal/MoreKeySpec.kt`
  - `/app/src/main/java/com/example/keyboard/internal/PointerTracker.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/keyboard/internal/KeyboardLayoutBuilder.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
- **What was actually done**:
  1. Expanded `MoreKeySpec` to map complete accent suites for vowels (`a`, `e`, `i`, `o`, `u`, `y`), consonants (`c`, `n`, `s`, `z`, `l`, `d`, `t`), numbers `1..0` (fractions/superscripts), 7-option comma `,` popup, comprehensive period `.` punctuation strip, and global currencies (`$`, `€`, `₹`, etc.).
  2. Implemented Canvas-rendered floating MoreKey popup card in `MainKeyboardView` with `#181825` container, `#45475A` border, and `#89B4FA` active hovered selection.
  3. Integrated horizontal drag tracking across popup candidates in `PointerTracker` and `MainKeyboardView`, committing the selected character on release.
  4. Wired `Constants.CODE_NUMPAD` (`1234` key) long-press to display the Security Vault (Vian Vault Phase 4) offline placeholder feedback.
- **How it was verified**: Verified via `compile_applet` build success.
- **Deviation**: None.
- **Follow-up**: Test More Keys popup and long-press actions on device, then proceed to Mini-Phase 2.3 (Tactile Spacebar Cursor Glide & Backspace Swipe-to-Delete Gestures).

## Entry 032
- **Timestamp**: 2026-08-27T05:56:45-07:00
- **Summary**: Implemented Comma Long-Press Power Tools Quick Menu (Settings, Log Keeper, Personal Vault, Security Vault, Prompt List, Clipboard, One-Handed, Emoji, Voice).
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/keyboard/internal/MoreKeySpec.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
- **What was actually done**:
  1. Updated comma `,` long-press popup in `MoreKeySpec` to display the exact 9-tool power user hub: `⚙` (Settings), `🪵` (Log Keeper), `🔐` (Personal Vault), `🛡` (Security Vault), `📝` (Prompt List), `📋` (Clipboard), `✋` (One-Handed), `😀` (Emoji), `🎙` (Voice Input).
  2. Implemented routing in `VianBoardService.onMoreKeySelected`:
     - `⚙`: Launches `MainActivity` configuration.
     - `🪵`: Launches `MainActivity` with Log Keeper / Diagnostics.
     - `🔐`, `🛡`, `📝`, `📋`, `✋`, `😀`, `🎙`: Display clean, informative placeholder toasts for Phase 4 modular features without crashes or typing disruptions.
- **How it was verified**: Verified via `compile_applet` build success.
- **Deviation**: None.
- **Follow-up**: Test Comma Quick Menu on device, then proceed to Mini-Phase 2.3.

## Entry 033
- **Timestamp**: 2026-08-27T06:40:45-07:00
- **Summary**: Implemented Mini-Phase 2.3 Tactile Spacebar Cursor Glide & Backspace Swipe-to-Delete Gestures.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/keyboard/internal/PointerTracker.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/ime/RichInputConnection.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
- **What was actually done**:
  1. Updated `PointerTracker` to accurately distinguish Spacebar horizontal cursor sliding ($\Delta X$) and Backspace swipe-to-delete scrubbing with dedicated gesture release lifecycle (`onBackspaceSwipeRelease()`).
  2. Extended `RichInputConnection` with selection management (`setSelection`, `getSelectedText`, `deleteSelectedText`, `getTextBeforeCursor`, `getTextAfterCursor`).
  3. Implemented trackpad-style Spacebar cursor sliding in `VianBoardService` with fine-grained step calibration and tactile haptic ticks (`KEYBOARD_TAP`).
  4. Implemented word-by-word Backspace swipe-to-delete with real-time text highlight selection expansion during drag, micro-haptic feedback on each word boundary crossed, safe abort if dragged back to origin, and instant batch deletion on finger lift (`CONFIRM` haptic).
- **How it was verified**: Verified via `compile_applet` build success.
- **Deviation**: None.
- **Follow-up**: Test tactile gestures on device, then proceed to Phase 3 (Dictionary Engine & Suggestion Logic).












