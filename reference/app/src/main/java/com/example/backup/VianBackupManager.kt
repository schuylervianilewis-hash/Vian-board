package com.example.backup

import android.content.Context
import android.net.Uri
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import com.example.settings.CustomShortcutsManager
import com.example.settings.KeyboardSettings
import com.example.settings.KeyboardSettingsManager
import com.example.vault.crypto.VaultCipher
import com.example.vault.model.VaultDatabase
import com.example.vault.storage.VaultRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Modular ZIP-based backup engine for Vian Board.
 * Supports granular section toggles, password-gated security vault encryption,
 * and HeliBoard ZIP backup auto-detection & migration.
 */
class VianBackupManager(private val context: Context) {

    data class BackupSelection(
        val includeSettings: Boolean = true,
        val includeShortcuts: Boolean = true,
        val includeVoiceConfig: Boolean = true,
        val includePersonalVault: Boolean = false,
        val includeSecurityVault: Boolean = false,
        val vaultPassword: CharArray? = null
    )

    data class ZipInspectionResult(
        val isVianBoardBackup: Boolean = false,
        val isHeliBoardBackup: Boolean = false,
        val timestamp: String? = null,
        val hasSettings: Boolean = false,
        val hasShortcuts: Boolean = false,
        val hasVoiceConfig: Boolean = false,
        val hasPersonalVault: Boolean = false,
        val hasSecurityVault: Boolean = false,
        val filesFound: List<String> = emptyList()
    )

    private val settingsManager = KeyboardSettingsManager(context)
    private val shortcutsManager = CustomShortcutsManager(context)
    private val vaultRepository = VaultRepository(context)

    /**
     * Inspects a ZIP archive from URI to determine contents and compatibility.
     */
    fun inspectZipBackup(uri: Uri): ZipInspectionResult? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val zipIn = ZipInputStream(inputStream)
            val filesFound = mutableListOf<String>()

            var entry: ZipEntry? = zipIn.nextEntry
            var manifestContent: String? = null
            var hasPreferencesJson = false

            while (entry != null) {
                val name = entry.name
                filesFound.add(name)

                if (name == "manifest.json") {
                    manifestContent = readStreamText(zipIn)
                } else if (name.contains("preferences.json", ignoreCase = true) || name.contains("shared_prefs", ignoreCase = true)) {
                    hasPreferencesJson = true
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()

            if (manifestContent != null) {
                val manifest = JSONObject(manifestContent)
                val sections = manifest.optJSONObject("sections") ?: JSONObject()
                ZipInspectionResult(
                    isVianBoardBackup = true,
                    timestamp = manifest.optString("timestamp", "Unknown"),
                    hasSettings = sections.optBoolean("settings", filesFound.contains("settings.json")),
                    hasShortcuts = sections.optBoolean("shortcuts", filesFound.contains("shortcuts.json")),
                    hasVoiceConfig = sections.optBoolean("voice_config", filesFound.contains("voice_config.json")),
                    hasPersonalVault = sections.optBoolean("personal_vault", filesFound.contains("personal_vault.enc")),
                    hasSecurityVault = sections.optBoolean("security_vault", filesFound.contains("security_vault.enc")),
                    filesFound = filesFound
                )
            } else if (hasPreferencesJson || filesFound.any { it.endsWith(".json") || it.endsWith(".txt") }) {
                // HeliBoard backup format detection
                ZipInspectionResult(
                    isHeliBoardBackup = true,
                    hasSettings = hasPreferencesJson,
                    hasShortcuts = filesFound.any { it.contains("dictionary", ignoreCase = true) || it.contains("shortcuts", ignoreCase = true) },
                    filesFound = filesFound
                )
            } else {
                ZipInspectionResult(filesFound = filesFound)
            }
        } catch (e: Exception) {
            LogKeeper.log(LogTag.BACKUP, LogLevel.ERROR, "Inspection failed: ${e.message}")
            null
        }
    }

