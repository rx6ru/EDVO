package org.example.edvo.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import org.example.edvo.theme.EdvoColor

enum class EdvoButtonType {
    Primary, Secondary, Destructive
}

@Composable
fun EdvoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: EdvoButtonType = EdvoButtonType.Primary,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    val containerColor = when(type) {
        EdvoButtonType.Primary -> EdvoColor.White
        else -> Color.Transparent
    }
    
    val contentColor = when(type) {
        EdvoButtonType.Primary -> EdvoColor.Black
        EdvoButtonType.Destructive -> EdvoColor.ErrorRed
        EdvoButtonType.Secondary -> EdvoColor.White
    }
    
    val border = when(type) {
        EdvoButtonType.Primary -> null
        EdvoButtonType.Secondary -> BorderStroke(1.dp, EdvoColor.White)
        EdvoButtonType.Destructive -> BorderStroke(1.dp, EdvoColor.ErrorRed)
    }

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = EdvoColor.DarkSurface,
            disabledContentColor = Color.Gray
        ),
        border = border,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 24.dp)
    ) {
        Text(text)
    }
}
