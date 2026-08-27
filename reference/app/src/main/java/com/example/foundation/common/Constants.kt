package com.example.foundation.common

/**
 * Global Constants for Vian Board layout, key codes, gestures, and timing.
 */
object Constants {

    // Special Key Codes (Negative integers aligned with HeliBoard / AOSP standard)
    const val CODE_UNSPECIFIED = -1
    const val CODE_SHIFT = -2
    const val CODE_SWITCH_ALPHA_SYMBOL = -3
    const val CODE_CAPSLOCK = -4
    const val CODE_DELETE = -5
    const val CODE_SETTINGS = -6
    const val CODE_SPACE = 32
    const val CODE_ENTER = 10
    const val CODE_TAB = 9
    const val CODE_ESCAPE = 27
    const val CODE_VOICE = -102
    const val CODE_CLIPBOARD = -103
    const val CODE_PROMPT_LIST = -104
    const val CODE_DESKTOP_NAV = -105
    const val CODE_EMOJI = -106
    const val CODE_VAULT = -107
    const val CODE_SUBTYPE_SWITCHER = -108
    const val CODE_SELECT_WORD = -109
    const val CODE_SELECT_ALL = -110
    const val CODE_COPY = -111
    const val CODE_PASTE = -112
    const val CODE_CUT = -113
    const val CODE_UNDO = -114
    const val CODE_REDO = -115

    // Desktop Nav Key Codes
    const val CODE_ARROW_UP = -201
    const val CODE_ARROW_DOWN = -202
    const val CODE_ARROW_LEFT = -203
    const val CODE_ARROW_RIGHT = -204
    const val CODE_HOME = -205
    const val CODE_END = -206
    const val CODE_PAGE_UP = -207
    const val CODE_PAGE_DOWN = -208

    // Shift States
    const val SHIFT_OFF = 0
    const val SHIFT_ON = 1
    const val SHIFT_LOCKED = 2

    // Gesture & Timing Defaults
    const val DEFAULT_LONG_PRESS_TIMEOUT_MS = 300L
    const val DEFAULT_DOUBLE_TAP_TIMEOUT_MS = 300L
    const val DEFAULT_KEYBOARD_HEIGHT_SCALE = 1.0f
    const val DEFAULT_BOTTOM_INSET_PADDING_DP = 0
    const val DEFAULT_KEY_CORNER_RADIUS_DP = 8

    const val MIN_KEYBOARD_HEIGHT_SCALE = 0.70f
    const val MAX_KEYBOARD_HEIGHT_SCALE = 1.30f
    const val MAX_BOTTOM_INSET_PADDING_DP = 48

    const val SPACEBAR_CURSOR_SLIDE_THRESHOLD_DP = 12f
    const val BACKSPACE_SWIPE_WORD_THRESHOLD_DP = 24f

    // Vault Timings
    const val VAULT_ACTIVE_SESSION_TIMEOUT_MS = 120_000L // 2 Minutes
    const val INLINE_CLIP_PASTE_VALIDITY_MS = 60_000L    // 60 Seconds
}
