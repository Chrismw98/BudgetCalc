package chrismw.budgetcalc.screens.settings

import MonetaryAmountVisualTransformation
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.ClickableDatePickerTextField
import chrismw.budgetcalc.components.CustomTextField
import chrismw.budgetcalc.components.EmphasisButton
import chrismw.budgetcalc.components.ExitDialog
import chrismw.budgetcalc.components.GenericDropDownMenu
import chrismw.budgetcalc.components.LoadingOverlay
import chrismw.budgetcalc.components.VerticalSpacer
import chrismw.budgetcalc.components.getStringForDayOfWeek
import chrismw.budgetcalc.components.rememberExitDialogState
import chrismw.budgetcalc.data.currency.Currency
import chrismw.budgetcalc.extensions.accent
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.DropDown
import chrismw.budgetcalc.helpers.dateString
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import kotlinx.collections.immutable.ImmutableList
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    viewState: SettingsViewModel.ViewState,
    onNavigateBack: () -> Unit,
    onSaveChanges: () -> Unit,
    onClickConstantBudget: () -> Unit,
    onClickBudgetRate: () -> Unit,
    onConstantBudgetAmountChanged: (String) -> Unit,
    onBudgetRateAmountChanged: (String) -> Unit,
    onDefaultPaymentDayChanged: (String) -> Unit,
    onCurrencyChanged: (Currency) -> Unit,
    onBudgetTypeChanged: (BudgetType) -> Unit,
    onDayOfWeekChanged: (DayOfWeek) -> Unit,
    onStartDateChanged: (LocalDate) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit,
    onUpdateExpandedDropDown: (DropDown) -> Unit,
) {
    val confirmExitDialogState = rememberExitDialogState()
    val onBackPressed: () -> Unit = {
        if (viewState.showConfirmExitDialog) {
            confirmExitDialogState.show()
        } else {
            onNavigateBack()
        }
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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (viewState.isLoading) {
                LoadingOverlay(
                    modifier = Modifier.fillMaxSize(),
                    visible = viewState.isLoading,
                )
            } else {
                VerticalSpacer(6.dp)

                DefinitionSection(
                    isBudgetConstant = viewState.isBudgetConstant,
                    onClickConstantBudget = onClickConstantBudget,
                    onClickBudgetRate = onClickBudgetRate,
                )

                VerticalSpacer(18.dp)

                DetailsSection(
                    currencySymbol = viewState.selectedCurrency?.symbol ?: "$",
                    selectedCurrency = viewState.selectedCurrency,
                    availableCurrencies = viewState.availableCurrencies,
                    isExpanded = viewState.currentlyExpandedDropDown == DropDown.CURRENCY,
                    isBudgetConstant = viewState.isBudgetConstant,
                    constantBudgetAmount = viewState.constantBudgetAmount,
                    budgetRateAmount = viewState.budgetRateAmount,
                    onCurrencyChanged = onCurrencyChanged,
                    onUpdateExpandedDropDown = onUpdateExpandedDropDown,
                    onConstantBudgetAmountChanged = onConstantBudgetAmountChanged,
                    onBudgetRateAmountChanged = onBudgetRateAmountChanged,
                )

                VerticalSpacer(18.dp)

                PeriodSection(
                    budgetType = viewState.budgetType,
                    defaultPaymentDayOfMonth = viewState.defaultPaymentDayOfMonth,
                    defaultPaymentDayOfWeek = viewState.defaultPaymentDayOfWeek,
                    dayOfWeekOptions = viewState.dayOfWeekOptions,
                    startDate = viewState.startDate,
                    endDate = viewState.endDate,
                    today = viewState.today,
                    currentlyExpandedDropDown = viewState.currentlyExpandedDropDown,
                    onBudgetTypeChanged = onBudgetTypeChanged,
                    onDefaultPaymentDayChanged = onDefaultPaymentDayChanged,
                    onDayOfWeekChanged = onDayOfWeekChanged,
                    onUpdateExpandedDropDown = onUpdateExpandedDropDown,
                    onStartDateChanged = onStartDateChanged,
                    onEndDateChanged = onEndDateChanged
                )

                VerticalSpacer(18.dp)

                EmphasisButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.settings_save_btn_label),
                    onClick = {
                        onSaveChanges()
                        onNavigateBack()
                    }
                )

                VerticalSpacer(24.dp)
            }

            ExitDialog(
                state = confirmExitDialogState,
                onConfirm = onNavigateBack,
                onDismiss = { confirmExitDialogState.hide() }
            )
        }

        LoadingOverlay(visible = viewState.isLoading)
    }
}

