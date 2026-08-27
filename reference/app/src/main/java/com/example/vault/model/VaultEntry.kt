package com.example.vault.model

import java.util.UUID

/**
 * Represents a single encrypted credential or secret record stored in the Vian Board Vault.
 */
data class VaultEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val totpSecret: String = "",
    val notes: String = "",
    val customFields: Map<String, String> = emptyMap(),
    val category: String = "General",
    val lastModified: Long = System.currentTimeMillis()
)
