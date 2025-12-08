package org.example.edvo.db

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        throw NotImplementedError("iOS implementation is stubbed on Windows dev environment")
    }
}
