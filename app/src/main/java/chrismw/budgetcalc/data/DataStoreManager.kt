package chrismw.budgetcalc.data

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
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.getBudgetTypeFromName
import kotlinx.coroutines.flow.Flow
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
        val CURRENCY = stringPreferencesKey("CURRENCY")

        val BUDGET_TYPE = stringPreferencesKey("BUDGET_TYPE")
        val DEFAULT_PAYMENT_DAY_OF_MONTH = intPreferencesKey("DEFAULT_PAYMENT_DAY_OF_MONTH")
        val DEFAULT_PAYMENT_DAY_OF_WEEK = intPreferencesKey("DEFAULT_PAYMENT_DAY_OF_WEEK")
        val DEFAULT_START_DATE = stringPreferencesKey("DEFAULT_START_DATE")
        val DEFAULT_END_DATE = stringPreferencesKey("DEFAULT_END_DATE")
    }

    suspend fun saveToDataStore(budgetData: BudgetData) {
        context.dataStore.edit { preferences ->
            preferences[IS_BUDGET_CONSTANT] = budgetData.isBudgetConstant
            updateOrRemoveNullableFloat(
                preferences,
                CONSTANT_BUDGET_AMOUNT,
                budgetData.constantBudgetAmount
            )
            updateOrRemoveNullableFloat(
                preferences,
                BUDGET_RATE_AMOUNT,
                budgetData.budgetRateAmount
            )
            updateOrRemoveNullableString(preferences, CURRENCY, budgetData.currency)

            preferences[BUDGET_TYPE] = budgetData.budgetType.name
            updateOrRemoveNullableInt(
                preferences,
                DEFAULT_PAYMENT_DAY_OF_MONTH,
                budgetData.defaultPaymentDayOfMonth
            )
            updateOrRemoveNullableInt(
                preferences,
                DEFAULT_PAYMENT_DAY_OF_WEEK,
                budgetData.defaultPaymentDayOfWeek
            )
            updateOrRemoveNullableString(
                preferences,
                DEFAULT_START_DATE,
                budgetData.defaultStartDate
            )
            updateOrRemoveNullableString(preferences, DEFAULT_END_DATE, budgetData.defaultEndDate)
        }
    }

    private fun updateOrRemoveNullableFloat(
        preferences: MutablePreferences,
        key: Preferences.Key<Float>,
        value: Float?
    ) {
        if (value != null) {
            preferences[key] = value
        } else {
            preferences.remove(key)
        }
    }

    private fun updateOrRemoveNullableInt(
        preferences: MutablePreferences,
        key: Preferences.Key<Int>,
        value: Int?
    ) {
        if (value != null) {
            preferences[key] = value
        } else {
            preferences.remove(key)
        }
    }

    private fun updateOrRemoveNullableString(
        preferences: MutablePreferences,
        key: Preferences.Key<String>,
        value: String?
    ) {
        if (value != null && value.isNotBlank()) {
            preferences[key] = value
        } else {
            preferences.remove(key)
        }
    }

    fun getFromDataStore(): Flow<BudgetData> = context.dataStore.data.map { preferences ->
        BudgetData(
            isBudgetConstant = preferences[IS_BUDGET_CONSTANT] ?: false,
            constantBudgetAmount = preferences[CONSTANT_BUDGET_AMOUNT],
            budgetRateAmount = preferences[BUDGET_RATE_AMOUNT],
            currency = preferences[CURRENCY] ?: "", //TODO: Can the ?: "" be removed? 2023-11-12

            budgetType = preferences[BUDGET_TYPE]?.let { getBudgetTypeFromName(it) }
                ?: BudgetType.Monthly,
            defaultPaymentDayOfMonth = preferences[DEFAULT_PAYMENT_DAY_OF_MONTH],
            defaultPaymentDayOfWeek = preferences[DEFAULT_PAYMENT_DAY_OF_WEEK],
            defaultStartDate = preferences[DEFAULT_START_DATE],
            defaultEndDate = preferences[DEFAULT_END_DATE],
        )
    }

    suspend fun getBudgetData(): BudgetData {
        return getFromDataStore().first()
    }

    suspend fun clearDataStore() = context.dataStore.edit {
        it.clear()
    }
}