@Composable
private fun PeriodSection(
    budgetType: BudgetType?,
    defaultPaymentDayOfMonth: String?,
    defaultPaymentDayOfWeek: DayOfWeek?,
    dayOfWeekOptions: ImmutableList<DayOfWeek>,
    startDate: LocalDate?,
    endDate: LocalDate?,
    today: LocalDate,
    currentlyExpandedDropDown: DropDown,
    onBudgetTypeChanged: (BudgetType) -> Unit,
    onDefaultPaymentDayChanged: (String) -> Unit,
    onDayOfWeekChanged: (DayOfWeek) -> Unit,
    onUpdateExpandedDropDown: (DropDown) -> Unit,
    onStartDateChanged: (LocalDate) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit
) {
    SettingsSection(
        modifier = Modifier.fillMaxWidth(),
        titleResId = R.string.settings_period_title,
        descriptionResId = R.string.settings_period_description,
    ) {
        Text(
            text = stringResource(R.string.settings_period_chips_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        BudgetTypeChips(
            selectedBudgetType = budgetType,
            onBudgetTypeChanged = onBudgetTypeChanged,
        )

        VerticalSpacer(8.dp)

        when (budgetType) {
            BudgetType.Monthly -> {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = defaultPaymentDayOfMonth.orEmpty(),
                    onValueChange = onDefaultPaymentDayChanged,
                    labelText = stringResource(R.string.settings_period_reset_monthly_subtitle),
                    placeholderText = stringResource(R.string.settings_period_reset_monthly_hint),
                    supportingText = stringResource(R.string.settings_period_reset_monthly_description),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                )
            }

            BudgetType.Weekly -> {
                GenericDropDownMenu<DayOfWeek>(
                    modifier = Modifier.fillMaxWidth(),
                    onSelectionChanged = onDayOfWeekChanged,
                    options = dayOfWeekOptions,
                    selectedOption = defaultPaymentDayOfWeek,
                    onParseOptionToString = { dayOfWeek -> dayOfWeek?.let { getStringForDayOfWeek(it) }.orEmpty() },
                    onExpandedMenuChanged = onUpdateExpandedDropDown,
                    dropDownType = DropDown.PAYMENT_DAY_OF_WEEK,
                    isExpanded = currentlyExpandedDropDown == DropDown.PAYMENT_DAY_OF_WEEK,
                    labelText = stringResource(R.string.settings_period_reset_weekly_subtitle),
                    placeholderText = stringResource(R.string.settings_period_reset_weekly_hint),
                    supportingText = stringResource(R.string.settings_period_reset_weekly_description),
                )
            }

            BudgetType.OnceOnly -> {
                ClickableDatePickerTextField(
                    value = startDate?.toEpochMillis()?.let { dateString(it) }
                        .orEmpty(), //TODO: This logic could be inside the ViewModel, or its own state
                    onDateSelected = onStartDateChanged,
                    label = stringResource(id = R.string.settings_period_reset_once_only_start_subtitle),
                    placeholderText = stringResource(id = R.string.settings_period_reset_once_only_hint),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_start_date),
                            contentDescription = null
                        )
                    },
                    initialDate = startDate ?: if (endDate != null) endDate.minusDays(1) else today,
                    allowedDateValidator = {
                        if (endDate != null) !it.isAfter(endDate) else true
                    }
                )

                VerticalSpacer(16.dp)

                ClickableDatePickerTextField(
                    value = endDate?.toEpochMillis()?.let { dateString(it) }.orEmpty(),
                    onDateSelected = onEndDateChanged,
                    label = stringResource(id = R.string.settings_period_reset_once_only_end_subtitle),
                    placeholderText = stringResource(id = R.string.settings_period_reset_once_only_hint),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_end_date),
                            contentDescription = null
                        )
                    },
                    initialDate = endDate ?: if (startDate != null) startDate.plusDays(1) else today.plusDays(
                        1
                    ),
                    allowedDateValidator = {
                        if (startDate != null) !it.isBefore(startDate) else true
                    }
                )
            }

            null -> {
                /* Empty state */
            }
        }
    }
}

