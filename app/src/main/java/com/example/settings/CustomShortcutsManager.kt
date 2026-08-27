package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.foundation.common.Constants

/**
 * Data class representing a customizable desktop navigation pad shortcut item.
 */
data class ShortcutItem(
    val id: String,
    val label: String,
    val code: Int
)

/**
 * Manages the 5 customizable action slots on the Desktop Navigation Pad and Suggestion Strip.
 */
class CustomShortcutsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_SHORTCUTS, Context.MODE_PRIVATE)

    fun loadShortcuts(): List<ShortcutItem> {
        val raw = prefs.getString(KEY_SHORTCUTS_SLOTS, null)
        if (raw.isNullOrEmpty()) {
            return DEFAULT_SHORTCUTS
        }
        return try {
            val ids = raw.split(",")
            ids.mapNotNull { id -> AVAILABLE_SHORTCUTS.firstOrNull { it.id == id.trim() } }
                .ifEmpty { DEFAULT_SHORTCUTS }
        } catch (e: Exception) {
            DEFAULT_SHORTCUTS
        }
    }

    fun saveShortcuts(shortcuts: List<ShortcutItem>) {
        val raw = shortcuts.take(5).joinToString(",") { it.id }
        prefs.edit().putString(KEY_SHORTCUTS_SLOTS, raw).apply()
    }

    companion object {
        private const val PREFS_SHORTCUTS = "vian_board_shortcuts"
        private const val KEY_SHORTCUTS_SLOTS = "configured_slots"

        val ITEM_SELECT_ALL = ShortcutItem("select_all", "Select All", Constants.CODE_SELECT_ALL)
        val ITEM_COPY = ShortcutItem("copy", "Copy", Constants.CODE_COPY)
        val ITEM_PASTE = ShortcutItem("paste", "Paste", Constants.CODE_PASTE)
        val ITEM_CUT = ShortcutItem("cut", "Cut", Constants.CODE_CUT)
        val ITEM_UNDO = ShortcutItem("undo", "Undo", Constants.CODE_UNDO)
        val ITEM_REDO = ShortcutItem("redo", "Redo", Constants.CODE_REDO)
        val ITEM_HOME = ShortcutItem("home", "Home", Constants.CODE_HOME)
        val ITEM_END = ShortcutItem("end", "End", Constants.CODE_END)
        val ITEM_PAGE_UP = ShortcutItem("page_up", "PgUp", Constants.CODE_PAGE_UP)
        val ITEM_PAGE_DOWN = ShortcutItem("page_down", "PgDn", Constants.CODE_PAGE_DOWN)

        val AVAILABLE_SHORTCUTS = listOf(
            ITEM_SELECT_ALL,
            ITEM_COPY,
            ITEM_PASTE,
            ITEM_CUT,
            ITEM_UNDO,
            ITEM_REDO,
            ITEM_HOME,
            ITEM_END,
            ITEM_PAGE_UP,
            ITEM_PAGE_DOWN
        )

        val DEFAULT_SHORTCUTS = listOf(
            ITEM_SELECT_ALL,
            ITEM_COPY,
            ITEM_PASTE,
            ITEM_HOME,
            ITEM_END
        )
    }
}
