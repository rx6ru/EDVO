package org.example.edvo.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onUnlockSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var passwordInput by remember { mutableStateOf("") }
    
    // Side effect to handle navigation on success
    LaunchedEffect(state) {
        if (state is AuthState.Unlocked) {
            onUnlockSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val s = state) {
            is AuthState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthState.SetupRequired -> {
                Text("Welcome to EDVO Vault", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Set a Mater Password to start.")
                Spacer(modifier = Modifier.height(8.dp))
                
                var confirmPasswordInput by remember { mutableStateOf("") }
                var errorText by remember { mutableStateOf<String?>(null) }

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { 
                        passwordInput = it 
                        errorText = null 
                    },
                    label = { Text("Create Master Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorText != null
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { 
                        confirmPasswordInput = it
                        errorText = null
                    },
                    label = { Text("Confirm Master Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorText != null
                )
                
                if (errorText != null) {
                    Text(
                        text = errorText!!, 
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { 
                    if (passwordInput != confirmPasswordInput) {
                        errorText = "Passwords do not match"
                    } else {
                        viewModel.register(passwordInput) 
                    }
                }) {
                    Text("Create Vault")
                }
            }
            is AuthState.Locked -> {
                Text("EDVO Vault Locked", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Master Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.login(passwordInput) }) {
                    Text("Unlock")
                }
            }
            is AuthState.Error -> {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { 
                    viewModel.resetError() 
                    passwordInput = ""
                }) {
                    Text("Try Again")
                }
            }
            is AuthState.Unlocked -> {
                Text("Unlocking...")
            }
        }
    }
}
