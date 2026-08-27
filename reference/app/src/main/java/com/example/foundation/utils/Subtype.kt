package com.example.foundation.utils

import com.example.foundation.common.LocaleUtils
import java.util.Locale

/**
 * Represents a keyboard subtype configuration (Locale, Keyboard Layout, and Display properties).
 */
data class Subtype(
    val localeString: String,
    val keyboardLayoutName: String = "qwerty",
    val secondaryLocaleString: String? = null
) {
    val primaryLocale: Locale get() = LocaleUtils.constructLocaleFromString(localeString)
    val secondaryLocale: Locale? get() = secondaryLocaleString?.let { LocaleUtils.constructLocaleFromString(it) }

    fun getDisplayName(): String {
        return SubtypeLocaleUtils.getSubtypeNameForSpacebar(primaryLocale, secondaryLocale)
    }

    companion object {
        val DEFAULT = Subtype(localeString = "en_US", keyboardLayoutName = "qwerty")
    }
}
