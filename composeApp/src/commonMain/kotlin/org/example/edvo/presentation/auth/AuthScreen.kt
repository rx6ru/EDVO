package org.example.edvo.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.edvo.presentation.designsystem.*
import org.jetbrains.compose.resources.painterResource
import edvo.composeapp.generated.resources.Res
import edvo.composeapp.generated.resources.edvo_base_logo

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onUnlockSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state) {
        if (state is AuthState.Unlocked) {
            onUnlockSuccess()
        }
    }
    
    // Startup Animation (Heartbeat) - Disabled per request
    // "Do not have the continus breathing animation of the edvo base logo"
    val pulseScale = 1.0f

    Scaffold(containerColor = NeoPaletteV2.Canvas) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Hero Element: Base Logo
            // "Use the edvo_base_logo for the starting page instead of the ghost icon"
            // "The apps name EDVO is not used on signup/login page"
            Image(
                painter = painterResource(Res.drawable.edvo_base_logo),
                contentDescription = "EDVO Logo",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("EDVO", style = NeoTypographyV2.Header().copy(fontSize = 32.sp))
            
            Spacer(modifier = Modifier.height(48.dp))

            when (val s = state) {
                is AuthState.Loading -> {
                     CircularProgressIndicator(color = NeoPaletteV2.Functional.SignalGreen)
                }
                is AuthState.SetupRequired -> {
                    SetupView(viewModel)
                }
                is AuthState.Locked -> {
                    LockedView(
                        onUnlock = { pwd -> viewModel.login(pwd) }
                    )
                }
                is AuthState.Error -> {
                    Text(
                        "Error: ${s.message}", 
                        style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalRed)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SmartButton(
                        text = "Retry",
                        onClick = { viewModel.resetError() },
                        isDestructive = true
                    )
                }
                is AuthState.Unlocked -> {
                     Text("Unlocked", style = NeoTypographyV2.Header().copy(color = NeoPaletteV2.Functional.SignalGreen))
                }
            }
        }
    }
}

@Composable
private fun SetupView(viewModel: AuthViewModel) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Create Master Password", style = NeoTypographyV2.Header())
        Text("This password encrypts all your data.", style = NeoTypographyV2.DataMono())
        Spacer(modifier = Modifier.height(24.dp))

        NeoInput(
            value = password,
            onValueChange = { password = it; errorText = null },
            label = "Master Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        NeoInput(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorText = null },
            label = "Confirm Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (errorText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorText!!, style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalRed))
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        SmartButton(
            text = "Set Password", 
            onClick = {
                if (password != confirmPassword) {
                    errorText = "Passwords do not match"
                } else if (password.length < 6) {
                    errorText = "Password is too weak"
                } else {
                    viewModel.register(password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LockedView(
    onUnlock: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Welcome Back", style = NeoTypographyV2.Header())
        Spacer(modifier = Modifier.height(32.dp))
        
        NeoInput(
            value = password,
            onValueChange = { password = it },
            label = "Master Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SmartButton(
            text = "Unlock",
            onClick = { onUnlock(password) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
