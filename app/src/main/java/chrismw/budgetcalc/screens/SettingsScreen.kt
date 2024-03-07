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
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.ExitDialog
import chrismw.budgetcalc.components.GenericDropDownMenu
import chrismw.budgetcalc.components.LoadingOverlay
import chrismw.budgetcalc.components.RadioItem
import chrismw.budgetcalc.components.StartToTargetDate
import chrismw.budgetcalc.components.getStringForDayOfWeek
import chrismw.budgetcalc.components.rememberExitDialogState
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.DropDown
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsViewModel.ViewState,
    onNavigateBack: () -> Unit,
    onLoadSettings: () -> Unit,
    onSaveChanges: () -> Unit,
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
    onUpdateExpandedDropDown: (DropDown) -> Unit,
) {
    val confirmExitDialogState = rememberExitDialogState()
    val onBackPressed: () -> Unit = {
        if (state.showConfirmExitDialog) {
            confirmExitDialogState.show()
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        onLoadSettings()
    }

    BackHandler {
        onBackPressed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
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

            GenericDropDownMenu<BudgetType>(
                modifier = Modifier.fillMaxWidth(),
                onSelectionChanged = { newSelection -> onBudgetTypeChanged(newSelection) },
                options = state.budgetTypeOptions,
                selectedOption = state.budgetType,
                onParseOptionToString = { budgetType -> budgetType?.let { stringResource(id = it.textRes) }.orEmpty() },
                onExpandedMenuChanged = onUpdateExpandedDropDown,
                isExpanded = state.currentlyExpandedDropDown == DropDown.BUDGET_TYPE,
                dropDownType = DropDown.BUDGET_TYPE,
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
                    GenericDropDownMenu<DayOfWeek>(
                        modifier = Modifier.fillMaxWidth(),
                        onSelectionChanged = { newSelection -> onDayOfWeekChanged(newSelection) },
                        options = state.dayOfWeekOptions,
                        selectedOption = state.defaultPaymentDayOfWeek,
                        onParseOptionToString = { dayOfWeek -> dayOfWeek?.let { getStringForDayOfWeek(it) }.orEmpty() },
                        onExpandedMenuChanged = onUpdateExpandedDropDown,
                        isExpanded = state.currentlyExpandedDropDown == DropDown.PAYMENT_DAY_OF_WEEK,
                        dropDownType = DropDown.PAYMENT_DAY_OF_WEEK,
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

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                onClick = {
                    onSaveChanges()
                    onNavigateBack()
                }
            ) {
                Text(text = stringResource(R.string.label_save_changes))
            }
        }

    }

    ExitDialog(
        state = confirmExitDialogState,
        onConfirm = onNavigateBack,
        onDismiss = { confirmExitDialogState.hide() }
    )

    LoadingOverlay(visible = state.isLoading)
}

//private fun formatNumberOrEmpty(number: Float?): String = numberFormat.format(number).orEmpty()

@Preview
@Composable
fun SettingsScreenPreviewConstantBudget_MonthlyBudget() {
    BudgetCalcTheme {
        SettingsScreen(
            state = SettingsViewModel.ViewState(
                isBudgetConstant = true
            ),
            onNavigateBack = {},
            onLoadSettings = {},
            onSaveChanges = {},
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
            onUpdateExpandedDropDown = {},
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreviewBudgetRate_WeeklyBudget() {
    BudgetCalcTheme {
        SettingsScreen(
            state = SettingsViewModel.ViewState(
                isBudgetConstant = false,
                budgetType = BudgetType.WEEKLY
            ),
            onNavigateBack = {},
            onLoadSettings = {},
            onSaveChanges = {},
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
            onUpdateExpandedDropDown = {},
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreviewBudgetRate_OnceOnlyBudget() {
    BudgetCalcTheme {
        SettingsScreen(
            state = SettingsViewModel.ViewState(
                isBudgetConstant = false,
                budgetType = BudgetType.ONCE_ONLY
            ),
            onNavigateBack = {},
            onLoadSettings = {},
            onSaveChanges = {},
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
            onUpdateExpandedDropDown = {},
        )
    }
}