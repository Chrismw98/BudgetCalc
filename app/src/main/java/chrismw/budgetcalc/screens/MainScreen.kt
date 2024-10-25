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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.CircularProgressbar
import chrismw.budgetcalc.components.MetricItem
import chrismw.budgetcalc.components.VerticalSpacer
import chrismw.budgetcalc.decimalFormatSymbols
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.extensions.toLocalDate
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.dateString
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    viewState: MainScreenViewModel.ViewState,
    onClickSettingsButton: () -> Unit,
    toggleShowDetails: () -> Unit,
    onPickTargetDate: (LocalDate) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.title_home))
                },
                actions = {
                    IconButton(onClick = onClickSettingsButton
                    ) {
                        Icon(imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { contentPadding ->
        if (!viewState.isLoading) {
            if (viewState.hasIncompleteData) {
                MissingDataContent(
                    contentPadding,
                    onClickSettingsButton
                )
            } else {
                MainScreenContent(
                    contentPadding,
                    viewState,
                    toggleShowDetails,
                    onPickTargetDate,
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
    onPickTargetDate: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        LazyColumn(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item("date_picker") {
                if (showDatePicker) {
                    DatePickerModal(
                        targetDate = viewState.targetDate,
                        budgetStartDate = viewState.startDate,
                        budgetEndDate = viewState.endDate,
                        onDateSelected = { selectedDate ->
                            showDatePicker = false
                            selectedDate?.let {
                                onPickTargetDate(it)
                            }
                        },
                        onDismiss = {
                            showDatePicker = false
                        }
                    )
                }
            }

            item("circular_progress_bar") {
                CircularProgressbar(
                    remainingBudget = viewState.remainingBudget ?: 0f,
                    remainingBudgetPercentage = viewState.remainingBudgetPercentage,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    targetDateString = dateString(viewState.targetDate.toEpochMillis()),
                    currency = viewState.currency,
                    onClick = { showDatePicker = true }
                )
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
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        viewState.metrics.forEach { metric ->
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                MetricItem(
                                    metric = metric,
                                    modifier = Modifier.padding(6.dp),
                                    currency = viewState.currency
                                )
                            }
                        }
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
        Text(
            modifier = Modifier.padding(bottom = 24.dp),
            text = if (isExpanded) stringResource(id = R.string.show_less) else stringResource(id = R.string.show_more),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start
        )

        HorizontalDivider(thickness = 1.dp)

        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            modifier = Modifier
                .size(size = 64.dp) //TODO: Check if this is the right way to set the size (2023-06-14)
                .padding(top = 30.dp),
            contentDescription = if (isExpanded) {
                stringResource(R.string.show_less)
            } else {
                stringResource(R.string.show_more)
            }
        )
    }
}

private fun validateNumberInputString(numberInputString: String, allowDecimalSeparator: Boolean = false): Boolean {
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
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(6.dp),
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

        Button(onClick = onNavigateToSettings) {
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
    budgetStartDate: LocalDate,
    budgetEndDate: LocalDate,
) {
    val targetDateMillis = targetDate.toEpochMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = targetDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= budgetStartDate.toEpochMillis() //TODO: Change this according to budget type
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in budgetStartDate.year..budgetEndDate.year
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
            onClickSettingsButton = {},
            toggleShowDetails = {},
            onPickTargetDate = {}
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
            onClickSettingsButton = {},
            toggleShowDetails = {},
            onPickTargetDate = {}
        )
    }
}