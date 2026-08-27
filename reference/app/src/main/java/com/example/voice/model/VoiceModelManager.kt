package com.example.voice.model

import android.content.Context
import android.net.Uri
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

/**
 * Model description metadata for post-install downloaded or imported models.
 */
data class VoiceModelInfo(
    val id: String,
    val name: String,
    val type: ModelType,
    val sizeBytes: Long,
    val localFile: File?,
    val isInstalled: Boolean,
    val downloadUrl: String? = null
) {
    enum class ModelType {
        WHISPER_TINY,
        WHISPER_BASE,
        WHISPER_SMALL,
        SILERO_VAD,
        CUSTOM
    }
}

/**
 * Manages external post-install model files (.bin, .gguf, .onnx).
 * ZERO models are bundled in the base APK to maintain minimal app footprint.
 */
class VoiceModelManager(private val context: Context) {

    private val modelsDir: File = File(context.filesDir, "voice_models").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Lists all models currently stored in the internal voice_models directory.
     */
    fun getInstalledModels(): List<VoiceModelInfo> {
        val files = modelsDir.listFiles() ?: return emptyList()
        return files.filter { it.isFile && it.length() > 0 }.map { file ->
            val type = when {
                file.name.contains("silero", ignoreCase = true) || file.name.endsWith(".onnx") -> VoiceModelInfo.ModelType.SILERO_VAD
                file.name.contains("tiny", ignoreCase = true) -> VoiceModelInfo.ModelType.WHISPER_TINY
                file.name.contains("base", ignoreCase = true) -> VoiceModelInfo.ModelType.WHISPER_BASE
                file.name.contains("small", ignoreCase = true) -> VoiceModelInfo.ModelType.WHISPER_SMALL
                else -> VoiceModelInfo.ModelType.CUSTOM
            }
            VoiceModelInfo(
                id = file.name,
                name = file.name,
                type = type,
                sizeBytes = file.length(),
                localFile = file,
                isInstalled = true
            )
        }
    }

    /**
     * Imports a user-selected model file via Storage Access Framework (SAF) URI.
     */
    fun importModelFromUri(uri: Uri, originalFileName: String?): VoiceModelInfo? {
        val fileName = originalFileName ?: "custom_model_${System.currentTimeMillis()}.bin"
        val destinationFile = File(modelsDir, fileName)

        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                LogKeeper.log(LogTag.VOICE, LogLevel.ERROR, "Failed to open input stream from URI: $uri")
                return null
            }

            FileOutputStream(destinationFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }
            inputStream.close()

            LogKeeper.log(
                LogTag.VOICE,
                LogLevel.INFO,
                "Model imported successfully: ${destinationFile.name} (${destinationFile.length() / (1024 * 1024)} MB)"
            )

            val type = if (fileName.contains("vad", ignoreCase = true)) {
                VoiceModelInfo.ModelType.SILERO_VAD
            } else {
                VoiceModelInfo.ModelType.CUSTOM
            }

            VoiceModelInfo(
                id = destinationFile.name,
                name = destinationFile.name,
                type = type,
                sizeBytes = destinationFile.length(),
                localFile = destinationFile,
                isInstalled = true
            )
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VOICE, LogLevel.ERROR, "Exception importing model: ${e.message}")
            if (destinationFile.exists()) destinationFile.delete()
            null
        }
    }

    /**
     * Deletes a model from local storage.
     */
    fun deleteModel(modelInfo: VoiceModelInfo): Boolean {
        val file = modelInfo.localFile ?: File(modelsDir, modelInfo.id)
        return if (file.exists()) {
            val deleted = file.delete()
            LogKeeper.log(LogTag.VOICE, LogLevel.INFO, "Model file deleted: ${modelInfo.name} (success=$deleted)")
            deleted
        } else {
            false
        }
    }

    /**
     * Verifies SHA-256 integrity hash of a model file.
     */
    fun verifySha256(file: File, expectedHash: String): Boolean {
        if (!file.exists()) return false
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { isStream ->
                val buffer = ByteArray(8192)
                var read: Int
                while (isStream.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            val hashBytes = digest.digest()
            val computedHash = hashBytes.joinToString("") { "%02x".format(it) }
            computedHash.equals(expectedHash, ignoreCase = true)
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VOICE, LogLevel.ERROR, "SHA256 calculation failed: ${e.message}")
            false
        }
    }

    companion object {
        val CURATED_MODELS = listOf(
            VoiceModelInfo(
                id = "whisper_tiny_en",
                name = "Whisper Tiny (English, ~39 MB)",
                type = VoiceModelInfo.ModelType.WHISPER_TINY,
                sizeBytes = 39 * 1024 * 1024L,
                localFile = null,
                isInstalled = false,
                downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin"
            ),
            VoiceModelInfo(
                id = "whisper_base_en",
                name = "Whisper Base (English, ~74 MB)",
                type = VoiceModelInfo.ModelType.WHISPER_BASE,
                sizeBytes = 74 * 1024 * 1024L,
                localFile = null,
                isInstalled = false,
                downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin"
            ),
            VoiceModelInfo(
                id = "silero_vad_v4",
                name = "Silero VAD v4 (~1.5 MB)",
                type = VoiceModelInfo.ModelType.SILERO_VAD,
                sizeBytes = 1536 * 1024L,
                localFile = null,
                isInstalled = false,
                downloadUrl = "https://github.com/snakers4/silero-vad/raw/master/files/silero_vad.onnx"
            )
        )
    }
}
