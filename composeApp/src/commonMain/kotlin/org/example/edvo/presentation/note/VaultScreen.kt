package org.example.edvo.presentation.note

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.components.util.BackHandler
import org.example.edvo.presentation.designsystem.*
import org.example.edvo.theme.EdvoColor
import org.example.edvo.util.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: NoteViewModel,
    onNoteClick: (String, String) -> Unit,
    onCreateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLockRequested: () -> Unit
) {
    val state by viewModel.listState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    // --- State: Multi-Selection ---
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val isSelectionMode = selectedIds.isNotEmpty()
                                                                  
    // --- Hardware Back Handler ---
    BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
    }

    // --- State: Delete Confirmation ---
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        val count = selectedIds.size
        AlertDialog(
            modifier = Modifier.border(1.dp, NeoPaletteV2.Functional.SignalRed, AlertDialogDefaults.shape),
            containerColor = EdvoColor.DarkSurface,
            titleContentColor = NeoPaletteV2.Functional.SignalRed,
            textContentColor = NeoPaletteV2.Functional.TextSecondary,
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (count > 0) "Delete $count Item${if (count > 1) "s" else ""}?" else "Delete selected?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedIds.forEach { viewModel.deleteNote(it) }
                        selectedIds = emptySet()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("DELETE", color = NeoPaletteV2.Functional.SignalRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = EdvoColor.LightGray)
                }
            }
        )
    }

    Scaffold(
        containerColor = NeoPaletteV2.Canvas,
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text(
                            "${selectedIds.size} SELECTED",
                            style = NeoTypographyV2.DataMono().copy(
                                fontWeight = FontWeight.Bold,
                                color = NeoPaletteV2.Functional.SignalRed
                            )
                        )
                    } else {
                        Text("EDVO Vault", style = NeoTypographyV2.Header())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoPaletteV2.Canvas),
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection", tint = NeoPaletteV2.AccentWhite)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = NeoPaletteV2.Functional.SignalRed)
                        }
                    } else {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NeoPaletteV2.AccentWhite)
                        }
                        TextButton(onClick = onLockRequested) {
                            Text("Lock", style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalRed))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = NeoPaletteV2.AccentWhite,
                    contentColor = NeoPaletteV2.SurfacePrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Entry")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (isSelectionMode) {
                        selectedIds = emptySet()
                    }
                    focusManager.clearFocus()
                }
        ) {
            when (val s = state) {
                is NoteListState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NeoPaletteV2.Functional.SignalGreen
                )
                is NoteListState.Error -> Text(
                    "Error: ${s.message}",
                    style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalRed),
                    modifier = Modifier.align(Alignment.Center)
                )
                is NoteListState.Success -> {
                    Column {
                        // Search Box
                        NeoInput(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            label = "SEARCH VAULT",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = NeoPaletteV2.Functional.TextSecondary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        if (s.notes.isEmpty() && searchQuery.isBlank()) {
                            EmptyVaultView()
                        } else if (s.notes.isEmpty() && searchQuery.isNotBlank()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No results found.", style = NeoTypographyV2.DataMono())
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = s.notes,
                                    key = { it.id }
                                ) { note ->
                                    val isSelected = selectedIds.contains(note.id)

                                    AnimatedNoteItem(
                                        modifier = Modifier.animateItem()
                                    ) {
                                        NeoCard(
                                            isSelected = isSelected,
                                            onClick = {
                                                if (isSelectionMode) {
                                                    selectedIds = if (isSelected) selectedIds - note.id else selectedIds + note.id
                                                } else {
                                                    onNoteClick(note.id, note.title)
                                                }
                                            },
                                            onLongClick = {
                                                // Enter selection mode or toggle
                                                selectedIds = if (isSelected) selectedIds - note.id else selectedIds + note.id
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = note.title.ifBlank { "Untitled" },
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Text(
                                                    text = DateUtil.formatShort(note.updatedAt),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                        color = Color.Gray
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Just-in-Time Entrance Animation Wrapper.
 * Animates opacity and translationY exactly when composed (entering viewport).
 */
@Composable
fun AnimatedNoteItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val slideAnim = remember { Animatable(50f) } // Start 50px down

    LaunchedEffect(Unit) {
        // Run animations in parallel
        // NO DELAY: Immediate trigger on composition
        launch {
            alphaAnim.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
        }
        launch {
            slideAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            alpha = alphaAnim.value
            translationY = slideAnim.value
        }
    ) {
        content()
    }
}

@Composable
fun EmptyVaultView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            CustomIcons.IconGhost,
            contentDescription = "Empty Vault",
            tint = NeoPaletteV2.Functional.TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "VAULT EMPTY", // Monospace as requested for empty state
            style = NeoTypographyV2.DataMono().copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified) // Adjust size if needed, or stick to DataMono style
        )
        HorizontalDivider(
            modifier = Modifier.width(32.dp).padding(vertical = 16.dp),
            color = NeoPaletteV2.BorderInactive
        )
        Text(
            "Add your data.",
            style = NeoTypographyV2.DataMono(),
            color = NeoPaletteV2.Functional.TextSecondary
        )
    }
}