    /**
     * Exports selected modules into a ZIP archive written to destination OutputStream.
     */
    fun createModularZipBackup(selection: BackupSelection, outputStream: OutputStream): Boolean {
        var zipOut: ZipOutputStream? = null
        try {
            zipOut = ZipOutputStream(outputStream)

            val manifest = JSONObject().apply {
                put("app", "Vian Board")
                put("version", "1.0")
                put("format", "modular_zip")
                put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()))

                val secObj = JSONObject().apply {
                    put("settings", selection.includeSettings)
                    put("shortcuts", selection.includeShortcuts)
                    put("voice_config", selection.includeVoiceConfig)
                    put("personal_vault", selection.includePersonalVault)
                    put("security_vault", selection.includeSecurityVault)
                }
                put("sections", secObj)
            }

            // 1. Write manifest.json
            writeZipEntry(zipOut, "manifest.json", manifest.toString(2).toByteArray(StandardCharsets.UTF_8))

            // 2. Settings
            if (selection.includeSettings) {
                val s = settingsManager.loadSettings()
                val settingsJson = JSONObject().apply {
                    put("heightScale", s.heightScale.toDouble())
                    put("bottomInsetPaddingDp", s.bottomInsetPaddingDp)
                    put("showNumberRow", s.showNumberRow)
                    put("hapticFeedbackEnabled", s.hapticFeedbackEnabled)
                    put("soundOnKeyPress", s.soundOnKeyPress)
                    put("smartMultiplyMorph", s.smartMultiplyMorph)
                    put("doubleSpacePeriod", s.doubleSpacePeriod)
                    put("currencySymbol", s.currencySymbol)
                }
                writeZipEntry(zipOut, "settings.json", settingsJson.toString(2).toByteArray(StandardCharsets.UTF_8))
            }

            // 3. Shortcuts
            if (selection.includeShortcuts) {
                val sc = shortcutsManager.loadShortcuts()
                val arr = JSONArray()
                sc.forEach { arr.put(it.id) }
                val scObj = JSONObject().apply { put("shortcuts", arr) }
                writeZipEntry(zipOut, "shortcuts.json", scObj.toString(2).toByteArray(StandardCharsets.UTF_8))
            }

            // 4. Voice Config
            if (selection.includeVoiceConfig) {
                val voiceObj = JSONObject().apply {
                    put("engine", "whisper_cpp")
                    put("sampleRate", 16000)
                    put("vadSensitivity", 0.6)
                }
                writeZipEntry(zipOut, "voice_config.json", voiceObj.toString(2).toByteArray(StandardCharsets.UTF_8))
            }

            // 5. Personal Vault (Placeholder)
            if (selection.includePersonalVault) {
                val pObj = JSONObject().apply { put("type", "personal_vault_placeholder") }
                writeZipEntry(zipOut, "personal_vault.enc", pObj.toString().toByteArray(StandardCharsets.UTF_8))
            }

            // 6. Security Vault (Password Gated)
            if (selection.includeSecurityVault && selection.vaultPassword != null) {
                val vaultFile = File(context.filesDir, "vault_store.enc")
                if (vaultFile.exists()) {
                    writeZipEntry(zipOut, "security_vault.enc", vaultFile.readBytes())
                }
            }

