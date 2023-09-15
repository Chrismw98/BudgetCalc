package chrismw.budgetcalc.prefdatastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import chrismw.budgetcalc.SettingsState
import chrismw.budgetcalc.decimalFormat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

const val SETTINGS_DATASTORE = "settings_datastore"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_DATASTORE)

//private var decimalFormatSymbols = DecimalFormatSymbols(Locale.getDefault())
//private var decimalFormat = DecimalFormat("#,##0.00", decimalFormatSymbols)
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

            updateOrRemoveNullableFloat(preferences, CONSTANT_BUDGET_AMOUNT, budgetData.constantBudgetAmount)

            updateOrRemoveNullableFloat(preferences, BUDGET_RATE_AMOUNT, budgetData.budgetRateAmount)
            preferences[BUDGET_RATE_UNIT] = budgetData.budgetRateUnit.name

            updateOrRemoveNullableInt(preferences, DEFAULT_PAYMENT_DAY, budgetData.defaultPaymentDay)
            preferences[CURRENCY] = budgetData.currency
            updateOrRemoveNullableInt(preferences, PAYMENT_CYCLE_LENGTH, budgetData.paymentCycleLength)
            preferences[PAYMENT_CYCLE_LENGTH_UNIT] = budgetData.paymentCycleLengthUnit.name
        }
    }

    private fun updateOrRemoveNullableFloat(preferences: MutablePreferences, key: Preferences.Key<Float>, value: Float?) {
        if (value != null) {
            preferences[key] = value
        } else {
            preferences.remove(key)
        }
    }

    private fun updateOrRemoveNullableInt(preferences: MutablePreferences, key: Preferences.Key<Int>, value: Int?) {
        if (value != null) {
            preferences[key] = value
        } else {
            preferences.remove(key)
        }
    }

    fun getFromDataStore() = context.dataStore.data.map { preferences ->
        BudgetData(
            isBudgetConstant = preferences[IS_BUDGET_CONSTANT] ?: false,

            constantBudgetAmount = preferences[CONSTANT_BUDGET_AMOUNT],

            budgetRateAmount = preferences[BUDGET_RATE_AMOUNT],
            budgetRateUnit = preferences[BUDGET_RATE_UNIT]
                ?.let { CustomTemporalUnit.valueOf(it) } ?: CustomTemporalUnit.DAYS,

            defaultPaymentDay = preferences[DEFAULT_PAYMENT_DAY],
            currency = preferences[CURRENCY] ?: "",
            paymentCycleLength = preferences[PAYMENT_CYCLE_LENGTH],
            paymentCycleLengthUnit = preferences[PAYMENT_CYCLE_LENGTH_UNIT]
                ?.let { CustomTemporalUnit.valueOf(it) } ?: CustomTemporalUnit.DAYS,
        )
    }

    suspend fun getBudgetData(): BudgetData {
        return getFromDataStore().first()
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

    val constantBudgetAmount: Float? = null,

    val budgetRateAmount: Float? = null,
    val budgetRateUnit: CustomTemporalUnit = CustomTemporalUnit.DAYS,

    val defaultPaymentDay: Int? = null,
    val currency: String = "", //TODO: Maybe make this nullable as well?
    val paymentCycleLength: Int? = null,
    val paymentCycleLengthUnit: CustomTemporalUnit = CustomTemporalUnit.DAYS,
) {

    fun toSettingsState(): SettingsState {
        return SettingsState(
            isBudgetConstant = isBudgetConstant,
            constantBudgetAmount = constantBudgetAmount?.let { decimalFormat.format(it) } ?: "",
            budgetRateAmount = budgetRateAmount?.let { decimalFormat.format(it) } ?: "",
            budgetRateUnit = budgetRateUnit.name,
            defaultPaymentDay = defaultPaymentDay?.toString() ?: "",
            currency = currency,
            paymentCycleLength = paymentCycleLength?.toString() ?: "",
            paymentCycleLengthUnit = paymentCycleLengthUnit.name,
        )
    }

    companion object {

        fun fromSettingsState(settingsState: SettingsState): BudgetData {

            return BudgetData(
                isBudgetConstant = settingsState.isBudgetConstant,
                constantBudgetAmount = settingsState.constantBudgetAmount?.toFloatOrNull(),
                budgetRateAmount = settingsState.budgetRateAmount?.toFloatOrNull(),
                budgetRateUnit = CustomTemporalUnit.valueOf(settingsState.budgetRateUnit ?: "DAYS"),
                defaultPaymentDay = settingsState.defaultPaymentDay?.toIntOrNull(),
                currency = settingsState.currency ?: "",
                paymentCycleLength = settingsState.paymentCycleLength?.toIntOrNull(),
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