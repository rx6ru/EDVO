package org.example.edvo.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint


@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onUnlockSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    
    // Biometric authenticator for actual fingerprint prompt
    val biometricAuthenticator = remember { BiometricAuthenticator() }
    
    LaunchedEffect(state) {
        if (state is AuthState.Unlocked) {
            onUnlockSuccess()
        }
    }
    
    val pulseScale = 1.0f
    
    // Derive alignment from actual IME height - syncs with system keyboard animation
    val density = androidx.compose.ui.platform.LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)

    val maxKeyboardHeight = with(density) { 100.dp.toPx() }
    
    // POSITIVE bias: Push content DOWN toward keyboard (Bottom alignment)
    val rawBias = (imeBottom / maxKeyboardHeight).coerceIn(0f, 1f)
    
    // Direction-aware animation: detect if opening or closing keyboard
    var previousBias by remember { mutableFloatStateOf(0f) }
    val isOpening = rawBias > previousBias
    LaunchedEffect(rawBias) { previousBias = rawBias }
    
    // Use different animation specs for opening vs closing
    val biasAnimSpec = if (isOpening) {
        // Opening: smooth spring
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    } else {
        // Closing: SNAP to match instant imePadding removal (prevents "drop to bottom")
        snap<Float>()
    }
    
    val smoothedBias by animateFloatAsState(
        targetValue = rawBias,
        animationSpec = biasAnimSpec,
        label = "alignmentBias"
    )
    val animatedAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = smoothedBias)
    
    // Use raw imeBottom for bottom padding (no smoothing needed)
    val isImeVisible = imeBottom > 0
    
    // Couple Hero/Spacer transitions - faster switch when closing
    val isCompactMode = if (isOpening) rawBias > 0.3f else rawBias > 0.1f
    
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
                // Animated Hero Element (coupled to alignment bias, not boolean)
                AuthHero(isCompact = isCompactMode, pulseScale = pulseScale)
                
                // Dynamic Spacer: Shrink gap when keyboard is sufficiently open (spring physics)
                val spacerHeight by animateDpAsState(
                    targetValue = if (isCompactMode) 16.dp else 48.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                Spacer(modifier = Modifier.height(spacerHeight))

                // Content Views
                when (val s = state) {
                    is AuthState.Loading -> CircularProgressIndicator(color = NeoPaletteV2.Functional.SignalGreen)
                    is AuthState.SetupRequired -> SetupView(viewModel, isCompactMode)
                    is AuthState.Locked -> LockedView(
                        onUnlock = { pwd -> viewModel.login(pwd) },
                        isCompact = isCompactMode,
                        biometricEnabled = biometricEnabled,
                        onBiometricClick = {
                            // Trigger actual biometric prompt
                            biometricAuthenticator.authenticate(
                                onSuccess = { viewModel.unlockWithBiometric() },
                                onError = { /* Show error toast or snackbar */ },
                                onCancel = { /* User cancelled, do nothing */ }
                            )
                        }
                    )
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

// Focus tracking for per-field layout optimization
private enum class FocusedField { NONE, MASTER, CONFIRM }

@Composable
private fun SetupView(viewModel: AuthViewModel, isCompact: Boolean) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var focusedField by remember { mutableStateOf(FocusedField.NONE) }
    
    // Focus-aware button spacing:
    // - CONFIRM focused: Nothing below, hug the button close (8dp)
    // - MASTER focused: Confirm field below, use normal spacing (16dp)
    // - Not compact: Use default spacing (32dp)
    val buttonSpacing = when {
        !isCompact -> 32.dp
        focusedField == FocusedField.CONFIRM -> 8.dp  // Tight!
        else -> 16.dp
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Create Master Password", style = NeoTypographyV2.Header())
        Text("This password encrypts all your data.", style = NeoTypographyV2.DataMono())
        Spacer(modifier = Modifier.height(24.dp))

        NeoPasswordInput(
            value = password,
            onValueChange = { password = it; errorText = null },
            label = "Master Password",
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) focusedField = FocusedField.MASTER }
        )
        Spacer(modifier = Modifier.height(16.dp))
        NeoPasswordInput(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorText = null },
            label = "Confirm Password",
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) focusedField = FocusedField.CONFIRM }
        )
        
        if (errorText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorText!!, style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalRed))
        }
        
        Spacer(modifier = Modifier.height(buttonSpacing))
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
    onUnlock: (String) -> Unit,
    isCompact: Boolean,
    biometricEnabled: Boolean = false,
    onBiometricClick: () -> Unit = {}
) {
    var password by remember { mutableStateOf("") }
    
    // Dynamic button spacing: tighter when keyboard is open
    val buttonSpacing = if (isCompact) 16.dp else 32.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Welcome Back", style = NeoTypographyV2.Header())
        Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 32.dp))
        
        // Password field with optional fingerprint button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom  // Align to bottom so icon matches text field
        ) {
            NeoPasswordInput(
                value = password,
                onValueChange = { password = it },
                label = "Master Password",
                modifier = Modifier.weight(1f)
            )
            
            // Fingerprint button (visible when biometric is enabled)
            if (biometricEnabled) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onBiometricClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Unlock with fingerprint",
                        tint = NeoPaletteV2.Functional.SignalGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(buttonSpacing))
        
        SmartButton(
            text = "Unlock",
            onClick = { onUnlock(password) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}