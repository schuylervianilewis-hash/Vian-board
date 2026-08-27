package com.example.vault.crypto

import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * High-security AES-256-GCM cipher with PBKDF2WithHmacSHA256 key derivation.
 * Includes zero-fill memory wiping capabilities to protect sensitive keys from heap dumps.
 */
object VaultCipher {

    private const val ALGORITHM = "AES"
    private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val SALT_LENGTH = 16
    private const val PBKDF2_ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256

    /**
     * Encrypts plaintext bytes using a master password with AES-256-GCM.
     * Returns: salt (16 bytes) + IV (12 bytes) + ciphertext + GCM tag.
     */
    fun encrypt(plainText: ByteArray, masterPassword: CharArray): ByteArray? {
        var keyBytes: ByteArray? = null
        try {
            val salt = ByteArray(SALT_LENGTH)
            SecureRandom().nextBytes(salt)

            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            keyBytes = deriveKey(masterPassword, salt)
            val secretKey = SecretKeySpec(keyBytes, ALGORITHM)

            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val cipherText = cipher.doFinal(plainText)

            val combined = ByteArray(SALT_LENGTH + GCM_IV_LENGTH + cipherText.size)
            System.arraycopy(salt, 0, combined, 0, SALT_LENGTH)
            System.arraycopy(iv, 0, combined, SALT_LENGTH, GCM_IV_LENGTH)
            System.arraycopy(cipherText, 0, combined, SALT_LENGTH + GCM_IV_LENGTH, cipherText.size)

            return combined
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VAULT, LogLevel.ERROR, "Encryption failed: ${e.message}")
            return null
        } finally {
            if (keyBytes != null) {
                zeroWipe(keyBytes)
            }
        }
    }

    /**
     * Decrypts encrypted payload using master password.
     */
    fun decrypt(encryptedPayload: ByteArray, masterPassword: CharArray): ByteArray? {
        if (encryptedPayload.size < SALT_LENGTH + GCM_IV_LENGTH) {
            LogKeeper.log(LogTag.VAULT, LogLevel.WARN, "Payload too short for decryption")
            return null
        }

        var keyBytes: ByteArray? = null
        try {
            val salt = ByteArray(SALT_LENGTH)
            System.arraycopy(encryptedPayload, 0, salt, 0, SALT_LENGTH)

            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(encryptedPayload, SALT_LENGTH, iv, 0, GCM_IV_LENGTH)

            val cipherTextSize = encryptedPayload.size - SALT_LENGTH - GCM_IV_LENGTH
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(encryptedPayload, SALT_LENGTH + GCM_IV_LENGTH, cipherText, 0, cipherTextSize)

            keyBytes = deriveKey(masterPassword, salt)
            val secretKey = SecretKeySpec(keyBytes, ALGORITHM)

            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            return cipher.doFinal(cipherText)
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VAULT, LogLevel.ERROR, "Decryption failed: ${e.message}")
            return null
        } finally {
            if (keyBytes != null) {
                zeroWipe(keyBytes)
            }
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    /**
     * Wipes a byte array with zeros in memory.
     */
    fun zeroWipe(bytes: ByteArray) {
        Arrays.fill(bytes, 0.toByte())
    }

    /**
     * Wipes a char array with zeros in memory.
     */
    fun zeroWipe(chars: CharArray) {
        Arrays.fill(chars, '0')
    }
}
