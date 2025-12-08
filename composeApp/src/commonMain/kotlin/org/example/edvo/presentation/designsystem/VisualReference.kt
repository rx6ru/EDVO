package org.example.edvo.presentation.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun VisualReferencePreview() {
    // This is a visual reference for the Neo-Pop Design System
    // It mocks the NoteListScreen structure to verify layout and theme
    
    // Using a simple Box to avoiding complex dependencies in preview
    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
       androidx.compose.material3.Text("Neo-Pop Visual Reference", style = NeoTypography.Header)
    }
}
