package org.example.edvo

import android.app.Activity
import android.view.WindowManager
import java.lang.ref.WeakReference

// Global reference holder - simple hack for static access from Composable context if needed, 
// though ideal way is passing Activity to App, but 'expect' functions are static.
object AndroidActivityTracker {
    var currentActivity: WeakReference<Activity>? = null
}

actual fun setScreenProtection(enabled: Boolean) {
    val activity = AndroidActivityTracker.currentActivity?.get() ?: return
    activity.runOnUiThread {
        if (enabled) {
            // "Enable Screenshots" means REMOVE FLAG_SECURE (allow)
            // "Disable Screenshots" means ADD FLAG_SECURE (disallow)
            
            // Wait, usually "Secure Mode" = No Screenshots.
            // Feature: "enable/disbale Screenshot in the app"
            // If Enabled -> clear FLAG_SECURE
            // If Disabled -> set FLAG_SECURE
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
}
