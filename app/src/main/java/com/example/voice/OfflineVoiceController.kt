package com.example.voice

import android.content.Context
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import com.example.voice.audio.RawAudioRecorder
import com.example.voice.engine.WhisperEngine
import com.example.voice.model.VoiceModelManager
import com.example.voice.vad.SileroVadDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * High-level Offline Neural Voice Input Controller for Vian Board.
 * Coordinates RawAudioRecorder, SileroVadDetector, and WhisperEngine without bundling any models into the APK.
 */
class OfflineVoiceController(
    private val context: Context
) : RawAudioRecorder.AudioChunkListener,
    SileroVadDetector.VadStateListener,
    WhisperEngine.TranscriptionCallback {

    interface VoiceSessionListener {
        fun onListeningStarted()
        fun onRmsDbChanged(rmsDb: Float)
        fun onPartialTranscription(text: String)
        fun onFinalTranscription(text: String)
        fun onError(error: String)
        fun onListeningStopped()
    }

    var listener: VoiceSessionListener? = null

    val audioRecorder = RawAudioRecorder()
    val vadDetector = SileroVadDetector()
    val whisperEngine = WhisperEngine()
    val modelManager = VoiceModelManager(context)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val recordedAudioBuffer = ArrayList<Short>()

    init {
        audioRecorder.listener = this
        vadDetector.listener = this
        whisperEngine.callback = this
        autoLoadInstalledModels()
    }

    /**
     * Attempts to automatically load any models present in internal storage.
     */
    fun autoLoadInstalledModels() {
        val installed = modelManager.getInstalledModels()

        // Load VAD model if present
        val vad = installed.firstOrNull { it.type == com.example.voice.model.VoiceModelInfo.ModelType.SILERO_VAD }
        if (vad?.localFile != null) {
            vadDetector.loadModel(vad.localFile)
        }

        // Load Whisper model if present
        val whisper = installed.firstOrNull {
            it.type == com.example.voice.model.VoiceModelInfo.ModelType.WHISPER_TINY ||
            it.type == com.example.voice.model.VoiceModelInfo.ModelType.WHISPER_BASE ||
            it.type == com.example.voice.model.VoiceModelInfo.ModelType.WHISPER_SMALL ||
            it.type == com.example.voice.model.VoiceModelInfo.ModelType.CUSTOM
        }
        if (whisper?.localFile != null) {
            whisperEngine.loadModel(whisper.localFile)
        }
    }

    fun startListening(): Boolean {
        recordedAudioBuffer.clear()
        vadDetector.reset()

        val started = audioRecorder.startRecording()
        if (started) {
            listener?.onListeningStarted()
            LogKeeper.log(LogTag.VOICE, LogLevel.INFO, "Voice session started")
        }
        return started
    }

    fun stopListening() {
        audioRecorder.stopRecording()
        listener?.onListeningStopped()
        LogKeeper.log(LogTag.VOICE, LogLevel.INFO, "Voice session stopped")

        // Trigger transcription on captured audio buffer
        processRecordedAudio()
    }

    private fun processRecordedAudio() {
        if (recordedAudioBuffer.isEmpty()) return

        coroutineScope.launch {
            val pcmArray = ShortArray(recordedAudioBuffer.size)
            for (i in recordedAudioBuffer.indices) {
                pcmArray[i] = recordedAudioBuffer[i]
            }

            val result = whisperEngine.transcribe(pcmArray, pcmArray.size)
            if (result.text.isNotEmpty()) {
                listener?.onFinalTranscription(result.text)
            }
        }
    }

    // RawAudioRecorder.AudioChunkListener
    override fun onAudioChunk(pcmData: ShortArray, readSize: Int, rmsDb: Float) {
        synchronized(recordedAudioBuffer) {
            for (i in 0 until readSize) {
                recordedAudioBuffer.add(pcmData[i])
            }
        }
        vadDetector.processFrame(pcmData, readSize)
        listener?.onRmsDbChanged(rmsDb)
    }

    override fun onRecordingError(errorMessage: String) {
        listener?.onError(errorMessage)
    }

    // SileroVadDetector.VadStateListener
    override fun onSpeechStart() {
        LogKeeper.log(LogTag.VOICE, LogLevel.DEBUG, "Speech started")
    }

    override fun onSpeechEnd(totalDurationMs: Long) {
        LogKeeper.log(LogTag.VOICE, LogLevel.DEBUG, "Speech ended after ${totalDurationMs}ms")
    }

    override fun onSpeechProbability(prob: Float) {
        // Continuous probability feed
    }

    // WhisperEngine.TranscriptionCallback
    override fun onPartialTranscription(text: String) {
        listener?.onPartialTranscription(text)
    }

    override fun onFinalTranscription(result: WhisperEngine.TranscriptionResult) {
        listener?.onFinalTranscription(result.text)
    }

    override fun onError(error: String) {
        listener?.onError(error)
    }
}
