package org.example.edvo.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.input.TextFieldValue
import org.example.edvo.presentation.components.util.EmptyTextToolbar

@Composable
fun SecureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    // 1. Maintain internal state as TextFieldValue to control selection
    // We map the String input to this internal state
    var textFieldValueState by remember { 
        mutableStateOf(TextFieldValue(text = value)) 
    }

    // Sync external String changes to internal TextFieldValue
    // (This handles when the parent updates the text, e.g. from DB)
    if (textFieldValueState.text != value) {
        textFieldValueState = textFieldValueState.copy(text = value)
    }

    // Check if we are in "Secure Mode" (Copy/Paste Disabled)
    // We infer this from the LocalTextToolbar. If it's our EmptyTextToolbar, security is ON.
    val isSecure = LocalTextToolbar.current == EmptyTextToolbar

    OutlinedTextField(
        value = textFieldValueState,
        onValueChange = { newValue ->
            if (isSecure) {
                val oldText = textFieldValueState.text
                val newText = newValue.text
                val charDiff = newText.length - oldText.length

                // Reject selection handles to prevent toolbar from appearing
                if (newValue.selection.length > 0) {
                     textFieldValueState = newValue.copy(selection = androidx.compose.ui.text.TextRange(newValue.selection.end))
                     return@OutlinedTextField
                }

                // Reject bulk insertion (Paste, Autocomplete, Swipe)
                // Only allow single character input or deletions
                if (charDiff > 1) {
                    return@OutlinedTextField
                }

                textFieldValueState = newValue
                onValueChange(newValue.text)
            } else {
                // Normal Mode: Allow everything
                textFieldValueState = newValue
                onValueChange(newValue.text)
            }
        },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines
    )
}
