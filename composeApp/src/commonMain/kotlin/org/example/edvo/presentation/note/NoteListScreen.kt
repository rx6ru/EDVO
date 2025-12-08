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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onNoteClick: (String, String) -> Unit,
    onCreateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLockRequested: () -> Unit
) {
    val state by viewModel.listState.collectAsState()

    Scaffold(
        containerColor = NeoPalette.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("EDVØ", style = NeoTypography.Header) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoPalette.Canvas),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NeoPalette.Primary)
                    }
                    TextButton(onClick = onLockRequested) {
                        Icon(CustomIcons.IconVault, contentDescription = "Lock", tint = NeoPalette.Functional.Destructive)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = NeoPalette.Primary,
                contentColor = NeoPalette.OnPrimary,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is NoteListState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NeoPalette.Primary
                )
                is NoteListState.Error -> Text(
                    "SYSTEM ERROR: ${s.message}", 
                    style = NeoTypography.DataLabel.copy(color = NeoPalette.Functional.Destructive), 
                    modifier = Modifier.align(Alignment.Center)
                )
                is NoteListState.Success -> {
                    if (s.notes.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                             Icon(
                                 CustomIcons.IconGhost, 
                                 contentDescription = "Empty", 
                                 tint = Color(0xFF888888),
                                 modifier = Modifier.size(64.dp)
                             )
                             Spacer(modifier = Modifier.height(16.dp))
                             Text(
                                 "NO_DATA_FOUND", 
                                 style = NeoTypography.DataLabel
                             )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(
                                items = s.notes,
                                key = { _, note -> note.id }
                            ) { index, note ->
                                // Staggered Entry Animation
                                val alphaAnim = remember { Animatable(0f) }
                                val slideAnim = remember { Animatable(100f) }
                                
                                LaunchedEffect(Unit) {
                                    val delay = index * 50
                                    alphaAnim.animateTo(1f, tween(300, delay))
                                }
                                LaunchedEffect(Unit) {
                                    val delay = index * 50
                                    slideAnim.animateTo(0f, tween(300, delay))
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
                                                .background(NeoPalette.Functional.Destructive, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                                .padding(horizontal = 24.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                CustomIcons.IconKill,
                                                contentDescription = "Delete",
                                                tint = NeoPalette.Primary
                                            )
                                        }
                                    },
                                    modifier = Modifier.graphicsLayer {
                                        alpha = alphaAnim.value
                                        translationY = slideAnim.value
                                    }
                                ) {
                                    NeoCard(
                                        onClick = { onNoteClick(note.id, note.title) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = note.title.ifBlank { "UNTITLED_ENTRY" },
                                                style = NeoTypography.Body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "ID_HASH: ${note.id.take(8)}",
                                                style = NeoTypography.DataLabel
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
