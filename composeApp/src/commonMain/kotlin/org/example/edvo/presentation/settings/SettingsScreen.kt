package org.example.edvo.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import org.example.edvo.presentation.components.EdvoButton
import org.example.edvo.presentation.components.EdvoButtonType
import org.example.edvo.presentation.components.EdvoCard
import org.example.edvo.presentation.components.EdvoScaffold
import org.example.edvo.presentation.components.EdvoTextField
import org.example.edvo.theme.EdvoColor
import org.example.edvo.presentation.designsystem.NeoSlideToAct
import org.example.edvo.presentation.designsystem.NeoSuccessOverlay
import org.example.edvo.presentation.designsystem.NeoFeedbackOverlay
import org.example.edvo.presentation.designsystem.FeedbackType
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import org.example.edvo.presentation.designsystem.NeoPasswordInput
import org.example.edvo.presentation.components.util.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onBackupClick: () -> Unit,
    onFeaturesClick: () -> Unit,
    onWipeSuccess: () -> Unit,
    viewModel: SettingsViewModel,
    state: SettingsState
) {
    BackHandler(enabled = true) { onBack() }

    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showBackupOptionsDialog by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var isExportMode by remember { mutableStateOf(true) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    // FileKit Launchers
    var pendingFile by remember { mutableStateOf<io.github.vinceglb.filekit.core.PlatformFile?>(null) }
    val exportData by viewModel.exportData.collectAsState()
    
    val screenshotsEnabled by viewModel.screenshotsEnabled.collectAsState()
    val copyPasteEnabled by viewModel.copyPasteEnabled.collectAsState()
    
    val exportLauncher = io.github.vinceglb.filekit.compose.rememberFileSaverLauncher { result -> 
        viewModel.clearExportData()
        result?.let {
            viewModel.onExportSuccess()
        } ?: run {
            // User cancelled the file picker, reset loading state
            viewModel.resetState()
        }
    }
    
    LaunchedEffect(exportData) {
        exportData?.let { bytes ->
            exportLauncher.launch(bytes = bytes, baseName = "edvo_backup", extension = "enc")
        }
    }
    
    val importLauncher = io.github.vinceglb.filekit.compose.rememberFilePickerLauncher(
        type = io.github.vinceglb.filekit.core.PickerType.File(extensions = listOf("enc")),
        mode = io.github.vinceglb.filekit.core.PickerMode.Single
    ) { result ->
        result?.let { file ->
            pendingFile = file
            isExportMode = false
            showBackupPasswordDialog = true
        }
    }

    // Feedback Overlays for Backup Operations
    // Feedback Overlays for Backup Operations
    // Derive current feedback type to ensure smooth transitions (single Dialog)
    val feedbackType = when {
        state is SettingsState.Loading -> FeedbackType.Loading
        successMessage != null -> FeedbackType.Success(successMessage!!)
        state is SettingsState.Error -> FeedbackType.Error((state as SettingsState.Error).message)
        else -> null
    }

    if (feedbackType != null) {
        NeoFeedbackOverlay(
            type = feedbackType,
            onDismiss = {
                if (feedbackType is FeedbackType.Success) {
                    successMessage = null
                }
                viewModel.resetState()
            },
            // Pass autoDismissMs only if it's a Success type
            autoDismissMs = if (feedbackType is FeedbackType.Success) 2000 else 0
        )
    }

    if (showBackupOptionsDialog) {
        AlertDialog(
            containerColor = EdvoColor.DarkSurface,
            titleContentColor = EdvoColor.White,
            textContentColor = EdvoColor.LightGray,
            onDismissRequest = { showBackupOptionsDialog = false },
            title = { Text("Backup Vault") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EdvoButton(
                        text = "Export Backup (Encrypt)",
                        onClick = { 
                            isExportMode = true
                            showBackupOptionsDialog = false
                            showBackupPasswordDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    EdvoButton(
                        text = "Import Backup (Decrypt)",
                        onClick = { 
                            showBackupOptionsDialog = false
                            importLauncher.launch()
                        },
                        type = EdvoButtonType.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBackupOptionsDialog = false }) { Text("Close", color = EdvoColor.LightGray) }
            }
        )
    }
    
    if (showBackupPasswordDialog) {
        var password by remember { mutableStateOf("") }
        val title = if (isExportMode) "Encrypt Backup" else "Decrypt Backup"
        
        AlertDialog(
            containerColor = EdvoColor.DarkSurface,
            titleContentColor = EdvoColor.White,
            textContentColor = EdvoColor.LightGray,
            onDismissRequest = { if (state !is SettingsState.Loading) showBackupPasswordDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    Text(if (isExportMode) "Set encryption password." else "Enter decryption password.")
                    Spacer(modifier = Modifier.height(16.dp))
                    NeoPasswordInput(
                        value = password,
                        onValueChange = { password = it },
                        label = "Backup Password",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                 EdvoButton(
                     text = if (isExportMode) "Export" else "Import",
                     onClick = {
                         if (password.length >= 4) {
                             if (isExportMode) {
                                  viewModel.exportBackup(password)
                                  showBackupPasswordDialog = false
                             } else {
                                 pendingFile?.let { 
                                     viewModel.importBackup(password, it)
                                     showBackupPasswordDialog = false
                                 }
                             }
                         }
                     },
                     enabled = state !is SettingsState.Loading
                 )
            },
            dismissButton = {
                TextButton(onClick = { showBackupPasswordDialog = false }) { Text("Cancel", color = EdvoColor.LightGray) }
            }
        )
    }

    if (showWipeConfirmDialog) {
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            modifier = Modifier.border(1.dp, EdvoColor.ErrorRed, AlertDialogDefaults.shape),
            containerColor = EdvoColor.DarkSurface,
            titleContentColor = EdvoColor.ErrorRed,
            textContentColor = EdvoColor.LightGray,
            onDismissRequest = { if (state !is SettingsState.Loading) showWipeConfirmDialog = false },
            title = { Text("Kill Switch Activation") },
            text = {
                Column {
                    Text(
                        "DANGER: Irreversibly delete ALL data.",
                        color = EdvoColor.ErrorRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EdvoTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Master Password",
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                         Text(error!!, color = EdvoColor.ErrorRed)
                    }
                }
            },
            confirmButton = {
                EdvoButton(
                    text = "NUKE DATA",
                    onClick = {
                         if (password.isBlank()) error = "Required" else viewModel.triggerKillSwitch(password)
                    },
                    type = EdvoButtonType.Destructive
                )
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) { Text("Cancel", color = EdvoColor.LightGray) }
            }
        )
    }

    LaunchedEffect(state) {
        when (state) {
            is SettingsState.DataWiped -> {
                showWipeConfirmDialog = false
                onWipeSuccess()
            }
            is SettingsState.Success -> {
                val s = state as SettingsState.Success
                if (s.type == OperationType.BACKUP_EXPORT || s.type == OperationType.BACKUP_IMPORT) {
                    successMessage = s.message
                }
            }
            else -> {}
        }
    }

    EdvoScaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", color = EdvoColor.White, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EdvoColor.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EdvoCard(onClick = onChangePasswordClick, modifier = Modifier.fillMaxWidth()) {
                Column {
                     Text("Change Master Password", style = MaterialTheme.typography.titleMedium, color = EdvoColor.White)
                     Text("Re-encrypt vault with new key", style = MaterialTheme.typography.bodySmall, color = EdvoColor.LightGray)
                }
            }
            
            EdvoCard(onClick = { showBackupOptionsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Column {
                     Text("Backup Vault", style = MaterialTheme.typography.titleMedium, color = EdvoColor.White)
                     Text("Export or Import encrypted database", style = MaterialTheme.typography.bodySmall, color = EdvoColor.LightGray)
                }
            }
            
            // Features Section - Now links to dedicated screen
            Text("FEATURES", style = MaterialTheme.typography.titleSmall, color = EdvoColor.LightGray)
            
            EdvoCard(onClick = onFeaturesClick, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Features & Security", style = MaterialTheme.typography.titleMedium, color = EdvoColor.White)
                    Text("Screenshots, Clipboard, Shake to Lock", style = MaterialTheme.typography.bodySmall, color = EdvoColor.LightGray)
                }
            }
            
            Text("MAINTENANCE", style = MaterialTheme.typography.titleSmall, color = EdvoColor.LightGray)
            
            val updateAvailable by viewModel.updateAvailable.collectAsState()
            val isChecking by viewModel.isCheckingUpdate.collectAsState()
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            
            EdvoCard(
                onClick = { viewModel.checkForUpdates() }, 
                modifier = Modifier.fillMaxWidth()
            ) {
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Column {
                         Text("Check for Updates", style = MaterialTheme.typography.titleMedium, color = EdvoColor.White)
                         Text(
                             if (isChecking) "Checking..." else "Current: v${org.example.edvo.getAppVersion()}", 
                             style = MaterialTheme.typography.bodySmall, 
                             color = EdvoColor.LightGray
                         )
                     }
                     if (updateAvailable != null) {
                         Text("Update!", color = NeoPaletteV2.Functional.SignalGreen, style = MaterialTheme.typography.titleSmall)
                     }
                 }
            }
            
            if (updateAvailable != null) {
                // State for dialog interaction
                val isDownloading by viewModel.isDownloading.collectAsState()
                val canAutoUpdate = remember { org.example.edvo.getUpdateCachePath() != null }
                
                AlertDialog(
                    containerColor = EdvoColor.DarkSurface,
                    titleContentColor = EdvoColor.White,
                    textContentColor = EdvoColor.LightGray,
                    onDismissRequest = { 
                        if (!isDownloading) viewModel.dismissUpdate() 
                    },
                    title = { Text(if (isDownloading) "Updating..." else "Update Available") },
                    text = { 
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isDownloading) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = NeoPaletteV2.Functional.SignalGreen
                                    )
                                }
                                Text(
                                    "Downloading update...", 
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Note: If prompted, please allow EDVO to install unknown apps from this source.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EdvoColor.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            } else {
                                Text("Version ${updateAvailable?.tag_name} is available.")
                                updateAvailable?.body?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        if (!isDownloading) {
                            if (canAutoUpdate) {
                                EdvoButton(
                                    text = "Update Now",
                                    onClick = { 
                                        updateAvailable?.let { 
                                            viewModel.downloadAndInstallUpdate(it, uriHandler::openUri) 
                                        }
                                    }
                                )
                            } else {
                                EdvoButton(
                                    text = "Download",
                                    onClick = {
                                        updateAvailable?.html_url?.let { uriHandler.openUri(it) }
                                        viewModel.dismissUpdate()
                                    }
                                )
                            }
                        }
                    },
                    dismissButton = {
                        if (!isDownloading) {
                            TextButton(onClick = { viewModel.dismissUpdate() }) { 
                                Text("Later", color = EdvoColor.LightGray) 
                            }
                        }
                    }
                )
            }
            
            // About Section
            Text("ABOUT", style = MaterialTheme.typography.titleSmall, color = EdvoColor.LightGray)
            
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Version", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                    Text("v0.5.1", style = MaterialTheme.typography.bodySmall, color = EdvoColor.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            var sliderResetKey by remember { mutableStateOf(0) }
            
            key(sliderResetKey) {
                NeoSlideToAct(
                    text = "SLIDE TO NUKE DATA",
                    onSwipeComplete = { 
                         showWipeConfirmDialog = true 
                    },
                    isDestructive = true,
                    modifier = Modifier.fillMaxWidth(),
                    onOrphaned = {}
                )
            }
            
            // If dialog is dismissed (cancelled), reset slider
            LaunchedEffect(showWipeConfirmDialog) {
                if (!showWipeConfirmDialog) {
                    sliderResetKey++
                }
            }
            
            Spacer(modifier = Modifier.height(88.dp)) // Push content above Bottom Bar


        }
    }
}
