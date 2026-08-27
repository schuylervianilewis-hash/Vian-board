package com.example.engine.core

/**
 * Immutable container holding the ranked list of suggested words for the top suggestion strip.
 * Typically presents: [Left: Raw/Alternative] [Center: Bold Auto-Correct] [Right: Next-Word/Prediction]
 */
class SuggestedWords(
    private val suggestions: List<SuggestedWordInfo>,
    val typedWord: String,
    val hasAutoCorrectionCandidate: Boolean,
    val willAutoCorrect: Boolean,
    val isPunctuationSuggestions: Boolean = false
) {
    val size: Int get() = suggestions.size

    val isEmpty: Boolean get() = suggestions.isEmpty()

    operator fun get(index: Int): SuggestedWordInfo {
        return suggestions[index]
    }

    fun getAll(): List<SuggestedWordInfo> = suggestions

    /**
     * Gets the designated center candidate for auto-correction upon Space/Punctuation tap.
     */
    fun getAutoCorrectionCandidate(): SuggestedWordInfo? {
        return if (willAutoCorrect) suggestions.firstOrNull { it.isAutoCorrect } else null
    }

    companion object {
        val EMPTY = SuggestedWords(
            suggestions = emptyList(),
            typedWord = "",
            hasAutoCorrectionCandidate = false,
            willAutoCorrect = false
        )

        fun fromList(
            list: List<SuggestedWordInfo>,
            typedWord: String,
            willAutoCorrect: Boolean
        ): SuggestedWords {
            return SuggestedWords(
                suggestions = list,
                typedWord = typedWord,
                hasAutoCorrectionCandidate = list.any { it.isAutoCorrect },
                willAutoCorrect = willAutoCorrect
            )
        }
    }
}
