package org.example.edvo.presentation.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object NeoPalette {
    val Canvas = Color(0xFF000000)
    val Surface = Color(0xFF0D0D0D)
    val Border = Color(0xFF222222)
    val Primary = Color(0xFFFFFFFF)
    val OnPrimary = Color(0xFF000000)
    
    object Functional {
        val Destructive = Color(0xFFFF453A)
        val SecureGreen = Color(0xFF00FF90)
        val WarningYellow = Color(0xFFFFD60A)
    }
}

object NeoTypography {
    val Header = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        letterSpacing = (-1.5).sp,
        color = NeoPalette.Primary
    )
    
    val DataLabel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color(0xFF888888)
    )
    
    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = NeoPalette.Primary
    )
    
    val ButtonText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        color = NeoPalette.OnPrimary
    )
}

object NeoPhysics {
    const val ButtonPressScale = 0.92f
    const val CardPressScale = 0.96f
    
    val SpringSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    object Haptics {
        val Tap = HapticFeedbackType.TextHandleMove // Closest mapping to Light on generic Compsoe
        val Success = HapticFeedbackType.LongPress // Substitution for Medium if unavailable
        val Error = HapticFeedbackType.LongPress
    }
}
