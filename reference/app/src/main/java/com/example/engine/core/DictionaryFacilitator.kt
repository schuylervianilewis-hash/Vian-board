package com.example.engine.core

import com.example.engine.dict.Dictionary
import com.example.engine.dict.DictionaryCollection
import com.example.engine.dict.DictionaryGroup
import com.example.foundation.utils.Subtype
import java.util.Locale

/**
 * Facilitates access to primary and secondary language dictionaries,
 * manages active subtype changes, and routes suggestion scoring.
 */
class DictionaryFacilitator {

    private val dictionaryCollection = DictionaryCollection()
    private var primaryGroup: DictionaryGroup? = null
    private var secondaryGroup: DictionaryGroup? = null

    val suggest: Suggest = Suggest()

    var activeSubtype: Subtype = Subtype.DEFAULT
        private set

    fun resetSubtype(subtype: Subtype) {
        this.activeSubtype = subtype
        val primaryLocale = subtype.primaryLocale
        primaryGroup = dictionaryCollection.getOrCreateGroup(primaryLocale)

        val secondaryLocale = subtype.secondaryLocale
        secondaryGroup = if (secondaryLocale != null) {
            dictionaryCollection.getOrCreateGroup(secondaryLocale)
        } else {
            null
        }

        // Set primary dictionary group into suggest engine
        primaryGroup?.let { suggest.setDictionary(it) }
    }

    fun addDictionaryToPrimary(dictionary: Dictionary) {
        primaryGroup?.addDictionary(dictionary)
    }

    fun getSuggestedWords(
        wordComposer: WordComposer,
        enableNumberProximity: Boolean = true
    ): SuggestedWords {
        return suggest.getSuggestedWords(
            wordComposer = wordComposer,
            locale = activeSubtype.primaryLocale,
            enableNumberProximity = enableNumberProximity
        )
    }

    fun close() {
        dictionaryCollection.clear()
        primaryGroup = null
        secondaryGroup = null
    }
}
