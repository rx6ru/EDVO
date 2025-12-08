package org.example.edvo.presentation.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onNoteClick: (String, String) -> Unit, // id, title (for transition if needed)
    onCreateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLockRequested: () -> Unit
) {
    val state by viewModel.listState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EDVØ") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    Button(onClick = onLockRequested) {
                        Text("Lock")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is NoteListState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is NoteListState.Error -> Text("Error: ${s.message}", modifier = Modifier.align(Alignment.Center))
                is NoteListState.Success -> {
                    if (s.notes.isEmpty()) {
                        Text("No notes yet.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(s.notes) { note ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { onNoteClick(note.id, note.title) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(note.title, style = MaterialTheme.typography.titleMedium)
                                        // No content preview here - strict privacy
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
