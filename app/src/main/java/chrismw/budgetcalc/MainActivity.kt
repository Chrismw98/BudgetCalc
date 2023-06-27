@file:OptIn(ExperimentalFoundationApi::class)

package chrismw.budgetcalc

import MetricAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chrismw.budgetcalc.components.CircularProgressbar
import chrismw.budgetcalc.components.MetricItem
import chrismw.budgetcalc.components.StartToTargetDate
import chrismw.budgetcalc.databinding.ActivityMainBinding
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var formatter = DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy", Locale.getDefault());

    private var binding: ActivityMainBinding? = null

    private var etBudgetAmount: EditText? = null
    private var etPaymentCycleLength: EditText? = null

    private var tvStartDate: TextView? = null
    private var tvTargetDate: TextView? = null
    private var startDate: LocalDate? = null
    private val today = LocalDate.now()

    private var targetDate = today
    private var latestPaymentDate: LocalDate? = null
    private var defaultBudgetAmount: Int = 0
    private var defaultLengthOfPaymentCycleInDays: Int = 0

    private var budgetAmount: Int = 0
    private var lengthOfPaymentCycleInDays: Int = 0

    private var dailyBudgetMetric = Metric("", 0.0, MetricUnit.CURRENCY_PER_DAY)
    private var currentBudgetMetric = Metric("", 0.0, MetricUnit.CURRENCY)
    private var remainingBudgetMetric = Metric("", 0.0, MetricUnit.CURRENCY)
    private var daysSinceStartMetric = Metric("", 0.0, MetricUnit.DAYS)
    private var daysRemainingMetric = Metric("", 0.0, MetricUnit.DAYS)

    private var defaultBudgetAmountInCurrencyPerDay: Int = 0
    private var defaultPaymentDayOfMonth: Int = 0
    private var defaultCurrency: String = ""

    private var startDatePicker: MaterialDatePicker<Long>? = null
    private var targetDatePicker: MaterialDatePicker<Long>? = null

    private var metricAdapter: MetricAdapter? = null

    private var settings: SharedPreferences? = null
    private var settingsEditor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding?.root)
        setContent {
            BudgetCalcTheme(darkTheme = false) {
                MyApp()
            }
        }

        settings = applicationContext.getSharedPreferences(
            Constants.SHARED_PREFERENCES_FILENAME,
            MODE_PRIVATE
        )
        settingsEditor = settings!!.edit()

        binding?.ibSettings?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent) //TODO: Change animation to slide
        }

        etBudgetAmount = binding?.etBudgetAmount
        etPaymentCycleLength = binding?.etPaymentCycleLength
        tvStartDate = binding?.tvStartDate
        tvTargetDate = binding?.tvTargetDate

        initializeEditTextFilters()

        val btnEasyAdjust = binding?.btnEasyAdjust
        btnEasyAdjust?.setOnClickListener {
            setStartDateToLatestPaymentDate()
            adjustTargetDateIfNeeded()
            updateDefaultValuesAndHintTexts()
            updateCalculationsInUI()
        }

        val btnJumpToToday = binding?.btnJumpToToday
        btnJumpToToday?.setOnClickListener {
            setTargetDateToToday()
            adjustStartDateIfNeeded()
            updateCalculationsInUI()
        }

        etBudgetAmount?.doOnTextChanged { text, start, before, count ->
            if (text == null || text.toString().isEmpty()) {
                budgetAmount = defaultBudgetAmount
                deleteValueFromSettings(Constants.LATEST_BUDGET_AMOUNT)
            } else {
                budgetAmount = text.toString().toInt()
                updateValueInSettings(Constants.LATEST_BUDGET_AMOUNT, text.toString().toInt())
            }
            updateCalculationsInUI()
        }

        etPaymentCycleLength?.doOnTextChanged { text, start, before, count ->
            if (text == null || text.toString().isEmpty()) {
                lengthOfPaymentCycleInDays = defaultLengthOfPaymentCycleInDays
                deleteValueFromSettings(Constants.LATEST_PAYMENT_CYCLE_LENGTH)
            } else {
                lengthOfPaymentCycleInDays = text.toString().toInt()
                updateValueInSettings(
                    Constants.LATEST_PAYMENT_CYCLE_LENGTH,
                    text.toString().toInt()
                )
            }
            updateCalculationsInUI()
        }

        binding?.ibHint?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.hint))
                .setIcon(R.drawable.ic_info)
                .setMessage(resources.getString(R.string.hint_message))
                .show()
        }

        initializeDataAndUI()

        tvStartDate?.setOnClickListener {
            resetStartDatePicker()
//            startDatePicker?.show(supportFragmentManager, startDatePicker!!.tag) //TODO: These need to be adjusted
        }

        tvTargetDate?.setOnClickListener {
            resetTargetDatePicker()
//            targetDatePicker?.show(supportFragmentManager, targetDatePicker!!.tag) //TODO: These need to be adjusted
        }

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding?.rvMetrics?.layoutManager = layoutManager
        metricAdapter = MetricAdapter(getMetricsList())
        binding?.rvMetrics?.adapter = metricAdapter
    }

    private fun setBudgetAmountToLastKnown() {
        val latestBudgetAmount = settings!!.getInt(Constants.LATEST_BUDGET_AMOUNT, -1)
        etBudgetAmount?.setText(latestBudgetAmount.toString(), TextView.BufferType.EDITABLE)
        budgetAmount = latestBudgetAmount
    }

    private fun setPaymentCycleLengthToLastKnown() {
        val latestPaymentCycleLength = settings!!.getInt(Constants.LATEST_PAYMENT_CYCLE_LENGTH, -1)
        etPaymentCycleLength?.setText(
            latestPaymentCycleLength.toString(),
            TextView.BufferType.EDITABLE
        )
        lengthOfPaymentCycleInDays = latestPaymentCycleLength
    }

    private fun updateValueInSettings(key: String, value: Int) {
        settingsEditor?.putInt(key, value)
        settingsEditor?.apply()
    }

    private fun deleteValueFromSettings(key: String) {
        if (settings?.contains(key) == true) {
            settingsEditor?.remove(key)
        }
        settingsEditor?.apply()
    }

    private fun initializeDataAndUI() {
        initializeConfigurableValuesBasedOnDefaultsOrSettings()
        initializeMetricStrings()
        initializeLatestPaymentDate()
        setStartDateToLatestPaymentDate()
        updateDefaultValuesAndHintTexts()
        updateTargetDateInUI()
        updateCalculationsInUI()
    }

    private fun initializeConfigurableValuesBasedOnDefaultsOrSettings() {
        defaultPaymentDayOfMonth = settings!!.getInt(
            Constants.LATEST_PAYMENT_DAY_OF_MONTH,
            Constants.defaultPaymentDayOfMonth
        )
        defaultBudgetAmountInCurrencyPerDay = settings!!.getInt(
            Constants.LATEST_BUDGET_AMOUNT_IN_MONETARY_UNITS_PER_DAY,
            Constants.defaultBudgetAmountInMonetaryUnitsPerDay
        )
        defaultCurrency = settings!!.getString(
            Constants.LATEST_CURRENCY,
            Constants.defaultCurrency
        ).toString()
        binding?.tvBudgetAmountUnit?.text = defaultCurrency
    }

    private fun updateDefaultLengthOfPaymentCycleInDays() {
        defaultLengthOfPaymentCycleInDays =
            ChronoUnit.DAYS.between(startDate, startDate?.plusMonths(1)).toInt()
        etPaymentCycleLength?.hint = defaultLengthOfPaymentCycleInDays.toString()
    }

    private fun updateDefaultBudgetAmount() {
        defaultBudgetAmount =
            defaultBudgetAmountInCurrencyPerDay * defaultLengthOfPaymentCycleInDays
        etBudgetAmount?.hint = defaultBudgetAmount.toString()
    }

    private fun getMetricsList(): ArrayList<Metric> {
        return arrayListOf(
            daysSinceStartMetric,
            daysRemainingMetric,
            dailyBudgetMetric,
            currentBudgetMetric,
            remainingBudgetMetric,
        )
    }

    private fun initializeMetricStrings() {
        dailyBudgetMetric.name = resources.getString(R.string.daily_budget)
        currentBudgetMetric.name = resources.getString(R.string.budget_until_target_date)
        remainingBudgetMetric.name = resources.getString(R.string.remaining_budget)
        daysSinceStartMetric.name = resources.getString(R.string.days_since_start)
        daysRemainingMetric.name = resources.getString(R.string.days_remaining)
    }

    private fun resetStartDatePicker() {
        startDatePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select start date")
                .setSelection(localDateToMillis(startDate!!))
                .build()
        startDatePicker?.addOnPositiveButtonClickListener {
            startDate = millisToLocalDate(it)
            adjustTargetDateIfNeeded()
            updateDefaultValuesAndHintTexts()
            updateStartDateInUI()
            updateCalculationsInUI()
        }
    }

    private fun updateDefaultValuesAndHintTexts() {
        updateDefaultLengthOfPaymentCycleInDays()
        if (settings!!.contains(Constants.LATEST_PAYMENT_CYCLE_LENGTH)) {
            setPaymentCycleLengthToLastKnown()
        } else {
            lengthOfPaymentCycleInDays = defaultLengthOfPaymentCycleInDays
        }

        updateDefaultBudgetAmount()
        if (settings!!.contains(Constants.LATEST_BUDGET_AMOUNT)) {
            setBudgetAmountToLastKnown()
        } else {
            budgetAmount = defaultBudgetAmount
        }
    }

    private fun resetTargetDatePicker() {
        targetDatePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select target date")
                .setSelection(localDateToMillis(targetDate))
                .build()
        targetDatePicker?.addOnPositiveButtonClickListener {
            targetDate = millisToLocalDate(it)
            adjustStartDateIfNeeded()
            updateTargetDateInUI()
            updateCalculationsInUI()
        }
    }

    private fun adjustTargetDateIfNeeded() {
        if (!startDate!!.isBefore(targetDate)) {
            targetDate = startDate!!.plusDays(1)
            updateTargetDateInUI()
        }
    }

    private fun adjustStartDateIfNeeded() {
        if (!startDate!!.isBefore(targetDate)) {
            startDate = targetDate.minusDays(1)
            updateStartDateInUI()
        }
    }

    private fun updateCalculationsInUI() {
        dailyBudgetMetric.value = budgetAmount.toDouble() / lengthOfPaymentCycleInDays.toDouble()

        daysSinceStartMetric.value = ChronoUnit.DAYS.between(startDate, targetDate).toDouble()
        Log.d("TEST", "Length of payment cycle: $lengthOfPaymentCycleInDays")
        Log.d("TEST", "Days since start: ${daysSinceStartMetric.value}")
        daysRemainingMetric.value = lengthOfPaymentCycleInDays - daysSinceStartMetric.value.toDouble()
        Log.d("TEST", "Days remaining: ${daysRemainingMetric.value}")

        currentBudgetMetric.value =
            if (daysSinceStartMetric.value.toDouble() <= lengthOfPaymentCycleInDays) daysSinceStartMetric.value.toDouble() * dailyBudgetMetric.value.toDouble() else budgetAmount.toDouble()
        remainingBudgetMetric.value =
            if (daysSinceStartMetric.value.toDouble() <= lengthOfPaymentCycleInDays) budgetAmount - currentBudgetMetric.value.toDouble() else 0.0

        metricAdapter?.notifyDataSetChanged()
    }

    private fun initializeLatestPaymentDate() {
        val todayAsDayOfMonth = today.dayOfMonth
        val lastMonth = today.minusMonths(1)

        latestPaymentDate = when {
            todayAsDayOfMonth < defaultPaymentDayOfMonth -> {
                val maxLengthOfLastMonth = lastMonth.month.length(lastMonth.isLeapYear)
                if (maxLengthOfLastMonth < defaultPaymentDayOfMonth) {
                    lastMonth.withDayOfMonth(maxLengthOfLastMonth)
                } else {
                    lastMonth.withDayOfMonth(defaultPaymentDayOfMonth)
                }
            }

            todayAsDayOfMonth > defaultPaymentDayOfMonth -> {
                today.withDayOfMonth(defaultPaymentDayOfMonth)
            }

            else -> {
                lastMonth
            }
        }
    }

    private fun updateStartDateInUI() {
        tvStartDate?.text = startDate?.format(formatter)
    }

    private fun updateTargetDateInUI() {
        tvTargetDate?.text = targetDate.format(formatter)
    }

    private fun setStartDateToLatestPaymentDate() {
        startDate = latestPaymentDate
        updateStartDateInUI()
    }

    private fun setTargetDateToToday() {
        targetDate = today
        updateTargetDateInUI()
    }

    private fun localDateToMillis(localDate: LocalDate): Long {
        return localDate.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun millisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun initializeEditTextFilters() {
        etBudgetAmount?.filters = arrayOf(
            InputFilterMinMax(
                1,
                Integer.MAX_VALUE
            )
        )
        etPaymentCycleLength?.filters = arrayOf(
            InputFilterMinMax(
                1,
                Integer.MAX_VALUE
            )
        )
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

    override fun onResume() {
        super.onResume()
        initializeDataAndUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        startDatePicker?.onDestroy()
        targetDatePicker?.onDestroy()

        binding = null
    }
}

@Composable
private fun MyApp() {
    Surface(
//        color = colorResource(id = R.color.color_background),
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()

    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            //TODO: Add top bar with settings navigation (2023-06-13)
            var startDate by rememberSaveable {
                mutableStateOf(LocalDate.now())
            }
            var targetDate by rememberSaveable {
                mutableStateOf(LocalDate.now().plusDays(1))
            }


            Spacer(modifier = Modifier.height(30.dp))

//            var remainingBudget by rememberSaveable {
//                mutableStateOf(246.672f)
//            }

            val maxBudget = 600f

            val lengthOfPaymentCycleInDays = startDate.lengthOfMonth()
            val dailyBudget = maxBudget / lengthOfPaymentCycleInDays

            val daysSinceStart = ChronoUnit.DAYS.between(startDate, targetDate)
            val daysRemaining = lengthOfPaymentCycleInDays - daysSinceStart

            val currentBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) daysSinceStart * dailyBudget else maxBudget
            val remainingBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) maxBudget - currentBudget else 0f

            CircularProgressbar(
                maxBudget = maxBudget,
                remainingBudget = remainingBudget,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )

            Spacer(modifier = Modifier.height(30.dp))

//            ButtonProgressbar {
//                remainingBudget = (Random.nextDouble() * (maxBudget + 1)).toFloat()
//            }

//            Spacer(modifier = Modifier.height(20.dp))

            StartToTargetDate(
                startDate = startDate,
                endDate = targetDate,
                onClickStartDate = {
                    startDate = it
                    if (!startDate.isBefore(targetDate)) {
                        targetDate = startDate.plusDays(1)
                    }
                },
                onClickTargetDate = {
                    targetDate = it
                    if (!startDate.isBefore(targetDate)) {
                        startDate = targetDate.minusDays(1)
                    }
                }
            )

            val exampleMetrics = arrayListOf(
                Metric(stringResource(id = R.string.days_since_start), daysSinceStart, MetricUnit.DAYS),
                Metric(stringResource(id = R.string.days_remaining), daysRemaining, MetricUnit.DAYS),
                Metric(stringResource(id = R.string.daily_budget), dailyBudget, MetricUnit.CURRENCY_PER_DAY),
                Metric(stringResource(id = R.string.budget_until_target_date), currentBudget, MetricUnit.CURRENCY),
                Metric(stringResource(id = R.string.remaining_budget), remainingBudget, MetricUnit.CURRENCY),
            )
            var expanded by rememberSaveable {
                mutableStateOf(true)
            }

            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp),
