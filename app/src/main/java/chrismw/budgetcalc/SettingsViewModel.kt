package chrismw.budgetcalc

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import chrismw.budgetcalc.prefdatastore.BudgetData
import chrismw.budgetcalc.prefdatastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _viewState: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())

    fun loadSettings() {
        viewModelScope.launch {
            _viewState.tryEmit(
                dataStoreManager.getBudgetData().toSettingsState()
            )
        }
    }

    private suspend fun updateViewState(callback: (SettingsState) -> SettingsState) {
        val oldValue = _viewState.value
        val updatedValue = callback(oldValue)
        _viewState.emit(updatedValue)
    }

    fun saveSettings() {
        viewModelScope.launch {
            dataStoreManager.saveToDataStore(
                BudgetData.fromSettingsState(
                    viewState.value
                )
            )
        }
    }

    //    val viewState: StateFlow<SettingsState> = dataStoreManager
//        .getFromDataStore()
//        .map { budgetData ->
//            budgetData.toSettingsState()
//        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = SettingsState()
//        )
    val viewState: StateFlow<SettingsState> = _viewState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState()
        )

    fun setIsBudgetConstant(value: Boolean) {
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    isBudgetConstant = value
                )
            }
//            dataStoreManager.saveToDataStore(
//                BudgetData.fromSettingsState(viewState.value
//                    .copy(
//                        isBudgetConstant = value
//                    )
//                )
//            )
        }
    }

    fun setBudgetToConstant(){
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    isBudgetConstant = true
                )
            }
        }
    }

    fun setBudgetToRate(){
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    isBudgetConstant = false
                )
            }
        }
    }

    fun setConstantBudgetAmount(value: String) {
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    constantBudgetAmount = value
                )
            }
//            dataStoreManager.saveToDataStore(
//                BudgetData.fromSettingsState(viewState.value
//                    .copy(
//                        constantBudgetAmount = value
//                    )
//                )
//            )
        }
    }

    fun setBudgetRateAmount(value: String) {
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    budgetRateAmount = value
                )
            }
//            dataStoreManager.saveToDataStore(
//                BudgetData.fromSettingsState(viewState.value
//                    .copy(
//                        budgetRateAmount = value
//                    )
//                )
//            )
        }
    }

    fun setBudgetRateUnit(value: String) { //TODO: This needs to be looked over again 2023-07-20
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    budgetRateUnit = value
                )
            }
//            dataStoreManager.saveToDataStore(
//                BudgetData.fromSettingsState(viewState.value
//                    .copy(
//                        budgetRateUnit = value
//                    )
//                )
//            )
        }
    }

    fun setDefaultPaymentDay(value: String) {
        if (!value.startsWith("0") && value.isDigitsOnly()) {
            val defaultPaymentDay = value.toIntOrNull()
            if (defaultPaymentDay == null || defaultPaymentDay in 1..31) {
                viewModelScope.launch {
                    updateViewState {
                        it.copy(
                            defaultPaymentDay = value
                        )
                    }
//                    dataStoreManager.saveToDataStore(
//                        BudgetData.fromSettingsState(viewState.value
//                            .copy(
//                                defaultPaymentDay = value
//                            )
//                        )
//                    )
                }
            }
        }
    }

    fun setCurrency(value: String) {
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    currency = value
                )
            }
//            dataStoreManager.saveToDataStore(
//                BudgetData.fromSettingsState(viewState.value
//                    .copy(
//                        currency = value
//                    )
//                )
//            )
        }
    }

    fun setPaymentCycleLength(value: String) {
        if (!value.startsWith("0") && value.isDigitsOnly()) {
            viewModelScope.launch {
                updateViewState {
                    it.copy(
                        paymentCycleLength = value
                    )
                }
//                dataStoreManager.saveToDataStore(
//                    BudgetData.fromSettingsState(viewState.value
//                        .copy(
//                            paymentCycleLength = value
//                        )
//                    )
//                )
            }
        }
    }

    fun setPaymentCycleUnit(value: String) { //TODO: This needs to be looked over again 2023-07-20
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    paymentCycleLengthUnit = value
                )
            }
//            dataStoreManager.saveToDataStore(
//                BudgetData.fromSettingsState(viewState.value
//                    .copy(
//                        paymentCycleLengthUnit = value
//                    )
//                )
//            )
        }
    }
}

data class SettingsState(
    val isBudgetConstant: Boolean = false,
    val constantBudgetAmount: String? = null,
    val budgetRateAmount: String? = null,
    val budgetRateUnit: String? = null, //TODO: Make this not use CustomTemporalUnit 2023-07-20
    val defaultPaymentDay: String? = null,
    val currency: String? = null,
    val paymentCycleLength: String? = null,
    val paymentCycleLengthUnit: String? = null //TODO: Make this not use CustomTemporalUnit 2023-07-20
)