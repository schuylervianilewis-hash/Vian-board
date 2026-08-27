package com.example.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogTag

/**
 * OnboardingManager: Tracks whether the first-time setup has been completed.
 */
class OnboardingManager(context: Context) {
    private val prefs = context.getSharedPreferences("vian_board_onboarding_prefs", Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("key_onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("key_onboarding_completed", completed).apply()
        LogKeeper.log(LogTag.SYSTEM, LogLevel.INFO, "Onboarding status updated: completed=$completed")
    }
}

/**
 * WelcomeOnboardingScreen: One-time first-launch setup flow.
 * Steps:
 * 1. Enable Vian Board in Android System Settings
 * 2. Select Vian Board as the Active/Default Input Method
 * 3. Quick access to Log Keeper for diagnostics
 * 4. Finish Setup button
 */
@Composable
fun WelcomeOnboardingScreen(
    onboardingManager: OnboardingManager,
    onOpenLogKeeper: () -> Unit,
    onSetupFinished: () -> Unit
) {
    val context = LocalContext.current
    var isEnabledInSettings by remember { mutableStateOf(false) }
    var isSelectedAsDefault by remember { mutableStateOf(false) }

    fun checkStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledList = imm.enabledInputMethodList
        val pkgName = context.packageName
        isEnabledInSettings = enabledList.any { it.packageName == pkgName }

        val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        isSelectedAsDefault = currentIme.contains(pkgName)
    }

    LaunchedEffect(Unit) {
        checkStatus()
        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "Navigated to: WelcomeOnboardingScreen")
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to Vian Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ultra-lightweight, privacy-first keyboard with zero-overhead diagnostics and desktop micro-gestures.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Steps Container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Step 1: Enable in Settings
                OnboardingStepCard(
                    stepNumber = "1",
                    title = "Enable in System Settings",
                    subtitle = if (isEnabledInSettings) "Vian Board is enabled" else "Allow Vian Board in Language & Input",
                    icon = Icons.Default.Settings,
                    isCompleted = isEnabledInSettings,
                    actionButtonText = if (isEnabledInSettings) "Enabled" else "Enable",
                    onActionClick = {
                        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "User tapped: Enable in Settings")
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    }
                )

                // Step 2: Select as Default
                OnboardingStepCard(
                    stepNumber = "2",
                    title = "Select Default Keyboard",
                    subtitle = if (isSelectedAsDefault) "Vian Board is active keyboard" else "Switch default input method to Vian Board",
                    icon = Icons.Default.Keyboard,
                    isCompleted = isSelectedAsDefault,
                    actionButtonText = if (isSelectedAsDefault) "Selected" else "Select",
                    onActionClick = {
                        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "User tapped: Select Default Keyboard")
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                    }
                )

                // Step 3: Log Keeper Preview / Diagnostics
                OnboardingStepCard(
                    stepNumber = "3",
                    title = "Log Keeper Diagnostics",
                    subtitle = "Verify live system telemetry and zero-PII privacy buffer",
                    icon = Icons.Default.ListAlt,
                    isCompleted = false,
                    actionButtonText = "Open Logs",
                    onActionClick = onOpenLogKeeper
                )
            }

            // Bottom Continue / Finish Button
            Button(
                onClick = {
                    onboardingManager.setOnboardingCompleted(true)
                    LogKeeper.log(LogTag.SYSTEM, LogLevel.INFO, "Onboarding setup completed")
                    onSetupFinished()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEnabledInSettings && isSelectedAsDefault) "Start Using Vian Board" else "Continue to Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingStepCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isCompleted: Boolean,
    actionButtonText: String,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isCompleted) {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(actionButtonText, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(actionButtonText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
