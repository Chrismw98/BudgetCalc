package chrismw.budgetcalc.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.extensions.dateString
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate

@Composable
fun StartToTargetDate(
    modifier: Modifier = Modifier,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onClickStartDate: (LocalDate) -> Unit,
    onClickTargetDate: (LocalDate) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        ClickableDatePickerTextField(
            modifier = Modifier.weight(1f),
            value = startDate?.toEpochMillis()?.let { dateString(it) } ?: stringResource(
                R.string.label_select_date), //TODO: This logic could be inside the ViewModel, or its own state
            onClick = onClickStartDate,
            label = stringResource(id = R.string.label_start_date),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Today,
                    contentDescription = null)
            },
            initialDate = startDate ?: if (endDate != null) endDate.minusDays(1) else LocalDate.now(),
            allowedDateValidator = {
                if (endDate != null) !it.isAfter(endDate) else true
            }
        )

        ClickableDatePickerTextField(
            modifier = Modifier.weight(1f),
            value = endDate?.toEpochMillis()?.let { dateString(it) } ?: stringResource(R.string.label_select_date),
            onClick = onClickTargetDate,
            label = stringResource(id = R.string.label_end_date),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Event,
                    contentDescription = null)
            },
            initialDate = endDate ?: if (startDate != null) startDate.plusDays(1) else LocalDate.now().plusDays(1),
            allowedDateValidator = {
                if (startDate != null) !it.isBefore(startDate) else true
            }
        )
//        Text(text = pickedDate.toString(),
//            style = MaterialTheme.typography.bodyLarge)

        /* This should be called in an onClick or an Effect */
//        dialogState.show()

//        val dialog = MaterialDialog()
//        val textState = remember { mutableStateOf(TextFieldValue()) }
//        dialog.build {
//            datepicker { date ->
//                val formattedDate = date.format(
//                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
//                )
//                textState.value = TextFieldValue(formattedDate)
//            }
//        }
    }
}

@Composable
fun ClickableDatePickerTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onClick: (LocalDate) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    initialDate: LocalDate? = null,
    allowedDateValidator: (LocalDate) -> Boolean = { true }
) {
    val dialogState = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton(stringResource(id = R.string.label_ok))
            negativeButton(stringResource(id = R.string.label_cancel))
        },
    ) {
        datepicker(
            initialDate = initialDate ?: LocalDate.now(),
            title = stringResource(id = R.string.dialog_title_pick_target_date),
            allowedDateValidator = allowedDateValidator,
        ) {
            onClick(it)
        }
    }

    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            value = value,
            onValueChange = {},
            label = {
                Text(text = label)
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            readOnly = false,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(1f)
                .clickable(
                    onClick = { dialogState.show() },
                    indication = ripple(
                        bounded = true
                    ),
                    interactionSource = MutableInteractionSource(),
                ),
        )
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 140)
@Composable
fun ReadOnlyTextFieldPreview() {
    var date by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }

    BudgetCalcTheme {
        ClickableDatePickerTextField(
            value = date.toString(),
            onClick = { date = it },
            label = "Start date",
            leadingIcon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar Icon") }
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Composable
private fun StartToEndDatePreview() {
    BudgetCalcTheme {
        var pickedStartDate by rememberSaveable {
            mutableStateOf(LocalDate.now())
        }

        var pickedEndDate: LocalDate? = null

        StartToTargetDate(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            startDate = pickedStartDate,
            endDate = pickedEndDate,
            onClickStartDate = { pickedStartDate = it },
            onClickTargetDate = { pickedEndDate = it }
        )
    }
}