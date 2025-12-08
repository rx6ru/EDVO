package org.example.edvo.presentation.components.util

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS (System gesture usually handles this, or use a specific implementation if needed later)
}
