package com.example.vault.kdbx

import android.content.Context
import android.net.Uri
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import com.example.vault.model.VaultDatabase
import com.example.vault.model.VaultEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * KeePass database (.kdbx) / KeePassDX import parser.
 * Reads KeePass database files or KeePass JSON/XML/CSV exported data structures.
 */
class KdbxParser(private val context: Context) {

    /**
     * Inspects a KeePass file header magic numbers (KDBX 3.x / 4.x).
     * Standard KeePass signatures:
     * Base Signature: 0x9AA2D903
     * Secondary Signature (KDBX): 0xB54BFB67 (KDBX 3.x/4.x)
     */
    fun validateKdbxHeader(inputStream: InputStream): Boolean {
        try {
            val headerBytes = ByteArray(8)
            val read = inputStream.read(headerBytes)
            if (read < 8) return false

            val buffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
            val sig1 = buffer.int.toLong() and 0xFFFFFFFFL
            val sig2 = buffer.int.toLong() and 0xFFFFFFFFL

            // 0x9AA2D903 = 2594326787L, 0xB54BFB67 = 3041655655L
            val isKeePass = (sig1 == 0x9AA2D903L && (sig2 == 0xB54BFB67L || sig2 == 0xB54BFB66L))
            LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "KeePass signature check: isKeePass=$isKeePass")
            return isKeePass
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VAULT, LogLevel.WARN, "Header inspection error: ${e.message}")
            return false
        }
    }

    /**
     * Parses a KeePass export file (JSON / KeePassDX exported records) into a VaultDatabase.
     */
    fun parseKeePassJsonExport(uri: Uri): VaultDatabase? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonObject = JSONObject(jsonString)
            val entriesArray = jsonObject.optJSONArray("entries") ?: JSONArray()
            val entriesList = ArrayList<VaultEntry>()

            for (i in 0 until entriesArray.length()) {
                val entryObj = entriesArray.getJSONObject(i)
                val entry = VaultEntry(
                    title = entryObj.optString("title", "Untitled"),
                    username = entryObj.optString("username", ""),
                    password = entryObj.optString("password", ""),
                    url = entryObj.optString("url", ""),
                    totpSecret = entryObj.optString("totpSecret", entryObj.optString("totp", "")),
                    notes = entryObj.optString("notes", ""),
                    category = entryObj.optString("group", entryObj.optString("category", "General"))
                )
                entriesList.add(entry)
            }

            LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "Parsed ${entriesList.size} entries from KeePass export")
            return VaultDatabase(
                name = jsonObject.optString("name", "KeePass Import"),
                entries = entriesList,
                isKdbxSource = true,
                sourcePath = uri.toString()
            )
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VAULT, LogLevel.ERROR, "Failed to parse KeePass export: ${e.message}")
            return null
        }
    }

    /**
     * Parses a KeePass CSV export file into a VaultDatabase.
     */
    fun parseKeePassCsvExport(uri: Uri): VaultDatabase? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))
            val entriesList = ArrayList<VaultEntry>()

            var line: String?
            var isFirstLine = true
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.isEmpty()) continue

                if (isFirstLine) {
                    isFirstLine = false
                    // Skip header line if present
                    if (currentLine.contains("Title", ignoreCase = true) || currentLine.contains("Group", ignoreCase = true)) {
                        continue
                    }
                }

                // Standard KeePass CSV columns: "Group","Title","Username","Password","URL","Notes"
                val cols = currentLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                    .map { it.trim().removeSurrounding("\"") }

                if (cols.size >= 4) {
                    val group = cols.getOrElse(0) { "General" }
                    val title = cols.getOrElse(1) { "Untitled" }
                    val username = cols.getOrElse(2) { "" }
                    val password = cols.getOrElse(3) { "" }
                    val url = cols.getOrElse(4) { "" }
                    val notes = cols.getOrElse(5) { "" }

                    entriesList.add(
                        VaultEntry(
                            title = if (title.isNotEmpty()) title else "Entry ${entriesList.size + 1}",
                            username = username,
                            password = password,
                            url = url,
                            notes = notes,
                            category = group
                        )
                    )
                }
            }

            LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "Parsed ${entriesList.size} entries from KeePass CSV")
            return VaultDatabase(
                name = "KeePass CSV Import",
                entries = entriesList,
                isKdbxSource = true,
                sourcePath = uri.toString()
            )
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VAULT, LogLevel.ERROR, "Failed to parse KeePass CSV: ${e.message}")
            return null
        }
    }
}
