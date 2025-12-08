package org.example.edvo.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

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
            val s = state as SettingsState.Success
            if (s.type == OperationType.PASSWORD_CHANGE) {
                viewModel.resetState()
                onLogoutRequired()
            }
        }
    }
    
    val isLoading = state is SettingsState.Loading
    val vmError = (state as? SettingsState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rotate Encryption Key") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
