package chrismw.budgetcalc.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String?,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    singleLine: Boolean = false,
    placeholderText: String? = null,
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    supportingText: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        labelText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            VerticalSpacer(6.dp)
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                enabled = enabled,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                placeholder = placeholderText?.let {
                    {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                },
                isError = isError,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                supportingText = supportingText?.let {
                    {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                }
            )

            if (readOnly && onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .alpha(1f)
                        .clickable(
                            onClick = onClick,
                            indication = ripple(
                                bounded = true,
                            ),
                            interactionSource = remember { MutableInteractionSource() },
                        ),
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, name = "With value")
private fun PreviewGenericDropDownMenu() {
    BudgetCalcTheme {
        CustomTextField(
            value = "Test",
            enabled = true,
            labelText = "Test Label",
            placeholderText = "Placeholder text",
            leadingIcon = {
                Text(text = "EG")
            },
            onValueChange = {},
        )
    }
}

@Composable
@Preview(showBackground = true, name = "No value, Enabled")
private fun PreviewDisabledEmptyGenericDropDownMenu() {
    BudgetCalcTheme {
        CustomTextField(
            value = "",
            enabled = true,
            labelText = "Test Label",
            placeholderText = "Placeholder text",
            leadingIcon = {
                Text(text = "EG")
            },
            onValueChange = {},
        )
    }
}


@Composable
@Preview(showBackground = true, name = "No value, Disabled")
private fun PreviewDisabledDisabledGenericDropDownMenu() {
    BudgetCalcTheme {
        CustomTextField(
            value = "",
            enabled = false,
            labelText = "Test Label",
            placeholderText = "Placeholder text",
            leadingIcon = {
                Text(text = "EG")
            },
            onValueChange = {},
            supportingText = "Supporting text",
        )
    }
}
