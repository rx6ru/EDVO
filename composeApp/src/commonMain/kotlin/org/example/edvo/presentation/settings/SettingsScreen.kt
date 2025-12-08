package org.example.edvo.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Warning // Added import for Warning icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onBackupClick: () -> Unit,
    onWipeSuccess: () -> Unit, // Callback for navigation after wipe
    viewModel: SettingsViewModel, // Added new parameter
    state: SettingsState // Added new parameter
) {
    var showWipeConfirmDialog by remember { mutableStateOf(false) } // Added state for dialog
    // Backup Options Dialog
    var showBackupOptionsDialog by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var isExportMode by remember { mutableStateOf(true) } // true = export, false = import
    
    // FileKit Launchers
    var pendingFile by remember { mutableStateOf<io.github.vinceglb.filekit.core.PlatformFile?>(null) }
    
    // Observable Export Data
    val exportData by viewModel.exportData.collectAsState()
    
    val exportLauncher = io.github.vinceglb.filekit.compose.rememberFileSaverLauncher { result ->
        // Cleanup after save attempt (successful or cancelled)
        viewModel.clearExportData()
    }
    
    // Launch saver when data is ready
    LaunchedEffect(exportData) {
        exportData?.let { bytes ->
            exportLauncher.launch(
                bytes = bytes,
                baseName = "edvo_backup", 
                extension = "enc"
            )
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

    if (showBackupOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showBackupOptionsDialog = false },
            title = { Text("Backup Vault") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            // Export Flow: 1. Close Option Dialog 2. Open Password Dialog
                            // 3. Confirm Password -> VM Prepare -> VM State Update -> LaunchedEffect -> Launcher
                            isExportMode = true
                            showBackupOptionsDialog = false
                            showBackupPasswordDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Backup")
                    }
                    OutlinedButton(
                        onClick = { 
                            showBackupOptionsDialog = false
                            importLauncher.launch()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import Backup")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBackupOptionsDialog = false }) { Text("Close") }
            }
        )
    }
    
    if (showBackupPasswordDialog) {
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        val title = if (isExportMode) "Encrypt Backup" else "Decrypt Backup"
        
        AlertDialog(
            onDismissRequest = { if (state !is SettingsState.Submitting) showBackupPasswordDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    Text(if (isExportMode) "Set a password to encrypt this backup file." else "Enter the password to decrypt the backup file.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Backup Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                 Button(
                     onClick = {
                         if (password.length < 4) {
                             error = "Password too short" 
                         } else {
                             if (isExportMode) {
                                  // Export: Prepare logic
                                  viewModel.exportBackup(password)
                                  showBackupPasswordDialog = false
                             } else {
                                 // Import: Logic needs file
                                 val file = pendingFile
                                 if (file != null) {
                                     viewModel.importBackup(password, file)
                                     showBackupPasswordDialog = false
                                 } else {
                                     error = "File lost"
                                 }
                             }
                         }
                     },
                     enabled = state !is SettingsState.Submitting
                 ) {
                     Text(if (isExportMode) "Export" else "Import")
                 }
            },
            dismissButton = {
                TextButton(onClick = { showBackupPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    // Kill Switch Confirmation Dialog
    if (showWipeConfirmDialog) {
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { if (state !is SettingsState.Submitting) showWipeConfirmDialog = false },
            title = { Text("Kill Switch Activation") },
            text = {
                Column {
                    Text(
                        "DANGER: This will permanently delete ALL data (notes, keys, settings). This action cannot be undone.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Enter Master Password to confirm:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Master Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = error != null
                    )
                    if (error != null) {
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                         if (password.isBlank()) {
                             error = "Password required"
                         } else {
                             viewModel.triggerKillSwitch(password)
                         }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = state !is SettingsState.Submitting
                ) {
                    Text("NUKE DATA")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Effect to handle navigation on success
    LaunchedEffect(state) {
        if (state is SettingsState.DataWiped) {
            showWipeConfirmDialog = false
            onWipeSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                         // I will use text button or look for Icon import being present
                         // Previous file had ArrowBack import. I should reuse or re-add it.
                         // But use simple text for now as fallback is safe.
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // Changed to use Icon
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Button 1: Change Master Password
            Button(
                onClick = onChangePasswordClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                        Text("Change Master Password", style = MaterialTheme.typography.titleMedium)
                        Text("Re-encrypt vault with new key", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // Button 2: Backup Data
            OutlinedButton(
                onClick = { showBackupOptionsDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = true,
                contentPadding = PaddingValues(16.dp)
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                        Text("Backup Vault", style = MaterialTheme.typography.titleMedium)
                        Text("Export or Import encrypted database", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            // Button 3: Kill Switch
            Button(
                onClick = { showWipeConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                contentPadding = PaddingValues(16.dp)
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                        Text("Kill Switch", style = MaterialTheme.typography.titleMedium)
                        Text("Nuclear Option: Irreversibly delete everything", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
