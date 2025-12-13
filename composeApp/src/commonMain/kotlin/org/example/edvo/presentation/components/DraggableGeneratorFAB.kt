package org.example.edvo.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import kotlin.math.roundToInt

/**
 * A draggable Floating Action Button that snaps to the nearest horizontal edge.
 * Touch events are ONLY consumed by the FAB itself, not the entire screen.
 */
@Composable
fun DraggableGeneratorFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val fabSizeDp = 56.dp
    val paddingDp = 16.dp
    
    val fabSizePx = with(density) { fabSizeDp.toPx() }
    val paddingPx = with(density) { paddingDp.toPx() }
    
    // Position state (Animatable for smooth snapping)
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    
    var parentWidth by remember { mutableStateOf(0f) }
    var parentHeight by remember { mutableStateOf(0f) }
    var isInitialized by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    // Use BoxWithConstraints to get parent size WITHOUT blocking touches
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Capture parent dimensions once
        LaunchedEffect(constraints) {
            parentWidth = constraints.maxWidth.toFloat()
            parentHeight = constraints.maxHeight.toFloat()
            
            if (!isInitialized && parentWidth > 0) {
                // Initial position: Bottom Right with padding
                val initX = parentWidth - fabSizePx - paddingPx
                val initY = parentHeight/4
                offsetX.snapTo(initX)
                offsetY.snapTo(initY)
                isInitialized = true
            }
        }

        // Only show FAB after initial position is set (prevents flash at 0,0)
        if (isInitialized) {
            // The FAB itself handles drag gestures
            FloatingActionButton(
                onClick = onClick,
                shape = CircleShape,
                containerColor = NeoPaletteV2.Functional.SignalGreen,
                contentColor = NeoPaletteV2.SurfacePrimary,
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                    .size(fabSizeDp)
                    .border(2.dp, NeoPaletteV2.SurfacePrimary, CircleShape)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                // Snap to nearest edge (left or right)
                                val boundary = parentWidth / 2
                                val targetX = if (offsetX.value < boundary) {
                                    paddingPx // Snap to left edge
                                } else {
                                    parentWidth - fabSizePx - paddingPx // Snap to right edge
                                }
                                
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = targetX,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                                
                                // Clamp Y within bounds
                                val clampedY = offsetY.value.coerceIn(
                                    paddingPx,
                                    parentHeight - fabSizePx - paddingPx
                                )
                                if (clampedY != offsetY.value) {
                                    scope.launch { offsetY.animateTo(clampedY) }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                            }
                        }
                    }
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = "Generate Password")
            }
        }
    }
}
