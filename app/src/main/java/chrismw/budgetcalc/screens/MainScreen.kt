package chrismw.budgetcalc.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.CircularProgressbar
import chrismw.budgetcalc.components.CircularTextOverview
import chrismw.budgetcalc.components.MetricItemCard
import chrismw.budgetcalc.components.VerticalSpacer
import chrismw.budgetcalc.decimalFormatSymbols
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.extensions.toLocalDate
import chrismw.budgetcalc.helpers.BudgetState
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.dateString
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    viewState: MainScreenViewModel.ViewState,
    onJumpToTodayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    toggleShowDetails: () -> Unit,
    onPickTargetDate: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.title_home))
                },
                actions = {
                    if (viewState.showJumpToTodayButton) {
                        IconButton(
                            onClick = onJumpToTodayClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null
                            )
                        }
                    }

                    IconButton(
                        onClick = onSettingsClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        if (!viewState.isLoading) {
            if (viewState.hasIncompleteData) {
                MissingDataContent(
                    contentPadding = contentPadding,
                    onSettingsClick = onSettingsClick,
                )
            } else {
                MainScreenContent(
                    contentPadding = contentPadding,
                    viewState = viewState,
                    toggleShowDetails = toggleShowDetails,
                    onPickTargetDate = onPickTargetDate,
                    onShowDatePicker = onShowDatePicker,
                    onHideDatePicker = onHideDatePicker,
                )
            }
        }
    }
}

@Composable
private fun MainScreenContent(
    contentPadding: PaddingValues,
    viewState: MainScreenViewModel.ViewState,
    toggleShowDetails: () -> Unit,
    onPickTargetDate: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
) {
    if (viewState.showDatePicker) {
        DatePickerModal(
            targetDate = viewState.targetDate,
            minDate = viewState.datePickerMinDate,
            maxDate = viewState.datePickerMaxDate,
            onDateSelected = { selectedDate ->
                onHideDatePicker()
                selectedDate?.let {
                    onPickTargetDate(it)
                }
            },
            onDismiss = onHideDatePicker,
        )
    }

    LazyColumn(
        modifier = Modifier
            .padding(contentPadding)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item("circular_overview") {
            when (viewState.budgetState) {
                is BudgetState.Ongoing -> {
                    CircularProgressbar(
                        remainingBudget = viewState.remainingBudget ?: 0f,
                        remainingBudgetPercentage = viewState.remainingBudgetPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        targetDateString = dateString(viewState.targetDate.toEpochMillis()),
                        currency = viewState.currency,
                        onClick = onShowDatePicker
                    )
                }

                else -> {
                    CircularTextOverview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        text = if (viewState.budgetState is BudgetState.Expired) {
                            stringResource(
                                id = viewState.budgetState.textResId,
                                viewState.budgetState.daysPastEnd,
                                pluralStringResource(
                                    id = R.plurals.days_mid_sentence,
                                    count = viewState.budgetState.daysPastEnd
                                )
                            )
                        } else {
                            stringResource(id = viewState.budgetState.textResId)
                        },
                        targetDateString = dateString(viewState.targetDate.toEpochMillis()),
                        backgroundCircleColor = if (viewState.budgetState is BudgetState.HasNotStarted) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        onClick = onShowDatePicker
                    )
                }
            }
        }

        item("spacer") {
            VerticalSpacer(24.dp)
        }

        item("toggle_details_button") {
            ToggleDetailsButton(
                isExpanded = viewState.isExpanded,
                onClick = toggleShowDetails,
            )
        }

        item("metrics_list") {
            AnimatedVisibility(
                visible = viewState.isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    viewState.metrics.forEach { metric ->
                        MetricItemCard(
                            metric = metric,
                            currency = viewState.currency
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleDetailsButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(
            onClick = onClick
        )
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (isExpanded) stringResource(id = R.string.show_less) else stringResource(
                    id = R.string.show_more
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            HorizontalDivider(thickness = 1.dp)
        }


        Icon(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 16.dp)
                .size(40.dp),
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (isExpanded) {
                stringResource(R.string.show_less)
            } else {
                stringResource(R.string.show_more)
            }
        )
    }
}

private fun validateNumberInputString(
    numberInputString: String,
    allowDecimalSeparator: Boolean = false
): Boolean {
    val maxDecimalSeparatorCount = if (allowDecimalSeparator) 1 else 0
    var decimalSeparatorCounter = 0
    for (char in numberInputString) {
        if (char == decimalFormatSymbols.decimalSeparator) {
            if (decimalSeparatorCounter < maxDecimalSeparatorCount) {
                decimalSeparatorCounter++
            } else {
                return false
            }
        } else if (!char.isDigit()) {
            return false
        }
    }
    return true
}

@Composable
private fun MissingDataContent(
    contentPadding: PaddingValues,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.title_welcome),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.text_enter_your_data),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onSettingsClick) {
            Text(text = stringResource(R.string.label_enter_data))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit,
    targetDate: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
) {
    val targetDateMillis = targetDate.toEpochMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = targetDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in minDate.toEpochMillis()..maxDate.toEpochMillis()
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in minDate.year..maxDate.year
            }
        },
        initialDisplayedMonthMillis = targetDateMillis
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
                    text = stringResource(id = R.string.dialog_title_pick_target_date)
                )
            },
            showModeToggle = false,
        )
    }
}

@Preview
@Composable
fun DefaultPreview() {
    BudgetCalcTheme {
        MainScreen(
            viewState = MainScreenViewModel.ViewState(
                isLoading = false,

                targetDate = LocalDate.of(2023, 9, 16),

                remainingBudget = 270f,
                currency = "€",
                metrics = persistentListOf(
                    Metric.DaysSinceStart(3),
                    Metric.DaysRemaining(27),
                    Metric.DailyBudget(10f),
                    Metric.BudgetUntilTargetDate(270f),
                    Metric.RemainingBudget(270f),
                    Metric.TotalBudget(540f),
                ),
                isExpanded = true,
                hasIncompleteData = false
            ),
            onJumpToTodayClick = {},
            onSettingsClick = {},
            toggleShowDetails = {},
            onPickTargetDate = {},
            onShowDatePicker = {},
            onHideDatePicker = {},
        )
    }
}

@Preview
@Composable
fun MissingDataPreview() {
    BudgetCalcTheme {
        MainScreen(
            viewState = MainScreenViewModel.ViewState(
                isLoading = false,
                hasIncompleteData = true
            ),
            onJumpToTodayClick = {},
            onSettingsClick = {},
            toggleShowDetails = {},
            onPickTargetDate = {},
            onShowDatePicker = {},
            onHideDatePicker = {},
        )
    }
}