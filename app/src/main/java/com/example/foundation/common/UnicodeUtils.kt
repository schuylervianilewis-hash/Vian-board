package com.example.foundation.common

import java.util.Locale

/**
 * Unicode string and character processing utilities.
 * Handles code points, case conversions, and accented character mappings (ē, ū, etc.).
 */
object UnicodeUtils {

    /**
     * Converts a string to uppercase respecting the given locale.
     */
    fun toUpperCase(text: String, locale: Locale): String {
        return text.uppercase(locale)
    }

    /**
     * Converts a string to lowercase respecting the given locale.
     */
    fun toLowerCase(text: String, locale: Locale): String {
        return text.lowercase(locale)
    }

    /**
     * Capitalizes first character of word respecting locale.
     */
    fun capitalizeFirstChar(text: String, locale: Locale): String {
        if (text.isEmpty()) return text
        return text.substring(0, 1).uppercase(locale) + text.substring(1)
    }

    /**
     * Checks if character is an ASCII digit ('0' - '9').
     */
    fun isAsciiDigit(codePoint: Int): Boolean {
        return codePoint in '0'.code..'9'.code
    }

    /**
     * Checks if character is an alphabetic letter.
     */
    fun isLetter(codePoint: Int): Boolean {
        return Character.isLetter(codePoint)
    }

    /**
     * Returns extended long-press accented popup keys for a base character.
     * Includes macron vowels (ē, ū, ā, ī, ō) and special characters.
     */
    fun getExtendedAccentsForChar(char: Char): List<String> {
        val lower = char.lowercaseChar()
        return when (lower) {
            'e' -> listOf("3", "é", "è", "ê", "ë", "ē", "ė", "ę", "€")
            'u' -> listOf("7", "ū", "ú", "ù", "û", "ü", "ų", "ů")
            'a' -> listOf("1", "ā", "á", "à", "â", "ä", "æ", "ã", "å", "ą")
            'i' -> listOf("8", "ī", "í", "ì", "î", "ï", "į", "ı")
            'o' -> listOf("9", "ō", "ó", "ò", "ô", "ö", "õ", "ø", "œ")
            'c' -> listOf("ç", "ć", "č")
            'n' -> listOf("ñ", "ń")
            's' -> listOf("ß", "ś", "š", "ş")
            'z' -> listOf("ž", "ź", "ż")
            'y' -> listOf("ý", "ÿ")
            'd' -> listOf("ð", "ď")
            't' -> listOf("þ", "ť")
            '$' -> listOf("₹", "$", "€", "£", "¥", "₩", "₽", "¢", "₿")
            '₹' -> listOf("₹", "$", "€", "£", "¥", "₩", "₽", "¢", "₿")
            '€' -> listOf("€", "$", "₹", "£", "¥", "₩", "₽", "¢", "₿")
            else -> emptyList()
        }
    }
}
