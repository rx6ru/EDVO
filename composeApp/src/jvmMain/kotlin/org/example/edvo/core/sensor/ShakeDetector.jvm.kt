package org.example.edvo.core.sensor

/**
 * JVM/Desktop implementation of ShakeDetector.
 * No-op since desktop devices don't have accelerometers.
 */
actual class ShakeDetector {
    
    actual fun startListening(onShake: () -> Unit) {
        // No-op: Desktop has no accelerometer
    }
    
    actual fun stopListening() {
        // No-op
    }
}
