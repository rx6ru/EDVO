package org.example.edvo.presentation.generator

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.delay
import org.example.edvo.presentation.designsystem.NeoPaletteV2
import org.example.edvo.presentation.designsystem.NeoTypographyV2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorBottomSheet(
    viewModel: GeneratorViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var copiedFeedback by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(copiedFeedback) {
        if (copiedFeedback) {
            delay(1500)
            copiedFeedback = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NeoPaletteV2.Canvas,
        contentColor = NeoPaletteV2.AccentWhite,
        dragHandle = { BottomSheetDefaults.DragHandle(color = NeoPaletteV2.BorderInactive) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .animateContentSize(), // ✅ Smooth height transition
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // --- OUTPUT SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeoPaletteV2.AccentWhite, RoundedCornerShape(12.dp))
                    .background(NeoPaletteV2.SurfaceMedium, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.output.ifEmpty { "Generating..." },
                    style = NeoTypographyV2.DataMono().copy(
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = NeoPaletteV2.Functional.SignalGreen,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                
                // ✅ Strength Indicator (creative bottom bar)
                if (state.mode != GeneratorMode.USERNAME) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(NeoPaletteV2.SurfacePrimary)
                    ) {
                        val strengthColor = when {
                            state.strength < 0.4f -> NeoPaletteV2.Functional.SignalRed
                            state.strength < 0.7f -> Color.Yellow
                            else -> NeoPaletteV2.Functional.SignalGreen
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(state.strength)
                                .background(strengthColor)
                        )
                    }
                }
            }
            
            // --- MODE TABS (✅ Full Names) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeneratorMode.entries.forEach { mode ->
                    val isSelected = state.mode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .border(
                                1.dp, 
                                if (isSelected) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.BorderInactive, 
                                RoundedCornerShape(18.dp)
                            )
                            .background(
                                if (isSelected) NeoPaletteV2.Functional.SignalGreen.copy(alpha = 0.1f) else Color.Transparent, 
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { viewModel.setMode(mode) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name, // ✅ Full name: PASSWORD, PASSPHRASE, USERNAME
                            style = NeoTypographyV2.DataMono().copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (isSelected) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.Functional.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // --- ACTIONS (✅ 1/4 Regen, 3/4 Copy, ✅ Centered Icons) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Regen Button (1/4 width, icon only, centered)
                Button(
                    onClick = { viewModel.generate() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp), // Remove default padding for centering
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeoPaletteV2.SurfaceSecondary,
                        contentColor = NeoPaletteV2.AccentWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeoPaletteV2.BorderInactive)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
                    }
                }
                
                // Copy Button (3/4 width, icon + text, centered)
                Button(
                    onClick = { 
                        clipboardManager.setText(AnnotatedString(state.output))
                        copiedFeedback = true
                    },
                    modifier = Modifier.weight(3f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (copiedFeedback) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.AccentWhite,
                        contentColor = NeoPaletteV2.SurfacePrimary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ContentCopy, 
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (copiedFeedback) "COPIED" else "COPY", 
                            style = NeoTypographyV2.ButtonText()
                        )
                    }
                }
            }

            // --- CONFIG ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeoPaletteV2.BorderInactive, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp) 
            ) {
                Text(
                    text = "OPTIONS", 
                    style = NeoTypographyV2.LabelSmall(), 
                    color = NeoPaletteV2.Functional.TextSecondary
                )
                
                when (state.mode) {
                    GeneratorMode.PASSWORD -> PasswordConfig(state, viewModel)
                    GeneratorMode.PASSPHRASE -> PassphraseConfig(state, viewModel)
                    GeneratorMode.USERNAME -> UsernameConfig(state, viewModel)
                }
            }
        }
    }
}
