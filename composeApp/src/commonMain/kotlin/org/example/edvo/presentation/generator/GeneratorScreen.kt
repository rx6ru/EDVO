package org.example.edvo.presentation.generator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import org.example.edvo.presentation.designsystem.NeoTypographyV2

@Composable
fun GeneratorScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoPaletteV2.Canvas)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GENERATOR",
            style = NeoTypographyV2.Header(),
            color = NeoPaletteV2.Functional.SignalGreen
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SECURE PASSWORD GENERATION\nMODULE OFFLINE",
            style = NeoTypographyV2.BodyAction(),
            color = NeoPaletteV2.Functional.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        // Artistic placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(NeoPaletteV2.SurfaceSecondary)
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NeoPaletteV2.Canvas)
            )
        }
    }
}
