package com.example.vault.totp

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * RFC 6238 TOTP (Time-based One-Time Password) Authenticator Engine.
 * Computes standard 6-digit verification codes using Base32 secrets.
 */
object TotpGenerator {

    private const val TIME_STEP_SECONDS = 30L
    private const val DIGITS = 6
    private val DIGITS_POWER = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000)

    /**
     * Generates a 6-digit TOTP code for the current time window.
     */
    fun generateCurrentCode(base32Secret: String): String? {
        val cleanSecret = base32Secret.replace(" ", "").uppercase()
        if (cleanSecret.isEmpty()) return null

        val currentWindow = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS
        return generateCode(cleanSecret, currentWindow)
    }

    /**
     * Returns remaining seconds in the current 30-second TOTP window.
     */
    fun getRemainingSeconds(): Int {
        val nowSec = System.currentTimeMillis() / 1000L
        return (TIME_STEP_SECONDS - (nowSec % TIME_STEP_SECONDS)).toInt()
    }

    fun generateCode(base32Secret: String, timeWindow: Long): String? {
        try {
            val keyBytes = decodeBase32(base32Secret) ?: return null
            val timeBytes = ByteBuffer.allocate(8).putLong(timeWindow).array()

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(keyBytes, "RAW"))
            val hash = mac.doFinal(timeBytes)

            val offset = hash[hash.size - 1].toInt() and 0xf
            val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                    ((hash[offset + 1].toInt() and 0xff) shl 16) or
                    ((hash[offset + 2].toInt() and 0xff) shl 8) or
                    (hash[offset + 3].toInt() and 0xff)

            val otp = binary % DIGITS_POWER[DIGITS]
            return "%0${DIGITS}d".format(otp)
        } catch (_: Exception) {
            return null
        }
    }

    private fun decodeBase32(base32: String): ByteArray? {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val clean = base32.trim().replace("=", "").uppercase()
        if (clean.isEmpty()) return null

        val output = ArrayList<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (c in clean) {
            val valIndex = chars.indexOf(c)
            if (valIndex < 0) return null // Invalid base32 character

            buffer = (buffer shl 5) or valIndex
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        return output.toByteArray()
    }
}
