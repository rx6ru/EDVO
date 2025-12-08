package org.example.edvo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import org.example.edvo.core.session.SessionManager
import org.example.edvo.data.repository.AuthRepositoryImpl
import org.example.edvo.data.repository.NoteRepositoryImpl
import org.example.edvo.presentation.auth.AuthScreen
import org.example.edvo.presentation.auth.AuthViewModel
import org.example.edvo.presentation.note.NoteDetailScreen
import org.example.edvo.presentation.note.NoteListScreen
import org.example.edvo.presentation.note.NoteViewModel
import org.example.edvo.presentation.settings.ChangePasswordScreen
import org.example.edvo.presentation.settings.SettingsScreen
import org.example.edvo.presentation.settings.SettingsViewModel
import org.example.edvo.theme.EdvoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Screen {
    AUTH, NOTE_LIST, NOTE_DETAIL, SETTINGS, CHANGE_PASSWORD
}

@Composable
@Preview
fun App() {
    EdvoTheme {
        var currentScreen by remember { mutableStateOf(Screen.AUTH) }
        var selectedNoteId by remember { mutableStateOf<String?>(null) }
        
        val database = DependencyInjection.database
        if (database == null) {
            Text("Database not initialized")
            return@EdvoTheme
        }

        val authRepository = remember { AuthRepositoryImpl(database, DependencyInjection.driverFactory) }
        val noteRepository = remember { NoteRepositoryImpl(database) }
        
        val authViewModel = remember { AuthViewModel(authRepository) }
        val noteViewModel = remember { NoteViewModel(noteRepository) }
        val settingsViewModel = remember { SettingsViewModel(authRepository) }
        
        when (currentScreen) {
            Screen.AUTH -> {
                AuthScreen(
                    viewModel = authViewModel,
                    onUnlockSuccess = { currentScreen = Screen.NOTE_LIST }
                )
            }
            Screen.NOTE_LIST -> {
                NoteListScreen(
                    viewModel = noteViewModel,
                    onNoteClick = { id, _ -> 
                        selectedNoteId = id
                        currentScreen = Screen.NOTE_DETAIL
                    },
                    onCreateClick = {
                        selectedNoteId = null
                        currentScreen = Screen.NOTE_DETAIL
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
            Screen.NOTE_DETAIL -> {
                NoteDetailScreen(
                    viewModel = noteViewModel,
                    noteId = selectedNoteId,
                    onBack = { currentScreen = Screen.NOTE_LIST }
                )
            }
            Screen.SETTINGS -> {
                val settingsState by settingsViewModel.state.collectAsState()
                SettingsScreen(
                    onBack = { currentScreen = Screen.NOTE_LIST },
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

object DependencyInjection {
    var database: org.example.edvo.db.EdvoDatabase? = null
    var driverFactory: org.example.edvo.db.DatabaseDriverFactory? = null
}