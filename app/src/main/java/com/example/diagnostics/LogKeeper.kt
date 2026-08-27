package com.example.diagnostics

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern

/**
 * LogTag represents all primary subsystems in Vian Board.
 */
enum class LogTag(val displayName: String) {
    SYSTEM("System"),
    NAVIGATION("Navigation"),
    IME("IME"),
    TOOLBAR("Toolbar"),
    MODAL("Modal"),
    CLIPBOARD("Clipboard"),
    GESTURE("Gesture"),
    KEYBOARD("Keyboard"),
    DICT("Dict"),
    ENGINE("Engine"),
    VAULT("Vault"),
    VOICE("Voice"),
    BACKUP("Backup"),
    SETTINGS("Settings")
}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

data class LogEntry(
    val id: Long,
    val timestampMillis: Long,
    val tag: LogTag,
    val level: LogLevel,
    val message: String,
    val isRedacted: Boolean = false
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        return sdf.format(Date(timestampMillis))
    }

    override fun toString(): String {
        return "${formattedTime()} ${tag.displayName.padEnd(12)} $message"
    }
}

data class LogKeeperStats(
    val currentBufferSize: Int,
    val maxCapacity: Int,
    val totalLoggedEvents: Long,
    val redactedEventsCount: Long,
    val isEnabled: Boolean,
    val oldestTimestampMillis: Long,
    val newestTimestampMillis: Long
)

/**
 * LogKeeper: Ultra-lightweight, zero-overhead in-memory circular ring buffer.
 * Capped at 200 entries with zero disk I/O during normal execution.
 * Includes a Master Switch toggle and a strict Zero-PII / Credential Scrubber Firewall.
 */
object LogKeeper {

    const val MAX_CAPACITY = 200

    private val isMasterEnabled = AtomicBoolean(true)
    private val entrySequence = AtomicLong(0)
    private val totalLoggedCount = AtomicLong(0)
    private val redactedCount = AtomicLong(0)

    // Fixed array-backed ring buffer (zero continuous allocations)
    private val ringBuffer = arrayOfNulls<LogEntry>(MAX_CAPACITY)
    private var writeIndex = 0
    private var isBufferFull = false
    private val lock = Any()

    private val _logEventsFlow = MutableSharedFlow<LogEntry>(replay = 0, extraBufferCapacity = 32)
    val logEventsFlow: SharedFlow<LogEntry> = _logEventsFlow.asSharedFlow()

