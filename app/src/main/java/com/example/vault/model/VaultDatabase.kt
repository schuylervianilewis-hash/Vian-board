package com.example.vault.model

/**
 * In-memory container holding unlocked credentials.
 */
data class VaultDatabase(
    val version: Int = 1,
    val name: String = "Vian Vault",
    val entries: List<VaultEntry> = emptyList(),
    val isKdbxSource: Boolean = false,
    val sourcePath: String? = null
)
