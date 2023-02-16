package chrismw.budgetcalc

import MetricAdapter
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


class MainActivity : AppCompatActivity() {

    var formatter = DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy", Locale.getDefault());

    var binding: ActivityMainBinding? = null

    var etBudgetAmount: EditText? = null
    var etDivideBy: EditText? = null

    var tvStartDate: TextView? = null
    var tvTargetDate: TextView? = null
    var startDate: LocalDate? = null
    val today = LocalDate.now()

    var targetDate = today
    var latestPaymentDate: LocalDate? = null

    var budgetAmount: Int = Constants.defaultBudgetAmount
    var totalDays: Int = Constants.defaultNumberOfDays

    var dailyBudgetMetric = Metric("Daily budget", 0.0, MetricUnit.EURO_PER_DAY)
    var currentBudgetMetric = Metric("Budget until today", 0.0, MetricUnit.EURO)
    var remainingBudgetMetric = Metric("Remaining budget", 0.0, MetricUnit.EURO)
    var daysSinceStartMetric = Metric("Days since start", 0.0, MetricUnit.DAYS)
    var daysRemainingMetric = Metric("Days remaining", 0.0, MetricUnit.DAYS)

    var startDatePicker: MaterialDatePicker<Long>? = null
    var targetDatePicker: MaterialDatePicker<Long>? = null

    var metricAdapter: MetricAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        etBudgetAmount = binding?.etBudgetAmount
        etDivideBy = binding?.etDivideBy
        tvStartDate = binding?.tvStartDate
        tvTargetDate = binding?.tvTargetDate

        etBudgetAmount?.hint = Constants.defaultBudgetAmount.toString()
        etDivideBy?.hint = Constants.defaultNumberOfDays.toString()

        val btnEasyAdjust = binding?.btnEasyAdjust
        btnEasyAdjust?.setOnClickListener {
            setStartDateToLatestPaymentDate()
            adjustTargetDateIfNeeded()
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

        etDivideBy?.doOnTextChanged { text, start, before, count ->
            totalDays = if (text == null || text!!.isEmpty()) {
                Constants.defaultNumberOfDays
            } else {
                text.toString().toInt()
            }
            updateCalculationsInUI()
        }

        binding?.ibHint?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Hint")
                .setItems(arrayOf(Constants.hintText)) { dialog, which ->

                }
                .show()
        }

        setLatestPaymentDate()

        setStartDateToLatestPaymentDate()
        updateTargetDateInUI()
        updateCalculationsInUI()

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

    private fun getMetricsList(): ArrayList<Metric> {
        return arrayListOf(
            daysSinceStartMetric,
            daysRemainingMetric,
            dailyBudgetMetric,
            currentBudgetMetric,
            remainingBudgetMetric,
        )
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
        dailyBudgetMetric.value = budgetAmount.toDouble() / totalDays.toDouble()

        daysSinceStartMetric.value = ChronoUnit.DAYS.between(startDate, targetDate).toDouble()
        daysRemainingMetric.value = totalDays - daysSinceStartMetric.value

        currentBudgetMetric.value =
            if (daysSinceStartMetric.value <= totalDays) daysSinceStartMetric.value * dailyBudgetMetric.value else budgetAmount.toDouble()
        remainingBudgetMetric.value =
            if (daysSinceStartMetric.value <= totalDays) budgetAmount - currentBudgetMetric.value else 0.0

        metricAdapter?.notifyDataSetChanged()

//        binding?.tvDaysValue?.text = "$daysSinceStartMetric Days"
//        binding?.tvDailyBudgetValue?.text = "${String.format("%.2f", dailyBudgetMetric)} €/Day"
//        binding?.tvTotalUntilTargetValue?.text = "${String.format("%.2f", currentBudgetMetric)} €"
//        binding?.tvBudgetRemainingValue?.text = "${String.format("%.2f", remainingBudgetMetric)} €"
//        binding?.tvDaysRemainingValue?.text = "$daysRemainingMetric Days"
    }

    private fun setLatestPaymentDate() {
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

        val latestPaymentDayOfWeek = latestPaymentDate!!.dayOfWeek
        if (latestPaymentDayOfWeek == DayOfWeek.SATURDAY) {
            latestPaymentDate = latestPaymentDate!!.minusDays(1)
        } else if (latestPaymentDayOfWeek == DayOfWeek.SUNDAY) {
            latestPaymentDate = latestPaymentDate!!.minusDays(2)
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