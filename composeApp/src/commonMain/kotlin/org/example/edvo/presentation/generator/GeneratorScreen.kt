package org.example.edvo.presentation.generator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.edvo.core.generator.SecurityGenerator
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import org.example.edvo.presentation.designsystem.NeoTypographyV2

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset

// ... existing imports ...

@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var copiedFeedback by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Correctly handle horizontal scroll:
    // Allow text to scroll first, but consume any "overscroll" to prevent Pager from swiping.
    val consumeHorizontalScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset, 
                available: Offset, 
                source: NestedScrollSource
            ): Offset {
                // Consume all leftover horizontal scroll so Pager doesn't get it
                return available 
            }
        }
    }

    LaunchedEffect(copiedFeedback) {
        if (copiedFeedback) {
            kotlinx.coroutines.delay(2000)
            copiedFeedback = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoPaletteV2.Canvas)
    ) {
        // --- Main Content (Scrollable) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Alignment Spacer (Scrollable)
            Spacer(modifier = Modifier.height(76.dp))

            // 1. Output Display (Card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeoPaletteV2.AccentWhite, RoundedCornerShape(12.dp))
                    .background(NeoPaletteV2.SurfaceMedium, RoundedCornerShape(12.dp))
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GENERATED ${state.mode.name}",
                    style = NeoTypographyV2.LabelSmall(),
                    color = NeoPaletteV2.Functional.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(7.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .nestedScroll(consumeHorizontalScroll), // Prevent swipe-through
                    contentAlignment = Alignment.Center
                ) {
                    val textScroll = rememberScrollState()
                    Text(
                        text = state.output.ifEmpty { "Generating..." },
                        style = NeoTypographyV2.DataMono().copy(
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = NeoPaletteV2.Functional.SignalGreen,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.horizontalScroll(textScroll) 
                    )
                }
            }

            // 2. Strength Line (Separated)
            if (state.mode != GeneratorMode.USERNAME) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NeoPaletteV2.SurfaceSecondary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(state.strength)
                            .background(
                                when {
                                    state.strength < 0.4f -> NeoPaletteV2.Functional.SignalRed
                                    state.strength < 0.7f -> Color.Yellow
                                    else -> NeoPaletteV2.Functional.SignalGreen
                                }
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // 3. Configuration (Compact)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeoPaletteV2.BorderInactive, RoundedCornerShape(12.dp))
                    .background(NeoPaletteV2.SurfaceSecondary, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp) 
            ) {
                Text(
                    "CONFIGURATION", 
                    style = NeoTypographyV2.LabelSmall(), 
                    color = NeoPaletteV2.Functional.TextSecondary
                )
                
                when(state.mode) {
                    GeneratorMode.PASSWORD -> PasswordConfig(state, viewModel)
                    GeneratorMode.PASSPHRASE -> PassphraseConfig(state, viewModel)
                    GeneratorMode.USERNAME -> UsernameConfig(state, viewModel)
                }
            }
        }

        // --- Bottom Control Deck (Pinned) ---
        val navParams = WindowInsets.navigationBars.asPaddingValues()
        val bottomNavHeight = 56.dp + 16.dp // Height + Spacer from NeoBottomBar
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomNavHeight + navParams.calculateBottomPadding() + 16.dp) // Dynamic Clearance
                .fillMaxWidth()
                .background(NeoPaletteV2.Canvas.copy(alpha = 0.95f))
                .border(
                    width = 1.dp, 
                    color = NeoPaletteV2.BorderInactive,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GeneratorMode.entries.forEach { mode ->
                        val isSelected = state.mode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp) // Slightly smaller for dense packing
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp, 
                                    color = NeoPaletteV2.BorderInactive, 
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .background(
                                    color = if (isSelected) NeoPaletteV2.AccentWhite else Color.Transparent, 
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { viewModel.setMode(mode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.name,
                                style = NeoTypographyV2.DataMono().copy(
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) NeoPaletteV2.SurfacePrimary else NeoPaletteV2.Functional.TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Row 2: Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Regenerate Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(1.dp, NeoPaletteV2.AccentWhite, RoundedCornerShape(12.dp))
                            .background(NeoPaletteV2.SurfaceSecondary, RoundedCornerShape(12.dp))
                            .clickable { viewModel.generate() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = NeoPaletteV2.AccentWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Copy Button
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(state.output))
                            copiedFeedback = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (copiedFeedback) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.AccentWhite,
                            contentColor = NeoPaletteV2.SurfacePrimary
                        )
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (copiedFeedback) "COPIED!" else "COPY",
                            style = NeoTypographyV2.ButtonText().copy(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}

// Config Composables (Reused)

@Composable
fun PasswordConfig(state: GeneratorState, viewModel: GeneratorViewModel) {
    ConfigSlider(
        label = "Length: ${state.pwLength}", 
        min = 6f, max = 64f, 
        value = state.pwLength.toFloat(),
        onValueChange = { viewModel.updatePasswordConfig(length = it.toInt(), regenerate = false) },
        onValueChangeFinished = { viewModel.generate() }
    )

    ConfigSwitch("Uppercase (A-Z)", state.pwUpper) { viewModel.updatePasswordConfig(upper = it) }
    ConfigSwitch("Lowercase (a-z)", state.pwLower) { viewModel.updatePasswordConfig(lower = it) }
    ConfigSwitch("Digits (0-9)", state.pwDigits) { viewModel.updatePasswordConfig(digits = it) }
    ConfigSwitch("Symbols (!@#)", state.pwSymbols) { viewModel.updatePasswordConfig(symbols = it) }
    ConfigSwitch("Avoid Ambiguous", state.pwAmbiguous) { viewModel.updatePasswordConfig(ambiguous = it) }
}

@Composable
fun PassphraseConfig(state: GeneratorState, viewModel: GeneratorViewModel) {
    ConfigSlider(
        label = "Words: ${state.ppWordCount}", 
        min = 3f, max = 12f, 
        value = state.ppWordCount.toFloat(),
        onValueChange = { viewModel.updatePassphraseConfig(count = it.toInt(), regenerate = false) },
        onValueChangeFinished = { viewModel.generate() }
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Separator", style = NeoTypographyV2.Body(), color = NeoPaletteV2.AccentWhite)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("-", "_", ".", " ").forEach { sep ->
                val label = if (sep == " ") "Space" else sep
                val isSelected = state.ppSeparator == sep
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .border(1.dp, if (isSelected) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.BorderInactive, RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeoPaletteV2.Functional.SignalGreen.copy(alpha=0.1f) else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { viewModel.updatePassphraseConfig(sep = sep) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label, 
                        style = NeoTypographyV2.DataMono(), 
                        color = if (isSelected) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.AccentWhite
                    )
                }
            }
        }
    }

    ConfigSwitch("Capitalize", state.ppCapitalize) { viewModel.updatePassphraseConfig(cap = it) }
    ConfigSwitch("Include Number", state.ppNumber) { viewModel.updatePassphraseConfig(num = it) }
}

@Composable
fun UsernameConfig(state: GeneratorState, viewModel: GeneratorViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Style", style = NeoTypographyV2.Body(), color = NeoPaletteV2.AccentWhite)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecurityGenerator.UsernameStyle.entries.forEach { style ->
                val isSelected = state.unStyle == style
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .border(1.dp, if (isSelected) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.BorderInactive, RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeoPaletteV2.Functional.SignalGreen.copy(alpha=0.1f) else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { viewModel.updateUsernameConfig(style = style) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style.name.replace("_", " "), 
                        style = NeoTypographyV2.DataMono(), 
                        color = if (isSelected) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.AccentWhite
                    )
                }
            }
        }
    }

    ConfigSwitch("Capitalize", state.unCapitalize) { viewModel.updateUsernameConfig(cap = it) }
    ConfigSwitch("Include Number", state.unNumber) { viewModel.updateUsernameConfig(num = it) }
}

@Composable
fun ConfigSlider(
    label: String, 
    min: Float, max: Float, value: Float, 
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Column {
        Text(label, style = NeoTypographyV2.Body(), color = NeoPaletteV2.AccentWhite)
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = NeoPaletteV2.Functional.SignalGreen,
                activeTrackColor = NeoPaletteV2.Functional.SignalGreen,
                inactiveTrackColor = NeoPaletteV2.BorderInactive
            )
        )
    }
}

@Composable
fun ConfigSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = NeoTypographyV2.Body(), color = NeoPaletteV2.AccentWhite)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeoPaletteV2.SurfacePrimary,
                checkedTrackColor = NeoPaletteV2.Functional.SignalGreen,
                uncheckedThumbColor = NeoPaletteV2.AccentWhite,
                uncheckedTrackColor = NeoPaletteV2.BorderInactive,
                uncheckedBorderColor = NeoPaletteV2.BorderInactive
            )
        )
    }
}
