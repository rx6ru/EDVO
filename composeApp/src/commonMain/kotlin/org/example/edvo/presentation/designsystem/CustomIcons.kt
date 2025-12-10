package org.example.edvo.presentation.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object CustomIcons {
    // A Geometric Lock: Square body, Semi-circle shackle
    val IconVault: ImageVector
        get() = ImageVector.Builder(
            name = "IconVault",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(NeoPalette.Primary),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Square,
                strokeLineJoin = StrokeJoin.Miter
            ) {
                // Shackle
                moveTo(7f, 10f)
                lineTo(7f, 6f)
                arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 6f)
                lineTo(17f, 10f)
                // Body
                moveTo(5f, 10f)
                lineTo(19f, 10f)
                lineTo(19f, 21f)
                lineTo(5f, 21f)
                close()
                // Keyhole
                moveTo(12f, 14f)
                lineTo(12f, 17f)
            }
        }.build()

    // A Minimalist Ghost: Geometric curve
    val IconGhost: ImageVector
        get() = ImageVector.Builder(
            name = "IconGhost",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF888888)),
                strokeLineWidth = 2f
            ) {
                moveTo(6f, 20f)
                lineTo(6f, 10f)
                arcTo(6f, 6f, 0f, false, true, 18f, 10f)
                lineTo(18f, 20f)
                
                // Jagged bottom
                lineTo(15f, 18f)
                lineTo(12f, 20f)
                lineTo(9f, 18f)
                lineTo(6f, 20f)
                
                // Eyes
                moveTo(9f, 12f)
                lineTo(9f, 12.01f) // Dot
                moveTo(15f, 12f)
                lineTo(15f, 12.01f) // Dot
            }
        }.build()

    // Geometric Kill Switch: Skull / Hazard
    val IconKill: ImageVector
        get() = ImageVector.Builder(
            name = "IconKill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(NeoPalette.Functional.Destructive),
                strokeLineWidth = 2f
            ) {
                // Skull Top
                moveTo(7f, 10f)
                arcTo(5f, 5f, 0f, false, true, 17f, 10f)
                lineTo(17f, 15f)
                lineTo(7f, 15f)
                close()
                
                // Crossbones (X)
                moveTo(20f, 4f)
                lineTo(4f, 20f)
            }
        }.build()

    // Geometric Cycle Arrows (Generator)
    val IconCycle: ImageVector
        get() = ImageVector.Builder(
            name = "IconCycle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(NeoPalette.Primary),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Top Arc Arrow
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, false, true, 12f, 21f)
                moveTo(21f, 12f)
                lineTo(21f, 16f)
                moveTo(21f, 12f)
                lineTo(17f, 12f)
                
                // Bottom Arc Arrow
                moveTo(3f, 12f)
                arcTo(9f, 9f, 0f, false, true, 12f, 3f)
                moveTo(3f, 12f)
                lineTo(3f, 8f)
                moveTo(3f, 12f)
                lineTo(7f, 12f)
            }
        }.build()
}
