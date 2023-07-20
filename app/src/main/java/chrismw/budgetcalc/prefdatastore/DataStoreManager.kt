package chrismw.budgetcalc.prefdatastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.time.temporal.TemporalUnit
import java.time.temporal.WeekFields
import java.util.Calendar

const val SETTINGS_DATASTORE = "settings_datastore"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_DATASTORE)

class DataStoreManager(val context: Context) {

    companion object {

        val CONSTANT_BUDGET_AMOUNT = stringPreferencesKey("CONSTANT_BUDGET_AMOUNT")

        val BUDGET_RATE_AMOUNT = stringPreferencesKey("BUDGET_RATE_AMOUNT")
        val BUDGET_RATE_UNIT = stringPreferencesKey("BUDGET_RATE_UNIT")

        val DEFAULT_PAYMENT_DAY = stringPreferencesKey("DEFAULT_PAYMENT_DAY")
        val CURRENCY = stringPreferencesKey("CURRENCY")
        val PAYMENT_CYCLE_LENGTH = stringPreferencesKey("PAYMENT_CYCLE_LENGTH")
        val PAYMENT_CYCLE_LENGTH_UNIT = stringPreferencesKey("PAYMENT_CYCLE_LENGTH_UNIT")
    }

    suspend fun saveToDataStore(budgetData: BudgetData) {
        context.dataStore.edit { preferences ->
            preferences[CONSTANT_BUDGET_AMOUNT] = budgetData.constantBudgetAmount.toString()

            preferences[BUDGET_RATE_AMOUNT] = budgetData.budgetRateAmount.toString()
            preferences[BUDGET_RATE_UNIT] = budgetData.budgetRateUnit.name

            preferences[DEFAULT_PAYMENT_DAY] = budgetData.defaultPaymentDay.toString()
            preferences[CURRENCY] = budgetData.currency
            preferences[PAYMENT_CYCLE_LENGTH] = budgetData.paymentCycleLength.toString()
            preferences[PAYMENT_CYCLE_LENGTH_UNIT] = budgetData.paymentCycleLengthUnit.name
        }
    }

    fun getFromDataStore() = context.dataStore.data.map { preferences ->
        BudgetData(
            constantBudgetAmount = preferences[CONSTANT_BUDGET_AMOUNT]?.toFloat() ?: 0f,

            budgetRateAmount = preferences[BUDGET_RATE_AMOUNT]?.toFloat() ?: 0f,
            budgetRateUnit = preferences[BUDGET_RATE_UNIT]
                ?.let { CustomTemporalUnit.valueOf(it) }?: CustomTemporalUnit.DAYS,

            defaultPaymentDay = preferences[DEFAULT_PAYMENT_DAY]?.toInt() ?: 0,
            currency = preferences[CURRENCY] ?: "$",
            paymentCycleLength = preferences[PAYMENT_CYCLE_LENGTH]?.toInt() ?: 0,
            paymentCycleLengthUnit = preferences[PAYMENT_CYCLE_LENGTH_UNIT]
                ?.let { CustomTemporalUnit.valueOf(it) } ?: CustomTemporalUnit.DAYS,
        )
    }

    suspend fun clearDataStore() = context.dataStore.edit {
        it.clear()
    }
}

data class BudgetData(
    val constantBudgetAmount: Float = 0f,

    val budgetRateAmount: Float = 0f,
    val budgetRateUnit: CustomTemporalUnit = CustomTemporalUnit.DAYS,

    val defaultPaymentDay: Int = 0,
    val currency: String = "",
    val paymentCycleLength: Int = 0,
    val paymentCycleLengthUnit: CustomTemporalUnit = CustomTemporalUnit.DAYS,
)

enum class CustomTemporalUnit {
    DAYS,
    WEEKS,
    MONTHS;
}