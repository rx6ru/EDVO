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
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.components.EdvoButton
import org.example.edvo.presentation.components.EdvoButtonType
import org.example.edvo.presentation.components.EdvoCard
import org.example.edvo.presentation.components.EdvoScaffold
import org.example.edvo.presentation.components.EdvoTextField
import org.example.edvo.theme.EdvoColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onBackupClick: () -> Unit,
    onWipeSuccess: () -> Unit,
    viewModel: SettingsViewModel,
    state: SettingsState
) {
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showBackupOptionsDialog by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var isExportMode by remember { mutableStateOf(true) }
    
    // FileKit Launchers
    var pendingFile by remember { mutableStateOf<io.github.vinceglb.filekit.core.PlatformFile?>(null) }
    val exportData by viewModel.exportData.collectAsState()
    
    val exportLauncher = io.github.vinceglb.filekit.compose.rememberFileSaverLauncher { 
        viewModel.clearExportData()
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

    // Dialogs
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
            onDismissRequest = { if (state !is SettingsState.Submitting) showBackupPasswordDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    Text(if (isExportMode) "Set encryption password." else "Enter decryption password.")
                    Spacer(modifier = Modifier.height(16.dp))
                    EdvoTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Backup Password",
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
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
                     enabled = state !is SettingsState.Submitting
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
            containerColor = EdvoColor.DarkSurface,
            titleContentColor = EdvoColor.ErrorRed,
            textContentColor = EdvoColor.LightGray,
            onDismissRequest = { if (state !is SettingsState.Submitting) showWipeConfirmDialog = false },
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
        if (state is SettingsState.DataWiped) {
            showWipeConfirmDialog = false
            onWipeSuccess()
        }
    }

    EdvoScaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", color = EdvoColor.White, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EdvoColor.Background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EdvoColor.White)
                    }
                }
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
            
            Spacer(modifier = Modifier.weight(1f))
            
            EdvoButton(
                text = "KILL SWITCH",
                onClick = { showWipeConfirmDialog = true },
                type = EdvoButtonType.Destructive,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
