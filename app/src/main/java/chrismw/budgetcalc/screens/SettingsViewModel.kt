package chrismw.budgetcalc.screens

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.prefdatastore.BudgetData
import chrismw.budgetcalc.prefdatastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.annotation.concurrent.Immutable
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

    private fun setIsBudgetConstant(value: Boolean) {
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    isBudgetConstant = value
                )
            }
        }
    }

    fun setBudgetToConstant() {
        setIsBudgetConstant(true)
    }

    fun setBudgetToRate() {
        setIsBudgetConstant(false)
    }

    fun setConstantBudgetAmount(value: String) {
        val correctedValue = correctFloatString(value)
        if (correctedValue != null) {
            viewModelScope.launch {
                updateViewState {
                    it.copy(
                        constantBudgetAmount = correctedValue
                    )
                }
            }
        }
    }

    fun setBudgetRateAmount(value: String) {
        val correctedValue = correctFloatString(value)
        if (correctedValue != null) {
            viewModelScope.launch {
                updateViewState {
                    it.copy(
                        budgetRateAmount = value
                    )
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
        }
    }

    fun setDefaultPaymentDay(value: String) {
        if (!value.startsWith("0") && value.isDigitsOnly()) {
            val defaultPaymentDay = value.toIntOrNull()
            if (defaultPaymentDay == null || defaultPaymentDay in 1..31) {
                viewModelScope.launch {
                    updateViewState {
                        it.copy(
                            defaultPaymentDayOfMonth = value
                        )
                    }
                }
            }
        }
    }

    fun setBudgetType(value: BudgetType) {
        viewModelScope.launch {
            updateViewState {
                it.copy(
                    budgetType = value
                )
            }
        }
    }
}

private fun correctFloatString(floatString: String): String? {
    val separator = '.'
    val split = floatString.split(separator)
    return if (floatString.count { it == separator } > 1) {
        null
    } else if (floatString == "$separator") {
        "0$separator"
    } else if (floatString.contains(separator) && floatString.split(separator)[1].length > 2) {
        null
    } else if (split.any { !it.isDigitsOnly() }) {
        null
    } else {
        floatString
    }
}

@Immutable
data class SettingsState(
    val isBudgetConstant: Boolean = false,
    val constantBudgetAmount: String? = null,
    val budgetRateAmount: String? = null,
    val currency: String? = null,

    val budgetType: BudgetType = BudgetType.MONTHLY,
    val defaultPaymentDayOfMonth: String? = null,
    val defaultPaymentDayOfWeek: String? = null,
    val defaultStartDate: LocalDate? = null,
    val defaultEndDate: LocalDate? = null,
)