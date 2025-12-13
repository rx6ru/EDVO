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
fun AssetDetailScreen(
    viewModel: AssetViewModel,
    generatorViewModel: org.example.edvo.presentation.generator.GeneratorViewModel,
    assetId: String?,
    initialTitle: String? = null,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) { onBack() }

    // Local state for editing
    var title by remember { mutableStateOf(initialTitle ?: "") }
    var content by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }

    // If existing asset, load data
    LaunchedEffect(assetId) {
        if (assetId != null) {
            viewModel.loadAssetDetail(assetId)
        } else {
            isLoaded = true // New asset is "loaded" immediately
        }
    }

    // Observe DB data to populate fields once
    val assetDetail by viewModel.detailState.collectAsState()
    LaunchedEffect(assetDetail) {
        if (assetDetail != null && assetDetail!!.id == assetId && !isLoaded) {
            title = assetDetail!!.title
            content = assetDetail!!.content
            isLoaded = true
        }
    }

    // State for Generator Sheet
    var showGeneratorSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (assetId == null) "New Asset" else "Edit Asset") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (assetId != null) {
                            IconButton(onClick = {
                                viewModel.deleteAsset(assetId)
                                onBack()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                        IconButton(onClick = {
                            viewModel.saveAsset(assetId, title, content)
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
                    if (assetDetail != null) {
                        Text(
                            text = "LAST MODIFIED: ${org.example.edvo.util.DateUtil.formatFull(assetDetail!!.updatedAt)}",
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

        // --- OVERLAY: Draggable Generator FAB ---
        org.example.edvo.presentation.components.DraggableGeneratorFAB(
            onClick = { showGeneratorSheet = true },
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart) // Anchored to box, position handled by internal logic
        )
        
        // --- OVERLAY: Generator Sheet ---
        if (showGeneratorSheet) {
            org.example.edvo.presentation.generator.GeneratorBottomSheet(
                viewModel = generatorViewModel,
                onDismiss = { showGeneratorSheet = false }
            )
        }
    }
}
