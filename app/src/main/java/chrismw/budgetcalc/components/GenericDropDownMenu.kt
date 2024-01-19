package chrismw.budgetcalc.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun <T> GenericDropDownMenu(
    modifier: Modifier = Modifier,
    onSelectionChanged: (T) -> Unit,
    options: ImmutableList<T>,
    selectedOption: T?,
    onParseOptionToString: @Composable (T?) -> String = {
        if (it is String) {
            it
        } else {
            it?.toString().orEmpty()
        }
    },
    onUpdateExpandedState: (Boolean) -> Unit,
    isExpanded: Boolean,
    enabled: Boolean = true,
    isError: Boolean = false,
    labelText: String,
) {

    val dropDownTextFieldValue = onParseOptionToString(selectedOption)

    if (enabled) {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = isExpanded,
            onExpandedChange = {
                onUpdateExpandedState(it)
            },
        ) {
            DropDownMenuTextField(
                modifier = Modifier.menuAnchor(),
                value = dropDownTextFieldValue,
                labelText = labelText,
                isExpanded = isExpanded,
                isError = isError,
                contentDescription = labelText,
            )
//        Cannot use ExposedDropdownMenu due to this bug:
//        https://issuetracker.google.com/issues/205589613
//        ExposedDropdownMenu(
            DropdownMenu(
                modifier = Modifier.exposedDropdownSize(
                    matchTextFieldWidth = true
                ),
                expanded = isExpanded,
                onDismissRequest = {
                    onUpdateExpandedState(false)
                }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onSelectionChanged(option)
                            onUpdateExpandedState(false)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        text = {
                            Text(
                                text = onParseOptionToString(option),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    )
                }
            }
        }
    } else {
        DropDownMenuTextField(
            modifier = modifier,
            value = dropDownTextFieldValue,
            enabled = false,
            labelText = labelText,
            isExpanded = isExpanded,
            isError = isError,
            contentDescription = labelText,
        )
    }
}

@Composable
private fun DropDownMenuTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: String,
    labelText: String,
    isExpanded: Boolean,
    isError: Boolean = false,
    contentDescription: String?,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        value = value,
        onValueChange = {},
        readOnly = true,
        label = {
            Text(text = labelText)
        },
        enabled = enabled,
        trailingIcon = {
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Outlined.KeyboardArrowUp
                } else {
                    Icons.Outlined.KeyboardArrowDown
                },
                contentDescription = contentDescription
            )
        },
        isError = isError,
    )
}

@Composable
@Preview(showBackground = true, name = "Chosen Option")
private fun PreviewGenericDropDownMenu() {
    BudgetCalcTheme {
        GenericDropDownMenu(
            onSelectionChanged = {},
            options = persistentListOf(),
            onUpdateExpandedState = {},
            selectedOption = "Test Option",
            isExpanded = false,
            labelText = "Test Label",
        )
    }
}

@Composable
@Preview(showBackground = true, name = "No chosen option, Enabled")
private fun PreviewEnabledEmptyGenericDropDownMenu() {
    BudgetCalcTheme {
        GenericDropDownMenu(
            onSelectionChanged = {},
            options = persistentListOf(),
            onUpdateExpandedState = {},
            selectedOption = null,
            isExpanded = false,
            labelText = "Test Label",
        )
    }
}

@Composable
@Preview(showBackground = true, name = "No chosen option, Disabled")
private fun PreviewDisabledEmptyGenericDropDownMenu() {
    BudgetCalcTheme {
        GenericDropDownMenu(
            onSelectionChanged = {},
            options = persistentListOf(),
            onUpdateExpandedState = {},
            selectedOption = null,
            isExpanded = false,
            enabled = false,
            labelText = "Test Label",
        )
    }
}
