package com.example.voice.audio

/**
 * Audio normalizer and resampler utility for offline Whisper speech inference.
 * Converts 16-bit Mono PCM ShortArrays [-32768, 32767] into normalized 32-bit FloatArrays [-1.0f, 1.0f],
 * with support for RMS energy and decibel measurement.
 */
object AudioResampler {

    /**
     * Converts a 16-bit short PCM buffer into normalized float audio samples for Whisper.cpp.
     */
    fun toNormalizedFloatArray(pcmShorts: ShortArray, length: Int): FloatArray {
        val floats = FloatArray(length)
        for (i in 0 until length) {
            floats[i] = (pcmShorts[i] / 32768.0f).coerceIn(-1.0f, 1.0f)
        }
        return floats
    }

    /**
     * Calculates the Root Mean Square (RMS) decibel level of a PCM audio buffer.
     */
    fun calculateDbLevel(pcmShorts: ShortArray, length: Int): Float {
        if (length == 0) return 0f
        var sumSquares = 0.0
        for (i in 0 until length) {
            val sample = pcmShorts[i].toDouble()
            sumSquares += sample * sample
        }
        val meanSquare = sumSquares / length
        val rms = Math.sqrt(meanSquare)
        return if (rms > 0.0) {
            (20 * Math.log10(rms / 32768.0)).toFloat().coerceIn(-60f, 0f)
        } else {
            -60f
        }
    }
}
