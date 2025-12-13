package org.example.edvo.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    
    val pulseScale = 1.0f
    
    // Derive alignment from actual IME height - syncs with system keyboard animation
    val density = androidx.compose.ui.platform.LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    // Normalize to 0-1 range (1 = fully bottom, 0 = center)
    val maxKeyboardHeight = with(density) { 300.dp.toPx() }
    val rawBias = (imeBottom / maxKeyboardHeight).coerceIn(0f, 1f)
    
    // Smooth the bias to eliminate jitter from system IME insets
    val smoothedBias by animateFloatAsState(
        targetValue = rawBias,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "smoothedBias"
    )
    val animatedAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = smoothedBias)
    
    // Use raw imeBottom for bottom padding (no smoothing needed)
    val isImeVisible = imeBottom > 0

    Scaffold(containerColor = NeoPaletteV2.Canvas) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding() // Shrinks the box when keyboard opens
                .padding(horizontal = 24.dp)
                // Add bottom padding ONLY when keyboard is open for small cushion
                .padding(bottom = if (isImeVisible) 16.dp else 0.dp),
            contentAlignment = animatedAlignment
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hero Element
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

                // Content Views
                when (val s = state) {
                    is AuthState.Loading -> CircularProgressIndicator(color = NeoPaletteV2.Functional.SignalGreen)
                    is AuthState.SetupRequired -> SetupView(viewModel)
                    is AuthState.Locked -> LockedView(onUnlock = { pwd -> viewModel.login(pwd) })
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
                    is AuthState.Unlocked -> Text("Unlocked", style = NeoTypographyV2.Header())
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

    // No local scroll/padding needed - handled by parent
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier.fillMaxWidth()
    ) {
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
