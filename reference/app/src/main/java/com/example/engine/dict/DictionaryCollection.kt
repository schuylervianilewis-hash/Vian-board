package com.example.engine.dict

import java.util.Locale

/**
 * Top-level container managing multiple language dictionary groups (e.g. Primary + Secondary Subtypes).
 */
class DictionaryCollection {

    private val groups = mutableMapOf<String, DictionaryGroup>()

    fun getOrCreateGroup(locale: Locale): DictionaryGroup {
        val key = locale.language
        return groups.getOrPut(key) {
            DictionaryGroup(locale)
        }
    }

    fun getGroup(locale: Locale): DictionaryGroup? {
        return groups[locale.language]
    }

    fun clear() {
        groups.values.forEach { it.close() }
        groups.clear()
    }
}
