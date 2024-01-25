package chrismw.budgetcalc.screens

import androidx.compose.runtime.Immutable
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.DropDown
import chrismw.budgetcalc.prefdatastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    companion object {

        val DAY_OF_WEEK_LIST = DayOfWeek.values().toList().toImmutableList()
        val BUDGET_TYPES_LIST = BudgetType.values().toList().toImmutableList()
    }

    private val budgetDataDTOStateFlow: MutableStateFlow<BudgetDataDTO> = MutableStateFlow(BudgetDataDTO())
    private val initialBudgetDataDTOStateFlow: MutableStateFlow<BudgetDataDTO> = MutableStateFlow(BudgetDataDTO())
    private val currentlyExpandedDropDownStateFlow: MutableStateFlow<DropDown> = MutableStateFlow(DropDown.NONE)

    private val hasBudgetDataDTOChangedFlow: Flow<Boolean> = combine(
        budgetDataDTOStateFlow,
        initialBudgetDataDTOStateFlow
    ) { currentBudgetDataDTO, initialBudgetDataDTO ->
        currentBudgetDataDTO != initialBudgetDataDTO
    }

    val viewState: StateFlow<SettingsState> = combine(
        budgetDataDTOStateFlow,
        hasBudgetDataDTOChangedFlow,
        currentlyExpandedDropDownStateFlow
    ) { budgetDataDTO,
        hasDataChanged,
        currentlyExpandedDropDown ->

        SettingsState(
            isBudgetConstant = budgetDataDTO.isBudgetConstant,

            constantBudgetAmount = budgetDataDTO.constantBudgetAmount,
            budgetRateAmount = budgetDataDTO.budgetRateAmount,
            currency = budgetDataDTO.currency,

            budgetType = budgetDataDTO.budgetType,
            defaultPaymentDayOfMonth = budgetDataDTO.defaultPaymentDayOfMonth,
            defaultPaymentDayOfWeek = budgetDataDTO.defaultPaymentDayOfWeek,
            startDate = budgetDataDTO.startDate,
            endDate = budgetDataDTO.endDate,

            budgetTypeOptions = BUDGET_TYPES_LIST,
            dayOfWeekOptions = DAY_OF_WEEK_LIST,
            currentlyExpandedDropDown = currentlyExpandedDropDown,
            showConfirmExitDialog = hasDataChanged
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState()
        )

    fun loadSettings() {
        viewModelScope.launch {
            val loadedBudgetDataDTO = dataStoreManager.getBudgetData().toBudgetDataDTO()
            budgetDataDTOStateFlow.value = loadedBudgetDataDTO
            initialBudgetDataDTOStateFlow.value = loadedBudgetDataDTO
        }
    }

    private fun updateBudgetDataDTO(callback: (BudgetDataDTO) -> BudgetDataDTO) {
        budgetDataDTOStateFlow.update { callback(it) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val currentBudgetDataDTO = budgetDataDTOStateFlow.value
            initialBudgetDataDTOStateFlow.value = currentBudgetDataDTO
            dataStoreManager.saveToDataStore(
                currentBudgetDataDTO.toBudgetData()
            )
        }
    }

    fun setIsBudgetConstant(value: Boolean) {
        updateBudgetDataDTO {
            it.copy(
                isBudgetConstant = value
            )
        }
    }

    fun setConstantBudgetAmount(value: String) {
        val correctedValue = correctFloatString(value)
        if (correctedValue != null) {
            updateBudgetDataDTO {
                it.copy(
                    constantBudgetAmount = correctedValue
                )
            }
        }
    }

    fun setBudgetRateAmount(value: String) {
        val correctedValue = correctFloatString(value)
        if (correctedValue != null) {
            updateBudgetDataDTO {
                it.copy(
                    budgetRateAmount = value
                )
            }
        }
    }

    fun setCurrency(value: String) {
        updateBudgetDataDTO {
            it.copy(
                currency = value
            )
        }
    }

    fun setDefaultPaymentDayOfMonth(value: String) {
        if (!value.startsWith("0") && value.isDigitsOnly()) {
            val defaultPaymentDay = value.toIntOrNull()
            if (defaultPaymentDay == null || defaultPaymentDay in 1..31) {
                updateBudgetDataDTO {
                    it.copy(
                        defaultPaymentDayOfMonth = value
                    )
                }
            }
        }
    }

    fun setDefaultPaymentDayOfWeek(value: DayOfWeek) {
        updateBudgetDataDTO {
            it.copy(
                defaultPaymentDayOfWeek = value
            )
        }
    }

    fun setBudgetType(value: BudgetType) {
        updateBudgetDataDTO {
            it.copy(
                budgetType = value
            )
        }
    }

    fun setStartDate(value: LocalDate) {
        updateBudgetDataDTO {
            it.copy(
                startDate = value
            )
        }
    }

    fun setEndDate(value: LocalDate) {
        updateBudgetDataDTO {
            it.copy(
                endDate = value
            )
        }
    }

    fun updateExpandedDropDown(value: DropDown) {
        currentlyExpandedDropDownStateFlow.value = value
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
    val defaultPaymentDayOfWeek: DayOfWeek? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    val budgetTypeOptions: ImmutableList<BudgetType> = persistentListOf(),
    val dayOfWeekOptions: ImmutableList<DayOfWeek> = persistentListOf(),
    val currentlyExpandedDropDown: DropDown = DropDown.NONE,

    val showConfirmExitDialog: Boolean = false,
)