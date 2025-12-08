package org.example.edvo.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.example.edvo.theme.EdvoColor

@Composable
fun EdvoScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = EdvoColor.Background,
        contentColor = EdvoColor.OnBackground,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        content = content
    )
}
