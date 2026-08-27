package com.example.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogKeeperTest {

    @Before
    fun setUp() {
        LogKeeper.clear()
    }

    @Test
    fun testLogAndRetrieve() {
        LogKeeper.i(LogTag.IME, "Test info message")
        LogKeeper.d(LogTag.TOUCH, "Test touch debug")

        val logs = LogKeeper.getLogs()
        assertEquals(2, logs.size)
        assertEquals("[IME]", logs[0].tag.prefix)
        assertEquals("Test info message", logs[0].message)
        assertEquals("[TOUCH]", logs[1].tag.prefix)
        assertEquals("Test touch debug", logs[1].message)
    }

    @Test
    fun testPrivacySanitization() {
        // Explicit sensitive context
        LogKeeper.i(LogTag.IME, "SecretPassword123", isSensitiveContext = true)
        val logs = LogKeeper.getLogs()
        assertEquals(1, logs.size)
        assertEquals("[REDACTED_SENSITIVE_INPUT]", logs[0].message)
        assertTrue(logs[0].isRedacted)

        // Pattern matching redaction
        LogKeeper.d(LogTag.VAULT, "User entered pin: 123456 in keypad")
        val vaultLogs = LogKeeper.getLogs(filterTag = LogTag.VAULT)
        assertEquals(1, vaultLogs.size)
        assertTrue(vaultLogs[0].message.contains("[REDACTED_PIN]"))
        assertTrue(vaultLogs[0].isRedacted)
    }

    @Test
    fun testFilterByTag() {
        LogKeeper.i(LogTag.IME, "IME event")
        LogKeeper.i(LogTag.GESTURE, "Gesture event")
        LogKeeper.i(LogTag.IME, "Another IME event")

        val imeLogs = LogKeeper.getLogs(filterTag = LogTag.IME)
        assertEquals(2, imeLogs.size)

        val gestureLogs = LogKeeper.getLogs(filterTag = LogTag.GESTURE)
        assertEquals(1, gestureLogs.size)
    }

    @Test
    fun testRingBufferCapacity() {
        for (i in 1..5500) {
            LogKeeper.v(LogTag.SYSTEM, "Event $i")
        }

        val stats = LogKeeper.getStats()
        assertEquals(LogKeeper.MAX_CAPACITY, stats.currentBufferSize)
        assertEquals(5500L, stats.totalLoggedEvents)

        val logs = LogKeeper.getLogs()
        assertEquals(LogKeeper.MAX_CAPACITY, logs.size)
        // First log in buffer should be from after wrapping (event 501)
        assertEquals("Event 501", logs.first().message)
        assertEquals("Event 5500", logs.last().message)
    }
}
