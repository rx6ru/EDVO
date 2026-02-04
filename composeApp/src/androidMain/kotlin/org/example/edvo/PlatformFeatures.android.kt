package org.example.edvo

import android.app.Activity
import android.view.WindowManager
import java.lang.ref.WeakReference
import kotlin.system.exitProcess
import org.example.edvo.BuildConfig
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

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

actual fun killApp() {
    AndroidActivityTracker.currentActivity?.get()?.let { activity ->
        activity.finishAffinity()
        exitProcess(0)
    }
}

actual fun getAppVersion(): String = BuildConfig.VERSION_NAME

actual fun getUpdateCachePath(): String? {
    return AndroidActivityTracker.currentActivity?.get()?.cacheDir?.absolutePath
}

actual fun installApk(path: String) {
    AndroidActivityTracker.currentActivity?.get()?.let { activity ->
        val file = File(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }
}

actual fun saveFile(path: String, data: ByteArray) {
    File(path).writeBytes(data)
}
