package org.example.edvo.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.components.EdvoCard
import org.example.edvo.presentation.components.EdvoScaffold
import org.example.edvo.presentation.components.util.BackHandler
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import org.example.edvo.theme.EdvoColor
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    onShakeConfigClick: () -> Unit = {}
) {
    BackHandler(enabled = true) { onBack() }
    
    val screenshotsEnabled by viewModel.screenshotsEnabled.collectAsState()
    val copyPasteEnabled by viewModel.copyPasteEnabled.collectAsState()
    val shakeToLockEnabled by viewModel.shakeToLockEnabled.collectAsState()

    EdvoScaffold(
        topBar = {
            TopAppBar(
                title = { Text("FEATURES", color = EdvoColor.White, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EdvoColor.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EdvoColor.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screenshots Toggle
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Screenshots", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                        Text(
                            if (screenshotsEnabled) "Allowed" else "Blocked (Secure)", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = if (screenshotsEnabled) EdvoColor.ErrorRed else EdvoColor.Primary
                        )
                    }
                    Switch(
                        checked = screenshotsEnabled,
                        onCheckedChange = { viewModel.toggleScreenshots(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                            uncheckedThumbColor = EdvoColor.LightGray,
                            uncheckedTrackColor = EdvoColor.Surface,
                            uncheckedBorderColor = EdvoColor.LightGray
                        )
                    )
                }
            }
            
            // Clipboard Toggle
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clipboard (Copy/Paste)", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                        Text(
                            if (copyPasteEnabled) "Enabled" else "Disabled", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = EdvoColor.LightGray
                        )
                    }
                    Switch(
                        checked = copyPasteEnabled,
                        onCheckedChange = { viewModel.toggleCopyPaste(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                            uncheckedThumbColor = EdvoColor.LightGray,
                            uncheckedTrackColor = EdvoColor.Surface,
                            uncheckedBorderColor = EdvoColor.LightGray
                        )
                    )
                }
            }
            
            // Shake to Lock Toggle
            EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shake to Lock", style = MaterialTheme.typography.bodyLarge, color = EdvoColor.White)
                            Text(
                                if (shakeToLockEnabled) "Enabled" else "Disabled", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = if (shakeToLockEnabled) NeoPaletteV2.Functional.SignalGreen else EdvoColor.LightGray
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Config Button (Visible only when enabled)
                            if (shakeToLockEnabled) {
                                IconButton(onClick = onShakeConfigClick) {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = "Configure Sensitivity",
                                        tint = NeoPaletteV2.Functional.SignalGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Spacer
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Switch(
                                checked = shakeToLockEnabled,
                                onCheckedChange = { viewModel.toggleShakeToLock(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                                    uncheckedThumbColor = EdvoColor.LightGray,
                                    uncheckedTrackColor = EdvoColor.Surface,
                                    uncheckedBorderColor = EdvoColor.LightGray
                                )
                            )
                        }
                    }
                    
                    if (shakeToLockEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "▲ Shake device firmly to lock. Configure sensitivity via settings icon.",
                            style = MaterialTheme.typography.labelSmall,
                            color = EdvoColor.LightGray.copy(alpha = 0.6f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(88.dp)) // Clear bottom nav
        }
    }
}
