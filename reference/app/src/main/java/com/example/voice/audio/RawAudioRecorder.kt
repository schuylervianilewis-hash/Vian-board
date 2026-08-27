package com.example.voice.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * High-performance 16kHz, 16-bit Mono PCM audio recording engine.
 * Employs zero-allocation circular buffer chunking for low-latency streaming to VAD and Whisper.
 */
class RawAudioRecorder(
    private val sampleRate: Int = SAMPLE_RATE_16KHZ,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    interface AudioChunkListener {
        fun onAudioChunk(pcmData: ShortArray, readSize: Int, rmsDb: Float)
        fun onRecordingError(errorMessage: String)
    }

    var listener: AudioChunkListener? = null

    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private var recordingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val bufferSize = Math.max(minBufferSize, CHUNK_SIZE_SAMPLES * 2)

    @SuppressLint("MissingPermission")
    fun startRecording(): Boolean {
        if (isRecording.get()) return true

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                LogKeeper.log(LogTag.VOICE, LogLevel.ERROR, "AudioRecord initialization failed")
                listener?.onRecordingError("AudioRecord failed to initialize")
                release()
                return false
            }

            audioRecord?.startRecording()
            isRecording.set(true)
            LogKeeper.log(LogTag.VOICE, LogLevel.INFO, "Raw audio recording started at $sampleRate Hz")

            recordingJob = coroutineScope.launch {
                val pcmBuffer = ShortArray(CHUNK_SIZE_SAMPLES)

                while (isActive && isRecording.get()) {
                    val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: -1
                    if (read > 0) {
                        val rmsDb = calculateRmsDb(pcmBuffer, read)
                        listener?.onAudioChunk(pcmBuffer, read, rmsDb)
                    } else if (read < 0) {
                        LogKeeper.log(LogTag.VOICE, LogLevel.WARN, "AudioRecord read error code: $read")
                        break
                    }
                }
            }
            return true
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VOICE, LogLevel.ERROR, "startRecording exception: ${e.message}")
            listener?.onRecordingError(e.message ?: "Unknown recording error")
            release()
            return false
        }
    }

    fun stopRecording() {
        if (!isRecording.getAndSet(false)) return

        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            LogKeeper.log(LogTag.VOICE, LogLevel.INFO, "Raw audio recording stopped")
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VOICE, LogLevel.WARN, "Error stopping AudioRecord: ${e.message}")
        } finally {
            release()
        }
    }

    fun isRecordingNow(): Boolean = isRecording.get()

    private fun release() {
        try {
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    private fun calculateRmsDb(buffer: ShortArray, readSize: Int): Float {
        var sumSquares = 0.0
        for (i in 0 until readSize) {
            val sample = buffer[i].toDouble()
            sumSquares += sample * sample
        }
        val meanSquare = sumSquares / readSize
        val rms = Math.sqrt(meanSquare)
        if (rms <= 0.0) return 0f
        val db = 20.0 * Math.log10(rms / 32767.0)
        // Normalize roughly between 0 dB and 100 dB
        return Math.max(0f, (db + 90.0).toFloat())
    }

    companion object {
        const val SAMPLE_RATE_16KHZ = 16000
        const val CHUNK_SIZE_SAMPLES = 512 // ~32ms at 16kHz (Standard Silero VAD frame window)
    }
}
