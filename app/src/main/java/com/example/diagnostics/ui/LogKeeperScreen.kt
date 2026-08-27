package com.example.diagnostics.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.LogEntry
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogTimeFilter(val label: String, val durationMillis: Long) {
    SIX_HOURS("6h", 6 * 60 * 60 * 1000L),
    TWELVE_HOURS("12h", 12 * 60 * 60 * 1000L),
    TWENTY_FOUR_HOURS("24h", 24 * 60 * 60 * 1000L),
    ALL("All", 0L)
}

/**
 * LogKeeperScreen: Strictly matches the reference UI design.
 * Features:
 * - Top Bar: [← Back], "Log Keeper" bold title, Master ON/OFF Switch, [Copy], [Download]
 * - Segmented Filter Tabs: [ 6h ] [ 12h ] [ 24h ] [ All ]
 * - Rounded cards (#EAF0F6) with Timestamp on left, Tag on right, and clear event text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogKeeperScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isMasterEnabled by remember { mutableStateOf(LogKeeper.isEnabled()) }
    var selectedFilter by remember { mutableStateOf(LogTimeFilter.ALL) }
    var logEntries by remember { mutableStateOf<List<LogEntry>>(emptyList()) }

    fun refreshLogs() {
        logEntries = LogKeeper.getLogs(timeWindowMillis = selectedFilter.durationMillis)
    }

    LaunchedEffect(selectedFilter, isMasterEnabled) {
        refreshLogs()
    }

    // Export .txt file launcher via SAF
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    val exportContent = LogKeeper.exportToText(timeWindowMillis = selectedFilter.durationMillis)
                    os.write(exportContent.toByteArray())
                }
                LogKeeper.log(LogTag.SYSTEM, LogLevel.INFO, "Log file exported successfully")
                Toast.makeText(context, "Log file exported to device", Toast.LENGTH_SHORT).show()
                refreshLogs()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Keeper",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "Exited LogKeeper screen")
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Master On/Off Switch Toggle
                    Switch(
                        checked = isMasterEnabled,
                        onCheckedChange = { enabled ->
                            isMasterEnabled = enabled
                            LogKeeper.setEnabled(enabled)
                            refreshLogs()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Copy Button
                    IconButton(
                        onClick = {
                            val text = LogKeeper.exportToText(timeWindowMillis = selectedFilter.durationMillis)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("VianBoard Logs", text)
                            clipboard.setPrimaryClip(clip)
                            LogKeeper.log(LogTag.SYSTEM, LogLevel.INFO, "Logs copied to clipboard")
                            Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                            refreshLogs()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Download Button
                    IconButton(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            exportFileLauncher.launch("vian_board_logs_$timestamp.txt")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Time Filter Tab Bar: [ 6h ] [ 12h ] [ 24h ] [ All ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogTimeFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable {
                                selectedFilter = filter
                                LogKeeper.log(LogTag.NAVIGATION, LogLevel.DEBUG, "Filtered logs: ${filter.label}")
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = filter.label,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(3.dp)
                                        .fillMaxWidth(0.7f)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Log Cards Stream
            if (logEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!isMasterEnabled) "Logging is currently paused (Master Switch OFF)" else "No logged events in this time window",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logEntries, key = { it.id }) { entry ->
                        LogCardItem(entry = entry)
                    }
                }
            }
        }
    }
}

/**
 * LogCardItem: Exact UI card matching reference screenshot.
 * Color: #EAF0F6, rounded corners, Timestamp on left, Tag on right, message below.
 */
@Composable
fun LogCardItem(entry: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEAF0F6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Header Row: Timestamp on Left | Component Tag on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.formattedTime(),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = entry.tag.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Message Body
            Text(
                text = entry.message,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (entry.level == LogLevel.ERROR) Color(0xFFDC2626) else Color(0xFF0F172A)
            )
        }
    }
}
