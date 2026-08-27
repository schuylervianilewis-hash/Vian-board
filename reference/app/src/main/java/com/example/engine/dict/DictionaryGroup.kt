package com.example.engine.dict

import java.util.Locale

/**
 * Composite group of dictionaries belonging to a single language or subtype.
 * Combines Main Binary Dict, User Words, and Shortcuts.
 */
class DictionaryGroup(
    override val locale: Locale,
    private val dictionaries: MutableList<Dictionary> = mutableListOf()
) : Dictionary {

    override val dictType: String = TYPE_GROUP

    override val isInitialized: Boolean
        get() = dictionaries.any { it.isInitialized }

    fun addDictionary(dictionary: Dictionary) {
        dictionaries.add(dictionary)
    }

    override fun isValidWord(word: String): Boolean {
        return dictionaries.any { it.isInitialized && it.isValidWord(word) }
    }

    override fun getFrequency(word: String): Int {
        var maxFreq = -1
        for (dict in dictionaries) {
            if (dict.isInitialized) {
                val freq = dict.getFrequency(word)
                if (freq > maxFreq) {
                    maxFreq = freq
                }
            }
        }
        return maxFreq
    }

    override fun getSuggestions(composedWord: String, maxSuggestions: Int): List<DictionarySuggestion> {
        val combined = mutableListOf<DictionarySuggestion>()
        for (dict in dictionaries) {
            if (dict.isInitialized) {
                combined.addAll(dict.getSuggestions(composedWord, maxSuggestions))
            }
        }
        return combined.sortedByDescending { it.score }.take(maxSuggestions)
    }

    override fun close() {
        dictionaries.forEach { it.close() }
        dictionaries.clear()
    }

    companion object {
        const val TYPE_GROUP = "dictionary_group"
    }
}
