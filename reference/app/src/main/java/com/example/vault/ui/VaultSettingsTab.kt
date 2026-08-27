package com.example.vault.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vault.kdbx.KdbxParser
import com.example.vault.model.VaultDatabase
import com.example.vault.model.VaultEntry
import com.example.vault.storage.VaultRepository
import com.example.vault.totp.TotpGenerator

/**
 * Vault management tab in Vian Board Settings.
 * Supports master password creation, entry CRUD, and KeePassDX database imports.
 */
@Composable
fun VaultSettingsTab() {
    val context = LocalContext.current
    val repository = remember { VaultRepository(context) }
    val kdbxParser = remember { KdbxParser(context) }

    var isVaultCreated by remember { mutableStateOf(repository.isVaultCreated()) }
    var isUnlocked by remember { mutableStateOf(repository.isUnlocked()) }
    var unlockedDb by remember { mutableStateOf(repository.getUnlockedDatabase()) }

    var masterPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showAddEntryDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && isUnlocked) {
            val db = kdbxParser.parseKeePassJsonExport(uri) ?: kdbxParser.parseKeePassCsvExport(uri)
            if (db != null) {
                repository.importExternalDatabase(db)
                unlockedDb = repository.getUnlockedDatabase()
                Toast.makeText(context, "Imported ${db.entries.size} entries from KeePass", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not parse file. Ensure it is a valid KeePass JSON or CSV.", Toast.LENGTH_LONG).show()
            }
        } else if (!isUnlocked) {
            Toast.makeText(context, "Unlock vault first before importing", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        val listener = object : VaultRepository.VaultStateListener {
            override fun onVaultUnlocked(database: VaultDatabase) {
                isUnlocked = true
                unlockedDb = database
            }

            override fun onVaultLocked() {
                isUnlocked = false
                unlockedDb = null
            }
        }
        repository.listener = listener
        onDispose {
            repository.listener = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("In-Keyboard Security Vault", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "AES-256-GCM encrypted local vault with zero-clipboard secret injection and KeePassDX import compatibility.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Vault Lock / Setup Status
        if (!isVaultCreated) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Create Master Password", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Set a strong master password to initialize your encrypted vault.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = masterPasswordInput,
                            onValueChange = { masterPasswordInput = it },
                            label = { Text("Master Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = { confirmPasswordInput = it },
                            label = { Text("Confirm Master Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (masterPasswordInput.isNotEmpty() && masterPasswordInput == confirmPasswordInput) {
                                    val success = repository.createVault(masterPasswordInput.toCharArray())
                                    if (success) {
                                        isVaultCreated = true
                                        isUnlocked = true
                                        unlockedDb = repository.getUnlockedDatabase()
                                        Toast.makeText(context, "Vault initialized", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Passwords do not match or are empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Initialize Vault")
                        }
                    }
                }
            }
        } else if (!isUnlocked) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Unlock Vault", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Enter your master password to manage entries.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = masterPasswordInput,
                            onValueChange = { masterPasswordInput = it },
                            label = { Text("Master Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val success = repository.unlockVault(masterPasswordInput.toCharArray())
                                if (success) {
                                    isUnlocked = true
                                    unlockedDb = repository.getUnlockedDatabase()
                                    masterPasswordInput = ""
                                } else {
                                    Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unlock Vault")
                        }
                    }
                }
            }
        } else {
            // Unlocked state controls
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vault Unlocked", fontWeight = FontWeight.Bold)
                            Text("${unlockedDb?.entries?.size ?: 0} stored entries", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { repository.lockVault() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lock")
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddEntryDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Entry")
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import KeePass")
                    }
                }
            }

            // Entries List
            val entries = unlockedDb?.entries ?: emptyList()
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No entries in vault. Tap 'Add Entry' or 'Import KeePass'.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(entries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    if (entry.username.isNotEmpty()) {
                                        Text("User: ${entry.username}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                IconButton(onClick = {
                                    repository.deleteEntry(entry.id)
                                    unlockedDb = repository.getUnlockedDatabase()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            if (entry.totpSecret.isNotEmpty()) {
                                val code = TotpGenerator.generateCurrentCode(entry.totpSecret)
                                if (code != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("2FA TOTP: $code", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEntryDialog) {
        AddEntryDialog(
            onDismiss = { showAddEntryDialog = false },
            onAdd = { newEntry ->
                repository.saveEntry(newEntry)
                unlockedDb = repository.getUnlockedDatabase()
                showAddEntryDialog = false
                Toast.makeText(context, "Entry saved", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AddEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (VaultEntry) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var totpSecret by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vault Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL / Domain") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = totpSecret, onValueChange = { totpSecret = it }, label = { Text("TOTP Secret (Base32)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotEmpty()) {
                    onAdd(
                        VaultEntry(
                            title = title,
                            username = username,
                            password = password,
                            url = url,
                            totpSecret = totpSecret,
                            notes = notes
                        )
                    )
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
