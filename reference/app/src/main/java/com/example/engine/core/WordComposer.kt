package com.example.engine.core

import com.example.foundation.common.Constants

/**
 * Tracks the characters, touch coordinates, and proximity candidates of the currently composing word.
 *
 * Implements the **Continuous Composition & Number-Adjacent Typo Proximity Engine**
 * to ensure that accidental numbers (e.g. typing 'hell8' instead of 'hello') do NOT break
 * word composition or clear suggestions.
 */
class WordComposer {

    private val codePoints = mutableListOf<Int>()
    private val touchCoordinates = mutableListOf<TouchPoint>()
    private var isCapitalized = false

    data class TouchPoint(val x: Float, val y: Float)

    val size: Int get() = codePoints.size

    val isEmpty: Boolean get() = codePoints.isEmpty()

    fun add(codePoint: Int, x: Float = 0f, y: Float = 0f) {
        codePoints.add(codePoint)
        touchCoordinates.add(TouchPoint(x, y))
    }

    fun deleteLast(): Int? {
        if (codePoints.isEmpty()) return null
        touchCoordinates.removeAt(touchCoordinates.size - 1)
        return codePoints.removeAt(codePoints.size - 1)
    }

    fun reset() {
        codePoints.clear()
        touchCoordinates.clear()
        isCapitalized = false
    }

    fun getComposedWord(): String {
        val sb = StringBuilder(codePoints.size)
        for (cp in codePoints) {
            sb.append(cp.toChar())
        }
        return sb.toString()
    }

    fun getCodePointAt(index: Int): Int {
        return codePoints[index]
    }

    fun setCapitalized(capitalized: Boolean) {
        isCapitalized = capitalized
    }

    fun isCapitalized(): Boolean = isCapitalized

    /**
     * Number-Aware Typo Proximity Engine:
     * When an accidental digit is typed (e.g. '8' adjacent to 'i'/'o', '3' adjacent to 'e'/'r'),
     * generates letter replacement permutations so the dictionary trie can score the intended word.
     */
    fun getProximityWordCandidates(): List<String> {
        val raw = getComposedWord()
        if (raw.isEmpty()) return emptyList()

        val results = mutableListOf<String>()
        results.add(raw)

        // Check if composed word has digits
        val hasDigits = raw.any { it.isDigit() }
        if (!hasDigits) return results

        // Generate letter replacement candidates for adjacent number keys
        var candidates = listOf("")
        for (char in raw) {
            val adjacentLetters = getAdjacentLettersForChar(char)
            val nextList = mutableListOf<String>()
            for (prefix in candidates) {
                for (replacement in adjacentLetters) {
                    nextList.add(prefix + replacement)
                }
            }
            candidates = nextList.take(16) // Prevent exponential explosion
        }

        results.addAll(candidates)
        return results.distinct()
    }

    companion object {
        /**
         * Spatial key adjacency map for top-row numbers (1-0) to letters.
         */
        fun getAdjacentLettersForChar(char: Char): List<Char> {
            return when (char) {
                '1' -> listOf('1', 'q', 'w')
                '2' -> listOf('2', 'w', 'e')
                '3' -> listOf('3', 'e', 'r')
                '4' -> listOf('4', 'r', 't')
                '5' -> listOf('5', 't', 'y')
                '6' -> listOf('6', 'y', 'u')
                '7' -> listOf('7', 'u', 'i')
                '8' -> listOf('8', 'i', 'o')
                '9' -> listOf('9', 'o', 'p')
                '0' -> listOf('0', 'p')
                else -> listOf(char)
            }
        }
    }
}
