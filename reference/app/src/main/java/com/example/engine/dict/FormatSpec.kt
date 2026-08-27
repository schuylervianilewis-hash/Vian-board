package com.example.engine.dict

/**
 * Binary Dictionary Format Specifications (AOSP & HeliBoard v2/v4 standards).
 */
object FormatSpec {

    // Magic Numbers for AOSP / HeliBoard Binary Dictionaries
    const val MAGIC_NUMBER_V2 = 0x9BC13AFE.toInt()
    const val MAGIC_NUMBER_V4 = 0x9BD03AFE.toInt()

    // Minimum and Maximum Supported Version
    const val MINIMUM_SUPPORTED_VERSION = 200
    const val MAXIMUM_SUPPORTED_VERSION = 403

    // Bit flags in dictionary trie node headers
    const val FLAG_IS_TERMINAL = 0x01
    const val FLAG_HAS_CHILDREN = 0x02
    const val FLAG_HAS_BIGRAMS = 0x04
    const val FLAG_IS_DELETED = 0x08
    const val FLAG_IS_BLACKLISTED = 0x10

    // Maximum word length handled by prediction trie
    const val MAX_WORD_LENGTH = 48

    // Probability & Frequency Limits
    const val MAX_PROBABILITY = 255
    const val MIN_PROBABILITY = 0
}
