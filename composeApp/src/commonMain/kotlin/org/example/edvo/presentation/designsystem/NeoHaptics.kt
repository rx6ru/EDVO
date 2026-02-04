package org.example.edvo.presentation.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * NeoHaptics: Centralized Haptic Feedback System.
 * provides semantic feedback actions like `click`, `subtle`, `error`.
 */
object NeoHaptics {
    
    @Composable
    fun current(): HapticFeedbackWrapper {
        val hapticFeedback = LocalHapticFeedback.current
        return remember(hapticFeedback) { HapticFeedbackWrapper(hapticFeedback) }
    }

    class HapticFeedbackWrapper(private val haptics: HapticFeedback) {
        
        fun click() {
            // Standard click feedback
             haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Often lighter than LongPress
             // Note: Compose HapticFeedbackType is limited (LongPress, TextHandleMove).
             // TextHandleMove is often the subtle "tick" on Android.
        }

        fun longPress() {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        fun toggle() {
             haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        
        fun success() {
             // Mimic success if possible, or double tick
             haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        
        fun error() {
             // Mimic error (usually buzzing)
             haptics.performHapticFeedback(HapticFeedbackType.LongPress)
             // In a real native implementation, we'd use Vibrator explicitly for patterns.
        }
    }
}
