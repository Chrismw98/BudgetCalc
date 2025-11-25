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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.extensions.toLocalDate
import chrismw.budgetcalc.helpers.dateString
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import java.time.LocalDate

@Composable
fun StartToTargetDate(
    modifier: Modifier = Modifier,
    startDate: LocalDate?,
    endDate: LocalDate?,
    today: LocalDate,
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
                R.string.label_select_date
            ), //TODO: This logic could be inside the ViewModel, or its own state
            onClick = onClickStartDate,
            label = stringResource(id = R.string.label_start_date),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null
                )
            },
            initialDate = startDate ?: if (endDate != null) endDate.minusDays(1) else today,
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
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null
                )
            },
            initialDate = endDate ?: if (startDate != null) startDate.plusDays(1) else today.plusDays(1),
            allowedDateValidator = {
                if (startDate != null) !it.isBefore(startDate) else true
            }
        )
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
    initialDate: LocalDate,
    allowedDateValidator: (LocalDate) -> Boolean = { true }
) {
    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        DatePickerModal(
            initialDate = initialDate,
            onDateSelected = { selectedDate ->
                showDatePicker = false
                selectedDate?.let {
                    onClick(it)
                }
            },
            onDismiss = {
                showDatePicker = false
            },
            label = label,
            allowedDateValidator = allowedDateValidator
        )
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
                    onClick = { showDatePicker = true },
                    indication = ripple(
                        bounded = true
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (LocalDate?) -> Unit,
    allowedDateValidator: (LocalDate) -> Boolean,
    onDismiss: () -> Unit,
    initialDate: LocalDate,
    label: String
) {
    val initialDateMillis = initialDate.toEpochMillis()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        initialDisplayedMonthMillis = initialDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return allowedDateValidator(utcTimeMillis.toLocalDate())
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis?.toLocalDate())
                    onDismiss()
                }) {
                Text(stringResource(id = R.string.label_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    text = label
                )
            },
            showModeToggle = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReadOnlyTextFieldPreview() {
    var date by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }

    BudgetCalcTheme {
        Surface(modifier = Modifier.padding(6.dp)) {
            ClickableDatePickerTextField(
                value = date.toString(),
                onClick = { date = it },
                label = "Start date",
                leadingIcon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar Icon") },
                allowedDateValidator = { true },
                initialDate = LocalDate.now()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartToEndDatePreview() {
    BudgetCalcTheme {
        val today = LocalDate.now()

        var pickedStartDate by rememberSaveable {
            mutableStateOf(LocalDate.now())
        }

        var pickedEndDate: LocalDate? = null
        Surface(modifier = Modifier.padding(6.dp)) {
            StartToTargetDate(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                startDate = pickedStartDate,
                endDate = pickedEndDate,
                today = today,
                onClickStartDate = { pickedStartDate = it },
                onClickTargetDate = { pickedEndDate = it }
            )
        }
    }
}