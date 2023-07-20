package chrismw.budgetcalc

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import chrismw.budgetcalc.prefdatastore.BudgetData
import chrismw.budgetcalc.prefdatastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

//    private val dataStore = DataStoreManager(context)

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    fun setIsBudgetConstant(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isBudgetConstant = value
            )
        }
    }

    fun setConstantBudgetAmount(value: String) {
        _uiState.update { currentState ->
            val newConstantBudgetAmount = value.toFloatOrNull()
            currentState.copy(
                constantBudgetAmount = newConstantBudgetAmount
            )
        }
    }

    fun setBudgetRateAmount(value: String) {
        _uiState.update { currentState ->
            val newBudgetRateAmount = value.toFloatOrNull()
            currentState.copy(
                budgetRateAmount = newBudgetRateAmount
            )
        }
    }

    fun setBudgetRateUnit(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                budgetRateUnit = value
            )
        }
    }

    fun setDefaultPaymentDay(value: String) {
        val newDefaultPaymentDay = value.toIntOrNull()
        _uiState.update { currentState ->
            currentState.copy(
                defaultPaymentDay = newDefaultPaymentDay
            )
        }
    }

    fun setCurrency(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                currency = value
            )
        }
    }

    fun setPaymentCycleLength(value: String) {
        val newPaymentCycleLength = value.toIntOrNull()
        _uiState.update { currentState ->
            currentState.copy(
                paymentCycleLength = newPaymentCycleLength
            )
        }
    }

    fun setPaymentCycleUnit(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                paymentCycleUnit = value
            )
        }
    }
}

data class SettingsState(
    val isBudgetConstant: Boolean = false,
    val constantBudgetAmount: Float? = null,
    val budgetRateAmount: Float? = null,
    val budgetRateUnit: String? = null,
    val defaultPaymentDay: Int? = null,
    val currency: String? = null,
    val paymentCycleLength: Int? = null,
    val paymentCycleUnit: String? = null
)