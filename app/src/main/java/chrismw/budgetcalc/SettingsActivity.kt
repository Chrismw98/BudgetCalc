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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import chrismw.budgetcalc.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private var binding: ActivitySettingsBinding? = null

    private var etPaymentDayOfMonth: EditText? = null
    private var etBudgetInMonetaryUnitsPerDay: EditText? = null
    private var etCurrency: EditText? = null

    private var settings: SharedPreferences? = null
    private var settingsEditor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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