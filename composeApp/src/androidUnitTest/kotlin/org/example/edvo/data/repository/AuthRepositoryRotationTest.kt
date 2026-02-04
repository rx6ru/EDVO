package org.example.edvo.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
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
    private lateinit var assetRepository: AssetRepositoryImpl

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EdvoDatabase.Schema.create(driver)
        database = EdvoDatabase(driver)
        authRepository = AuthRepositoryImpl(database)
        assetRepository = AssetRepositoryImpl(database)
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

        // 2. Add some encrypted assets
        assetRepository.saveAsset(null, "Note 1", "Secret Content 1")
        assetRepository.saveAsset(null, "Note 2", "Secret Content 2")
        
        // Verify assets exist
        val assetsBefore = assetRepository.getAssets().first()
        assertTrue(assetsBefore.isNotEmpty())
        
        // Find asset by title (don't rely on order from selectAll)
        val allAssets = database.assetQueries.selectAll().executeAsList()
        val asset1 = allAssets.find { it.title == "Note 1" }!!
        val asset1Id = asset1.id
        val detailBefore = assetRepository.getAssetById(asset1Id)
        assertEquals("Secret Content 1", detailBefore?.content)

        // 3. Clear session
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
        val detailAfter = assetRepository.getAssetById(asset1Id)
        println("DEBUG: detailAfter = $detailAfter")
        println("DEBUG: content = ${detailAfter?.content}")
        
        assertNotNull("Asset should exist", detailAfter)
        assertEquals("Title should match", "Note 1", detailAfter?.title)
        assertEquals("Content should be decryptable with new key", "Secret Content 1", detailAfter?.content)
    }

    @Test
    fun testWipeData() = runBlocking {
        // 1. Register and add data
        authRepository.register("password")
        assetRepository.saveAsset(null, "Note 1", "Content 1")
        
        assertTrue(SessionManager.isSessionActive())
        assertTrue(authRepository.isUserRegistered())
        
        val assets = assetRepository.getAssets().first()
        assertTrue(assets.isNotEmpty())

        // 2. Wipe Data
        authRepository.wipeData()

        // 3. Verify
        assertFalse("Session should be cleared", SessionManager.isSessionActive())
        assertFalse("User should not be registered", authRepository.isUserRegistered())
        
        // Check Assets are gone
        val remainingAssets = database.assetQueries.selectAll().executeAsList()
        assertTrue("All assets should be deleted", remainingAssets.isEmpty())
        
        val salt = database.appConfigQueries.selectByKey("master_salt").executeAsOneOrNull()
        assertNull("Salt should be deleted", salt)
    }
}
