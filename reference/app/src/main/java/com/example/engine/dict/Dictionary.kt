package com.example.engine.dict

import java.util.Locale

/**
 * Common interface for all Vian Board dictionary sources (Binary, User, Contacts, Shortcuts).
 */
interface Dictionary {

    val dictType: String
    val locale: Locale
    val isInitialized: Boolean

    /**
     * Checks if a word exists in the dictionary.
     */
    fun isValidWord(word: String): Boolean

    /**
     * Returns the frequency score for a word (0-255) or -1 if not found.
     */
    fun getFrequency(word: String): Int

    /**
     * Generates word suggestions for a given input prefix or typo candidate.
     */
    fun getSuggestions(
        composedWord: String,
        maxSuggestions: Int = 10
    ): List<DictionarySuggestion>

    /**
     * Closes or unmaps resources.
     */
    fun close()
}

/**
 * Data class representing a suggestion candidate emitted by a dictionary.
 */
data class DictionarySuggestion(
    val word: String,
    val score: Int,
    val sourceDictType: String,
    val isExactMatch: Boolean = false,
    val isAutoCorrectCandidate: Boolean = false
)
