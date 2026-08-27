package com.example.engine.core

import com.example.engine.dict.Dictionary
import com.example.engine.dict.DictionarySuggestion
import java.util.Locale

/**
 * Suggestion scoring and auto-correct decision engine.
 * Ranks raw typed words, dictionary matches, number proximity candidates, and shortcuts.
 */
class Suggest(
    private var dictionary: Dictionary? = null
) {
    var autoCorrectSensitivity: Sensitivity = Sensitivity.MODEST

    enum class Sensitivity(val scoreThreshold: Int) {
        OFF(Int.MAX_VALUE),
        MODEST(160),
        AGGRESSIVE(110),
        VERY_AGGRESSIVE(70)
    }

    fun setDictionary(dict: Dictionary) {
        this.dictionary = dict
    }

    /**
     * Evaluates the composed word, queries dictionaries and number proximity variants,
     * and produces the final SuggestedWords object for the suggestion bar.
     */
    fun getSuggestedWords(
        wordComposer: WordComposer,
        locale: Locale,
        enableNumberProximity: Boolean = true
    ): SuggestedWords {
        val rawWord = wordComposer.getComposedWord()
        if (rawWord.isEmpty()) {
            return SuggestedWords.EMPTY
        }

        val suggestions = mutableListOf<SuggestedWordInfo>()

        // 1. Raw typed word candidate (Left slot)
        val rawExactFreq = dictionary?.getFrequency(rawWord) ?: -1
        val isRawValid = rawExactFreq > 0
        suggestions.add(
            SuggestedWordInfo(
                word = rawWord,
                score = if (isRawValid) rawExactFreq + 10 else 0,
                kind = SuggestedWordInfo.Kind.TYPED,
                sourceDict = "raw",
                isExactMatch = isRawValid
            )
        )

        // 2. Query Dictionary for Direct & Proximity Candidates
        val searchCandidates = if (enableNumberProximity) {
            wordComposer.getProximityWordCandidates()
        } else {
            listOf(rawWord)
        }

        var bestMatch: DictionarySuggestion? = null
        var bestScore = -1

        for (candidate in searchCandidates) {
            val dictResults = dictionary?.getSuggestions(candidate, 5) ?: emptyList()
            for (res in dictResults) {
                // Apply penalty for number proximity replacements
                val adjustedScore = if (candidate != rawWord) res.score - 30 else res.score
                if (adjustedScore > bestScore && res.word.lowercase() != rawWord.lowercase()) {
                    bestScore = adjustedScore
                    bestMatch = res.copy(score = adjustedScore)
                }
            }
        }

        // 3. Add Best Auto-Correct Candidate (Center slot)
        var willAutoCorrect = false
        if (bestMatch != null && autoCorrectSensitivity != Sensitivity.OFF) {
            val threshold = autoCorrectSensitivity.scoreThreshold
            val isStrongCandidate = bestMatch.score >= threshold
            if (isStrongCandidate && !isRawValid) {
                willAutoCorrect = true
            }

            suggestions.add(
                SuggestedWordInfo(
                    word = bestMatch.word,
                    score = bestMatch.score,
                    kind = SuggestedWordInfo.Kind.CORRECTION,
                    sourceDict = bestMatch.sourceDictType,
                    isAutoCorrect = willAutoCorrect
                )
            )
        }

        return SuggestedWords.fromList(
            list = suggestions.take(3),
            typedWord = rawWord,
            willAutoCorrect = willAutoCorrect
        )
    }
}
