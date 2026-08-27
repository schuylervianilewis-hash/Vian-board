package com.example.engine.dict

/**
 * Encapsulates word probability, bigram weight, and unigram frequency scores.
 */
data class Probability(
    val frequency: Int,
    val isShortcut: Boolean = false,
    val isBlacklisted: Boolean = false
) {
    val normalizedScore: Float
        get() = (frequency.coerceIn(0, 255) / 255f)

    companion object {
        val ZERO = Probability(0)
        val MAX = Probability(255)

        fun fromByte(b: Int): Probability {
            return Probability(b and 0xFF)
        }
    }
}
