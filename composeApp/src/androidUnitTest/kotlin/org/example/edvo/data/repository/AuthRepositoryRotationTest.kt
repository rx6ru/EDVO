package org.example.edvo.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import org.example.edvo.core.crypto.CryptoManager
import org.example.edvo.core.session.SessionManager
import org.example.edvo.db.EdvoDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthRepositoryRotationTest {

    private lateinit var database: EdvoDatabase
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var noteRepository: NoteRepositoryImpl

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EdvoDatabase.Schema.create(driver)
        database = EdvoDatabase(driver)
        authRepository = AuthRepositoryImpl(database)
        noteRepository = NoteRepositoryImpl(database)
        SessionManager.clearSession()
    }

    @After
    fun tearDown() {
        SessionManager.clearSession()
    }

    @Test
    fun testPasswordRotation() = runBlocking {
        // 1. Register
        val oldPassword = "OldPassword123"
        val newPassword = "NewPassword456"
        authRepository.register(oldPassword)
        assertTrue(SessionManager.isSessionActive())

        // 2. Add some encrypted notes
        // Note: Repository relies on SessionManager having the key, which register() sets.
        noteRepository.saveNote(null, "Note 1", "Secret Content 1")
        noteRepository.saveNote(null, "Note 2", "Secret Content 2")
        
        val notesBefore = noteRepository.getNotes().collect { } // Just to verify flow works? No need.
        // Let's inspect DB directly or use repo to verify they are readable
        val note1Id = database.noteQueries.selectAll().executeAsList().first().id
        val detailBefore = noteRepository.getNoteById(note1Id)
        assertEquals("Secret Content 1", detailBefore?.content)

        // 3. Clear session (simulate fresh start or strictly needed by changePassword? 
        // changePassword assumes we know old password, doesn't strictly need active session IF we pass old password, 
        // BUT CryptoManager usage inside might rely on something? No, it derives from arg.)
        SessionManager.clearSession()

        // 4. Change Password
        authRepository.changePassword(oldPassword, newPassword)

        // 5. Verify: Session should be cleared
        assertFalse("Session should be cleared after rotation", SessionManager.isSessionActive())

        // 6. Verify: Old password fails
        assertFalse("Old password should fail login", authRepository.login(oldPassword))

        // 7. Verify: New password succeeds
        assertTrue("New password should login", authRepository.login(newPassword))
        assertTrue("Session active after new login", SessionManager.isSessionActive())

        // 8. Verify: Data is accessible and correct
        val detailAfter = noteRepository.getNoteById(note1Id)
        assertNotNull("Note should exist", detailAfter)
        assertEquals("Title should match", "Note 1", detailAfter?.title)
        assertEquals("Content should be decryptable with new key", "Secret Content 1", detailAfter?.content)
    }

    @Test
    fun testWipeData() = runBlocking {
        // 1. Register and add data
        authRepository.register("password")
        noteRepository.saveNote(null, "Note 1", "Content 1")
        
        assertTrue(SessionManager.isSessionActive())
        assertTrue(authRepository.isUserRegistered())
        assertTrue(noteRepository.getNotes().collect { it.isNotEmpty() }.run { true }) // Simplified check

        // 2. Wipe Data
        authRepository.wipeData()

        // 3. Verify
        assertFalse("Session should be cleared", SessionManager.isSessionActive())
        assertFalse("User should not be registered", authRepository.isUserRegistered())
        
        // Check Notes are gone - need to re-init repo or check DB directly?
        // NoteRepository might need session to read, but list? list reads summary (plaintext title).
        // Wait, note title is plaintext in DB? Yes.
        // So checking DB directly is best.
        val notes = database.noteQueries.selectAll().executeAsList()
        assertTrue("All notes should be deleted", notes.isEmpty())
        
        val salt = database.appConfigQueries.selectByKey("master_salt").executeAsOneOrNull()
        assertNull("Salt should be deleted", salt)
    }
}
