package org.example.edvo.presentation.designsystem

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated checkmark that draws itself in with a smooth animation.
 * Uses path animation to progressively reveal the check shape.
 */
@Composable
fun AnimatedCheck(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = NeoPaletteV2.Functional.SignalGreen,
    strokeWidth: Dp = 4.dp,
    animationDuration: Int = 400
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // Trigger animation on composition
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // Animate from 0 to 1
    val progress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "checkProgress"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size.minDimension
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        
        // Checkmark points (relative to canvas)
        // Start point (left)
        val startX = canvasSize * 0.2f
        val startY = canvasSize * 0.5f
        
        // Middle point (bottom of check)
        val midX = canvasSize * 0.4f
        val midY = canvasSize * 0.7f
        
        // End point (top right)
        val endX = canvasSize * 0.8f
        val endY = canvasSize * 0.3f
        
        // Total path length (approximate)
        val leg1Length = kotlin.math.sqrt(
            (midX - startX) * (midX - startX) + (midY - startY) * (midY - startY)
        )
        val leg2Length = kotlin.math.sqrt(
            (endX - midX) * (endX - midX) + (endY - midY) * (endY - midY)
        )
        val totalLength = leg1Length + leg2Length
        
        // Current draw length based on progress
        val currentLength = totalLength * progress
        
        // Draw first leg (start to mid)
        if (currentLength > 0) {
            val leg1Progress = (currentLength / leg1Length).coerceIn(0f, 1f)
            val currentMidX = startX + (midX - startX) * leg1Progress
            val currentMidY = startY + (midY - startY) * leg1Progress
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(currentMidX, currentMidY),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round
            )
        }
        
        // Draw second leg (mid to end)
        if (currentLength > leg1Length) {
            val leg2Progress = ((currentLength - leg1Length) / leg2Length).coerceIn(0f, 1f)
            val currentEndX = midX + (endX - midX) * leg2Progress
            val currentEndY = midY + (endY - midY) * leg2Progress
            
            drawLine(
                color = color,
                start = Offset(midX, midY),
                end = Offset(currentEndX, currentEndY),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round
            )
        }
    }
}