//                    .animateContentSize(
//                        animationSpec = tween(
//                            durationMillis = 300,
//                            easing = FastOutSlowInEasing
////                            dampingRatio = Spring.DampingRatioMediumBouncy,
////                            stiffness = Spring.StiffnessLow
//                        )
//                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clickable(
                        onClick = { expanded = !expanded }
                    )
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 24.dp),
                        text = if (expanded) stringResource(id = R.string.show_less) else stringResource(id = R.string.show_more),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start
                    )

                    Divider(thickness = 1.dp)

//                    IconButton(
//                        modifier = Modifier.padding(top = 30.dp),
//                        onClick = { expanded = !expanded }
//                    ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        modifier = Modifier
                            .size(size = 64.dp) //TODO: Check if this is the right way to set the size (2023-06-14)
                            .padding(top = 30.dp),
//                            tint = colorResource(id = R.color.color_accent), //TODO: Use Material UI Colors (2023-06-14), //TODO: Use Material UI Colors (2023-06-14)
                        contentDescription = if (expanded) {
                            stringResource(R.string.show_less)
                        } else {
                            stringResource(R.string.show_more)
                        }
                    )
//                    }
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.heightIn(max = 250.dp)) {
                        items(items = exampleMetrics) { metric ->
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                MetricItem(metric = metric, modifier = Modifier.padding(6.dp))
                            }

                        }
                    }
                }
            }

        }

    }
}

@Preview
@Composable
fun DefaultPreview() {
    BudgetCalcTheme {
        MyApp()
    }
}