            zipOut.finish()
            LogKeeper.log(LogTag.BACKUP, LogLevel.INFO, "Modular ZIP backup created successfully")
            return true
        } catch (e: Exception) {
            LogKeeper.log(LogTag.BACKUP, LogLevel.ERROR, "Failed to create ZIP backup: ${e.message}")
            return false
        } finally {
            try { zipOut?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Restores selected components from a Vian Board or HeliBoard ZIP archive.
     */
    fun restoreFromZipBackup(
        uri: Uri,
        restoreSettings: Boolean,
        restoreShortcuts: Boolean,
        restoreVoice: Boolean,
        restoreVault: Boolean,
        vaultPassword: CharArray? = null
    ): Boolean {
        var zipIn: ZipInputStream? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            zipIn = ZipInputStream(inputStream)

            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                val name = entry.name

                when {
                    name == "settings.json" && restoreSettings -> {
                        val text = readStreamText(zipIn)
                        restoreSettingsJson(text)
                    }
                    name == "shortcuts.json" && restoreShortcuts -> {
                        val text = readStreamText(zipIn)
                        restoreShortcutsJson(text)
                    }
                    name == "security_vault.enc" && restoreVault && vaultPassword != null -> {
                        val payload = readStreamBytes(zipIn)
                        // Verify decryption before saving
                        val decrypted = VaultCipher.decrypt(payload, vaultPassword)
                        if (decrypted != null) {
                            VaultCipher.zeroWipe(decrypted)
                            val vaultFile = File(context.filesDir, "vault_store.enc")
                            vaultFile.writeBytes(payload)
                            LogKeeper.log(LogTag.BACKUP, LogLevel.INFO, "Security vault restored successfully")
                        } else {
                            LogKeeper.log(LogTag.BACKUP, LogLevel.WARN, "Vault password failed during restore")
                        }
                    }
                    // HeliBoard translation
                    name.contains("preferences.json", ignoreCase = true) && restoreSettings -> {
                        val text = readStreamText(zipIn)
                        restoreHeliBoardPreferences(text)
                    }
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }

            LogKeeper.log(LogTag.BACKUP, LogLevel.INFO, "ZIP restore completed")
            return true
        } catch (e: Exception) {
            LogKeeper.log(LogTag.BACKUP, LogLevel.ERROR, "Restore from ZIP failed: ${e.message}")
            return false
        } finally {
            try { zipIn?.close() } catch (_: Exception) {}
        }
    }

    private fun restoreSettingsJson(jsonString: String) {
        val sObj = JSONObject(jsonString)
        val current = settingsManager.loadSettings()
        val restored = current.copy(
            heightScale = sObj.optDouble("heightScale", current.heightScale.toDouble()).toFloat(),
            bottomInsetPaddingDp = sObj.optInt("bottomInsetPaddingDp", current.bottomInsetPaddingDp),
            showNumberRow = sObj.optBoolean("showNumberRow", current.showNumberRow),
            hapticFeedbackEnabled = sObj.optBoolean("hapticFeedbackEnabled", current.hapticFeedbackEnabled),
            soundOnKeyPress = sObj.optBoolean("soundOnKeyPress", current.soundOnKeyPress),
            smartMultiplyMorph = sObj.optBoolean("smartMultiplyMorph", current.smartMultiplyMorph),
            doubleSpacePeriod = sObj.optBoolean("doubleSpacePeriod", current.doubleSpacePeriod),
            currencySymbol = sObj.optString("currencySymbol", current.currencySymbol)
        )
        settingsManager.saveSettings(restored)
    }

    private fun restoreShortcutsJson(jsonString: String) {
        val root = JSONObject(jsonString)
        val array = root.optJSONArray("shortcuts") ?: return
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        val items = list.mapNotNull { id ->
            CustomShortcutsManager.AVAILABLE_SHORTCUTS.firstOrNull { it.id == id }
        }
        if (items.isNotEmpty()) {
            shortcutsManager.saveShortcuts(items)
        }
    }

    private fun restoreHeliBoardPreferences(jsonString: String) {
        try {
            val root = JSONObject(jsonString)
            val current = settingsManager.loadSettings()
            val restored = current.copy(
                hapticFeedbackEnabled = root.optBoolean("vibrate_on_keypress", current.hapticFeedbackEnabled),
                soundOnKeyPress = root.optBoolean("sound_on_keypress", current.soundOnKeyPress),
                showNumberRow = root.optBoolean("show_number_row", current.showNumberRow)
            )
            settingsManager.saveSettings(restored)
            LogKeeper.log(LogTag.BACKUP, LogLevel.INFO, "Imported HeliBoard preferences successfully")
        } catch (e: Exception) {
            LogKeeper.log(LogTag.BACKUP, LogLevel.WARN, "HeliBoard preference mapping note: ${e.message}")
        }
    }

    private fun writeZipEntry(zipOut: ZipOutputStream, entryName: String, data: ByteArray) {
        val entry = ZipEntry(entryName)
        zipOut.putNextEntry(entry)
        zipOut.write(data)
        zipOut.closeEntry()
    }

    private fun readStreamText(stream: InputStream): String {
        val writer = StringWriter()
        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val buffer = CharArray(1024)
        var n: Int
        while (reader.read(buffer).also { n = it } != -1) {
            writer.write(buffer, 0, n)
        }
        return writer.toString()
    }

    private fun readStreamBytes(stream: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var n: Int
        while (stream.read(buffer).also { n = it } != -1) {
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }
}
