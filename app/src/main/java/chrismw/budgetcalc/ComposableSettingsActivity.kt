package chrismw.budgetcalc

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import chrismw.budgetcalc.components.RadioItem
import chrismw.budgetcalc.databinding.ActivitySettingsBinding
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat

val numberFormat = NumberFormat.getNumberInstance()

@AndroidEntryPoint
class ComposableSettingsActivity : ComponentActivity() {

    private var binding: ActivitySettingsBinding? = null

    private var etPaymentDayOfMonth: EditText? = null
    private var etBudgetInMonetaryUnitsPerDay: EditText? = null
    private var etCurrency: EditText? = null

    private var settings: SharedPreferences? = null
    private var settingsEditor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivitySettingsBinding.inflate(layoutInflater)
//        setContentView(binding?.root)

//        val viewModel = SettingsViewModel()
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        val viewModel: SettingsViewModel by viewModels()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect {

                    setContent {
                        BudgetCalcTheme(darkTheme = false) {
                            SettingsScreen(
                                state = it,
                                onNavigateBack = {
                                    lifecycleScope.launch {
                                        viewModel.saveSettings()
                                    }
                                },
                                onLoadSettings = viewModel::loadSettings,
                                onClickConstantBudget = {
                                    if (!it.isBudgetConstant) {
                                        viewModel.setIsBudgetConstant(true)
                                    }
                                },
                                onClickBudgetRate = {
                                    if (it.isBudgetConstant) {
                                        viewModel.setIsBudgetConstant(false)
                                    }
                                },
                                onConstantBudgetAmountChanged = viewModel::setConstantBudgetAmount,
                                onBudgetRateAmountChanged = viewModel::setBudgetRateAmount,
                                onDefaultPaymentDayChanged = viewModel::setDefaultPaymentDay,
                                onCurrencyChanged = viewModel::setCurrency,
                                onPaymentCycleLengthChanged = viewModel::setPaymentCycleLength,
                            )
                        }
                    }
                }
            }
        }

