package chrismw.budgetcalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.prefdatastore.BudgetData
import chrismw.budgetcalc.prefdatastore.CustomTemporalUnit
import chrismw.budgetcalc.prefdatastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val viewState: StateFlow<SettingsState> = dataStoreManager
        .getFromDataStore()
        .map { budgetData ->
            budgetData.toSettingsState()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState()
        )

    fun setIsBudgetConstant(value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        isBudgetConstant = value
                    )
                )
            )
        }
    }

    fun setConstantBudgetAmount(value: String) {
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        constantBudgetAmount = value.toFloatOrNull()
                    )
                )
            )
        }
    }

    fun setBudgetRateAmount(value: String) {
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        budgetRateAmount = value.toFloatOrNull()
                    )
                )
            )
        }
    }

    fun setBudgetRateUnit(value: String) { //TODO: This needs to be looked over again 2023-07-20
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        budgetRateUnit = value
                    )
                )
            )
        }
    }

    fun setDefaultPaymentDay(value: String) {
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        defaultPaymentDay = value.toIntOrNull()
                    )
                )
            )
        }
    }

    fun setCurrency(value: String) {
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        currency = value
                    )
                )
            )
        }
    }

    fun setPaymentCycleLength(value: String) {
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        paymentCycleLength = value.toIntOrNull()
                    )
                )
            )
        }
    }

    fun setPaymentCycleUnit(value: String) { //TODO: This needs to be looked over again 2023-07-20
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(viewState.value
                    .copy(
                        paymentCycleLengthUnit = value
                    )
                )
            )
        }
    }
}

data class SettingsState(
    val isBudgetConstant: Boolean = false,
    val constantBudgetAmount: Float? = null,
    val budgetRateAmount: Float? = null,
    val budgetRateUnit: String? = null, //TODO: Make this not use CustomTemporalUnit 2023-07-20
    val defaultPaymentDay: Int? = null,
    val currency: String? = null,
    val paymentCycleLength: Int? = null,
    val paymentCycleLengthUnit: String? = null //TODO: Make this not use CustomTemporalUnit 2023-07-20
)