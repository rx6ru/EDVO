package org.example.edvo.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onLogoutRequired: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    
    // Handle State Changes
    LaunchedEffect(state) {
        if (state is SettingsState.Success) {
            viewModel.resetState()
            onLogoutRequired()
        }
    }
    
    val isLoading = state is SettingsState.Submitting
    val vmError = (state as? SettingsState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rotate Encryption Key") },
                navigationIcon = {
                     // Back navigation should be disabled during loading? 
                     // User said "Warning: do not close app". 
                     // If loading, maybe disable back?
                     // Standard practice: if isLoading, disable interactions.
                     // But we can leave back enabled if we haven't started transaction yet.
                     // Once started (Submitting), we should block.
                    IconButton(onClick = onBack, enabled = !isLoading) {
                        Text("Back") // Placeholder Icon or text? User said "Back Navigation Icon" for Settings.
                        // For this screen, implies standard back behavior.
                        // I'll use standard ArrowBack text/icon later or if I have clean import.
                        // I'll stick to text for safety or standard icon if imported in file.
                        // Let's use simple Text "Back" or Icon if I add imports. 
                        // I'll add imports for Icons.
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "WARNING: Do not close the app or turn off your device during this process. Re-encrypting your vault may take a moment.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            
            HorizontalDivider()

            if (vmError != null) {
                Text(vmError, color = MaterialTheme.colorScheme.error)
            }
            if (errorText != null) {
                 Text(errorText!!, color = MaterialTheme.colorScheme.error)
            }
            
            OutlinedTextField(
                value = oldPass,
                onValueChange = { oldPass = it; errorText = null },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
            
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it; errorText = null },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
            
            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it; errorText = null },
                label = { Text("Confirm New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = confirmPass.isNotEmpty() && confirmPass != newPass,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (newPass != confirmPass) {
                        errorText = "New passwords do not match"
                    } else if (newPass.length < 4) {
                        errorText = "Password too short"
                    } else {
                         viewModel.changePassword(oldPass, newPass)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && oldPass.isNotBlank() && newPass.isNotBlank() && confirmPass.isNotBlank()
            ) {
                 if (isLoading) {
                     CircularProgressIndicator(
                         modifier = Modifier.size(24.dp), 
                         color = MaterialTheme.colorScheme.onPrimary
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("Re-Encrypting Data...")
                 } else {
                     Text("Update & Re-Encrypt")
                 }
            }
        }
    }
}
