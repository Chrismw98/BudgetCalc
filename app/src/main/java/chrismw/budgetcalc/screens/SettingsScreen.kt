package chrismw.budgetcalc.screens

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import chrismw.budgetcalc.components.RadioItem
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

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
) {
    LaunchedEffect(key1 = true) {
        onLoadSettings()
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

            var isPaymentCycleUnitExpanded by remember {
                mutableStateOf(false)
            }

            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = isPaymentCycleUnitExpanded,
                onExpandedChange = {
//                        onExpandedMenuChanged(if (it) type else DropDown.NONE)
                    isPaymentCycleUnitExpanded = !isPaymentCycleUnitExpanded
                }
            ) {
                OutlinedTextField( //TODO: Latest TODO - 2023-11-13
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true,
                    value = stringResource(id = state.budgetType.textRes),
                    onValueChange = {}, //TODO: Implement this
                    readOnly = true,
                    label = {
                        Text(
                            text = "Budget Type" //TODO: Exctract string resource
                        )
                    },
                    enabled = true, //TODO: Adjust this
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = isPaymentCycleUnitExpanded
                        )
                    }
                )
//        Can't use ExposedDropdownMenu because of this bug:
//        https://issuetracker.google.com/issues/205589613
//        ExposedDropdownMenu(
                ExposedDropdownMenu(
                    modifier = Modifier.exposedDropdownSize(
                        matchTextFieldWidth = true
                    ),
                    expanded = isPaymentCycleUnitExpanded,
                    onDismissRequest = {
//                            onExpandedMenuChanged(DropDown.NONE)
                        //TODO: Implement this
                    }
                ) {
//                    val units = listOf("Days", "Weeks", "Months")
                    BudgetType.values().forEach {
                        DropdownMenuItem(
                            onClick = {

//                                    onLocationChanged(it)
//                                    onExpandedMenuChanged(DropDown.NONE)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            text = {
                                Text(text = stringResource(id = it.textRes))
                            }
                        )
                    }
                }
            }

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

            //TODO: Consider adding a save button + dialog 2023-12-07

//            Row(
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                OutlinedTextField(
//                    modifier = Modifier.weight(1f),
//                    value = state.defaultPaymentDayOfMonth.orEmpty(),
//                    onValueChange = onPaymentCycleLengthChanged,
//                    label = {
//                        Text(
//                            text = stringResource(id = R.string.payment_cycle_length)
//                        )
//                    },
//                    keyboardOptions = KeyboardOptions.Default.copy(
//                        keyboardType = KeyboardType.Decimal,
//                        imeAction = ImeAction.Next,
//                    ),
//                )
//
//                var isPaymentCycleUnitExpanded by remember {
//                    mutableStateOf(false)
//                }
//
//                Spacer(modifier = Modifier.width(6.dp))
//
//                ExposedDropdownMenuBox(
//                    modifier = Modifier.weight(0.5f),
//                    expanded = isPaymentCycleUnitExpanded,
//                    onExpandedChange = {
////                        onExpandedMenuChanged(if (it) type else DropDown.NONE)
//                        isPaymentCycleUnitExpanded = !isPaymentCycleUnitExpanded
//                    }
//                ) {
//                    OutlinedTextField(
//                        modifier = Modifier
//                            .menuAnchor(),
//                        singleLine = true,
//                        value = stringResource(id = R.string.days),
//                        onValueChange = {}, //TODO: Implement this
//                        readOnly = true,
//                        label = {
//                            Text(
//                                text = "Unit"
//                            )
//                        },
//                        enabled = true, //TODO: Adjust this
//                        trailingIcon = {
//                            ExposedDropdownMenuDefaults.TrailingIcon(
//                                expanded = isPaymentCycleUnitExpanded
//                            )
//                        }
//                    )
////        Can't use ExposedDropdownMenu because of this bug:
////        https://issuetracker.google.com/issues/205589613
////        ExposedDropdownMenu(
//                    ExposedDropdownMenu(
//                        modifier = Modifier.exposedDropdownSize(
//                            matchTextFieldWidth = true
//                        ),
//                        expanded = isPaymentCycleUnitExpanded,
//                        onDismissRequest = {
////                            onExpandedMenuChanged(DropDown.NONE)
//                            //TODO: Implement this
//                        }
//                    ) {
//                        val units = listOf("Days", "Weeks", "Months")
//                        units.forEach {
//                            DropdownMenuItem(
//                                onClick = {
////                                    onLocationChanged(it)
////                                    onExpandedMenuChanged(DropDown.NONE)
//                                },
//                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//                                text = {
//                                    Text(text = it)
//                                }
//                            )
//                        }
//                    }
//                }
//            }
        }

    }
}

//private fun formatNumberOrEmpty(number: Float?): String = numberFormat.format(number).orEmpty()

@Preview
@Composable
fun SettingsScreenPreviewConstantBudget() {
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
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreviewBudgetRate() {
    BudgetCalcTheme {
        SettingsScreen(
            state = SettingsState(
                isBudgetConstant = false
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
        )
    }
}