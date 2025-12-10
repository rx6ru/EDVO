package org.example.edvo

import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.edvo.core.session.SessionManager
import org.example.edvo.data.repository.AuthRepositoryImpl
import org.example.edvo.data.repository.AssetRepositoryImpl
import org.example.edvo.presentation.auth.AuthScreen
import org.example.edvo.presentation.auth.AuthViewModel
import org.example.edvo.presentation.note.AssetDetailScreen
import org.example.edvo.presentation.note.AssetViewModel
import org.example.edvo.presentation.settings.ChangePasswordScreen
import org.example.edvo.presentation.settings.SettingsScreen
import org.example.edvo.presentation.designsystem.NeoTheme
import org.example.edvo.presentation.note.VaultScreen
import org.example.edvo.presentation.settings.SettingsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalTextToolbar
import org.example.edvo.presentation.components.util.EmptyTextToolbar

enum class Screen {
    AUTH, GENERATOR, ASSET_LIST, ASSET_DETAIL, SETTINGS, CHANGE_PASSWORD
}

@Composable
@Preview
fun App() {
    NeoTheme {
        // Ensure non-null capture for smart casting
        val db = DependencyInjection.database
        val driver = DependencyInjection.driverFactory
        
        if (db == null || driver == null) {
            Text("Database/Driver not initialized")
            return@NeoTheme
        }

        // Global Repositories (Stateless or Long-lived)
        val authRepository = remember { AuthRepositoryImpl(db, driver) }
        val assetRepository = remember { AssetRepositoryImpl(db) }
        val authViewModel = remember { AuthViewModel(authRepository) }

        var isSessionActive by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            if (!isSessionActive) {
                // AUTH SCREEN (Guard)
                AuthScreen(
                    viewModel = authViewModel,
                    onUnlockSuccess = { isSessionActive = true }
                )
            } else {
                // AUTHENTICATED SESSION SCOPE
                // Any state created here is destroyed when isSessionActive becomes false.
                
                // We create ViewModels here so they are destroyed on Logout.
                val assetViewModel = remember { AssetViewModel(assetRepository) }
                val settingsViewModel = remember { SettingsViewModel(authRepository) }
                val generatorViewModel = remember { org.example.edvo.presentation.generator.GeneratorViewModel() }
                
                AuthenticatedContent(
                    authViewModel = authViewModel, // Passed for resetError if needed
                    assetViewModel = assetViewModel,
                    settingsViewModel = settingsViewModel,
                    generatorViewModel = generatorViewModel,
                    onLogout = {
                        SessionManager.clearSession()
                        isSessionActive = false
                        authViewModel.resetError()
                    }
                )
            }
        }
    }
}

