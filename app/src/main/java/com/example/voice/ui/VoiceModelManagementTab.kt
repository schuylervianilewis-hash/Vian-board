package com.example.voice.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voice.model.VoiceModelInfo
import com.example.voice.model.VoiceModelManager

/**
 * Settings tab for managing offline neural voice models (Whisper & Silero VAD).
 * Allows SAF file imports and listing installed models without bundling models into the base APK.
 */
@Composable
fun VoiceModelManagementTab() {
    val context = LocalContext.current
    val modelManager = remember { VoiceModelManager(context) }
    var installedModels by remember { mutableStateOf(modelManager.getInstalledModels()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_model.bin"
            val imported = modelManager.importModelFromUri(uri, fileName)
            if (imported != null) {
                installedModels = modelManager.getInstalledModels()
                Toast.makeText(context, "Model imported successfully: ${imported.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to import model", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Offline Neural Voice Input", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Zero bundled models. Direct raw audio recording (16kHz PCM) with external Whisper / Silero VAD models.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Storage & SAF Import Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Import External Model", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Import .bin, .gguf, or .onnx model weights from your device storage.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Model File (SAF)")
                    }
                }
            }
        }

        // Installed Models List
        item {
            Text("Installed Models (${installedModels.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (installedModels.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No models installed. Tap 'Import Model File' above to add Whisper (.bin) or Silero VAD (.onnx) weights.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(installedModels) { model ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(model.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Type: ${model.type.name} • Size: ${model.sizeBytes / (1024 * 1024)} MB",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = {
                            modelManager.deleteModel(model)
                            installedModels = modelManager.getInstalledModels()
                            Toast.makeText(context, "Model removed", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Model", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Recommended / Curated Specifications
        item {
            Text("Compatible Model Specifications", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(VoiceModelManager.CURATED_MODELS) { curated ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(curated.name, fontWeight = FontWeight.Medium)
                    Text(
                        "Target: ${curated.type.name} • Expected: ~${curated.sizeBytes / (1024 * 1024)} MB",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
