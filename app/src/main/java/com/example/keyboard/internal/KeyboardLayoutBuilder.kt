package com.example.keyboard.internal

import android.graphics.RectF
import com.example.foundation.common.Constants

/**
 * Layout builder that defines and arranges rows of keys for QWERTY, Symbols, and Numbers.
 */
class KeyboardLayoutBuilder {

    enum class LayoutMode {
        ALPHA_LOWER,
        ALPHA_UPPER,
        ALPHA_CAPSLOCK,
        SYMBOLS_1,
        SYMBOLS_2
    }

    /**
     * Builds key list with computed RectF bounds based on canvas width, height, and layout mode.
     */
    fun buildKeyboard(
        width: Float,
        height: Float,
        mode: LayoutMode,
        showNumberRow: Boolean = true,
        currencySymbol: String = "$"
    ): List<Key> {
        val keys = mutableListOf<Key>()
        if (width <= 0 || height <= 0) return keys

        val rows = getRowSpecs(mode, showNumberRow, currencySymbol)
        val rowCount = rows.size
        val rowHeight = height / rowCount
        val keyMargin = 3f

        for (rowIndex in 0 until rowCount) {
            val rowSpec = rows[rowIndex]
            val keyCount = rowSpec.size
            val top = rowIndex * rowHeight

            // Calculate total relative weight
            val totalWeight = rowSpec.sumOf { it.weight.toDouble() }.toFloat()
            var currentX = 0f

            for (spec in rowSpec) {
                val keyWidth = (spec.weight / totalWeight) * width
                val bounds = RectF(
                    currentX + keyMargin,
                    top + keyMargin,
                    currentX + keyWidth - keyMargin,
                    top + rowHeight - keyMargin
                )

                val moreKeys = MoreKeySpec.getMoreKeysFor(spec.label, spec.code)
                keys.add(
                    Key(
                        code = spec.code,
                        label = spec.label,
                        hintLabel = spec.hintLabel,
                        bounds = bounds,
                        isFunctional = spec.isFunctional,
                        moreKeys = moreKeys
                    )
                )
                currentX += keyWidth
            }
        }

        return keys
    }

    data class KeySpec(
        val code: Int,
        val label: String,
        val hintLabel: String? = null,
        val weight: Float = 1.0f,
        val isFunctional: Boolean = false
    )

    private fun getRowSpecs(
        mode: LayoutMode,
        showNumberRow: Boolean,
        currencySymbol: String
    ): List<List<KeySpec>> {
        val rows = mutableListOf<List<KeySpec>>()

        if (mode == LayoutMode.ALPHA_LOWER || mode == LayoutMode.ALPHA_UPPER || mode == LayoutMode.ALPHA_CAPSLOCK) {
            val isUpper = mode == LayoutMode.ALPHA_UPPER || mode == LayoutMode.ALPHA_CAPSLOCK

            // Row 0 (Optional Number Row)
            if (showNumberRow) {
                val numLabels = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                rows.add(numLabels.map { KeySpec(it[0].code, it, weight = 1.0f) })
            }

            // Row 1 (Q-P)
            val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
            val r1Hints = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            rows.add(r1.mapIndexed { idx, s ->
                val lbl = if (isUpper) s.uppercase() else s
                KeySpec(lbl[0].code, lbl, hintLabel = if (!showNumberRow) r1Hints[idx] else null, weight = 1.0f)
            })

            // Row 2 (A-L)
            val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
            rows.add(r2.map { s ->
                val lbl = if (isUpper) s.uppercase() else s
                KeySpec(lbl[0].code, lbl, weight = 1.0f)
            })

            // Row 3 (Shift, Z-M, Delete)
            val r3Keys = mutableListOf<KeySpec>()
            r3Keys.add(KeySpec(Constants.CODE_SHIFT, "⇧", weight = 1.5f, isFunctional = true))
            val r3Letters = listOf("z", "x", "c", "v", "b", "n", "m")
            r3Letters.forEach { s ->
                val lbl = if (isUpper) s.uppercase() else s
                r3Keys.add(KeySpec(lbl[0].code, lbl, weight = 1.0f))
            }
            r3Keys.add(KeySpec(Constants.CODE_DELETE, "⌫", weight = 1.5f, isFunctional = true))
            rows.add(r3Keys)

            // Row 4 (Bottom Bar: ?123, Comma, Space, Period, Enter)
            val r4Keys = listOf(
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "?123", weight = 1.4f, isFunctional = true),
                KeySpec(','.code, ",", weight = 1.0f),
                KeySpec(Constants.CODE_SPACE, "English", weight = 4.6f),
                KeySpec('.'.code, ".", weight = 1.0f),
                KeySpec(Constants.CODE_ENTER, "↵", weight = 1.4f, isFunctional = true)
            )
            rows.add(r4Keys)
        } else if (mode == LayoutMode.SYMBOLS_1) {
            // Symbols 1 Layout
            val r1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            rows.add(r1.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r2 = listOf("@", "#", currencySymbol, "_", "&", "-", "+", "(", ")", "/")
            rows.add(r2.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r3Keys = mutableListOf<KeySpec>()
            r3Keys.add(KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "=\\<", weight = 1.5f, isFunctional = true))
            val r3Syms = listOf("*", "\"", "'", ":", ";", "!", "?")
            r3Syms.forEach { r3Keys.add(KeySpec(it[0].code, it, weight = 1.0f)) }
            r3Keys.add(KeySpec(Constants.CODE_DELETE, "⌫", weight = 1.5f, isFunctional = true))
            rows.add(r3Keys)

            val r4Keys = listOf(
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "ABC", weight = 1.4f, isFunctional = true),
                KeySpec(','.code, ",", weight = 1.0f),
                KeySpec(Constants.CODE_SPACE, "", weight = 4.6f),
                KeySpec('.'.code, ".", weight = 1.0f),
                KeySpec(Constants.CODE_ENTER, "↵", weight = 1.4f, isFunctional = true)
            )
            rows.add(r4Keys)
        } else {
            // Symbols 2 Layout
            val r1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
            rows.add(r1.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r2 = listOf("£", "¥", "€", "¢", "^", "°", "=", "{", "}", "\\")
            rows.add(r2.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r3Keys = mutableListOf<KeySpec>()
            r3Keys.add(KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "?123", weight = 1.5f, isFunctional = true))
            val r3Syms = listOf("%", "©", "®", "™", "✓", "[", "]")
            r3Syms.forEach { r3Keys.add(KeySpec(it[0].code, it, weight = 1.0f)) }
            r3Keys.add(KeySpec(Constants.CODE_DELETE, "⌫", weight = 1.5f, isFunctional = true))
            rows.add(r3Keys)

            val r4Keys = listOf(
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "ABC", weight = 1.4f, isFunctional = true),
                KeySpec('<'.code, "<", weight = 1.0f),
                KeySpec(Constants.CODE_SPACE, "", weight = 4.6f),
                KeySpec('>'.code, ">", weight = 1.0f),
                KeySpec(Constants.CODE_ENTER, "↵", weight = 1.4f, isFunctional = true)
            )
            rows.add(r4Keys)
        }

        return rows
    }
}
