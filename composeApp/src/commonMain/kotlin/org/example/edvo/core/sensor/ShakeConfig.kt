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
        
        // Limits - G Force (expanded for more flexibility)
        const val MIN_G_FORCE = 1.0f
        const val MAX_G_FORCE = 6.0f
        
        // Limits - Shake Count (expanded: 1 = single shake, up to 10)
        const val MIN_SHAKE_COUNT = 1
        const val MAX_SHAKE_COUNT = 10
        
        // Limits - Time Window (expanded for fast and slow shakers)
        const val MIN_TIME_WINDOW = 200
        const val MAX_TIME_WINDOW = 2000
        
        // Limits - Cooldown (expanded from 200ms to 10s)
        const val MIN_COOLDOWN = 200
        const val MAX_COOLDOWN = 10000
        
        /** Default configuration */
        val Default = ShakeConfig()
    }
}