//        setSupportActionBar(binding?.toolbarSettings)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding?.toolbarSettings?.setNavigationOnClickListener {
            onBackPressed()
        }

        settings = applicationContext.getSharedPreferences(
            Constants.SHARED_PREFERENCES_FILENAME,
            MODE_PRIVATE
        )
        settingsEditor = settings!!.edit()

        etPaymentDayOfMonth = binding?.etPaymentDayOfMonth
        etBudgetInMonetaryUnitsPerDay = binding?.etBudgetInMonetaryUnitsPerDay
        etCurrency = binding?.etCurrency

        initializeEditTextFilters()
        initializeHintTexts()
        initializeFieldValuesIfKnown()

        etPaymentDayOfMonth?.doOnTextChanged { text, _, _, _ ->
            if (text == null || text.toString().isEmpty()) {
                deleteValueFromSettings(Constants.LATEST_PAYMENT_DAY_OF_MONTH)
            } else {
                updateIntValueInSettings(
                    Constants.LATEST_PAYMENT_DAY_OF_MONTH,
                    text.toString().toInt()
                )
            }
        }

        etBudgetInMonetaryUnitsPerDay?.doOnTextChanged { text, _, _, _ ->
            if (text == null || text.toString().isEmpty()) {
                deleteValueFromSettings(Constants.LATEST_BUDGET_AMOUNT_IN_MONETARY_UNITS_PER_DAY)
            } else {
                updateIntValueInSettings(
                    Constants.LATEST_BUDGET_AMOUNT_IN_MONETARY_UNITS_PER_DAY,
                    text.toString().toInt()
                )
            }
        }

        etCurrency?.doOnTextChanged { text, _, _, _ ->
            if (text == null || text.toString().isEmpty()) {
                deleteValueFromSettings(Constants.LATEST_CURRENCY)
            } else {
                updateStringValueInSettings(Constants.LATEST_CURRENCY, text.toString())
            }
        }
    }

    private fun initializeEditTextFilters() {
        etPaymentDayOfMonth?.filters = arrayOf(
            InputFilterMinMax(
                1,
                31
            )
        ) //Because a month can only have 1 to 31 days at most (2023-02-16)
    }

    private fun initializeFieldValuesIfKnown() {
        if (settings!!.contains(Constants.LATEST_PAYMENT_DAY_OF_MONTH)) {
            setPaymentDayOfMonthToLastKnown()
        }
        if (settings!!.contains(Constants.LATEST_BUDGET_AMOUNT_IN_MONETARY_UNITS_PER_DAY)) {
            setBudgetInMonetaryUnitsPerDayToLastKnown()
        }
        if (settings!!.contains(Constants.LATEST_CURRENCY)) {
            setCurrencyToLastKnown()
        }
    }

    private fun initializeHintTexts() {
        etPaymentDayOfMonth?.hint = Constants.defaultPaymentDayOfMonth.toString()
        etBudgetInMonetaryUnitsPerDay?.hint =
            Constants.defaultBudgetAmountInMonetaryUnitsPerDay.toString()
        etCurrency?.hint = Constants.defaultCurrency
    }

    private fun setPaymentDayOfMonthToLastKnown() {
        val latestPaymentDayOfMonth = settings!!.getInt(Constants.LATEST_PAYMENT_DAY_OF_MONTH, -1)
        etPaymentDayOfMonth?.setText(
            latestPaymentDayOfMonth.toString(),
            TextView.BufferType.EDITABLE
        )
    }

    private fun setBudgetInMonetaryUnitsPerDayToLastKnown() {
        val latestBudgetInCurrencyPerDay =
            settings!!.getInt(Constants.LATEST_BUDGET_AMOUNT_IN_MONETARY_UNITS_PER_DAY, -1)
        etBudgetInMonetaryUnitsPerDay?.setText(
            latestBudgetInCurrencyPerDay.toString(),
            TextView.BufferType.EDITABLE
        )
    }

    private fun setCurrencyToLastKnown() {
        val latestCurrency = settings!!.getString(Constants.LATEST_CURRENCY, "")
        etCurrency?.setText(latestCurrency, TextView.BufferType.EDITABLE)
    }

    private fun updateIntValueInSettings(key: String, value: Int) {
        settingsEditor?.putInt(key, value)
        settingsEditor?.apply()
    }

    private fun updateStringValueInSettings(key: String, value: String) {
        settingsEditor?.putString(key, value)
        settingsEditor?.apply()
    }

    private fun deleteValueFromSettings(key: String) {
        if (settings?.contains(key) == true) {
            settingsEditor?.remove(key)
        }
        settingsEditor?.apply()
    }

    //Taken from https://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}

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
    onPaymentCycleLengthChanged: (String) -> Unit,
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
                text = "Budget is constant amount",
                isSelected = state.isBudgetConstant,
                onClick = onClickConstantBudget
            )

            RadioItem(
                modifier = Modifier.fillMaxWidth(),
                text = "Budget is a rate",
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
                        onValueChange = onBudgetRateAmountChanged,
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


                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    var isBudgetRateUnitExpanded by remember { //TODO: Check if this is needed
                        mutableStateOf(false)
                    }

                    ExposedDropdownMenuBox(
                        modifier = Modifier.weight(0.5f),
                        expanded = isBudgetRateUnitExpanded,
                        onExpandedChange = {
//                        onExpandedMenuChanged(if (it) type else DropDown.NONE)
                            isBudgetRateUnitExpanded = !isBudgetRateUnitExpanded
                        }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .menuAnchor(),
                            singleLine = true,
                            value = stringResource(id = R.string.days),
                            onValueChange = {}, //TODO: Implement this
                            readOnly = true,
                            label = {
                                Text(
                                    text = "Unit"
                                )
                            },
                            enabled = true, //TODO: Adjust this
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = isBudgetRateUnitExpanded
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
                            expanded = isBudgetRateUnitExpanded,
                            onDismissRequest = {
//                            onExpandedMenuChanged(DropDown.NONE)
                                //TODO: Implement this
                            }
                        ) {
                            val units = listOf("Days", "Weeks", "Months")
                            units.forEach {
                                DropdownMenuItem(
                                    onClick = {
//                                    onLocationChanged(it)
//                                    onExpandedMenuChanged(DropDown.NONE)
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    text = {
                                        Text(text = it)
                                    }
                                )
                            }
                        }
                    }

                }
            }


            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.defaultPaymentDay.orEmpty(),
                onValueChange = onDefaultPaymentDayChanged,
                label = {
                    Text(
                        text = stringResource(id = R.string.default_payment_date)
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
            )


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


            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.paymentCycleLength.orEmpty(),
                    onValueChange = onPaymentCycleLengthChanged,
                    label = {
                        Text(
                            text = stringResource(id = R.string.payment_cycle_length)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                )

                var isPaymentCycleUnitExpanded by remember {
                    mutableStateOf(false)
                }

                Spacer(modifier = Modifier.width(6.dp))

                ExposedDropdownMenuBox(
                    modifier = Modifier.weight(0.5f),
                    expanded = isPaymentCycleUnitExpanded,
                    onExpandedChange = {
//                        onExpandedMenuChanged(if (it) type else DropDown.NONE)
                        isPaymentCycleUnitExpanded = !isPaymentCycleUnitExpanded
                    }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor(),
                        singleLine = true,
                        value = stringResource(id = R.string.days),
                        onValueChange = {}, //TODO: Implement this
                        readOnly = true,
                        label = {
                            Text(
                                text = "Unit"
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
                        val units = listOf("Days", "Weeks", "Months")
                        units.forEach {
                            DropdownMenuItem(
                                onClick = {
//                                    onLocationChanged(it)
//                                    onExpandedMenuChanged(DropDown.NONE)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                text = {
                                    Text(text = it)
                                }
                            )
                        }
                    }
                }
            }
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
            onPaymentCycleLengthChanged = {},
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
            onPaymentCycleLengthChanged = {},
        )
    }
}