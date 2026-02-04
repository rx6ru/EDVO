package org.example.edvo.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.example.edvo.core.session.SessionManager
import org.example.edvo.db.EdvoDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.example.edvo.core.crypto.CryptoManager

class PersistenceTest {

    private lateinit var database: EdvoDatabase
    private lateinit var assetRepository: AssetRepositoryImpl
    private lateinit var authRepository: AuthRepositoryImpl
    
    // Test Key
    private val TEST_PASSWORD = "StrongTestPassword123!"
    private val ASSET_TITLE = "Integration Test Asset"
    private val ASSET_CONTENT = "This content should be encrypted and persisted."

    @Before
    fun setUp() = runBlocking {
        // Use In-Memory Driver for Integration Testing
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EdvoDatabase.Schema.create(driver)
        database = EdvoDatabase(driver)
        
        assetRepository = AssetRepositoryImpl(database)
        authRepository = AuthRepositoryImpl(database)
        
        // Ensure clean state
        SessionManager.clearSession()
        
        // Setup User & Session
        authRepository.register(TEST_PASSWORD)
        assertTrue("Session should be active after register", SessionManager.isSessionActive())
    }

    @After
    fun tearDown() {
        SessionManager.clearSession()
    }

    @Test
    fun testAssetLifecycle_CRUD() = runBlocking {
        // 1. CREATE
        assetRepository.saveAsset(null, ASSET_TITLE, ASSET_CONTENT)
        
        // Verify list updates
        val list1 = assetRepository.getAssets().first()
        assertEquals(1, list1.size)
        assertEquals(ASSET_TITLE, list1[0].title)
        val createdId = list1[0].id
        
        // 2. READ (with Decryption)
        val detail = assetRepository.getAssetById(createdId)
        assertNotNull(detail)
        assertEquals(ASSET_TITLE, detail?.title)
        assertEquals(ASSET_CONTENT, detail?.content)
        
        // 3. VERIFY ENCRYPTION IN DB
        // Query DB directly to ensure content is NOT plain text
        val rawEntity = database.assetQueries.getById(createdId).executeAsOne()
        val plainBytes = ASSET_CONTENT.encodeToByteArray()
        // Content should NOT be equal to plain bytes (probabilistic check, but generally safe)
        assertFalse("DB content should be encrypted", 
            rawEntity.content_encrypted.contentEquals(plainBytes))

        // 4. UPDATE
        val updatedContent = "Updated Secret Content"
        assetRepository.saveAsset(createdId, "Updated Title", updatedContent)
        
        // Verify Update
        val updatedDetail = assetRepository.getAssetById(createdId)
        assertEquals("Updated Title", updatedDetail?.title)
        assertEquals(updatedContent, updatedDetail?.content)
        
        // 5. DELETE
        assetRepository.deleteAsset(createdId)
        
        val list2 = assetRepository.getAssets().first()
        assertTrue(list2.isEmpty())
        
        val deletedDetail = assetRepository.getAssetById(createdId)
        assertNull(deletedDetail)
    }

    @Test
    fun testSessionLockout_PreventsAccess() = runBlocking {
        // 1. Create Data
        assetRepository.saveAsset(null, "Locked Asset", "Locked Content")
        val id = assetRepository.getAssets().first().first().id
        
        // 2. Lockout
        SessionManager.clearSession()
        assertFalse(SessionManager.isSessionActive())
        
        // 3. Attempt Access -> Should Fail or Error
        // AssetRepositoryImpl.getAssetById throws/returns error when session is locked
        try {
            val result = assetRepository.getAssetById(id)
            // Expecting null or an error object depending on implementation?
            // AuthRepositoryImpl allows access only with session key.
            // Let's check implementation behavior: 
            // AssetRepositoryImpl line 70: val masterKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Session locked")
            // It runs in try/catch? No, getAssetById has try/catch around decryption, but getMasterKey check is BEFORE try/catch?
            // Checking file: Line 41: val masterKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Session locked")
            // This is inside withContext.
            // So it should throw.
            fail("Should throw IllegalStateException exception due to locked session")
        } catch (e: IllegalStateException) {
            assertEquals("Session locked", e.message)
        } catch (e: Exception) {
            // Also acceptable if wrapped
             assertTrue(e.message?.contains("Session locked") == true)
        }
    }
}
