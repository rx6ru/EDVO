package org.example.edvo

expect fun setScreenProtection(enabled: Boolean)

/**
 * Force-kill the application (for shake-to-kill feature).
 * Android: finishAffinity + exitProcess
 * Desktop/iOS: exit process or no-op
 */
expect fun killApp()