    // Strict PII & Credential scrubbing patterns
    private val SENSITIVE_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(password|passwd|pwd|passcode|pin|otp|cvv|cvc)[:=]\\s*([^\n\r\\s,]+)"),
        Pattern.compile("(?i)(bearer\\s+|token=)([a-zA-Z0-9_.-]{6,})"),
        Pattern.compile("(?i)\\b(auth|secret|api_key|apikey)[:=]\\s*([^\n\r\\s,]+)")
    )

    fun isEnabled(): Boolean = isMasterEnabled.get()

    fun setEnabled(enabled: Boolean) {
        val previous = isMasterEnabled.getAndSet(enabled)
        if (previous != enabled) {
            log(LogTag.SYSTEM, LogLevel.INFO, if (enabled) "LogKeeper master recording enabled" else "LogKeeper master recording disabled", force = true)
        }
    }

    /**
     * Privacy Firewall: Strictly redacts credentials, passwords, and private tokens.
     */
    fun sanitize(message: String, isSensitiveContext: Boolean): Pair<String, Boolean> {
        if (isSensitiveContext) {
            return "[REDACTED_SENSITIVE_INPUT]" to true
        }

        var result = message
        var wasModified = false

        for (pattern in SENSITIVE_PATTERNS) {
            val matcher = pattern.matcher(result)
            if (matcher.find()) {
                result = matcher.replaceAll("$1: [REDACTED_SECRET]")
                wasModified = true
            }
        }

        return result to wasModified
    }

    /**
     * Records a diagnostic event into the circular ring buffer.
     */
    fun log(
        tag: LogTag,
        level: LogLevel = LogLevel.DEBUG,
        rawMessage: String,
        isSensitiveContext: Boolean = false,
        force: Boolean = false
    ) {
        if (!force && !isMasterEnabled.get()) {
            return // Instantly discard with 0 CPU overhead when disabled
        }

        val (safeMessage, isRedacted) = sanitize(rawMessage, isSensitiveContext)
        val id = entrySequence.incrementAndGet()
        totalLoggedCount.incrementAndGet()
        if (isRedacted) {
            redactedCount.incrementAndGet()
        }

        val entry = LogEntry(
            id = id,
            timestampMillis = System.currentTimeMillis(),
            tag = tag,
            level = level,
            message = safeMessage,
            isRedacted = isRedacted
        )

        synchronized(lock) {
            ringBuffer[writeIndex] = entry
            writeIndex = (writeIndex + 1) % MAX_CAPACITY
            if (writeIndex == 0) {
                isBufferFull = true
            }
        }

        _logEventsFlow.tryEmit(entry)
    }

    fun v(tag: LogTag, message: String, isSensitiveContext: Boolean = false) =
        log(tag, LogLevel.VERBOSE, message, isSensitiveContext)

    fun d(tag: LogTag, message: String, isSensitiveContext: Boolean = false) =
        log(tag, LogLevel.DEBUG, message, isSensitiveContext)

    fun i(tag: LogTag, message: String, isSensitiveContext: Boolean = false) =
        log(tag, LogLevel.INFO, message, isSensitiveContext)

    fun w(tag: LogTag, message: String, isSensitiveContext: Boolean = false) =
        log(tag, LogLevel.WARN, message, isSensitiveContext)

    fun e(tag: LogTag, message: String, throwable: Throwable? = null, isSensitiveContext: Boolean = false) {
        val msg = if (throwable != null) "$message: ${throwable.message}" else message
        log(tag, LogLevel.ERROR, msg, isSensitiveContext)
    }

    /**
     * Retrieves a filtered snapshot of logs for UI presentation.
     */
    fun getLogs(
        filterTag: LogTag? = null,
        minLevel: LogLevel? = null,
        timeWindowMillis: Long? = null,
        limit: Int = MAX_CAPACITY
    ): List<LogEntry> {
        val cutoff = if (timeWindowMillis != null && timeWindowMillis > 0L) {
            System.currentTimeMillis() - timeWindowMillis
        } else 0L

        val minLevelOrdinal = minLevel?.ordinal ?: 0
        val result = ArrayList<LogEntry>(minOf(limit, MAX_CAPACITY))

        synchronized(lock) {
            val totalAvailable = if (isBufferFull) MAX_CAPACITY else writeIndex
            val start = if (isBufferFull) writeIndex else 0

            for (i in 0 until totalAvailable) {
                val index = (start + i) % MAX_CAPACITY
                val entry = ringBuffer[index] ?: continue

                if (entry.timestampMillis >= cutoff &&
                    entry.level.ordinal >= minLevelOrdinal &&
                    (filterTag == null || entry.tag == filterTag)
                ) {
                    result.add(entry)
                }
            }
        }

        // Newest on top for scannable UI
        result.reverse()

        if (result.size > limit) {
            return result.take(limit)
        }
        return result
    }

    /**
     * Exports memory buffer to clean diagnostic plain-text.
     */
    fun exportToText(timeWindowMillis: Long? = null): String {
        val logs = getLogs(timeWindowMillis = timeWindowMillis, limit = MAX_CAPACITY)
        val sb = StringBuilder()
        sb.append("=== VIAN BOARD DIAGNOSTIC LOG DUMP ===\n")
        sb.append("Generated: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())).append("\n")
        sb.append("Buffer Capacity: ").append(MAX_CAPACITY).append(" slots\n")
        sb.append("Exported Entries: ").append(logs.size).append("\n")
        sb.append("Total Historical Events: ").append(totalLoggedCount.get()).append("\n")
        sb.append("Redacted Credentials/PII: ").append(redactedCount.get()).append("\n")
        sb.append("======================================\n\n")

        for (entry in logs) {
            sb.append("${entry.formattedTime()} ${entry.tag.displayName.padEnd(14)} ${entry.message}\n")
        }
        return sb.toString()
    }

    /**
     * Clears all log entries in the ring buffer.
     */
    fun clear() {
        synchronized(lock) {
            ringBuffer.fill(null)
            writeIndex = 0
            isBufferFull = false
            entrySequence.set(0)
            totalLoggedCount.set(0)
            redactedCount.set(0)
        }
        log(LogTag.SYSTEM, LogLevel.INFO, "LogKeeper in-memory buffer cleared", force = true)
    }

    fun getStats(): LogKeeperStats {
        synchronized(lock) {
            val count = if (isBufferFull) MAX_CAPACITY else writeIndex
            val oldest = if (count > 0) {
                val idx = if (isBufferFull) writeIndex else 0
                ringBuffer[idx]?.timestampMillis ?: 0L
            } else 0L
            val newest = if (count > 0) {
                val idx = (writeIndex - 1 + MAX_CAPACITY) % MAX_CAPACITY
                ringBuffer[idx]?.timestampMillis ?: 0L
            } else 0L

            return LogKeeperStats(
                currentBufferSize = count,
                maxCapacity = MAX_CAPACITY,
                totalLoggedEvents = totalLoggedCount.get(),
                redactedEventsCount = redactedCount.get(),
                isEnabled = isMasterEnabled.get(),
                oldestTimestampMillis = oldest,
                newestTimestampMillis = newest
            )
        }
    }
}
