package org.example.edvo.presentation.note

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.components.EdvoCard
import org.example.edvo.presentation.components.EdvoScaffold
import org.example.edvo.theme.EdvoColor

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

    EdvoScaffold(
        topBar = {
            TopAppBar(
                title = { Text("EDVO", color = EdvoColor.White, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EdvoColor.Background),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = EdvoColor.White)
                    }
                    TextButton(onClick = onLockRequested) {
                        Text("LOCK", color = EdvoColor.ErrorRed)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = EdvoColor.White,
                contentColor = EdvoColor.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is NoteListState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = EdvoColor.White
                )
                is NoteListState.Error -> Text(
                    "Error: ${s.message}", 
                    color = EdvoColor.ErrorRed, 
                    modifier = Modifier.align(Alignment.Center)
                )
                is NoteListState.Success -> {
                    if (s.notes.isEmpty()) {
                         Text(
                             "Add Data to your Vault", 
                             color = EdvoColor.LightGray, 
                             modifier = Modifier.align(Alignment.Center)
                         )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = s.notes,
                                key = { _, note -> note.id }
                            ) { index, note ->
                                // Staggered Entry Animation
                                val alphaAnim = remember { Animatable(0f) }
                                val slideAnim = remember { Animatable(50f) }
                                
                                LaunchedEffect(Unit) {
                                    val delay = index * 50 // 50ms stagger
                                    // kotlinx.coroutines.delay(delay.toLong()) // Simple delay, check imports
                                    // Using animation spec delay for cleaner composition
                                    alphaAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 300, delayMillis = delay)
                                    )
                                }
                                LaunchedEffect(Unit) {
                                    val delay = index * 50
                                    slideAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 300, delayMillis = delay)
                                    )
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
                                        val alignment = Alignment.CenterEnd
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(EdvoColor.ErrorRed, MaterialTheme.shapes.medium)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = alignment
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    modifier = Modifier.graphicsLayer {
                                        alpha = alphaAnim.value
                                        translationY = slideAnim.value
                                    }
                                ) {
                                    EdvoCard(
                                        onClick = { onNoteClick(note.id, note.title) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = note.title.ifBlank { "Untitled Note" },
                                                style = MaterialTheme.typography.titleMedium,
                                                color = EdvoColor.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "ID: ${note.id.take(8)}...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = EdvoColor.LightGray,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
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
