package org.example.edvo.presentation.designsystem

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * NeoMotion: Centralized Animation System for EDVO.
 * Defines standard transitions and motion patterns to ensure "Fluid" feel.
 */
object NeoMotion {

    /**
     * Standard Navigation Transitions for AnimatedContent.
     */
    object Navigation {
        
        // Define Screen types to determine hierarchy
        // This is a helper, usage logic resides in the transitionSpec lambda typically,
        // but we can provide pre-canned ContentTransform specs here.

        fun lateral(): ContentTransform = 
            EnterTransition.None togetherWith ExitTransition.None

        fun push(): ContentTransform = 
            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                slideOutHorizontally { width -> -width / 4 } + fadeOut())

        fun pop(): ContentTransform = 
            (slideInHorizontally { width -> -width / 4 } + fadeIn()).togetherWith(
                slideOutHorizontally { width -> width } + fadeOut())
    }

    /**
     * Logic to determine transition direction based on "Depth".
     * @return 1 for Push (Deepen), -1 for Pop (Surface), 0 for Lateral
     */
    fun <T> getTransitionDirection(initial: T, target: T, hierarchy: List<Set<T>>): Int {
        // Simple hierarchy check:
        // Levels: [Root], [Detail, Features], [Deep Config]
        val initialLevel = hierarchy.indexOfFirst { it.contains(initial) }
        val targetLevel = hierarchy.indexOfFirst { it.contains(target) }

        return when {
            initialLevel == -1 || targetLevel == -1 -> 0 // Unknown
            targetLevel > initialLevel -> 1 // Push
            targetLevel < initialLevel -> -1 // Pop
            else -> 0 // Lateral
        }
    }
}
