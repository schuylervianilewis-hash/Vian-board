package com.example.engine.dict

import com.example.foundation.utils.ByteArrayDictBuffer

/**
 * Parses and decodes binary dictionary headers, trie nodes, word tables, and prefix suggestions.
 * Follows the AOSP / HeliBoard Binary Dictionary format specifications (v2 & v4).
 */
class BinaryDictDecoder(private val buffer: ByteArrayDictBuffer) {

    var formatVersion: Int = 0
        private set

    var headerSize: Int = 0
        private set

    var trieRootOffset: Int = 0
        private set

    var localeString: String = "en"
        private set

    var hasBigrams: Boolean = false
        private set

    /**
     * Reads and validates the dictionary header magic number and metadata.
     * Returns true if valid AOSP/HeliBoard binary dictionary.
     */
    fun readAndValidateHeader(): Boolean {
        if (buffer.capacity < 16) return false
        buffer.position = 0

        val magic = buffer.readInt()
        if (magic != FormatSpec.MAGIC_NUMBER_V2 && magic != FormatSpec.MAGIC_NUMBER_V4) {
            return false
        }

        formatVersion = buffer.readShort()
        if (formatVersion < FormatSpec.MINIMUM_SUPPORTED_VERSION || formatVersion > FormatSpec.MAXIMUM_SUPPORTED_VERSION) {
            return false
        }

        headerSize = buffer.readShort()
        val flags = buffer.readShort()
        hasBigrams = (flags and 0x01) != 0

        trieRootOffset = headerSize
        return true
    }

    /**
     * Traverses the trie for an exact word match.
     * Returns the frequency (0-255) or -1 if not found.
     */
    fun getFrequencyForWord(word: String): Int {
        if (!readAndValidateHeader() || word.isEmpty()) return -1

        var currentOffset = trieRootOffset
        val chars = word.lowercase()
        var charIndex = 0

        while (charIndex < chars.length && currentOffset < buffer.capacity) {
            buffer.position = currentOffset
            val nodeCount = buffer.readByte()
            var matched = false

            for (i in 0 until nodeCount) {
                val flags = buffer.readByte()
                val codePoint = buffer.readByte() // Character code
                val hasChildren = (flags and FormatSpec.FLAG_HAS_CHILDREN) != 0
                val isTerminal = (flags and FormatSpec.FLAG_IS_TERMINAL) != 0

                val freq = if (isTerminal) buffer.readByte() else 0
                val childOffset = if (hasChildren) buffer.readUint24() else 0

                if (codePoint.toChar() == chars[charIndex]) {
                    if (charIndex == chars.length - 1) {
                        return if (isTerminal) freq else -1
                    }
                    if (hasChildren && childOffset > 0) {
                        currentOffset = childOffset
                        charIndex++
                        matched = true
                        break
                    } else {
                        return -1
                    }
                }
            }

            if (!matched) return -1
        }
        return -1
    }

    /**
     * Collects words in the trie that start with [prefix], up to [maxCount] suggestions.
     */
    fun getWordsMatchingPrefix(prefix: String, maxCount: Int = 10): List<Pair<String, Int>> {
        if (!readAndValidateHeader() || prefix.isEmpty()) return emptyList()

        val results = mutableListOf<Pair<String, Int>>()
        var currentOffset = trieRootOffset
        val chars = prefix.lowercase()
        var charIndex = 0

        // Step 1: Navigate to the node matching the prefix
        while (charIndex < chars.length && currentOffset < buffer.capacity) {
            buffer.position = currentOffset
            val nodeCount = buffer.readByte()
            var matched = false

            for (i in 0 until nodeCount) {
                val flags = buffer.readByte()
                val codePoint = buffer.readByte()
                val hasChildren = (flags and FormatSpec.FLAG_HAS_CHILDREN) != 0
                val isTerminal = (flags and FormatSpec.FLAG_IS_TERMINAL) != 0
                val freq = if (isTerminal) buffer.readByte() else 0
                val childOffset = if (hasChildren) buffer.readUint24() else 0

                if (codePoint.toChar() == chars[charIndex]) {
                    if (charIndex == chars.length - 1) {
                        if (isTerminal) {
                            results.add(Pair(prefix, freq))
                        }
                        if (hasChildren && childOffset > 0) {
                            // Collect children descendants
                            collectSubtreeWords(childOffset, StringBuilder(prefix), results, maxCount)
                        }
                        return results.sortedByDescending { it.second }.take(maxCount)
                    }
                    if (hasChildren && childOffset > 0) {
                        currentOffset = childOffset
                        charIndex++
                        matched = true
                        break
                    } else {
                        return results
                    }
                }
            }

            if (!matched) return results
        }

        return results.sortedByDescending { it.second }.take(maxCount)
    }

    private fun collectSubtreeWords(
        offset: Int,
        currentWord: StringBuilder,
        results: MutableList<Pair<String, Int>>,
        maxCount: Int
    ) {
        if (offset >= buffer.capacity || results.size >= maxCount * 3) return
        buffer.position = offset
        val nodeCount = buffer.readByte()

        for (i in 0 until nodeCount) {
            val flags = buffer.readByte()
            val codePoint = buffer.readByte()
            val hasChildren = (flags and FormatSpec.FLAG_HAS_CHILDREN) != 0
            val isTerminal = (flags and FormatSpec.FLAG_IS_TERMINAL) != 0
            val freq = if (isTerminal) buffer.readByte() else 0
            val childOffset = if (hasChildren) buffer.readUint24() else 0

            val ch = codePoint.toChar()
            currentWord.append(ch)

            if (isTerminal) {
                results.add(Pair(currentWord.toString(), freq))
            }

            if (hasChildren && childOffset > 0 && results.size < maxCount * 3) {
                val savePos = buffer.position
                collectSubtreeWords(childOffset, currentWord, results, maxCount)
                buffer.position = savePos
            }

            currentWord.setLength(currentWord.length - 1)
        }
    }
}
