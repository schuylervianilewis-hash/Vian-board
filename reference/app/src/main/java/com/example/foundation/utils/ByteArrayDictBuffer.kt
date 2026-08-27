package com.example.foundation.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Fast zero-allocation byte reader for binary dictionary data.
 * Wraps either a direct ByteBuffer or a byte array for reading format specs, tries, and word frequencies.
 */
class ByteArrayDictBuffer(private val buffer: ByteBuffer) {

    init {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    constructor(byteArray: ByteArray) : this(ByteBuffer.wrap(byteArray))

    val capacity: Int get() = buffer.capacity()

    var position: Int
        get() = buffer.position()
        set(value) {
            buffer.position(value)
        }

    fun readByte(): Int {
        return buffer.get().toInt() and 0xFF
    }

    fun readShort(): Int {
        return buffer.short.toInt() and 0xFFFF
    }

    fun readInt(): Int {
        return buffer.int
    }

    /**
     * Reads a 24-bit (3-byte) unsigned integer used in AOSP dictionary trie offsets.
     */
    fun readUint24(): Int {
        val b1 = readByte()
        val b2 = readByte()
        val b3 = readByte()
        return (b3 shl 16) or (b2 shl 8) or b1
    }

    /**
     * Reads a UTF-8 encoded string up to a null terminator (0x00) or max length.
     */
    fun readNullTerminatedUtf8String(maxLength: Int = 128): String {
        val bytes = ByteArray(maxLength)
        var count = 0
        while (count < maxLength) {
            val b = buffer.get()
            if (b == 0.toByte()) break
            bytes[count++] = b
        }
        return String(bytes, 0, count, Charsets.UTF_8)
    }
}
