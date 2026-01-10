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
 * Animated cross (X) that draws itself in with a smooth animation.
 * Uses path animation to progressively reveal both lines of the X.
 */
@Composable
fun AnimatedCross(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = NeoPaletteV2.Functional.SignalRed,
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
        label = "crossProgress"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size.minDimension
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        
        // Cross points (relative to canvas) - padding from edges
        val padding = canvasSize * 0.25f
        
        // First line: top-left to bottom-right
        val line1StartX = padding
        val line1StartY = padding
        val line1EndX = canvasSize - padding
        val line1EndY = canvasSize - padding
        
        // Second line: top-right to bottom-left
        val line2StartX = canvasSize - padding
        val line2StartY = padding
        val line2EndX = padding
        val line2EndY = canvasSize - padding
        
        // Draw first line (first half of animation)
        val line1Progress = (progress * 2).coerceIn(0f, 1f)
        if (line1Progress > 0) {
            val currentEndX = line1StartX + (line1EndX - line1StartX) * line1Progress
            val currentEndY = line1StartY + (line1EndY - line1StartY) * line1Progress
            
            drawLine(
                color = color,
                start = Offset(line1StartX, line1StartY),
                end = Offset(currentEndX, currentEndY),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round
            )
        }
        
        // Draw second line (second half of animation)
        val line2Progress = ((progress - 0.5f) * 2).coerceIn(0f, 1f)
        if (line2Progress > 0) {
            val currentEndX = line2StartX + (line2EndX - line2StartX) * line2Progress
            val currentEndY = line2StartY + (line2EndY - line2StartY) * line2Progress
            
            drawLine(
                color = color,
                start = Offset(line2StartX, line2StartY),
                end = Offset(currentEndX, currentEndY),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round
            )
        }
    }
}
