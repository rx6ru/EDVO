package org.example.edvo.core.sensor

/**
 * Platform-specific shake detector.
 * Uses accelerometer to detect firm shaking motion.
 */
expect class ShakeDetector {
    /**
     * Start listening for shake events.
     * @param onShake Callback invoked when a shake is detected
     */
    fun startListening(onShake: () -> Unit)
    
    /**
     * Stop listening for shake events.
     * Call this when the feature is disabled or app goes to background.
     */
    fun stopListening()
}
