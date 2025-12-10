package org.example.edvo

import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    AUTH, ASSET_LIST, ASSET_DETAIL, SETTINGS, CHANGE_PASSWORD
}

@Composable
@Preview
fun App() {
    NeoTheme {
        var currentScreen by remember { mutableStateOf(Screen.AUTH) }
        var selectedAssetId by remember { mutableStateOf<String?>(null) }
        
        // Ensure non-null capture for smart casting
        val db = DependencyInjection.database
        val driver = DependencyInjection.driverFactory
        
        if (db == null || driver == null) {
            Text("Database/Driver not initialized")
            return@NeoTheme
        }

        val authRepository = remember { AuthRepositoryImpl(db, driver) }
        val assetRepository = remember { AssetRepositoryImpl(db) }
        
        val authViewModel = remember { AuthViewModel(authRepository) }
        val assetViewModel = remember { AssetViewModel(assetRepository) }
        val settingsViewModel = remember { SettingsViewModel(authRepository) }
        
        val copyPasteEnabled by settingsViewModel.copyPasteEnabled.collectAsState()
        
        val localTextToolbar = LocalTextToolbar.current
        val emptyTextToolbar = remember { EmptyTextToolbar }
        
        val localClipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val noOpClipboardManager = remember { NoOpClipboardManager() }
        
        CompositionLocalProvider(
            LocalTextToolbar provides if (copyPasteEnabled) localTextToolbar else emptyTextToolbar,
            androidx.compose.ui.platform.LocalClipboardManager provides if (copyPasteEnabled) localClipboardManager else noOpClipboardManager
        ) {
            when (currentScreen) {
                Screen.AUTH -> {
                    AuthScreen(
                        viewModel = authViewModel,
                        onUnlockSuccess = { currentScreen = Screen.ASSET_LIST }
                    )
                }
                Screen.ASSET_LIST -> {
                    VaultScreen(
                        viewModel = assetViewModel,
                        onAssetClick = { id, _ -> 
                            selectedAssetId = id
                            currentScreen = Screen.ASSET_DETAIL
                        },
                        onCreateClick = {
                            selectedAssetId = null
                            currentScreen = Screen.ASSET_DETAIL
                        },
                        onSettingsClick = {
                                currentScreen = Screen.SETTINGS
                        },
                        onLockRequested = { 
                            SessionManager.clearSession()
                            currentScreen = Screen.AUTH 
                            authViewModel.resetError()
                        }
                    )
                }
                Screen.ASSET_DETAIL -> {
                    AssetDetailScreen(
                        viewModel = assetViewModel,
                        assetId = selectedAssetId,
                        onBack = { currentScreen = Screen.ASSET_LIST }
                    )
                }
                Screen.SETTINGS -> {
                    val settingsState by settingsViewModel.state.collectAsState()
                    SettingsScreen(
                        onBack = { currentScreen = Screen.ASSET_LIST },
                        onChangePasswordClick = { currentScreen = Screen.CHANGE_PASSWORD },
                        onBackupClick = { /* Handled in Screen */ },
                        onWipeSuccess = { 
                            currentScreen = Screen.AUTH 
                            // Ensure fresh state
                            authViewModel.resetError()
                            settingsViewModel.resetState()
                        },
                        viewModel = settingsViewModel,
                        state = settingsState
                    )
                }
                Screen.CHANGE_PASSWORD -> {
                    ChangePasswordScreen(
                        viewModel = settingsViewModel,
                        onBack = { currentScreen = Screen.SETTINGS },
                        onLogoutRequired = {
                            currentScreen = Screen.AUTH
                            authViewModel.resetError()
                        }
                    )
                }
            }
        }
    }
}

// EmptyTextToolbar moved to org.example.edvo.presentation.components.util

class NoOpClipboardManager : androidx.compose.ui.platform.ClipboardManager {
    override fun getText(): androidx.compose.ui.text.AnnotatedString? = null
    override fun setText(annotatedString: androidx.compose.ui.text.AnnotatedString) {}
}

object DependencyInjection {
    var database: org.example.edvo.db.EdvoDatabase? = null
    var driverFactory: org.example.edvo.db.DatabaseDriverFactory? = null
}