@Composable
private fun BudgetTypeChips(
    selectedBudgetType: BudgetType?,
    onBudgetTypeChanged: (BudgetType) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BudgetTypeChip(
            labelResId = R.string.settings_period_chips_monthly,
            selected = selectedBudgetType == BudgetType.Monthly,
            onClick = { onBudgetTypeChanged(BudgetType.Monthly) },
        )

        BudgetTypeChip(
            labelResId = R.string.settings_period_chips_weekly,
            selected = selectedBudgetType == BudgetType.Weekly,
            onClick = { onBudgetTypeChanged(BudgetType.Weekly) },
        )

        BudgetTypeChip(
            labelResId = R.string.settings_period_chips_once_only,
            selected = selectedBudgetType == BudgetType.OnceOnly,
            onClick = { onBudgetTypeChanged(BudgetType.OnceOnly) },
        )
    }
}

@Composable
private fun BudgetTypeChip(
    @StringRes labelResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    AssistChip(
        label = {
            Text(
                text = stringResource(labelResId),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        onClick = onClick,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.surface
            } else {
                Color.White
            },
            labelColor = if (selected) {
                MaterialTheme.colorScheme.accent
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = if (selected) {
                MaterialTheme.colorScheme.accent
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        )
    )
}

@Composable
private fun DetailsSection(
    currencySymbol: String,
    selectedCurrency: Currency?,
    availableCurrencies: ImmutableList<Currency>,
    isExpanded: Boolean,
    isBudgetConstant: Boolean?,
    constantBudgetAmount: String?,
    budgetRateAmount: String?,
    onCurrencyChanged: (Currency) -> Unit,
    onUpdateExpandedDropDown: (DropDown) -> Unit,
    onConstantBudgetAmountChanged: (String) -> Unit,
    onBudgetRateAmountChanged: (String) -> Unit
) {
    SettingsSection(
        modifier = Modifier.fillMaxWidth(),
        titleResId = R.string.settings_details_title,
        descriptionResId = R.string.settings_details_description,
    ) {
        val leadingIconStyle = if (currencySymbol.length <= 2) {
            MaterialTheme.typography.titleLarge
        } else {
            MaterialTheme.typography.titleMedium
        }

        GenericDropDownMenu<Currency>(
            modifier = Modifier.fillMaxWidth(),
            onSelectionChanged = onCurrencyChanged,
            options = availableCurrencies,
            selectedOption = selectedCurrency,
            onParseOptionToString = {
                it?.toDisplayName().orEmpty()
            },
            onExpandedMenuChanged = onUpdateExpandedDropDown,
            dropDownType = DropDown.CURRENCY,
            isExpanded = isExpanded,
            labelText = stringResource(R.string.settings_details_currency_label),
            placeholderText = stringResource(R.string.settings_details_currency_hint),
            leadingIcon = {
                Text(
                    text = currencySymbol,
                    style = leadingIconStyle,
                )
            }
        )

        VerticalSpacer(16.dp)

        CustomTextField(
            modifier = Modifier.fillMaxWidth(), //TODO: Refactoring potential, implementing sealed class?
            enabled = isBudgetConstant != null,
            value = when (isBudgetConstant) {
                true -> constantBudgetAmount
                false -> budgetRateAmount
                null -> ""
            }.orEmpty(),
            onValueChange = when (isBudgetConstant) {
                true -> onConstantBudgetAmountChanged
                false -> onBudgetRateAmountChanged
                null -> { _ -> }
            },
            labelText = stringResource(
                id = when (isBudgetConstant) {
                    true -> R.string.settings_details_amount_constant_label
                    false -> R.string.settings_details_amount_daily_label
                    null -> R.string.settings_details_amount_undefined_label
                }
            ),
            singleLine = true,
            placeholderText = stringResource(
                id = when (isBudgetConstant) {
                    null -> R.string.settings_details_amount_undefined_hint
                    else -> R.string.settings_details_amount_defined_hint
                }
            ),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_budget_amount),
                    contentDescription = null,
                )
            },
            visualTransformation = MonetaryAmountVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
        )
    }
}