@Composable
fun AuthenticatedContent(
    authViewModel: AuthViewModel,
    assetViewModel: AssetViewModel,
    settingsViewModel: SettingsViewModel,
    generatorViewModel: org.example.edvo.presentation.generator.GeneratorViewModel,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.ASSET_LIST) }
    var selectedAssetId by remember { mutableStateOf<String?>(null) }
    var selectedPage by remember { mutableStateOf(1) } // Default to Vault (1)

    val copyPasteEnabled by settingsViewModel.copyPasteEnabled.collectAsState()
    
    val localTextToolbar = LocalTextToolbar.current
    val emptyTextToolbar = remember { EmptyTextToolbar }
    val localClipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val noOpClipboardManager = remember { NoOpClipboardManager() }

    // Navigation Items & Pager
    val navItems = listOf(
        org.example.edvo.presentation.components.NavigationItem("generator", "Generator", org.example.edvo.presentation.designsystem.CustomIcons.IconCycle),
        org.example.edvo.presentation.components.NavigationItem("vault", "Vault", org.example.edvo.presentation.designsystem.CustomIcons.IconVault),
        org.example.edvo.presentation.components.NavigationItem("settings", "Settings", androidx.compose.material.icons.Icons.Default.Settings)
    )
    
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 }, initialPage = 1)

    // Sync Pager -> Screen
    LaunchedEffect(pagerState.currentPage) {
        if (currentScreen in listOf(Screen.GENERATOR, Screen.ASSET_LIST, Screen.SETTINGS)) {
             when(pagerState.currentPage) {
                 0 -> currentScreen = Screen.GENERATOR
                 1 -> currentScreen = Screen.ASSET_LIST
                 2 -> currentScreen = Screen.SETTINGS
             }
        }
    }
    
    // Reverse sync removed to prevent lag. Navigation drives Pager directly.

    val currentNavItem = when (currentScreen) {
        Screen.GENERATOR -> navItems[0]
        Screen.ASSET_LIST -> navItems[1]
        Screen.SETTINGS -> navItems[2]
        else -> navItems[1]
    }
    
    val showBottomBar = currentScreen in listOf(Screen.GENERATOR, Screen.ASSET_LIST, Screen.SETTINGS)
    val coroutineScope = rememberCoroutineScope()
    
    CompositionLocalProvider(
        LocalTextToolbar provides if (copyPasteEnabled) localTextToolbar else emptyTextToolbar,
        androidx.compose.ui.platform.LocalClipboardManager provides if (copyPasteEnabled) localClipboardManager else noOpClipboardManager
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (currentScreen in listOf(Screen.GENERATOR, Screen.ASSET_LIST, Screen.SETTINGS)) {
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true
                    ) { page ->
                        when (page) {
                            0 -> org.example.edvo.presentation.generator.GeneratorScreen(viewModel = generatorViewModel)
                            1 -> VaultScreen(
                                    viewModel = assetViewModel,
                                    onAssetClick = { id, _ -> 
                                        selectedAssetId = id
                                        currentScreen = Screen.ASSET_DETAIL
                                    },
                                    onCreateClick = {
                                        selectedAssetId = null
                                        currentScreen = Screen.ASSET_DETAIL
                                    },
                                    onLockRequested = onLogout
                                )
                            2 -> {
                                val settingsState by settingsViewModel.state.collectAsState()
                                SettingsScreen(
                                    onBack = { currentScreen = Screen.ASSET_LIST },
                                    onChangePasswordClick = { currentScreen = Screen.CHANGE_PASSWORD },
                                    onBackupClick = { },
                                    onWipeSuccess = { 
                                        onLogout()
                                        // Reset state is handled by destruction
                                    },
                                    viewModel = settingsViewModel,
                                    state = settingsState
                                )
                            }
                        }
                    }
                } else {
                    // Leaf Screens
                     when (currentScreen) {
                        Screen.ASSET_DETAIL -> {
                            AssetDetailScreen(
                                viewModel = assetViewModel,
                                assetId = selectedAssetId,
                                onBack = { currentScreen = Screen.ASSET_LIST }
                            )
                        }
                        Screen.CHANGE_PASSWORD -> {
                            ChangePasswordScreen(
                                viewModel = settingsViewModel,
                                onBack = { currentScreen = Screen.SETTINGS },
                                onLogoutRequired = onLogout
                            )
                        }
                        else -> {} 
                    }
                }
            }
            
            if (showBottomBar) {
                org.example.edvo.presentation.components.NeoBottomBar(
                    items = navItems,
                    selectedItem = currentNavItem,
                    onItemSelect = { item ->
                        coroutineScope.launch {
                             when (item.id) {
                                "generator" -> pagerState.animateScrollToPage(0)
                                "vault" -> pagerState.animateScrollToPage(1)
                                "settings" -> pagerState.animateScrollToPage(2)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                )
            }
        }
    }
}

// ... NoOpClipboardManager and DependencyInjection remain same ...
class NoOpClipboardManager : androidx.compose.ui.platform.ClipboardManager {
    override fun getText(): androidx.compose.ui.text.AnnotatedString? = null
    override fun setText(annotatedString: androidx.compose.ui.text.AnnotatedString) {}
}

object DependencyInjection {
    var database: org.example.edvo.db.EdvoDatabase? = null
    var driverFactory: org.example.edvo.db.DatabaseDriverFactory? = null
}