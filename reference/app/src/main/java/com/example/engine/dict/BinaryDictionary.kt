package com.example.engine.dict

import android.content.Context
import com.example.foundation.utils.ByteArrayDictBuffer
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

/**
 * High-performance, zero-heap memory-mapped (mmap) binary dictionary.
 * Loads and searches AOSP/HeliBoard .dict files directly with Trie prefix traversal.
 */
class BinaryDictionary(
    private val dictFile: File,
    override val locale: Locale
) : Dictionary {

    override val dictType: String = TYPE_BINARY

    private var byteBuffer: ByteBuffer? = null
    private var decoder: BinaryDictDecoder? = null

    override var isInitialized: Boolean = false
        private set

    /**
     * Initializes the dictionary using memory-mapped I/O (mmap).
     */
    fun load(): Boolean {
        if (!dictFile.exists() || dictFile.length() < 16) {
            isInitialized = false
            return false
        }

        try {
            FileInputStream(dictFile).use { fis ->
                val channel = fis.channel
                byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                val dictBuffer = ByteArrayDictBuffer(byteBuffer!!)
                decoder = BinaryDictDecoder(dictBuffer)
                isInitialized = decoder?.readAndValidateHeader() == true
            }
        } catch (e: Exception) {
            isInitialized = false
        }

        return isInitialized
    }

    override fun isValidWord(word: String): Boolean {
        if (!isInitialized) return false
        return getFrequency(word) > 0
    }

    override fun getFrequency(word: String): Int {
        if (!isInitialized || decoder == null) return -1
        return decoder!!.getFrequencyForWord(word)
    }

    override fun getSuggestions(composedWord: String, maxSuggestions: Int): List<DictionarySuggestion> {
        if (!isInitialized || composedWord.isEmpty() || decoder == null) return emptyList()

        val results = mutableListOf<DictionarySuggestion>()
        val matches = decoder!!.getWordsMatchingPrefix(composedWord, maxSuggestions)

        for ((word, score) in matches) {
            results.add(
                DictionarySuggestion(
                    word = word,
                    score = score,
                    sourceDictType = dictType,
                    isExactMatch = word.equals(composedWord, ignoreCase = true),
                    isAutoCorrectCandidate = word.equals(composedWord, ignoreCase = true) || (results.isEmpty() && score > 150)
                )
            )
        }

        return results
    }

    override fun close() {
        byteBuffer = null
        decoder = null
        isInitialized = false
    }

    companion object {
        const val TYPE_BINARY = "binary_dict"

        /**
         * Copies asset binary dictionary to private storage and loads it.
         */
        fun createFromAsset(context: Context, assetPath: String, targetFile: File, locale: Locale): BinaryDictionary {
            if (!targetFile.exists() || targetFile.length() == 0L) {
                targetFile.parentFile?.mkdirs()
                context.assets.open(assetPath).use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            val dict = BinaryDictionary(targetFile, locale)
            dict.load()
            return dict
        }
    }
}
