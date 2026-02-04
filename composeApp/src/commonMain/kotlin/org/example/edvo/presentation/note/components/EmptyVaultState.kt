package org.example.edvo.presentation.note.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.designsystem.*

/**
 * Simple Empty Vault State - Icon + Text only, no button.
 */
@Composable
fun EmptyVaultState(
    onCreateClick: () -> Unit = {} // Kept for API compat but unused
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            CustomIcons.IconGhost,
            contentDescription = "Empty Vault",
            tint = NeoPaletteV2.Functional.TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "VAULT EMPTY",
            style = NeoTypographyV2.DataMono(),
            color = NeoPaletteV2.Functional.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Tap + to add your first entry",
            style = NeoTypographyV2.LabelSmall(),
            color = NeoPaletteV2.Functional.TextSecondary.copy(alpha = 0.6f)
        )
    }
}
