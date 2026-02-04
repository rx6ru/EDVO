package org.example.edvo.system

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.example.edvo.core.session.SessionManager
import org.example.edvo.data.repository.AssetRepositoryImpl
import org.example.edvo.data.repository.AuthRepositoryImpl
import org.example.edvo.db.EdvoDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SystemFlowTest {

    private lateinit var database: EdvoDatabase
    private lateinit var authRepo: AuthRepositoryImpl
    private lateinit var assetRepo: AssetRepositoryImpl

    private val MASTER_PASSWORD = "CorrectHorseBatteryStaple"

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EdvoDatabase.Schema.create(driver)
        database = EdvoDatabase(driver)
        authRepo = AuthRepositoryImpl(database)
        assetRepo = AssetRepositoryImpl(database)
        SessionManager.clearSession()
    }

    @After
    fun tearDown() {
        SessionManager.clearSession()
    }

    @Test
    fun flow_FreshStart() = runBlocking {
        // 1. Install App (Setup) -> User is not registered
        assertFalse(authRepo.isUserRegistered())

        // 2. Open -> Set Master Password
        authRepo.register(MASTER_PASSWORD)
        assertTrue(authRepo.isUserRegistered())
        assertTrue(SessionManager.isSessionActive())

        // 3. Land on Empty Vault
        val assets = assetRepo.getAssets().first()
        assertTrue("Vault should be empty on fresh start", assets.isEmpty())
    }

    @Test
    fun flow_DailyDrive() = runBlocking {
        // Setup: User Registered & Logged In
        authRepo.register(MASTER_PASSWORD)

        // 1. Add Asset
        assetRepo.saveAsset(null, "Grocery List", "Milk, Eggs, Bread")
        
        // 2. Scroll List (Verify it appears)
        var assets = assetRepo.getAssets().first()
        assertEquals(1, assets.size)
        assertEquals("Grocery List", assets[0].title)
        
        // 3. Edit Asset
        val id = assets[0].id
        assetRepo.saveAsset(id, "Grocery List", "Milk, Eggs, Bread, Coffee")
        
        // 4. Verify Content
        val detail = assetRepo.getAssetById(id)
        assertEquals("Milk, Eggs, Bread, Coffee", detail?.content)
        
        // 5. Search (Simulated by filtering logic, usually in VM but Repo returns all)
        // AssetRepository returns all, filtering happens in VM. 
        // We verify data integrity here.
        assets = assetRepo.getAssets().first()
        assertEquals(1, assets.size)
    }

    @Test
    fun flow_DisasterRecovery() = runBlocking {
        // 1. Setup Data
        authRepo.register(MASTER_PASSWORD)
        assetRepo.saveAsset(null, "Critical Info", "Launch Codes: 12345")
        assetRepo.saveAsset(null, "Memories", "Photo of cat")
        
        val originalAssets = assetRepo.getAssets().first()
        assertEquals(2, originalAssets.size)

        // 2. Export Backup
        val backupBytes = authRepo.exportBackup(MASTER_PASSWORD)
        assertTrue("Backup should not be empty", backupBytes.isNotEmpty())
        
        // 3. DISASTER: Wipe Data (Accidental Delete)
        authRepo.wipeData()
        assertFalse(authRepo.isUserRegistered())
        assertTrue(assetRepo.getAssets().first().isEmpty())

        // 4. Reinstall / Restore
        // Register first? Or Restore creates user? 
        // ImportBackup usually requires an active session in current implementation?
        // Let's check AuthRepositoryImpl.importBackup:
        // "val sessionKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Unlock vault first")"
        // This implies User must REGISTER first to have a session key to encrypt the imported data into.
        
        // So User Reinstalls -> Registers (Fresh Start)
        authRepo.register(MASTER_PASSWORD)
        
        // 5. Import Backup
        authRepo.importBackup(MASTER_PASSWORD, backupBytes)
        
        // 6. Verify Vault Restored
        val restoredAssets = assetRepo.getAssets().first()
        assertEquals(2, restoredAssets.size)
        
        // Verify Content Integrity
        val criticalAsset = restoredAssets.find { it.title == "Critical Info" }
        assertNotNull(criticalAsset)
        val detail = assetRepo.getAssetById(criticalAsset!!.id)
        assertEquals("Launch Codes: 12345", detail?.content)
    }

    @Test
    fun flow_PanicMode() = runBlocking {
        // 1. Setup
        authRepo.register(MASTER_PASSWORD)
        assetRepo.saveAsset(null, "Secret details", "Hidden")
        
        // 2. Trigger Kill Switch
        authRepo.wipeData()
        
        // 3. Verify App Restarts/Crashes (Simulated by state check)
        // Session cleared
        assertFalse(SessionManager.isSessionActive())
        
        // 4. Verify Login Fails
        assertFalse(authRepo.login(MASTER_PASSWORD))
        
        // 5. Verify Data Gone
        // To verify data gone without session (which throws), we check DB directly or try login
        // Since login fails, regular flow prevents access. 
        // Checking low level:
        val rawCount = database.assetQueries.selectAll().executeAsList().size
        assertEquals("Database should be empty", 0, rawCount)
    }
}
