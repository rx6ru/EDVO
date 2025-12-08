package org.example.edvo.presentation.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import org.jetbrains.compose.resources.Font
import edvo.composeapp.generated.resources.Res
import edvo.composeapp.generated.resources.spacemono_regular
import edvo.composeapp.generated.resources.spacemono_bold

object NeoPaletteV2 {
    val Canvas = Color(0xFF000000)
    val SurfacePrimary = Color(0xFF0A0A0A)
    val SurfaceSecondary = Color(0xFF111111)
    val BorderInactive = Color(0xFF222222)
    val AccentWhite = Color(0xFFFFFFFF)
    
    object Functional {
        val SignalGreen = Color(0xFF00FF94)
        val SignalRed = Color(0xFFFF3B30)
        val TextSecondary = Color(0xFF888888)
        val MatrixText = Color(0xFF00FF94)
    }
}

@Composable
fun getSpaceMonoFontFamily() = FontFamily(
    Font(Res.font.spacemono_regular, FontWeight.Normal),
    Font(Res.font.spacemono_bold, FontWeight.Bold)
)


object NeoTypographyV2 {
    
    @Composable
    fun Header() = TextStyle(
        fontFamily = getSpaceMonoFontFamily(),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp,
        color = NeoPaletteV2.AccentWhite
    )
    
    @Composable
    fun BodyAction() = TextStyle(
        fontFamily = getSpaceMonoFontFamily(),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        color = NeoPaletteV2.AccentWhite
    )
    
    @Composable
    fun DataMono() = TextStyle(
        fontFamily = getSpaceMonoFontFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = NeoPaletteV2.Functional.TextSecondary
    )
}

object NeoPhysicsV2 {
    val SpringBouncy = spring<Float>(
        dampingRatio = 0.55f, 
        stiffness = 400f
    )
    
    val SpringSnappy = spring<Float>(
        dampingRatio = 0.7f, 
        stiffness = 600f
    )
    
    const val CardScale = 0.96f
    const val ButtonScale = 0.92f
}

@Composable
fun NeoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = NeoPaletteV2.Canvas,
            surface = NeoPaletteV2.SurfacePrimary,
            onBackground = NeoPaletteV2.AccentWhite,
            onSurface = NeoPaletteV2.AccentWhite,
            primary = NeoPaletteV2.AccentWhite,
            error = NeoPaletteV2.Functional.SignalRed
        ),
        content = content
    )
}
