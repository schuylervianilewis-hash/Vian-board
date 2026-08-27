package com.example.backup

import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Result data extracted from a legacy HeliBoard backup file (.zip or .json).
 */
data class HeliBoardBackupResult(
    val isValid: Boolean,
    val extractedSettingsCount: Int,
    val enabledLocales: List<String>,
    val customKeysMap: Map<String, Any>,
    val rawJson: String?
)

/**
 * Lossless parser for legacy HeliBoard backup archives (.zip) and JSON preference files.
 */
object HeliBoardBackupParser {

    /**
     * Parses a HeliBoard backup JSON string.
     */
    fun parseJsonString(jsonString: String): HeliBoardBackupResult {
        return try {
            val json = JSONObject(jsonString)
            val locales = mutableListOf<String>()
            val customKeys = mutableMapOf<String, Any>()

            if (json.has("enabled_locales")) {
                val locArray = json.getJSONArray("enabled_locales")
                for (i in 0 until locArray.length()) {
                    locales.add(locArray.getString(i))
                }
            }

            val keysIterator = json.keys()
            var count = 0
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                customKeys[key] = json.get(key)
                count++
            }

            HeliBoardBackupResult(
                isValid = true,
                extractedSettingsCount = count,
                enabledLocales = locales,
                customKeysMap = customKeys,
                rawJson = jsonString
            )
        } catch (e: Exception) {
            HeliBoardBackupResult(
                isValid = false,
                extractedSettingsCount = 0,
                enabledLocales = emptyList(),
                customKeysMap = emptyMap(),
                rawJson = null
            )
        }
    }

    /**
     * Extracts and parses HeliBoard backup from a .zip stream.
     */
    fun parseZipStream(inputStream: InputStream): HeliBoardBackupResult {
        return try {
            val zis = ZipInputStream(inputStream)
            var entry = zis.nextEntry
            var jsonContent: String? = null

            while (entry != null) {
                if (entry.name.endsWith(".json") || entry.name == "preferences") {
                    jsonContent = zis.bufferedReader().readText()
                    break
                }
                entry = zis.nextEntry
            }
            zis.close()

            if (jsonContent != null) {
                parseJsonString(jsonContent)
            } else {
                HeliBoardBackupResult(false, 0, emptyList(), emptyMap(), null)
            }
        } catch (e: Exception) {
            HeliBoardBackupResult(false, 0, emptyList(), emptyMap(), null)
        }
    }
}
