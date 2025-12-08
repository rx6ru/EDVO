package org.example.edvo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // Initialize Database
    val driver = org.example.edvo.db.DatabaseDriverFactory().createDriver()
    org.example.edvo.DependencyInjection.database = org.example.edvo.db.EdvoDatabase(driver)

    Window(
        onCloseRequest = ::exitApplication,
        title = "EDVO",
    ) {
        App()
    }
}