package org.example.edvo.presentation.components.util

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

object EmptyTextToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        // No-op: Menu suppressed
    }

    override fun hide() {
        // No-op
    }
}
