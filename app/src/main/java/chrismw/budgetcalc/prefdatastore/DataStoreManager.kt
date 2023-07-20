package chrismw.budgetcalc.prefdatastore

import android.content.ComponentCallbacks
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import chrismw.budgetcalc.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

const val SETTINGS_DATASTORE = "settings_datastore"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_DATASTORE)

//val _currentSettings: MutableStateFlow<BudgetData> = MutableStateFlow(BudgetData())

class DataStoreManager(val context: Context) {

//    val budgetDataFlow: Flow<BudgetData?>
//        get() {
//            return context.dataStore.data.map { preferences ->
//                BudgetData(
//                    isBudgetConstant = preferences[IS_BUDGET_CONSTANT] ?: false,
//
//                    constantBudgetAmount = preferences[CONSTANT_BUDGET_AMOUNT] ?: 0f,
//
//                    budgetRateAmount = preferences[BUDGET_RATE_AMOUNT]?.toFloat() ?: 0f,
//                    budgetRateUnit = preferences[BUDGET_RATE_UNIT]
//                        ?.let { CustomTemporalUnit.valueOf(it) } ?: CustomTemporalUnit.DAYS,
//
//                    defaultPaymentDay = preferences[DEFAULT_PAYMENT_DAY]?.toInt() ?: 0,
//                    currency = preferences[CURRENCY] ?: "$",
//                    paymentCycleLength = preferences[PAYMENT_CYCLE_LENGTH]?.toInt() ?: 0,
//                    paymentCycleLengthUnit = preferences[PAYMENT_CYCLE_LENGTH_UNIT]
//                        ?.let { CustomTemporalUnit.valueOf(it) } ?: CustomTemporalUnit.DAYS,
//                )
//            }
//        }

    companion object {

        val IS_BUDGET_CONSTANT = booleanPreferencesKey("IS_BUDGET_CONSTANT)")

        val CONSTANT_BUDGET_AMOUNT = floatPreferencesKey("CONSTANT_BUDGET_AMOUNT")

        val BUDGET_RATE_AMOUNT = floatPreferencesKey("BUDGET_RATE_AMOUNT")
        val BUDGET_RATE_UNIT = stringPreferencesKey("BUDGET_RATE_UNIT")

        val DEFAULT_PAYMENT_DAY = intPreferencesKey("DEFAULT_PAYMENT_DAY")
        val CURRENCY = stringPreferencesKey("CURRENCY")
        val PAYMENT_CYCLE_LENGTH = intPreferencesKey("PAYMENT_CYCLE_LENGTH")
        val PAYMENT_CYCLE_LENGTH_UNIT = stringPreferencesKey("PAYMENT_CYCLE_LENGTH_UNIT")
    }

    suspend fun saveToDataStore(budgetData: BudgetData) {
        context.dataStore.edit { preferences ->
            preferences[IS_BUDGET_CONSTANT] = budgetData.isBudgetConstant

            preferences[CONSTANT_BUDGET_AMOUNT] = budgetData.constantBudgetAmount

            preferences[BUDGET_RATE_AMOUNT] = budgetData.budgetRateAmount
            preferences[BUDGET_RATE_UNIT] = budgetData.budgetRateUnit.name

            preferences[DEFAULT_PAYMENT_DAY] = budgetData.defaultPaymentDay
            preferences[CURRENCY] = budgetData.currency
            preferences[PAYMENT_CYCLE_LENGTH] = budgetData.paymentCycleLength
            preferences[PAYMENT_CYCLE_LENGTH_UNIT] = budgetData.paymentCycleLengthUnit.name
        }
    }

    fun getFromDataStore() = context.dataStore.data.map { preferences ->
        BudgetData(
            isBudgetConstant = preferences[IS_BUDGET_CONSTANT] ?: false,

            constantBudgetAmount = preferences[CONSTANT_BUDGET_AMOUNT] ?: 0f,

            budgetRateAmount = preferences[BUDGET_RATE_AMOUNT]?.toFloat() ?: 0f,
            budgetRateUnit = preferences[BUDGET_RATE_UNIT]
                ?.let { CustomTemporalUnit.valueOf(it) } ?: CustomTemporalUnit.DAYS,

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

//    suspend fun updateBudgetData(callback: (BudgetData) -> BudgetData) {
//        val oldValue = .value
//        val updatedValue = callback(
//            oldValue
//        )
//        _currentSettings.emit(updatedValue)
//    }
}

data class BudgetData(
    val isBudgetConstant: Boolean = false,

    val constantBudgetAmount: Float = 0f,

    val budgetRateAmount: Float = 0f,
    val budgetRateUnit: CustomTemporalUnit = CustomTemporalUnit.DAYS,

    val defaultPaymentDay: Int = 0,
    val currency: String = "",
    val paymentCycleLength: Int = 0,
    val paymentCycleLengthUnit: CustomTemporalUnit = CustomTemporalUnit.DAYS,
) {

    fun toSettingsState(): SettingsState {
        return SettingsState(
            isBudgetConstant = isBudgetConstant,
            constantBudgetAmount = constantBudgetAmount,
            budgetRateAmount = budgetRateAmount,
            budgetRateUnit = budgetRateUnit.name,
            defaultPaymentDay = defaultPaymentDay,
            currency = currency,
            paymentCycleLength = paymentCycleLength,
            paymentCycleLengthUnit = paymentCycleLengthUnit.name,
        )
    }

    companion object {

        fun fromSettingsState(settingsState: SettingsState): BudgetData {

            return BudgetData(
                isBudgetConstant = settingsState.isBudgetConstant,
                constantBudgetAmount = settingsState.constantBudgetAmount ?: 0f,
                budgetRateAmount = settingsState.budgetRateAmount ?: 0f,
                budgetRateUnit = CustomTemporalUnit.valueOf(settingsState.budgetRateUnit ?: "DAYS"),
                defaultPaymentDay = settingsState.defaultPaymentDay ?: 0,
                currency = settingsState.currency ?: "",
                paymentCycleLength = settingsState.paymentCycleLength ?: 0,
                paymentCycleLengthUnit = CustomTemporalUnit.valueOf(settingsState.paymentCycleLengthUnit ?: "DAYS"),
            )
        }
    }
}

enum class CustomTemporalUnit {
    DAYS,
    WEEKS,
    MONTHS;
}