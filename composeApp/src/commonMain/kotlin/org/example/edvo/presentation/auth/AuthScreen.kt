package org.example.edvo.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.components.EdvoButton
import org.example.edvo.presentation.components.EdvoScaffold
import org.example.edvo.presentation.components.EdvoTextField
import org.example.edvo.theme.EdvoColor

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

    EdvoScaffold {
        // Entry Animation
        val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }
        val transition = updateTransition(transitionState, label = "Entry")
        
        val offsetY by transition.animateDp(
            transitionSpec = { tween(800, easing = FastOutSlowInEasing) },
            label = "Offset"
        ) { visible -> if (visible) 0.dp else 100.dp }

        val alpha by transition.animateFloat(
            transitionSpec = { tween(800) },
            label = "Alpha"
        ) { visible -> if (visible) 1f else 0f }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .graphicsLayer {
                    translationY = offsetY.toPx()
                    this.alpha = alpha
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Title
            Text(
                "EDVO",
                style = MaterialTheme.typography.displayLarge,
                color = EdvoColor.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Encrypted Data Vault ∅ne",
                style = MaterialTheme.typography.labelLarge,
                color = EdvoColor.LightGray,
                modifier = Modifier.alpha(0.7f)
            )
            Spacer(modifier = Modifier.height(48.dp))

            when (val s = state) {
                is AuthState.Loading -> {
                     CircularProgressIndicator(color = EdvoColor.White)
                }
                is AuthState.SetupRequired -> {
                    SetupView(viewModel)
                }
                is AuthState.Locked -> {
                    LockedView(
                        password = passwordInput,
                        onPasswordChange = { passwordInput = it },
                        onUnlock = { viewModel.login(passwordInput) }
                    )
                }
                is AuthState.Error -> {
                    Text(
                        "Error: ${s.message}", 
                        color = EdvoColor.ErrorRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    EdvoButton(
                        text = "Try Again",
                        onClick = { 
                            viewModel.resetError() 
                            passwordInput = ""
                        }
                    )
                }
                is AuthState.Unlocked -> {
                     CircularProgressIndicator(color = EdvoColor.White)
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
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Create Master Password", color = EdvoColor.White)
        Spacer(modifier = Modifier.height(24.dp))
        
        EdvoTextField(
            value = password,
            onValueChange = { password = it; errorText = null },
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        EdvoTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorText = null },
            label = "Confirm",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (errorText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorText!!, color = EdvoColor.ErrorRed)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        EdvoButton(
            text = "Initialize Vault", 
            onClick = {
                if (password != confirmPassword) {
                    errorText = "Passwords do not match"
                } else if (password.length < 6) {
                    errorText = "Password too short"
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
    password: String,
    onPasswordChange: (String) -> Unit,
    onUnlock: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EdvoTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Master Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Pulse Animation for Unlock Button
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        Box(modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }) {
            EdvoButton(
                text = "Unlock Vault",
                onClick = onUnlock,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
