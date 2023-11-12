@file:OptIn(ExperimentalMaterial3Api::class)

package chrismw.budgetcalc.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import chrismw.budgetcalc.R

@Composable
private fun UnsavedChangesDialog(
    state: UnsavedChangesDialogState = rememberUnsavedChangesDialogState(),
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (state.isVisible) {
        AlertDialog(
            onDismissRequest = { state.hide() },
            confirmButton = {
                TextButton(onClick = {
                    state.hide()
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.label_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    state.hide()
                }) {
                    Text(text = stringResource(id = R.string.label_cancel))
                }
            },
            title = {
                Text(text = stringResource(id = R.string.dialog_title_unsaved_changes))
            },
            text = {
                Text(text = stringResource(id = R.string.dialog_text_unsaved_changes))
            }
        )
    }
}

@Composable
internal fun rememberUnsavedChangesDialogState(): UnsavedChangesDialogState {
    return remember {
        UnsavedChangesDialogState()
    }
}

@Stable
class UnsavedChangesDialogState {

    var isVisible: Boolean by mutableStateOf(false)
        private set

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}
