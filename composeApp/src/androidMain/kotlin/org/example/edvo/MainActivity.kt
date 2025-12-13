package org.example.edvo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidActivityTracker.currentActivity = WeakReference(this)

        // Initialize Database
        val factory = org.example.edvo.db.DatabaseDriverFactory(applicationContext)
        org.example.edvo.DependencyInjection.driverFactory = factory
        val driver = factory.createDriver()
        org.example.edvo.DependencyInjection.database = org.example.edvo.db.EdvoDatabase(driver)
        
        // Initialize Shake Detector
        org.example.edvo.DependencyInjection.shakeDetector = org.example.edvo.core.sensor.ShakeDetector(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}