@Composable
private fun DefinitionSection(
    isBudgetConstant: Boolean?,
    onClickConstantBudget: () -> Unit,
    onClickBudgetRate: () -> Unit,
) {
    SettingsSection(
        modifier = Modifier.fillMaxWidth(),
        titleResId = R.string.settings_definition_title,
        descriptionResId = R.string.settings_definition_description,
    ) {
        BudgetDefinitionCard(
            labelResId = R.string.settings_definition_constant_label,
            descriptionResId = R.string.settings_definition_constant_description,
            iconResId = R.drawable.ic_currency,
            selected = isBudgetConstant == true,
            onClick = onClickConstantBudget,
        )

        VerticalSpacer(12.dp)

        BudgetDefinitionCard(
            labelResId = R.string.settings_definition_daily_label,
            descriptionResId = R.string.settings_definition_daily_description,
            iconResId = R.drawable.ic_event,
            selected = isBudgetConstant == false,
            onClick = onClickBudgetRate,
        )
    }
}

@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    SettingsCard(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(titleResId),
            style = MaterialTheme.typography.labelLarge,
        )

        VerticalSpacer(6.dp)

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(descriptionResId),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )

        VerticalSpacer(16.dp)

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.inverseOnSurface,
        )

        VerticalSpacer(16.dp)

        content()
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreviewConstantBudget_MonthlyBudget() {
    BudgetCalcTheme {
        SettingsScreen(
            viewState = SettingsViewModel.ViewState(
                isLoading = false,
                isBudgetConstant = true,
                constantBudgetAmount = null,
                budgetType = BudgetType.OnceOnly,
            ),
            onNavigateBack = {},
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

//@Preview
//@Composable
//fun SettingsScreenPreviewBudgetRate_WeeklyBudget() {
//    BudgetCalcTheme {
//        SettingsScreen(
//            viewState = SettingsViewModel.ViewState(
//                isLoading = false,
//                isBudgetConstant = false,
//                budgetType = BudgetType.Weekly,
//                selectedCurrency = Currency("USD", "US Dollar", "$"),
//                availableCurrencies = persistentListOf(
//                    Currency("USD", "US Dollar", "$"),
//                    Currency("EUR", "Euro", "€"),
//                    Currency("GBP", "British Pound", "£"),
//                )
//            ),
//            onNavigateBack = {},
//            onSaveChanges = {},
//            onClickConstantBudget = {},
//            onClickBudgetRate = {},
//            onBudgetRateAmountChanged = {},
//            onConstantBudgetAmountChanged = {},
//            onCurrencyChanged = {},
//            onDefaultPaymentDayChanged = {},
//            onBudgetTypeChanged = {},
//            onDayOfWeekChanged = {},
//            onStartDateChanged = {},
//            onEndDateChanged = {},
//            onUpdateExpandedDropDown = {},
//        )
//    }
//}
//
//@Preview
//@Composable
//fun SettingsScreenPreviewBudgetRate_OnceOnlyBudget() {
//    BudgetCalcTheme {
//        SettingsScreen(
//            viewState = SettingsViewModel.ViewState(
//                isLoading = false,
//                isBudgetConstant = false,
//                budgetType = BudgetType.OnceOnly
//            ),
//            onNavigateBack = {},
//            onSaveChanges = {},
//            onClickConstantBudget = {},
//            onClickBudgetRate = {},
//            onBudgetRateAmountChanged = {},
//            onConstantBudgetAmountChanged = {},
//            onCurrencyChanged = {},
//            onDefaultPaymentDayChanged = {},
//            onBudgetTypeChanged = {},
//            onDayOfWeekChanged = {},
//            onStartDateChanged = {},
//            onEndDateChanged = {},
//            onUpdateExpandedDropDown = {},
//        )
//    }
//}
