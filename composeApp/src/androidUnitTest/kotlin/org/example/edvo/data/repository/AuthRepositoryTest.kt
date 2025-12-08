package org.example.edvo.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import org.example.edvo.core.session.SessionManager
import org.example.edvo.db.EdvoDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class AuthRepositoryTest {

    private lateinit var database: EdvoDatabase
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EdvoDatabase.Schema.create(driver)
        database = EdvoDatabase(driver)
        repository = AuthRepositoryImpl(database)
        SessionManager.clearSession()
    }

    @After
    fun tearDown() {
        SessionManager.clearSession()
    }

    @Test
    fun testRegistrationAndLoginFlow() = runBlocking {
        assertFalse("User should not be registered initially", repository.isUserRegistered())

        val password = "StrongMasterPassword123!"
        repository.register(password)

        assertTrue("User should be registered after calls", repository.isUserRegistered())
        assertTrue("Session should be active after registration", SessionManager.isSessionActive())

        // Clear session to test login
        SessionManager.clearSession()
        assertFalse("Session should be inactive", SessionManager.isSessionActive())

        // Test Login Success
        val loginSuccess = repository.login(password)
        assertTrue("Login with correct password should succeed", loginSuccess)
        assertTrue("Session should be active after login", SessionManager.isSessionActive())
    }

    @Test
    fun testLoginFailure() = runBlocking {
        val password = "StrongMasterPassword123!"
        repository.register(password)
        SessionManager.clearSession()

        // Test Login Failure
        val wrongPassword = "WrongPassword"
        val loginSuccess = repository.login(wrongPassword)
        assertFalse("Login with wrong password should fail", loginSuccess)
        assertFalse("Session should remain inactive", SessionManager.isSessionActive())
    }
}
