package com.example.engine.core

/**
 * Represents a single suggested word candidate with its scoring metrics and source origin.
 */
data class SuggestedWordInfo(
    val word: String,
    val score: Int,
    val kind: Kind,
    val sourceDict: String,
    val isAutoCorrect: Boolean = false,
    val isExactMatch: Boolean = false
) {
    enum class Kind {
        TYPED,          // Raw literal string as typed by the user
        CORRECTION,     // Auto-correct candidate
        PREDICTION,     // Next-word / prefix completion
        SHORTCUT,       // Personal dictionary abbreviation expansion
        MASKED_VAULT    // Sensitive password / secret shortcut
    }

    val isTypedWord: Boolean get() = kind == Kind.TYPED

    companion object {
        fun createRawTyped(word: String): SuggestedWordInfo {
            return SuggestedWordInfo(
                word = word,
                score = 0,
                kind = Kind.TYPED,
                sourceDict = "raw",
                isExactMatch = true
            )
        }
    }
}
