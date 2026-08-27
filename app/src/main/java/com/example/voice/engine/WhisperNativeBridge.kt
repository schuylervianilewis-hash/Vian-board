package com.example.voice.engine

import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag

/**
 * JNI Bindings and dynamic library loader for whisper.cpp / GGML tensor inference.
 * Matches FUTO Voice Input and whisper.cpp C-API signatures with graceful fallback.
 */
object WhisperNativeBridge {

    private var isNativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("whisper")
            isNativeLibraryLoaded = true
            LogKeeper.log(LogTag.VOICE, LogLevel.INFO, "libwhisper.so loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            isNativeLibraryLoaded = false
            LogKeeper.log(LogTag.VOICE, LogLevel.INFO, "libwhisper.so unbundled — running in managed fallback mode")
        } catch (e: Exception) {
            isNativeLibraryLoaded = false
            LogKeeper.log(LogTag.VOICE, LogLevel.WARN, "Whisper native load warning: ${e.message}")
        }
    }

    fun isAvailable(): Boolean = isNativeLibraryLoaded

    // Native JNI method declarations matching whisper.cpp bindings
    external fun initContext(modelPath: String): Long
    external fun freeContext(contextPtr: Long)
    external fun fullTranscribe(contextPtr: Long, samples: FloatArray, threads: Int, language: String): String
    external fun getSystemInfo(): String
}
