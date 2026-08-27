package com.example.diagnostics

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern

/**
 * LogTag represents the subsystems inside Vian Board.
 */
enum class LogTag(val prefix: String) {
    IME("[IME]"),
    TOUCH("[TOUCH]"),
    GESTURE("[GESTURE]"),
    KEYBOARD("[KEYBOARD]"),
    DICT("[DICT]"),
    MODAL("[MODAL]"),
    VAULT("[VAULT]"),
    SETTINGS("[SETTINGS]"),
    VOICE("[VOICE]"),
    BACKUP("[BACKUP]"),
    SYSTEM("[SYSTEM]")
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
        return "${formattedTime()} ${level.name.padEnd(5)} ${tag.prefix} $message"
    }
}

data class LogKeeperStats(
    val currentBufferSize: Int,
    val maxCapacity: Int,
    val totalLoggedEvents: Long,
    val redactedEventsCount: Long,
    val oldestTimestampMillis: Long,
    val newestTimestampMillis: Long
)

/**
 * LogKeeper is a high-performance, non-blocking in-memory circular ring buffer.
 * It provides zero-disk I/O diagnostics for sub-millisecond keyboard performance
 * and strict privacy sanitization to ensure sensitive credentials are never stored.
 */
object LogKeeper {

    const val MAX_CAPACITY = 5000

    private val entrySequence = AtomicLong(0)
    private val totalLoggedCount = AtomicLong(0)
    private val redactedCount = AtomicLong(0)

    // Array-backed ring buffer to avoid frequent heap allocations
    private val ringBuffer = arrayOfNulls<LogEntry>(MAX_CAPACITY)
    private var writeIndex = 0
    private var isBufferFull = false
    private val lock = Any()

    private val _logEventsFlow = MutableSharedFlow<LogEntry>(replay = 0, extraBufferCapacity = 64)
    val logEventsFlow: SharedFlow<LogEntry> = _logEventsFlow.asSharedFlow()

    // Regular expressions for privacy sanitization
    private val PIN_OR_NUM_PATTERN = Pattern.compile("(?i)\\b(pin|passcode|otp|cvv|cvc)[:=]\\s*([0-9a-z]{3,10})")
    private val SENSITIVE_TOKEN_PATTERN = Pattern.compile("(?i)(bearer\\s+|token=)([a-zA-Z0-9_.-]{8,})")

    /**
     * Privacy Sanitizer: Redacts sensitive passwords, PINs, or token patterns.
     */
    fun sanitize(message: String, isSensitiveContext: Boolean): Pair<String, Boolean> {
        if (isSensitiveContext) {
            return "[REDACTED_SENSITIVE_INPUT]" to true
        }

        var result = message
        var wasModified = false

        if (PIN_OR_NUM_PATTERN.matcher(result).find()) {
            result = PIN_OR_NUM_PATTERN.matcher(result).replaceAll("$1: [REDACTED_PIN]")
            wasModified = true
        }

        if (SENSITIVE_TOKEN_PATTERN.matcher(result).find()) {
            result = SENSITIVE_TOKEN_PATTERN.matcher(result).replaceAll("$1[REDACTED_TOKEN]")
            wasModified = true
        }

        return result to wasModified
    }

    /**
     * Records a diagnostic log entry into the circular ring buffer.
     */
    fun log(
        tag: LogTag,
        level: LogLevel = LogLevel.DEBUG,
        rawMessage: String,
        isSensitiveContext: Boolean = false
    ) {
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
     * Retrieves snapshot of log entries with optional filters.
     */
    fun getLogs(
        filterTag: LogTag? = null,
        minLevel: LogLevel? = null,
        timeWindowMillis: Long? = null,
        limit: Int = MAX_CAPACITY
    ): List<LogEntry> {
        val cutoff = if (timeWindowMillis != null) System.currentTimeMillis() - timeWindowMillis else 0L
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

        if (result.size > limit) {
            return result.takeLast(limit)
        }
        return result
    }

    /**
     * Generates a plain-text export of all buffered diagnostics.
     */
    fun exportToText(filterTag: LogTag? = null, timeWindowMillis: Long? = null): String {
        val logs = getLogs(filterTag = filterTag, timeWindowMillis = timeWindowMillis, limit = MAX_CAPACITY)
        val sb = StringBuilder()
        sb.append("=== VIAN BOARD DIAGNOSTIC LOG DUMP ===\n")
        sb.append("Export Time: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())).append("\n")
        sb.append("Total Events in Buffer: ").append(logs.size).append("\n")
        sb.append("Total Redacted Events: ").append(redactedCount.get()).append("\n")
        sb.append("=======================================\n\n")

        for (entry in logs) {
            sb.append(entry.toString()).append("\n")
        }
        return sb.toString()
    }

    /**
     * Clears all log entries in the ring buffer and resets counters.
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
    }

    /**
     * Returns diagnostics performance statistics.
     */
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
                oldestTimestampMillis = oldest,
                newestTimestampMillis = newest
            )
        }
    }
}
