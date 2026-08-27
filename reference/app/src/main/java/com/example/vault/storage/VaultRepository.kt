package com.example.vault.storage

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import com.example.vault.crypto.VaultCipher
import com.example.vault.model.VaultDatabase
import com.example.vault.model.VaultEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Repository responsible for encrypted on-disk storage, memory cache, and auto-locking timeouts.
 */
class VaultRepository(private val context: Context) {

    interface VaultStateListener {
        fun onVaultUnlocked(database: VaultDatabase)
        fun onVaultLocked()
    }

    var listener: VaultStateListener? = null

    private val vaultFile = File(context.filesDir, "vault_store.enc")
    private var unlockedDatabase: VaultDatabase? = null
    private var cachedMasterPassword: CharArray? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var autoLockTimeoutMs = 60_000L // 1 minute default
    private val lockRunnable = Runnable {
        LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "Auto-lock timer expired. Locking vault.")
        lockVault()
    }

    fun isVaultCreated(): Boolean = vaultFile.exists() && vaultFile.length() > 0

    fun isUnlocked(): Boolean = unlockedDatabase != null

    fun getUnlockedDatabase(): VaultDatabase? = unlockedDatabase

    fun setAutoLockTimeout(timeoutMs: Long) {
        this.autoLockTimeoutMs = timeoutMs
        resetAutoLockTimer()
    }

    fun resetAutoLockTimer() {
        mainHandler.removeCallbacks(lockRunnable)
        if (isUnlocked() && autoLockTimeoutMs > 0) {
            mainHandler.postDelayed(lockRunnable, autoLockTimeoutMs)
        }
    }

    /**
     * Initializes a brand new vault with master password.
     */
    fun createVault(masterPassword: CharArray): Boolean {
        val initialDb = VaultDatabase(entries = emptyList())
        val saved = saveDatabase(initialDb, masterPassword)
        if (saved) {
            unlockedDatabase = initialDb
            cachedMasterPassword = masterPassword.clone()
            resetAutoLockTimer()
            listener?.onVaultUnlocked(initialDb)
            LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "New vault created and unlocked")
        }
        return saved
    }

    /**
     * Unlocks an existing vault using the master password.
     */
    fun unlockVault(masterPassword: CharArray): Boolean {
        if (!isVaultCreated()) return false

        try {
            val encryptedBytes = vaultFile.readBytes()
            val decryptedBytes = VaultCipher.decrypt(encryptedBytes, masterPassword)

            if (decryptedBytes == null) {
                LogKeeper.log(LogTag.VAULT, LogLevel.WARN, "Vault unlock failed: Invalid master password or payload corruption")
                return false
            }

            val jsonString = String(decryptedBytes, StandardCharsets.UTF_8)
            VaultCipher.zeroWipe(decryptedBytes)

            val db = deserializeDatabase(jsonString)
            unlockedDatabase = db
            cachedMasterPassword = masterPassword.clone()
            resetAutoLockTimer()

            LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "Vault unlocked successfully (${db.entries.size} entries)")
            listener?.onVaultUnlocked(db)
            return true
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VAULT, LogLevel.ERROR, "Exception unlocking vault: ${e.message}")
            return false
        }
    }

    /**
     * Locks the vault, clearing unlocked credentials and zero-wiping password memory.
     */
    fun lockVault() {
        mainHandler.removeCallbacks(lockRunnable)
        unlockedDatabase = null
        cachedMasterPassword?.let {
            VaultCipher.zeroWipe(it)
        }
        cachedMasterPassword = null

        LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "Vault locked and memory scrubbed")
        listener?.onVaultLocked()
    }

    /**
     * Adds or updates a single entry in the unlocked database.
     */
    fun saveEntry(entry: VaultEntry): Boolean {
        val currentDb = unlockedDatabase ?: return false
        val masterPass = cachedMasterPassword ?: return false

        val updatedList = currentDb.entries.filter { it.id != entry.id }.toMutableList()
        updatedList.add(0, entry)

        val newDb = currentDb.copy(entries = updatedList)
        val success = saveDatabase(newDb, masterPass)
        if (success) {
            unlockedDatabase = newDb
            resetAutoLockTimer()
        }
        return success
    }

    /**
     * Deletes an entry by ID.
     */
    fun deleteEntry(entryId: String): Boolean {
        val currentDb = unlockedDatabase ?: return false
        val masterPass = cachedMasterPassword ?: return false

        val updatedList = currentDb.entries.filter { it.id != entryId }
        val newDb = currentDb.copy(entries = updatedList)

        val success = saveDatabase(newDb, masterPass)
        if (success) {
            unlockedDatabase = newDb
            resetAutoLockTimer()
        }
        return success
    }

    /**
     * Imports entries from an external database (e.g. KeePassDX import).
     */
    fun importExternalDatabase(externalDb: VaultDatabase): Boolean {
        val currentDb = unlockedDatabase ?: return false
        val masterPass = cachedMasterPassword ?: return false

        val mergedEntries = (externalDb.entries + currentDb.entries).distinctBy { "${it.title}_${it.username}" }
        val newDb = currentDb.copy(entries = mergedEntries)

        val success = saveDatabase(newDb, masterPass)
        if (success) {
            unlockedDatabase = newDb
            resetAutoLockTimer()
            LogKeeper.log(LogTag.VAULT, LogLevel.INFO, "Imported ${externalDb.entries.size} entries into vault")
        }
        return success
    }

    private fun saveDatabase(database: VaultDatabase, masterPassword: CharArray): Boolean {
        return try {
            val jsonString = serializeDatabase(database)
            val plainBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

            val encryptedBytes = VaultCipher.encrypt(plainBytes, masterPassword)
            VaultCipher.zeroWipe(plainBytes)

            if (encryptedBytes != null) {
                vaultFile.writeBytes(encryptedBytes)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            LogKeeper.log(LogTag.VAULT, LogLevel.ERROR, "Failed to save encrypted vault: ${e.message}")
            false
        }
    }

    private fun serializeDatabase(database: VaultDatabase): String {
        val root = JSONObject()
        root.put("version", database.version)
        root.put("name", database.name)

        val entriesArray = JSONArray()
        for (entry in database.entries) {
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("title", entry.title)
                put("username", entry.username)
                put("password", entry.password)
                put("url", entry.url)
                put("totpSecret", entry.totpSecret)
                put("notes", entry.notes)
                put("category", entry.category)
                put("lastModified", entry.lastModified)
            }
            entriesArray.put(obj)
        }
        root.put("entries", entriesArray)
        return root.toString()
    }

    private fun deserializeDatabase(jsonString: String): VaultDatabase {
        val root = JSONObject(jsonString)
        val entriesArray = root.optJSONArray("entries") ?: JSONArray()
        val entriesList = ArrayList<VaultEntry>()

        for (i in 0 until entriesArray.length()) {
            val obj = entriesArray.getJSONObject(i)
            entriesList.add(
                VaultEntry(
                    id = obj.optString("id"),
                    title = obj.optString("title"),
                    username = obj.optString("username"),
                    password = obj.optString("password"),
                    url = obj.optString("url"),
                    totpSecret = obj.optString("totpSecret"),
                    notes = obj.optString("notes"),
                    category = obj.optString("category", "General"),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                )
            )
        }

        return VaultDatabase(
            version = root.optInt("version", 1),
            name = root.optString("name", "Vian Vault"),
            entries = entriesList
        )
    }
}
