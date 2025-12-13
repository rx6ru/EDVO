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
import org.example.edvo.core.sensor.ShakeConfig
import org.example.edvo.presentation.components.EdvoButton
import org.example.edvo.presentation.components.EdvoButtonType
import org.example.edvo.presentation.components.EdvoCard
import org.example.edvo.presentation.components.EdvoScaffold
import org.example.edvo.presentation.components.util.BackHandler
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import org.example.edvo.theme.EdvoColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShakeSensitivityScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    BackHandler(enabled = true) { onBack() }
    
    val config by viewModel.shakeConfig.collectAsState()

    EdvoScaffold(
        topBar = {
            TopAppBar(
                title = { Text("SHAKE SENSITIVITY", color = EdvoColor.White, style = MaterialTheme.typography.titleLarge) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // G-Force Threshold
            ConfigSliderCard(
                title = "Force Required",
                description = "How hard you need to shake the device",
                value = config.gForceThreshold,
                valueLabel = "${String.format("%.1f", config.gForceThreshold)}g",
                minValue = ShakeConfig.MIN_G_FORCE,
                maxValue = ShakeConfig.MAX_G_FORCE,
                steps = 49, // 0.1 increments for 1.0-6.0 range
                onValueChange = { viewModel.updateShakeConfig(gForce = it) },
                leftLabel = "Lighter",
                rightLabel = "Harder"
            )
            
            // Shake Count
            ConfigSliderCard(
                title = "Shake Count",
                description = "Number of shakes needed to trigger lock",
                value = config.shakeCount.toFloat(),
                valueLabel = "${config.shakeCount} shakes",
                minValue = ShakeConfig.MIN_SHAKE_COUNT.toFloat(),
                maxValue = ShakeConfig.MAX_SHAKE_COUNT.toFloat(),
                steps = ShakeConfig.MAX_SHAKE_COUNT - ShakeConfig.MIN_SHAKE_COUNT - 1,
                onValueChange = { viewModel.updateShakeConfig(count = it.roundToInt()) },
                leftLabel = "Fewer",
                rightLabel = "More"
            )
            
            // Time Window
            ConfigSliderCard(
                title = "Pattern Speed",
                description = "Time allowed between consecutive shakes",
                value = config.timeWindow.toFloat(),
                valueLabel = "${config.timeWindow}ms",
                minValue = ShakeConfig.MIN_TIME_WINDOW.toFloat(),
                maxValue = ShakeConfig.MAX_TIME_WINDOW.toFloat(),
                steps = 17, // 100ms increments for 200-2000 range
                onValueChange = { viewModel.updateShakeConfig(window = it.roundToInt()) },
                leftLabel = "Faster",
                rightLabel = "Slower"
            )
            
            // Cooldown
            ConfigSliderCard(
                title = "Wait After Lock",
                description = "Cooldown before next shake can trigger",
                value = config.cooldown.toFloat(),
                valueLabel = "${config.cooldown / 1000.0}s",
                minValue = ShakeConfig.MIN_COOLDOWN.toFloat(),
                maxValue = ShakeConfig.MAX_COOLDOWN.toFloat(),
                steps = 19, // 500ms increments for 200-10000 range
                onValueChange = { viewModel.updateShakeConfig(cooldown = it.roundToInt()) },
                leftLabel = "Shorter",
                rightLabel = "Longer"
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Reset Button
            EdvoButton(
                text = "Reset to Defaults",
                onClick = { viewModel.resetShakeConfigToDefaults() },
                type = EdvoButtonType.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(88.dp)) // Clear bottom nav
        }
    }
}

@Composable
private fun ConfigSliderCard(
    title: String,
    description: String,
    value: Float,
    valueLabel: String,
    minValue: Float,
    maxValue: Float,
    steps: Int,
    onValueChange: (Float) -> Unit,
    leftLabel: String,
    rightLabel: String
) {
    EdvoCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = EdvoColor.White)
                Text(valueLabel, style = MaterialTheme.typography.bodyLarge, color = NeoPaletteV2.Functional.SignalGreen)
            }
            
            Text(description, style = MaterialTheme.typography.bodySmall, color = EdvoColor.LightGray)
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = minValue..maxValue,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = NeoPaletteV2.Functional.SignalGreen,
                    activeTrackColor = NeoPaletteV2.Functional.SignalGreen,
                    inactiveTrackColor = EdvoColor.Surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(leftLabel, style = MaterialTheme.typography.labelSmall, color = EdvoColor.LightGray.copy(alpha = 0.6f))
                Text(rightLabel, style = MaterialTheme.typography.labelSmall, color = EdvoColor.LightGray.copy(alpha = 0.6f))
            }
        }
    }
}
