package chrismw.budgetcalc.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.GenericDropDownMenu
import chrismw.budgetcalc.components.RadioItem
import chrismw.budgetcalc.components.StartToTargetDate
import chrismw.budgetcalc.components.getStringForDayOfWeek
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsState,
    onNavigateBack: () -> Unit,
    onLoadSettings: () -> Unit,
    onClickConstantBudget: () -> Unit,
    onClickBudgetRate: () -> Unit,
    onConstantBudgetAmountChanged: (String) -> Unit,
    onBudgetRateAmountChanged: (String) -> Unit,
    onDefaultPaymentDayChanged: (String) -> Unit,
    onCurrencyChanged: (String) -> Unit,
    onBudgetTypeChanged: (BudgetType) -> Unit,
    onDayOfWeekChanged: (DayOfWeek) -> Unit,
    onStartDateChanged: (LocalDate) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit,
) {
    LaunchedEffect(Unit) {
        onLoadSettings()
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            RadioItem(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.label_is_budget_constant),
                isSelected = state.isBudgetConstant,
                onClick = onClickConstantBudget
            )

            RadioItem(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.label_is_budget_rate),
                isSelected = !state.isBudgetConstant,
                onClick = onClickBudgetRate
            )

            if (state.isBudgetConstant) {

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.constantBudgetAmount.orEmpty(),
                    onValueChange = onConstantBudgetAmountChanged,
                    label = {
                        Text(
                            text = stringResource(id = R.string.budget_amount)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = state.budgetRateAmount.orEmpty(),
                        onValueChange = onBudgetRateAmountChanged, //TODO: Change this component to handle input better (2023-11-12)
                        label = {
                            Text(
                                text = stringResource(id = R.string.budget_in_monetary_units_per_day) //TODO: Change me
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.currency.orEmpty(),
                onValueChange = onCurrencyChanged,
                label = {
                    Text(
                        text = stringResource(id = R.string.currency)
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 6.dp)
            )

//            RadioItem(
//                modifier = Modifier.fillMaxWidth(),
//                text = stringResource(R.string.label_specify_cyclic_budget),
//                isSelected = state.isBudgetConstant,
//                onClick = onClickConstantBudget
//            )
//
//            RadioItem(
//                modifier = Modifier.fillMaxWidth(),
//                text = stringResource(R.string.label_specify_start_end_date),
//                isSelected = !state.isBudgetConstant,
//                onClick = onClickBudgetRate
//            ) //TODO: Enable me

            var isPaymentCycleUnitExpanded by remember { //TODO: Move this to the ViewModel
                mutableStateOf(false)
            }

            GenericDropDownMenu<BudgetType>(
                modifier = Modifier.fillMaxWidth(),
                onSelectionChanged = { newSelection -> onBudgetTypeChanged(newSelection) },
                options = SettingsViewModel.BUDGET_TYPES_LIST, //TODO: Move this to the ViewState
                selectedOption = state.budgetType,
                onParseOptionToString = { budgetType -> budgetType?.let { stringResource(id = it.textRes) }.orEmpty() },
                onUpdateExpandedState = { newExpandedState -> isPaymentCycleUnitExpanded = newExpandedState },
                isExpanded = isPaymentCycleUnitExpanded,
                labelText = stringResource(R.string.label_budget_type)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            when (state.budgetType) {
                BudgetType.MONTHLY -> {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.defaultPaymentDayOfMonth.orEmpty(),
                        onValueChange = onDefaultPaymentDayChanged,
                        label = {
                            Text(
                                text = stringResource(id = R.string.label_payment_day_of_month)
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                    )
                }

                BudgetType.WEEKLY -> {
                    var isDefaultPaymentDayOfWeekExpanded by remember { //TODO: Move this to the ViewModel
                        mutableStateOf(false)
                    }

                    GenericDropDownMenu<DayOfWeek>(
                        modifier = Modifier.fillMaxWidth(),
                        onSelectionChanged = { newSelection -> onDayOfWeekChanged(newSelection) },
                        options = SettingsViewModel.DAY_OF_WEEK_LIST, //TODO: Move this to the ViewState
                        selectedOption = state.defaultPaymentDayOfWeek,
                        onParseOptionToString = { dayOfWeek -> dayOfWeek?.let { getStringForDayOfWeek(it) }.orEmpty() },
                        onUpdateExpandedState = { newExpandedState -> isDefaultPaymentDayOfWeekExpanded = newExpandedState },
                        isExpanded = isDefaultPaymentDayOfWeekExpanded,
                        labelText = stringResource(id = R.string.label_payment_day_of_week)
                    )
                }

                BudgetType.ONCE_ONLY -> {
                    StartToTargetDate(
                        modifier = Modifier.fillMaxWidth(),
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onClickStartDate = onStartDateChanged,
                        onClickTargetDate = onEndDateChanged
                    )
                }
            }

            //TODO: Consider adding a save button + dialog 2023-12-07
        }

    }
}

//private fun formatNumberOrEmpty(number: Float?): String = numberFormat.format(number).orEmpty()

@Preview
@Composable
fun SettingsScreenPreviewConstantBudget_MonthlyBudget() {
    BudgetCalcTheme {
        SettingsScreen(
            state = SettingsState(
                isBudgetConstant = true
            ),
            onNavigateBack = {},
            onLoadSettings = {},
            onClickConstantBudget = {},
            onClickBudgetRate = {},
            onBudgetRateAmountChanged = {},
            onConstantBudgetAmountChanged = {},
            onCurrencyChanged = {},
            onDefaultPaymentDayChanged = {},
            onBudgetTypeChanged = {},
            onDayOfWeekChanged = {},
            onStartDateChanged = {},
            onEndDateChanged = {},
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreviewBudgetRate_WeeklyBudget() {
    BudgetCalcTheme {
        SettingsScreen(
            state = SettingsState(
                isBudgetConstant = false,
                budgetType = BudgetType.WEEKLY
            ),
            onNavigateBack = {},
            onLoadSettings = {},
            onClickConstantBudget = {},
            onClickBudgetRate = {},
            onBudgetRateAmountChanged = {},
            onConstantBudgetAmountChanged = {},
            onCurrencyChanged = {},
            onDefaultPaymentDayChanged = {},
            onBudgetTypeChanged = {},
            onDayOfWeekChanged = {},
            onStartDateChanged = {},
            onEndDateChanged = {},
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreviewBudgetRate_OnceOnlyBudget() {
    BudgetCalcTheme {
        SettingsScreen(
            state = SettingsState(
                isBudgetConstant = false,
                budgetType = BudgetType.ONCE_ONLY
            ),
            onNavigateBack = {},
            onLoadSettings = {},
            onClickConstantBudget = {},
            onClickBudgetRate = {},
            onBudgetRateAmountChanged = {},
            onConstantBudgetAmountChanged = {},
            onCurrencyChanged = {},
            onDefaultPaymentDayChanged = {},
            onBudgetTypeChanged = {},
            onDayOfWeekChanged = {},
            onStartDateChanged = {},
            onEndDateChanged = {},
        )
    }
}