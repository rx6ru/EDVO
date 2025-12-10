package org.example.edvo.presentation.designsystem

import org.example.edvo.presentation.components.EdvoTextField
import org.example.edvo.theme.EdvoColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

val NeoCardShape = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) NeoPhysicsV2.CardScale else 1f,
        animationSpec = NeoPhysicsV2.SpringBouncy
    )
    
    val haptic = LocalHapticFeedback.current

    // Card shape: subtle CutCorner for "Terminal" aesthetic.
    val shape = NeoCardShape

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Light placeholder
                            onClick?.invoke()
                        },
                        onLongClick = {
                            if (onLongClick != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClick()
                            }
                        }
                    )
                } else Modifier
            ),
        color = NeoPaletteV2.SurfacePrimary,
        shape = shape,
        border = if (isSelected) BorderStroke(2.dp, NeoPaletteV2.Functional.SignalRed) else BorderStroke(1.dp, Color.White),
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SmartButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) NeoPhysicsV2.ButtonScale else 1f,
        animationSpec = NeoPhysicsV2.SpringSnappy
    )
    
    val haptic = LocalHapticFeedback.current
    
    val backgroundColor = if (isDestructive) NeoPaletteV2.Functional.SignalRed else NeoPaletteV2.AccentWhite
    val contentColor = if (isDestructive) NeoPaletteV2.AccentWhite else NeoPaletteV2.SurfacePrimary

    // Terminal style sharp corners
    val shape = RoundedCornerShape(4.dp) 

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                 color = if (enabled) backgroundColor else NeoPaletteV2.BorderInactive, 
                 shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Medium placeholder
                    onClick()
                }
            )
            .height(56.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = NeoTypographyV2.BodyAction().copy(
                color = if (enabled) contentColor else Color.Gray
            )
        )
    }
}

@Composable
fun NeoInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    onFocusChange: ((Boolean) -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = NeoTypographyV2.DataMono(),
            color = if (isError) NeoPaletteV2.Functional.SignalRed else NeoPaletteV2.Functional.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = NeoTypographyV2.BodyAction().copy(color = NeoPaletteV2.AccentWhite),
            cursorBrush = SolidColor(NeoPaletteV2.Functional.SignalGreen),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeoPaletteV2.SurfaceSecondary, RoundedCornerShape(4.dp))
                        .border(
                             width = 1.dp,
                             // Focused: SignalGreen
                             // Unfocused: LightGray
                             color = if (isFocused) NeoPaletteV2.Functional.SignalGreen else Color.LightGray.copy(alpha = 0.5f),
                             shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        trailingIcon()
                    }
                }
            },
            modifier = Modifier.onFocusChanged { 
                isFocused = it.isFocused
                onFocusChange?.invoke(it.isFocused)
            }
        )
    }
}

@Composable
fun NeoSlideToAct(
    text: String,
    onOrphaned: () -> Unit,
    onSwipeComplete: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    BoxWithConstraints(
        modifier = modifier
            .height(56.dp)
            .background(
                color = if (isDestructive) NeoPaletteV2.Functional.SignalRed.copy(alpha = 0.2f) else NeoPaletteV2.SurfaceSecondary,
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                1.dp, 
                if (isDestructive) NeoPaletteV2.Functional.SignalRed else NeoPaletteV2.BorderInactive, 
                RoundedCornerShape(28.dp)
            )
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val thumbSize = 56.dp
        
        val density = androidx.compose.ui.platform.LocalDensity.current
        val thumbSizePx = with(density) { thumbSize.toPx() }
        val trackLength = maxWidthPx - thumbSizePx
        
        var offsetX by remember { mutableStateOf(0f) }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = NeoTypographyV2.DataMono(),
                color = if (isDestructive) NeoPaletteV2.Functional.SignalRed else NeoPaletteV2.Functional.TextSecondary,
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - (offsetX / trackLength).coerceIn(0f, 1f)
                }
            )
        }

        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(offsetX.toInt(), 0) }
                .size(thumbSize)
                .padding(4.dp)
                .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(0f, trackLength)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        if (offsetX > trackLength * 0.8f) {
                            offsetX = trackLength
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipeComplete()
                        } else {
                            offsetX = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}
