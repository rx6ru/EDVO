package org.example.edvo.presentation.note

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.edvo.presentation.components.util.BackHandler
import org.example.edvo.presentation.components.SecureTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    viewModel: NoteViewModel,
    noteId: String?,
    initialTitle: String? = null,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) { onBack() }

    // Local state for editing
    var title by remember { mutableStateOf(initialTitle ?: "") }
    var content by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }

    // If existing note, load data
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNoteDetail(noteId)
        } else {
            isLoaded = true // New note is "loaded" immediately
        }
    }

    // Observe DB data to populate fields once
    val noteDetail by viewModel.detailState.collectAsState()
    LaunchedEffect(noteDetail) {
        if (noteDetail != null && noteDetail!!.id == noteId && !isLoaded) {
            title = noteDetail!!.title
            content = noteDetail!!.content
            isLoaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != null) {
                        IconButton(onClick = {
                            viewModel.deleteNote(noteId)
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.saveNote(noteId, title, content)
                        onBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        if (!isLoaded) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                SecureTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Visible in List)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Metadata
                if (noteDetail != null) {
                    Text(
                        text = "LAST MODIFIED: ${org.example.edvo.util.DateUtil.formatFull(noteDetail!!.updatedAt)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                SecureTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Secure Content") },
                    modifier = Modifier.fillMaxSize(),
                    // multiline
                )
            }
        }
    }
}
