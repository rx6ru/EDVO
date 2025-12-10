package org.example.edvo.presentation.note

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
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
    viewModel: AssetViewModel,
    onAssetClick: (String, String) -> Unit,
    onCreateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLockRequested: () -> Unit
) {
    val state by viewModel.listState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }

    // Scroll to top when sort changes
    LaunchedEffect(sortOption, sortOrder) {
        listState.scrollToItem(0)
    }

    var showSortMenu by remember { mutableStateOf(false) }

    // --- Focus State for Search ---
    var isSearchFocused by remember { mutableStateOf(false) }

    // --- State: Multi-Selection ---
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val isSelectionMode = selectedIds.isNotEmpty()
                                                                  
    // --- Hardware Back Handler ---
    // Handle both Selection Mode and Search Focus
    BackHandler(enabled = isSelectionMode || isSearchFocused) {
        if (isSelectionMode) {
            selectedIds = emptySet()
        } else if (isSearchFocused) {
            focusManager.clearFocus()
            isSearchFocused = false
        }
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
                        selectedIds.forEach { viewModel.deleteAsset(it) }
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
                        // Sort Button & Menu
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = NeoPaletteV2.AccentWhite)
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                containerColor = EdvoColor.DarkSurface,
                                modifier = Modifier.border(1.dp, NeoPaletteV2.BorderInactive, RoundedCornerShape(4.dp))
                            ) {
                                SortOption.entries.forEach { option ->
                                    val isActive = sortOption == option
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    option.name.replace("_", " "), 
                                                    style = NeoTypographyV2.DataMono(),
                                                    color = if (isActive) NeoPaletteV2.Functional.SignalGreen else NeoPaletteV2.Functional.TextSecondary
                                                )
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                        contentDescription = null,
                                                        tint = NeoPaletteV2.Functional.SignalGreen
                                                    )
                                                }
                                            }
                                        },
                                        onClick = { 
                                            viewModel.onSortChange(option)
                                            // Don't close menu immediately if toggle? No, better UX to close or stay?
                                            // User might want to toggle order. Let's keep it open?
                                            // Usually menus close. Let's verify requirement.
                                            // "clicking them again will switch... represented by up/down arrow"
                                            // implies interactivity.
                                            // Let's keep menu open if clicking same option, close if different? 
                                            // Or close always? standard behavior is close.
                                            // Let's close for now, user can reopen. It's safer.
                                            showSortMenu = false 
                                        }
                                    )
                                }
                            }
                        }
                        
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NeoPaletteV2.AccentWhite)
                        }
                        IconButton(onClick = onLockRequested) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = NeoPaletteV2.Functional.SignalRed)
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
                is AssetListState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NeoPaletteV2.Functional.SignalGreen
                )
                is AssetListState.Error -> Text(
                    "Error: ${s.message}",
                    style = NeoTypographyV2.DataMono().copy(color = NeoPaletteV2.Functional.SignalRed),
                    modifier = Modifier.align(Alignment.Center)
                )
                is AssetListState.Success -> {
                    Column {
                        // Search Box
                        NeoInput(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            label = "SEARCH ASSETS",
                            leadingIcon = {
                                if (isSearchFocused) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Close Search",
                                        tint = NeoPaletteV2.Functional.TextSecondary,
                                        modifier = Modifier.clickable { focusManager.clearFocus() }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = NeoPaletteV2.Functional.TextSecondary
                                    )
                                }
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = NeoPaletteV2.Functional.TextSecondary,
                                        modifier = Modifier
                                            .clickable { viewModel.onSearchQueryChange("") }
                                    )
                                }
                            },
                            onFocusChange = { isSearchFocused = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // Data Count Indicator
                        if (s.assets.isNotEmpty() || searchQuery.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                val label = if (searchQuery.isNotBlank()) "MATCHES" else "VAULT"
                                Text(
                                    text = "$label: ${s.assets.size}",
                                    style = NeoTypographyV2.DataMono(),
                                    color = if (searchQuery.isNotBlank() && s.assets.isEmpty()) NeoPaletteV2.Functional.SignalRed else NeoPaletteV2.Functional.TextSecondary
                                )
                            }
                        }

                        if (s.assets.isEmpty() && searchQuery.isBlank()) {
                            EmptyVaultView(mode = EmptyViewMode.NoData)
                        } else if (s.assets.isEmpty() && searchQuery.isNotBlank()) {
                            EmptyVaultView(mode = EmptyViewMode.NoResults)
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = s.assets,
                                    key = { it.id }
                                ) { asset ->
                                    val isSelected = selectedIds.contains(asset.id)

                                    AnimatedAssetItem(
                                        modifier = Modifier.animateItem()
                                    ) {
                                        NeoCard(
                                            isSelected = isSelected,
                                            onClick = {
                                                if (isSelectionMode) {
                                                    selectedIds = if (isSelected) selectedIds - asset.id else selectedIds + asset.id
                                                } else {
                                                    onAssetClick(asset.id, asset.title)
                                                }
                                            },
                                            onLongClick = {
                                                // Enter selection mode or toggle
                                                selectedIds = if (isSelected) selectedIds - asset.id else selectedIds + asset.id
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = asset.title.ifBlank { "Untitled" },
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
                                                    text = DateUtil.formatShort(asset.updatedAt),
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
fun AnimatedAssetItem(
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

enum class EmptyViewMode { NoData, NoResults }

@Composable
fun EmptyVaultView(mode: EmptyViewMode) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            CustomIcons.IconGhost,
            contentDescription = "Empty Vault",
            tint = if (mode == EmptyViewMode.NoResults) NeoPaletteV2.Functional.SignalRed else NeoPaletteV2.Functional.TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (mode == EmptyViewMode.NoResults) "NO MATCHES" else "VAULT EMPTY", 
            style = NeoTypographyV2.DataMono().copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified, fontWeight = FontWeight.Bold),
            color = if (mode == EmptyViewMode.NoResults) NeoPaletteV2.Functional.SignalRed else NeoPaletteV2.Functional.TextSecondary
        )
 
    }
}
