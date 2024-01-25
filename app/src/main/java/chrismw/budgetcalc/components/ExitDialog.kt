package chrismw.budgetcalc.components

import androidx.compose.material3.AlertDialog
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
internal fun ExitDialog(
    state: ExitDialogState = rememberExitDialogState(),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    titleText: String = stringResource(id = R.string.dialog_title_unsaved_changes),
    dialogText: String = stringResource(id = R.string.dialog_text_unsaved_changes),
    confirmButtonText: String = stringResource(id = R.string.label_leave_without_saving),
    dismissButtonText: String = stringResource(id = R.string.label_cancel),
    isDismissible: Boolean = true,
) {
    if (state.isVisible) {
        AlertDialog(
            onDismissRequest = {
                if (isDismissible) {
                    state.hide()
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    state.hide()
                    onConfirm()
                }) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    state.hide()
                    onDismiss()
                }) {
                    Text(text = dismissButtonText)
                }
            },
            title = {
                Text(text = titleText)
            },
            text = {
                Text(text = dialogText)
            }
        )
    }
}

@Composable
internal fun rememberExitDialogState(): ExitDialogState {
    return remember {
        ExitDialogState()
    }
}

@Stable
internal class ExitDialogState() {

    var isVisible: Boolean by mutableStateOf(false)
        private set

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}
