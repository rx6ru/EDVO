package org.example.edvo.core.sensor

/**
 * Configuration for shake detection sensitivity.
 * All values have safe defaults and min/max limits.
 */
data class ShakeConfig(
    /** G-force threshold for detecting a shake (1.5 - 4.0) */
    val gForceThreshold: Float = DEFAULT_G_FORCE,
    
    /** Number of shakes required to trigger (2 - 5) */
    val shakeCount: Int = DEFAULT_SHAKE_COUNT,
    
    /** Time window for consecutive shakes in ms (400 - 1200) */
    val timeWindow: Int = DEFAULT_TIME_WINDOW,
    
    /** Cooldown after trigger in ms (500 - 5000) */
    val cooldown: Int = DEFAULT_COOLDOWN
) {
    companion object {
        // Defaults
        const val DEFAULT_G_FORCE = 2.2f
        const val DEFAULT_SHAKE_COUNT = 2
        const val DEFAULT_TIME_WINDOW = 800
        const val DEFAULT_COOLDOWN = 1000
        
        // Limits - G Force
        const val MIN_G_FORCE = 1.5f
        const val MAX_G_FORCE = 4.0f
        
        // Limits - Shake Count
        const val MIN_SHAKE_COUNT = 2
        const val MAX_SHAKE_COUNT = 5
        
        // Limits - Time Window
        const val MIN_TIME_WINDOW = 400
        const val MAX_TIME_WINDOW = 1200
        
        // Limits - Cooldown
        const val MIN_COOLDOWN = 500
        const val MAX_COOLDOWN = 5000
        
        /** Default configuration */
        val Default = ShakeConfig()
    }
}
