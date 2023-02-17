package chrismw.budgetcalc

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import chrismw.budgetcalc.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private var binding: ActivitySettingsBinding? = null

    private var etPaymentDayOfMonth: EditText? = null
    private var etBudgetInCurrencyPerDay: EditText? = null
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
        etBudgetInCurrencyPerDay = binding?.etBudgetInCurrencyPerDay
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

        etBudgetInCurrencyPerDay?.doOnTextChanged { text, _, _, _ ->
            if (text == null || text.toString().isEmpty()) {
                deleteValueFromSettings(Constants.LATEST_BUDGET_AMOUNT_IN_CURRENCY_PER_DAY)
            } else {
                updateIntValueInSettings(
                    Constants.LATEST_BUDGET_AMOUNT_IN_CURRENCY_PER_DAY,
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
        etPaymentDayOfMonth?.filters = arrayOf(InputFilterMinMax(1,31)) //Because a month can only have 1 to 31 days at most (2023-02-16)
    }

    private fun initializeFieldValuesIfKnown() {
        if (settings!!.contains(Constants.LATEST_PAYMENT_DAY_OF_MONTH)) {
            setPaymentDayOfMonthToLastKnown()
        }
        if (settings!!.contains(Constants.LATEST_BUDGET_AMOUNT_IN_CURRENCY_PER_DAY)) {
            setBudgetInCurrencyPerDayToLastKnown()
        }
        if (settings!!.contains(Constants.LATEST_CURRENCY)) {
            setCurrencyToLastKnown()
        }
    }

    private fun initializeHintTexts() {
        etPaymentDayOfMonth?.hint = Constants.defaultPaymentDayOfMonth.toString()
        etBudgetInCurrencyPerDay?.hint = Constants.defaultBudgetAmountInCurrencyPerDay.toString()
        etCurrency?.hint = Constants.defaultCurrency
    }

    private fun setPaymentDayOfMonthToLastKnown() {
        val latestPaymentDayOfMonth = settings!!.getInt(Constants.LATEST_PAYMENT_DAY_OF_MONTH, -1)
        etPaymentDayOfMonth?.setText(
            latestPaymentDayOfMonth.toString(),
            TextView.BufferType.EDITABLE
        )
    }

    private fun setBudgetInCurrencyPerDayToLastKnown() {
        val latestBudgetInCurrencyPerDay =
            settings!!.getInt(Constants.LATEST_BUDGET_AMOUNT_IN_CURRENCY_PER_DAY, -1)
        etBudgetInCurrencyPerDay?.setText(
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

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}