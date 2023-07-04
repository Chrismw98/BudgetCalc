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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import chrismw.budgetcalc.Metric
import chrismw.budgetcalc.MetricUnit
import chrismw.budgetcalc.R
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

//private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartToTargetDate(
    modifier: Modifier = Modifier,
    startDate: LocalDate,
    endDate: LocalDate,
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
            value = startDate.format(formatter),
            onClick = onClickStartDate,
            label = stringResource(id = R.string.budget_start_date_excl),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Today,
                    contentDescription = null)
            },
            initialDate = startDate
        )

        ClickableDatePickerTextField(
            modifier = Modifier.weight(1f),
            value = endDate.format(formatter),
            onClick = onClickTargetDate,
            label = stringResource(id = R.string.target_date_incl),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Event,
                    contentDescription = null)
            },
            initialDate = endDate
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableDatePickerTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onClick: (LocalDate) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    initialDate: LocalDate = LocalDate.now(),
) {
    val dialogState = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton(stringResource(id = R.string.ok))
            negativeButton(stringResource(id = R.string.cancel))
        },
    ) {
        datepicker(
            initialDate = initialDate,
            title = "Pick a date",
//            allowedDateValidator = {
//                it.dayOfMonth % 2 == 1
//            },
        ) {
            onClick(it)
        }
    }

    Box(
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            value = value,
            onValueChange = {},
            label = {
                Text(text = label)
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            readOnly = true,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(1f)
                .clickable(
                    onClick = { dialogState.show() },
                    indication = rememberRipple(
                        bounded = true
                    ),
                    interactionSource = MutableInteractionSource(),
                ),
        )
    }
}

@Preview(showBackground = false, widthDp = 300, heightDp = 140)
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

@Preview(showBackground = false, widthDp = 600, heightDp = 800)
@Composable
fun StartToEndDatePreview() {
    val testMetric = Metric("Days since start", 19.0, MetricUnit.DAYS)
    BudgetCalcTheme {
        var pickedStartDate by rememberSaveable {
            mutableStateOf(LocalDate.now())
        }

        var pickedEndDate = LocalDate.now()

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