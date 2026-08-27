package com.example.foundation.utils

import com.example.foundation.common.LocaleUtils
import java.util.Locale

/**
 * Subtype and locale display formatting utilities for the spacebar label and language switchers.
 */
object SubtypeLocaleUtils {

    /**
     * Formats the spacebar label for a primary subtype and optional secondary language.
     * e.g. "English (US)" or "English / Español"
     */
    fun getSubtypeNameForSpacebar(primaryLocale: Locale, secondaryLocale: Locale? = null): String {
        val primaryName = primaryLocale.getDisplayLanguage(primaryLocale).replaceFirstChar { it.uppercase(primaryLocale) }
        if (secondaryLocale == null || LocaleUtils.isSameLanguage(primaryLocale, secondaryLocale)) {
            return primaryName
        }
        val secondaryName = secondaryLocale.getDisplayLanguage(secondaryLocale).replaceFirstChar { it.uppercase(secondaryLocale) }
        return "$primaryName / $secondaryName"
    }

    /**
     * Returns full localized display name for language picker menus.
     */
    fun getFullDisplayName(locale: Locale): String {
        val language = locale.getDisplayLanguage(Locale.getDefault()).replaceFirstChar { it.uppercase(Locale.getDefault()) }
        val country = locale.getDisplayCountry(Locale.getDefault())
        return if (country.isNotEmpty()) "$language ($country)" else language
    }
}
