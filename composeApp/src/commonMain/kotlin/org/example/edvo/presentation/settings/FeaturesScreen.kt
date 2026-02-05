package org.example.edvo.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.components.EdvoCard
import org.example.edvo.presentation.components.EdvoScaffold
import org.example.edvo.presentation.components.util.BackHandler
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import org.example.edvo.theme.EdvoColor
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import org.example.edvo.presentation.designsystem.NeoInput
import org.example.edvo.presentation.designsystem.NeoPasswordInput
import org.example.edvo.presentation.designsystem.SmartButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    onShakeConfigClick: () -> Unit = {}
) {
    BackHandler(enabled = true) { onBack() }
    
    val screenshotsEnabled by viewModel.screenshotsEnabled.collectAsState()
    val copyPasteEnabled by viewModel.copyPasteEnabled.collectAsState()
    val shakeToLockEnabled by viewModel.shakeToLockEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val shakeToKillEnabled by viewModel.shakeToKillEnabled.collectAsState()
    
    // Password verification dialog state for biometric enable
    var showBiometricPasswordDialog by remember { mutableStateOf(false) }
    var biometricPassword by remember { mutableStateOf("") }
    var biometricPasswordError by remember { mutableStateOf<String?>(null) }
    var isVerifyingPassword by remember { mutableStateOf(false) }
    
    // Biometric Authenticator for key enrollment/confirmation
    val biometricAuthenticator = remember { org.example.edvo.presentation.auth.BiometricAuthenticator() }

    EdvoScaffold(
        topBar = {
            TopAppBar(
                title = { Text("FEATURES", color = EdvoColor.White, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EdvoColor.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EdvoColor.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screenshots Toggle
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Screenshots", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                        Text(
                            if (screenshotsEnabled) "Allowed" else "Blocked (Secure)", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = if (screenshotsEnabled) EdvoColor.ErrorRed else EdvoColor.Primary
                        )
                    }
                    Switch(
                        checked = screenshotsEnabled,
                        onCheckedChange = { viewModel.toggleScreenshots(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                            uncheckedThumbColor = EdvoColor.LightGray,
                            uncheckedTrackColor = EdvoColor.Surface,
                            uncheckedBorderColor = EdvoColor.LightGray
                        )
                    )
                }
            }
            
            // Clipboard Toggle
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clipboard (Copy/Paste)", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                        Text(
                            if (copyPasteEnabled) "Enabled" else "Disabled", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = EdvoColor.LightGray
                        )
                    }
                    Switch(
                        checked = copyPasteEnabled,
                        onCheckedChange = { viewModel.toggleCopyPaste(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                            uncheckedThumbColor = EdvoColor.LightGray,
                            uncheckedTrackColor = EdvoColor.Surface,
                            uncheckedBorderColor = EdvoColor.LightGray
                        )
                    )
                }
            }
            
            // Shake to Lock Toggle
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shake to Lock", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                            Text(
                                if (shakeToLockEnabled) "Enabled" else "Disabled", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = if (shakeToLockEnabled) NeoPaletteV2.Functional.SignalGreen else EdvoColor.LightGray
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Config Button (Visible only when enabled)
                            if (shakeToLockEnabled) {
                                IconButton(onClick = onShakeConfigClick) {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = "Configure Sensitivity",
                                        tint = NeoPaletteV2.Functional.SignalGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Spacer
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Switch(
                                checked = shakeToLockEnabled,
                                onCheckedChange = { viewModel.toggleShakeToLock(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                                    uncheckedThumbColor = EdvoColor.LightGray,
                                    uncheckedTrackColor = EdvoColor.Surface,
                                    uncheckedBorderColor = EdvoColor.LightGray
                                )
                            )
                        }
                    }
                    
                    if (shakeToLockEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "▲ Shake device firmly to lock. Configure sensitivity via settings icon.",
                            style = MaterialTheme.typography.labelSmall,
                            color = EdvoColor.LightGray.copy(alpha = 0.6f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
            
            // Shake to Kill Toggle (Nested under Shake to Lock)
            if (shakeToLockEnabled) {
                EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shake to Kill App", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                            Text(
                                if (shakeToKillEnabled) "Force-close on shake" else "Lock on shake (default)", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = if (shakeToKillEnabled) NeoPaletteV2.Functional.Warning else EdvoColor.LightGray
                            )
                        }
                        Switch(
                            checked = shakeToKillEnabled,
                            onCheckedChange = { viewModel.toggleShakeToKill(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeoPaletteV2.Functional.Warning,
                                uncheckedThumbColor = EdvoColor.LightGray,
                                uncheckedTrackColor = EdvoColor.Surface,
                                uncheckedBorderColor = EdvoColor.LightGray
                            )
                        )
                    }
                }
            }
            
            // Biometric Unlock Toggle
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Biometric Unlock", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                        Text(
                            if (biometricEnabled) "Fingerprint enabled" else "Disabled", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = if (biometricEnabled) NeoPaletteV2.Functional.SignalGreen else EdvoColor.LightGray
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Show password verification dialog
                                showBiometricPasswordDialog = true
                            } else {
                                biometricAuthenticator.clearCredentials()  // Remove Keystore key + encrypted data
                                viewModel.disableBiometricUnlock()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                            uncheckedThumbColor = EdvoColor.LightGray,
                            uncheckedTrackColor = EdvoColor.Surface,
                            uncheckedBorderColor = EdvoColor.LightGray
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(88.dp)) // Clear bottom nav
        }
    }
    
    // Password Verification Dialog for Biometric Enable
    if (showBiometricPasswordDialog) {
        AlertDialog(
            containerColor = EdvoColor.DarkSurface,
            titleContentColor = EdvoColor.White,
            textContentColor = EdvoColor.LightGray,
            onDismissRequest = {
                showBiometricPasswordDialog = false
                biometricPassword = ""
                biometricPasswordError = null
            },
            title = { Text("Enable Biometric Unlock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter your master password to enable fingerprint unlock.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    NeoPasswordInput(
                        value = biometricPassword,
                        onValueChange = { 
                            biometricPassword = it
                            biometricPasswordError = null
                        },
                        label = "Master Password",
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (biometricPasswordError != null) {
                        Text(
                            biometricPasswordError!!,
                            color = NeoPaletteV2.Functional.SignalRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                SmartButton(
                    text = if (isVerifyingPassword) "Verifying..." else "Enable",
                    onClick = {
                        isVerifyingPassword = true
                        viewModel.verifyPassword(
                            password = biometricPassword,
                            onSuccess = {
                                // Password verified. Now store the key and prompt fingerprint to confirm.
                                isVerifyingPassword = false
                                showBiometricPasswordDialog = false
                                
                                // Store the master key (session is active from password verification)
                                val masterKey = org.example.edvo.core.session.SessionManager.getMasterKey()
                                if (masterKey != null) {
                                    // Prompt user to Confirm with Fingerprint + Encrypt Key
                                    biometricAuthenticator.enableBiometric(
                                        masterKey = masterKey,
                                        onSuccess = {
                                            viewModel.enableBiometricUnlock()
                                            biometricPassword = ""
                                            biometricPasswordError = null
                                        },
                                        onError = { error ->
                                            biometricPasswordError = error
                                        },
                                        onCancel = {
                                            biometricPassword = ""
                                            biometricPasswordError = null
                                        }
                                    )
                                } else {
                                    // No session active - shouldn't happen after password verify
                                    biometricPassword = ""
                                    biometricPasswordError = null
                                }
                            },
                            onError = { error ->
                                isVerifyingPassword = false
                                biometricPasswordError = error
                            }
                        )
                    },
                    modifier = Modifier.width(130.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showBiometricPasswordDialog = false
                    biometricPassword = ""
                    biometricPasswordError = null
                }) {
                    Text("Cancel", color = EdvoColor.LightGray)
                }
            }
        )
    }
}
