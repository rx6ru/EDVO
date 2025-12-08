package org.example.edvo.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EdvoDatabase.Schema.create(driver)
        return driver
    }

    actual fun deleteDatabase() {
        // In-memory DB is cleared when app restarts or driver closes.
        // Nothing to delete on disk.
    }
}
