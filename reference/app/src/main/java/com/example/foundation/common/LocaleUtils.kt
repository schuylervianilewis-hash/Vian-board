package com.example.foundation.common

import java.util.Locale

/**
 * Utility functions for parsing, canonicalizing, and comparing locales and BCP-47 language tags.
 */
object LocaleUtils {

    val DEFAULT_LOCALE: Locale = Locale.US

    /**
     * Parses a string locale tag (e.g. "en_US", "en-US", "hi_IN", "es") into a java.util.Locale.
     */
    fun constructLocaleFromString(localeString: String?): Locale {
        if (localeString.isNullOrEmpty()) {
            return DEFAULT_LOCALE
        }
        val normalized = localeString.replace('-', '_')
        val parts = normalized.split('_')
        return when (parts.size) {
            1 -> Locale(parts[0])
            2 -> Locale(parts[0], parts[1])
            3 -> Locale(parts[0], parts[1], parts[2])
            else -> Locale(parts[0], parts[1])
        }
    }

    /**
     * Formats locale to standard BCP-47 tag string (e.g. "en-US").
     */
    fun getLanguageTag(locale: Locale): String {
        return locale.toLanguageTag()
    }

    /**
     * Returns true if both locales share the same language code (e.g. "en_US" and "en_GB").
     */
    fun isSameLanguage(loc1: Locale?, loc2: Locale?): Boolean {
        if (loc1 == null || loc2 == null) return false
        return loc1.language.equals(loc2.language, ignoreCase = true)
    }

    /**
     * Returns appropriate default currency symbol for given locale.
     * Special support for Indian Rupee (₹) for India, Dollar ($) for US, Euro (€) for EU.
     */
    fun getCurrencySymbolForLocale(locale: Locale): String {
        val country = locale.country.uppercase(Locale.US)
        val language = locale.language.lowercase(Locale.US)
        return when {
            country == "IN" || language == "hi" -> "₹"
            country == "GB" -> "£"
            country == "JP" -> "¥"
            country == "KR" -> "₩"
            country == "RU" -> "₽"
            country in listOf("DE", "FR", "IT", "ES", "NL", "BE", "AT", "PT", "IE", "FI", "GR") -> "€"
            else -> "$"
        }
    }
}
