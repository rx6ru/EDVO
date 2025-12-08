package org.example.edvo.presentation.note

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.designsystem.*
import org.example.edvo.theme.EdvoColor

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

    Scaffold(
        containerColor = NeoPaletteV2.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("EDVO Vault", style = NeoTypographyV2.Header()) }, // Version removed
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoPaletteV2.Canvas),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NeoPaletteV2.AccentWhite)
                    }
                    TextButton(onClick = onLockRequested) {
                        Text("Lock", style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalRed))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = NeoPaletteV2.AccentWhite,
                contentColor = NeoPaletteV2.SurfacePrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Entry")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clickable(interactionSource = interactionSource, indication = null) {
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
                            // Only show Empty State if no notes at all AND no search query
                            EmptyVaultView()
                        } else if (s.notes.isEmpty() && searchQuery.isNotBlank()) {
                             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                 Text("No results found.", style = NeoTypographyV2.DataMono())
                             }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(
                                    items = s.notes,
                                    key = { _, note -> note.id }
                                ) { index, note ->
                                    // Staggered Entry Animation (75ms as requested)
                                    val slideAnim = remember { Animatable(100f) }
                                    val alphaAnim = remember { Animatable(0f) }
                                    
                                    LaunchedEffect(Unit) {
                                        val delay = index * 75
                                        slideAnim.animateTo(0f, tween(400, delay))
                                    }
                                    LaunchedEffect(Unit) {
                                        val delay = index * 75
                                        alphaAnim.animateTo(1f, tween(400, delay))
                                    }

                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                                viewModel.deleteNote(note.id)
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                    )

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer {
                                                        shape = NeoCardShape
                                                        clip = true
                                                    }
                                                    .background(NeoPaletteV2.Functional.SignalRed, NeoCardShape)
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.White
                                                )
                                            }
                                        },
                                        modifier = Modifier.graphicsLayer {
                                            translationY = slideAnim.value
                                            alpha = alphaAnim.value
                                        }
                                    ) {
                                        NeoCard(
                                            onClick = { onNoteClick(note.id, note.title) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = note.title.ifBlank { "Untitled" },
                                                        style = NeoTypographyV2.BodyAction()
                                                    )
                                                    // Simulated "Timestamp" or metadata
                                                    Text(
                                                        "Encrypted",
                                                        style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalGreen)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "ID: ${note.id.take(8)}",
                                                    style = NeoTypographyV2.DataMono()
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

@Composable
fun EmptyVaultView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
         // Use Ghost Icon for empty state
         Icon(
             CustomIcons.IconGhost,
             contentDescription = "Empty Vault",
             tint = NeoPaletteV2.Functional.TextSecondary,
             modifier = Modifier.size(64.dp)
         )
         Spacer(modifier = Modifier.height(16.dp))
         Text(
             "Vault Empty", 
             style = NeoTypographyV2.Header()
         )
         HorizontalDivider(
             modifier = Modifier.width(32.dp).padding(vertical = 16.dp),
             color = NeoPaletteV2.BorderInactive
         )
         Text(
             "Add your data.", 
             style = NeoTypographyV2.DataMono()
         )
    }
}
