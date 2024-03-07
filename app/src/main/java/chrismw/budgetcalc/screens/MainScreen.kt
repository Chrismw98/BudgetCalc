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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.CircularProgressbar
import chrismw.budgetcalc.components.MetricItem
import chrismw.budgetcalc.decimalFormatSymbols
import chrismw.budgetcalc.extensions.dateString
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricType
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    state: MainScreenViewModel.ViewState,
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
//        AnimatedVisibility(
//            visible = !state.isLoading,
//            enter = fadeIn(),
//            exit = fadeOut(),
//        ) {
        if (!state.isLoading) {
            if (state.hasIncompleteData) {
                MissingDataContent(
                    contentPadding,
                    onClickSettingsButton
                )
            } else {
                MainScreenContent(
                    contentPadding,
                    state,
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
    state: MainScreenViewModel.ViewState,
    toggleShowDetails: () -> Unit,
    onPickTargetDate: (LocalDate) -> Unit
) {
    val datePickerDialogState = rememberMaterialDialogState()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            CircularProgressbar(
                remainingBudget = state.remainingBudget ?: 0f,
                remainingBudgetPercentage = state.remainingBudgetPercentage,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                targetDateString = dateString(state.targetDateInEpochMillis),
                currency = state.currency,
                onClick = { datePickerDialogState.show() }
            )

            Column(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clickable(
                        onClick = toggleShowDetails
                    )
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 24.dp),
                        text = if (state.isExpanded) stringResource(id = R.string.show_less) else stringResource(id = R.string.show_more),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start
                    )

                    Divider(thickness = 1.dp)

//                    IconButton(
//                        modifier = Modifier.padding(top = 30.dp),
//                        onClick = { expanded = !expanded }
//                    ) {
                    Icon(
                        imageVector = if (state.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        modifier = Modifier
                            .size(size = 64.dp) //TODO: Check if this is the right way to set the size (2023-06-14)
                            .padding(top = 30.dp),
//                            tint = colorResource(id = R.color.color_accent), //TODO: Use Material UI Colors (2023-06-14)
                        contentDescription = if (state.isExpanded) {
                            stringResource(R.string.show_less)
                        } else {
                            stringResource(R.string.show_more)
                        }
                    )
//                    }
                }
                AnimatedVisibility(
                    visible = state.isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.heightIn(max = 250.dp)) {
                        items(items = state.metrics) { metric ->
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                MetricItem(
                                    metric = metric,
                                    modifier = Modifier.padding(6.dp),
                                    currency = state.currency
                                )
                            }

                        }
                    }
                }
            }

        }

    }

    MaterialDialog(
        dialogState = datePickerDialogState,
        buttons = {
            positiveButton(stringResource(id = R.string.label_ok))
            negativeButton(stringResource(id = R.string.label_cancel))
        },
    ) {
        datepicker(
            initialDate = state.targetDate,
            title = stringResource(R.string.dialog_title_pick_target_date),
            allowedDateValidator = {
                !it.isBefore(state.startDate)
            },
        ) {
            onPickTargetDate(it)
        }
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

@Preview
@Composable
fun DefaultPreview() {
    BudgetCalcTheme {
        MainScreen(
            state = MainScreenViewModel.ViewState(
                targetDate = LocalDate.of(2023, 9, 16),
                targetDateInEpochMillis = 1694857746876,

                maxBudget = 300f,
                remainingBudget = 270f,
                currency = "€",
                metrics = persistentListOf(
                    Metric(MetricType.DAYS_SINCE_START, 3, MetricUnit.DAYS),
                    Metric(MetricType.DAYS_REMAINING, 27, MetricUnit.DAYS),
                    Metric(MetricType.DAILY_BUDGET, 10f, MetricUnit.CURRENCY_PER_DAY),
                    Metric(MetricType.BUDGET_UNTIL_TARGET_DATE, 270f, MetricUnit.CURRENCY),
                    Metric(MetricType.REMAINING_BUDGET, 270f, MetricUnit.CURRENCY),
                    Metric(MetricType.TOTAL_BUDGET, 540f, MetricUnit.CURRENCY),
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
            state = MainScreenViewModel.ViewState(
                hasIncompleteData = true
            ),
            onClickSettingsButton = {},
            toggleShowDetails = {},
            onPickTargetDate = {}
        )
    }
}