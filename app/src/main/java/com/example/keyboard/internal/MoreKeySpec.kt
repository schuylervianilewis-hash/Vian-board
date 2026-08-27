package com.example.keyboard.internal

import com.example.foundation.common.UnicodeUtils

/**
 * Parses and generates long-press popup key specifications for accents, symbols, and currencies.
 */
object MoreKeySpec {

    /**
     * Returns popup keys for a specific key code / label and layout mode.
     */
    fun getMoreKeysFor(label: String, isUppercase: Boolean = false): List<String> {
        if (label.isEmpty()) return emptyList()

        val rawList: List<String> = when (label.lowercase()) {
            "a" -> listOf("1", "ā", "á", "à", "â", "ä", "æ", "ã", "å", "ą")
            "b" -> listOf("β")
            "c" -> listOf("ç", "ć", "č")
            "d" -> listOf("ð", "ď")
            "e" -> listOf("3", "é", "è", "ê", "ë", "ē", "ė", "ę", "€")
            "g" -> listOf("ğ", "ģ")
            "i" -> listOf("8", "ī", "í", "ì", "î", "ï", "į", "ı")
            "k" -> listOf("ķ")
            "l" -> listOf("ł", "ĺ", "ľ")
            "n" -> listOf("ñ", "ń", "ň")
            "o" -> listOf("9", "ō", "ó", "ò", "ô", "ö", "õ", "ø", "œ")
            "p" -> listOf("0", "π", "¶")
            "q" -> listOf("1")
            "r" -> listOf("4", "ř", "ŕ")
            "s" -> listOf("ß", "ś", "š", "ş", "$")
            "t" -> listOf("5", "þ", "ť", "ţ")
            "u" -> listOf("7", "ū", "ú", "ù", "û", "ü", "ų", "ů")
            "w" -> listOf("2")
            "y" -> listOf("6", "ý", "ÿ")
            "z" -> listOf("ž", "ź", "ż")
            "1" -> listOf("¹", "½", "⅓", "¼", "⅛")
            "2" -> listOf("²", "⅔")
            "3" -> listOf("³", "¾", "⅜")
            "4" -> listOf("⁴")
            "5" -> listOf("⁵", "⅝")
            "6" -> listOf("⁶")
            "7" -> listOf("⁷", "⅞")
            "8" -> listOf("⁸")
            "9" -> listOf("⁹")
            "0" -> listOf("⁰", "°", "∅")
            "," -> listOf("⚙", "🪵", "🔐", "🛡", "📝", "📋", "✋", "😀", "🎙")
            "." -> listOf("!", "?", ",", ":", ";", "-", "/", "@", "~")
            "?" -> listOf("!", "¿", "‽")
            "!" -> listOf("?", "¡")
            "-" -> listOf("–", "—", "_")
            "/" -> listOf("\\", "|")
            "\"" -> listOf("“", "”", "«", "»")
            "'" -> listOf("‘", "’", "`")
            "%" -> listOf("‰", "℅")
            "=" -> listOf("≠", "≈", "≡", "≤", "≥")
            "+" -> listOf("±", "∓")
            "*" -> listOf("×", "•", "★")
            "$" -> listOf("₹", "€", "£", "¥", "₩", "₽", "¢", "₿")
            "₹" -> listOf("$", "€", "£", "¥", "₩", "₽", "¢", "₿")
            "€" -> listOf("$", "₹", "£", "¥", "₩", "₽", "¢", "₿")
            else -> emptyList()
        }

        return if (isUppercase) {
            rawList.map { candidate ->
                if (candidate.length == 1 && candidate[0].isLetter()) {
                    candidate.uppercase()
                } else {
                    candidate
                }
            }
        } else {
            rawList
        }
    }
}

