package org.example.edvo.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(EdvoDatabase.Schema, context, "edvo.db")
    }

    actual fun deleteDatabase() {
        context.deleteDatabase("edvo.db")
    }
}
