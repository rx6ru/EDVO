package org.example.edvo.presentation.designsystem

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * Types of feedback the overlay can display.
 */
sealed class FeedbackType {
    object Loading : FeedbackType()
    data class Success(val message: String) : FeedbackType()
    data class Error(val message: String) : FeedbackType()
}

/**
 * Unified feedback overlay that shows loading spinner, animated success check,
 * or animated error cross with appropriate messages.
 *
 * @param type The type of feedback to display
 * @param onDismiss Callback when overlay should be dismissed
 * @param autoDismissMs Auto-dismiss delay for Success type (0 to disable)
 */
@Composable
fun NeoFeedbackOverlay(
    type: FeedbackType,
    onDismiss: () -> Unit,
    autoDismissMs: Long = 2000
) {
    // Auto-dismiss for success
    LaunchedEffect(type) {
        if (type is FeedbackType.Success && autoDismissMs > 0) {
            delay(autoDismissMs)
            onDismiss()
        }
    }
    
    Dialog(
        onDismissRequest = {
            // Only allow dismiss for Success and Error, not Loading
            if (type !is FeedbackType.Loading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = type !is FeedbackType.Loading,
            dismissOnClickOutside = type !is FeedbackType.Loading
        )
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .background(NeoPaletteV2.SurfaceSecondary, RoundedCornerShape(16.dp))
                .border(2.dp, NeoPaletteV2.BorderInactive, RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (type) {
                    is FeedbackType.Loading -> {
                        // Animated spinner
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = NeoPaletteV2.Functional.SignalGreen,
                            strokeWidth = 4.dp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Processing...",
                            style = NeoTypographyV2.BodyAction(),
                            color = NeoPaletteV2.AccentWhite
                        )
                    }
                    
                    is FeedbackType.Success -> {
                        // Animated checkmark with green background
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    NeoPaletteV2.Functional.SignalGreen.copy(alpha = 0.1f),
                                    RoundedCornerShape(40.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedCheck(
                                size = 48.dp,
                                strokeWidth = 5.dp,
                                animationDuration = 500
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = type.message,
                            style = NeoTypographyV2.BodyAction(),
                            color = NeoPaletteV2.AccentWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    is FeedbackType.Error -> {
                        // Animated cross with red background
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    NeoPaletteV2.Functional.SignalRed.copy(alpha = 0.1f),
                                    RoundedCornerShape(40.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedCross(
                                size = 48.dp,
                                strokeWidth = 5.dp,
                                animationDuration = 500
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = type.message,
                            style = NeoTypographyV2.BodyAction(),
                            color = NeoPaletteV2.Functional.SignalRed,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(onClick = onDismiss) {
                            Text(
                                "Dismiss",
                                color = NeoPaletteV2.Functional.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
