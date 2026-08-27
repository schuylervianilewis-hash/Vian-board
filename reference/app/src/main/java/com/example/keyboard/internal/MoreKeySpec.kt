package com.example.keyboard.internal

import com.example.foundation.common.UnicodeUtils

/**
 * Parses and generates long-press popup key specifications for accents, symbols, and currencies.
 */
object MoreKeySpec {

    /**
     * Returns popup keys for a specific key code / label.
     */
    fun getMoreKeysFor(label: String, code: Int): List<String> {
        if (label.length == 1) {
            val char = label[0]
            val extended = UnicodeUtils.getExtendedAccentsForChar(char)
            if (extended.isNotEmpty()) return extended
        }

        return when (label) {
            "?" -> listOf("!", "¿", "‽")
            "!" -> listOf("?", "¡")
            "-" -> listOf("–", "—", "_")
            "/" -> listOf("\\", "|")
            "\"" -> listOf("“", "”", "«", "»")
            "'" -> listOf("‘", "’", "`")
            "%" -> listOf("‰", "℅")
            "=" -> listOf("≠", "≈", "≡", "≤", "≥")
            else -> emptyList()
        }
    }
}
