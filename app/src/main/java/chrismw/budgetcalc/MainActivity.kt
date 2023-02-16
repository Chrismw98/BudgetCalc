package chrismw.budgetcalc

import MetricAdapter
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chrismw.budgetcalc.databinding.ActivityMainBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


class MainActivity : AppCompatActivity() {

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
    private var defaultLengthOfPaymentCycleInDays: Int = 0

    private var budgetAmount: Int = Constants.defaultBudgetAmount
    private var lengthOfPaymentCycleInDays: Int? = null

    private var dailyBudgetMetric = Metric("", 0.0, MetricUnit.EURO_PER_DAY)
    private var currentBudgetMetric = Metric("", 0.0, MetricUnit.EURO)
    private var remainingBudgetMetric = Metric("", 0.0, MetricUnit.EURO)
    private var daysSinceStartMetric = Metric("", 0.0, MetricUnit.DAYS)
    private var daysRemainingMetric = Metric("", 0.0, MetricUnit.DAYS)

    private var startDatePicker: MaterialDatePicker<Long>? = null
    private var targetDatePicker: MaterialDatePicker<Long>? = null

    private var metricAdapter: MetricAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        etBudgetAmount = binding?.etBudgetAmount
        etPaymentCycleLength = binding?.etPaymentCycleLength
        tvStartDate = binding?.tvStartDate
        tvTargetDate = binding?.tvTargetDate

        etBudgetAmount?.hint = Constants.defaultBudgetAmount.toString()

        val btnEasyAdjust = binding?.btnEasyAdjust
        btnEasyAdjust?.setOnClickListener {
            setStartDateToLatestPaymentDate()
            adjustTargetDateIfNeeded()
            updateDefaultLengthOfPaymentCycleInDays()
            updateCalculationsInUI()
        }

        val btnJumpToToday = binding?.btnJumpToToday
        btnJumpToToday?.setOnClickListener {
            setTargetDateToToday()
            adjustStartDateIfNeeded()
            updateCalculationsInUI()
        }

        etBudgetAmount?.doOnTextChanged { text, start, before, count ->
            budgetAmount = if (text == null || text!!.isEmpty()) {
                Constants.defaultBudgetAmount
            } else {
                text.toString().toInt()
            }
            updateCalculationsInUI()
        }

        etPaymentCycleLength?.doOnTextChanged { text, start, before, count ->
            lengthOfPaymentCycleInDays = if (text == null || text!!.isEmpty()) {
                defaultLengthOfPaymentCycleInDays
            } else {
                text.toString().toInt()
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
            startDatePicker?.show(supportFragmentManager, startDatePicker!!.tag)
        }

        tvTargetDate?.setOnClickListener {
            resetTargetDatePicker()
            targetDatePicker?.show(supportFragmentManager, targetDatePicker!!.tag)
        }

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding!!.rvMetrics.layoutManager = layoutManager
        metricAdapter = MetricAdapter(getMetricsList())
        binding?.rvMetrics?.adapter = metricAdapter

    }

    private fun initializeDataAndUI() {
        initializeMetricStrings()
        initializeLatestPaymentDate()
        setStartDateToLatestPaymentDate()
        updateDefaultLengthOfPaymentCycleInDays()
        initializeLengthOfPaymentCycleInDays()
        updateTargetDateInUI()
        updateCalculationsInUI()
    }

    private fun initializeLengthOfPaymentCycleInDays() {
        if (lengthOfPaymentCycleInDays == null){
            lengthOfPaymentCycleInDays = defaultLengthOfPaymentCycleInDays
        }
    }

    private fun updateDefaultLengthOfPaymentCycleInDays() {
        defaultLengthOfPaymentCycleInDays =
            ChronoUnit.DAYS.between(startDate, startDate?.plusMonths(1)).toInt()
        etPaymentCycleLength?.hint = defaultLengthOfPaymentCycleInDays.toString()
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
        currentBudgetMetric.name = resources.getString(R.string.budget_until_today)
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
            updateDefaultLengthOfPaymentCycleInDays()
            updateStartDateInUI()
            updateCalculationsInUI()
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
        dailyBudgetMetric.value = budgetAmount.toDouble() / lengthOfPaymentCycleInDays!!.toDouble()

        daysSinceStartMetric.value = ChronoUnit.DAYS.between(startDate, targetDate).toDouble()
        Log.d("TEST","Length of payment cycle: $lengthOfPaymentCycleInDays")
        Log.d("TEST","Days since start: ${daysSinceStartMetric.value}")
        daysRemainingMetric.value = lengthOfPaymentCycleInDays!! - daysSinceStartMetric.value
        Log.d("TEST","Days remaining: ${daysRemainingMetric.value}")

        currentBudgetMetric.value =
            if (daysSinceStartMetric.value <= lengthOfPaymentCycleInDays!!) daysSinceStartMetric.value * dailyBudgetMetric.value else budgetAmount.toDouble()
        remainingBudgetMetric.value =
            if (daysSinceStartMetric.value <= lengthOfPaymentCycleInDays!!) budgetAmount - currentBudgetMetric.value else 0.0

        metricAdapter?.notifyDataSetChanged()

//        binding?.tvDaysValue?.text = "$daysSinceStartMetric Days"
//        binding?.tvDailyBudgetValue?.text = "${String.format("%.2f", dailyBudgetMetric)} €/Day"
//        binding?.tvTotalUntilTargetValue?.text = "${String.format("%.2f", currentBudgetMetric)} €"
//        binding?.tvBudgetRemainingValue?.text = "${String.format("%.2f", remainingBudgetMetric)} €"
//        binding?.tvDaysRemainingValue?.text = "$daysRemainingMetric Days"
    }

    private fun initializeLatestPaymentDate() {
        val dayOfMonth = today.dayOfMonth
        if (dayOfMonth < Constants.defaultPaymentDayOfMonth) {
            latestPaymentDate = today.minusMonths(1)
            latestPaymentDate =
                latestPaymentDate!!.withDayOfMonth(Constants.defaultPaymentDayOfMonth)
        } else if (dayOfMonth > Constants.defaultPaymentDayOfMonth) {
            latestPaymentDate = today.withDayOfMonth(Constants.defaultPaymentDayOfMonth)
        } else {
            latestPaymentDate = today.minusMonths(1)
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
        startDatePicker?.onDestroy()
        targetDatePicker?.onDestroy()

        binding = null
    }
}