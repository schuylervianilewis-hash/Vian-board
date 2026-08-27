package com.example.foundation.utils

/**
 * RingCharBuffer is a lightweight circular buffer of characters.
 * Used for instant tracking of recently typed characters, double-space period triggers,
 * smart multiply morphing (1920x1080 -> 1920×1080), and punctuation space eating.
 */
class RingCharBuffer(private val capacity: Int = 32) {

    private val buffer = CharArray(capacity)
    private var head = 0
    private var size = 0

    fun push(c: Char) {
        buffer[head] = c
        head = (head + 1) % capacity
        if (size < capacity) {
            size++
        }
    }

    fun getLastChar(): Char? {
        if (size == 0) return null
        val idx = (head - 1 + capacity) % capacity
        return buffer[idx]
    }

    fun getCharBeforeLast(): Char? {
        if (size < 2) return null
        val idx = (head - 2 + capacity) % capacity
        return buffer[idx]
    }

    fun getRecentString(length: Int): String {
        val count = minOf(length, size)
        val chars = CharArray(count)
        for (i in 0 until count) {
            val idx = (head - count + i + capacity) % capacity
            chars[i] = buffer[idx]
        }
        return String(chars)
    }

    fun clear() {
        head = 0
        size = 0
    }
}
