package org.example.edvo.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Production-grade Android ShakeDetector using accelerometer.
 * Uses g-force normalization for device-independent detection.
 * Reads configuration from ShakeConfig passed at startListening.
 */
actual class ShakeDetector(private val context: Context) : SensorEventListener {
    
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var onShakeCallback: (() -> Unit)? = null
    
    // Shake detection state
    private var shakeCount = 0
    private var lastShakeTime: Long = 0
    private var lastTriggerTime: Long = 0
    private var lastUpdateTime: Long = 0
    
    // Config (updated on each startListening)
    private var config: ShakeConfig = ShakeConfig.Default
    
    companion object {
        // Fixed processing interval
        private const val UPDATE_INTERVAL = 40
        
        // Ignore detection when phone is flat (prevents table bumps)
        private const val FLAT_Z_THRESHOLD = 9.5f
        private const val FLAT_XY_THRESHOLD = 2.0f
    }
    
    actual fun startListening(onShake: () -> Unit) {
        this.onShakeCallback = onShake
        
        // Load current config from DependencyInjection
        org.example.edvo.DependencyInjection.shakeConfig?.let {
            config = it
        }
        
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        
        // Prefer LINEAR_ACCELERATION (no gravity noise), fallback to ACCELEROMETER
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME // Faster than SENSOR_DELAY_UI
            )
        }
        
        // Reset state
        shakeCount = 0
        lastShakeTime = 0
        lastTriggerTime = 0
        lastUpdateTime = 0
    }
    
    actual fun stopListening() {
        sensorManager?.unregisterListener(this)
        onShakeCallback = null
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        val currentTime = System.currentTimeMillis()
        
        // Throttle to prevent overprocessing
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) return
        lastUpdateTime = currentTime
        
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        // Skip if phone is flat on table (prevents accidental locks from bumps)
        // Only apply this check for TYPE_ACCELEROMETER (includes gravity)
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (abs(z) > FLAT_Z_THRESHOLD && abs(x) < FLAT_XY_THRESHOLD && abs(y) < FLAT_XY_THRESHOLD) {
                return
            }
        }
        
        // Calculate g-force (normalized, device-independent)
        val gForce = if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            // LINEAR_ACCELERATION already excludes gravity
            sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        } else {
            // ACCELEROMETER includes gravity, need to normalize
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            sqrt(gX * gX + gY * gY + gZ * gZ)
        }
        
        // Check if this is a shake (using dynamic config)
        if (gForce > config.gForceThreshold) {
            // Check if we're within the shake window
            if (currentTime - lastShakeTime < config.timeWindow) {
                shakeCount++
            } else {
                // Too much time passed, reset count
                shakeCount = 1
            }
            
            lastShakeTime = currentTime
            
            // Check if shake count threshold reached
            if (shakeCount >= config.shakeCount) {
                // Check cooldown (using lastTriggerTime, not lastUpdateTime)
                if (currentTime - lastTriggerTime > config.cooldown) {
                    lastTriggerTime = currentTime
                    shakeCount = 0
                    onShakeCallback?.invoke()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
