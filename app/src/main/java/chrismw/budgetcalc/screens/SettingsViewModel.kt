package chrismw.budgetcalc.screens

import androidx.compose.runtime.Immutable
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.data.budget.BudgetDataRepository
import chrismw.budgetcalc.data.currency.Currency
import chrismw.budgetcalc.data.currency.CurrencyRepository
import chrismw.budgetcalc.di.DateNow
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.Constants.MAX_DIGITS_FOR_CONSTANT_BUDGET_AMOUNT
import chrismw.budgetcalc.helpers.Constants.MAX_DIGITS_FOR_DAILY_BUDGET_RATE
import chrismw.budgetcalc.helpers.DropDown
import chrismw.budgetcalc.helpers.UiBudgetData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val budgetDataRepository: BudgetDataRepository,
    private val currencyRepository: CurrencyRepository,
    @DateNow private val nowDateProvider: Provider<LocalDate>
) : ViewModel() {

    companion object {

        val DAY_OF_WEEK_LIST = DayOfWeek.values().toList().toImmutableList()
        val BUDGET_TYPES_LIST = persistentListOf(BudgetType.OnceOnly, BudgetType.Weekly, BudgetType.Monthly)
    }

    private val uiBudgetDataStateFlow: MutableStateFlow<UiBudgetData> = MutableStateFlow(UiBudgetData())
    private val initialUiBudgetDataStateFlow: MutableStateFlow<UiBudgetData> = MutableStateFlow(UiBudgetData())
    private val currentlyExpandedDropDownStateFlow: MutableStateFlow<DropDown> = MutableStateFlow(DropDown.NONE)

    private val hasBudgetDataDTOChangedFlow: Flow<Boolean> = combine(
        uiBudgetDataStateFlow,
        initialUiBudgetDataStateFlow
    ) { currentBudgetDataDTO, initialBudgetDataDTO ->
        currentBudgetDataDTO != initialBudgetDataDTO
    }

    val viewState: StateFlow<ViewState> = combine(
        uiBudgetDataStateFlow,
        hasBudgetDataDTOChangedFlow,
        currentlyExpandedDropDownStateFlow,
        currencyRepository.currenciesFlow,
    ) { uiBudgetData,
        hasDataChanged,
        currentlyExpandedDropDown,
        currencies ->

        ViewState(
            isLoading = false,
            today = nowDateProvider.get(),

            isBudgetConstant = uiBudgetData.isBudgetConstant,

            selectedCurrency = uiBudgetData.currency,
            availableCurrencies = currencies.toImmutableList(),
            constantBudgetAmount = uiBudgetData.constantBudgetAmount,
            budgetRateAmount = uiBudgetData.budgetRateAmount,

            budgetType = uiBudgetData.budgetType,
            defaultPaymentDayOfMonth = uiBudgetData.defaultPaymentDayOfMonth,
            defaultPaymentDayOfWeek = uiBudgetData.defaultPaymentDayOfWeek,
            startDate = uiBudgetData.startDate,
            endDate = uiBudgetData.endDate,

            budgetTypeOptions = BUDGET_TYPES_LIST,
            dayOfWeekOptions = DAY_OF_WEEK_LIST,
            currentlyExpandedDropDown = currentlyExpandedDropDown,
            showConfirmExitDialog = hasDataChanged
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ViewState()
    )

    fun loadSettings() {
        viewModelScope.launch {
            val loadedBudgetDataDTO = budgetDataRepository.getBudgetData()
            val currencyMap = currencyRepository.codeToCurrencyMapFlow.first()
            val initialUiBudgetData = loadedBudgetDataDTO.toUiBudgetData(currencyMap)

            uiBudgetDataStateFlow.value = initialUiBudgetData
            initialUiBudgetDataStateFlow.value = initialUiBudgetData
        }
    }

    private fun updateUiBudgetData(callback: (UiBudgetData) -> UiBudgetData) {
        uiBudgetDataStateFlow.update { callback(it) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val currentUiBudgetData = uiBudgetDataStateFlow.value
            initialUiBudgetDataStateFlow.value = currentUiBudgetData
            budgetDataRepository.saveBudgetData(currentUiBudgetData.toDTO())
        }
    }

    fun setIsBudgetConstant(value: Boolean) {
        updateUiBudgetData {
            it.copy(
                isBudgetConstant = value
            )
        }
    }

    fun setConstantBudgetAmount(value: String) {
        val trimmedValue = value.trimZeros()
        if (trimmedValue.isDigitsOnly() && trimmedValue.length <= MAX_DIGITS_FOR_CONSTANT_BUDGET_AMOUNT) {
            updateUiBudgetData {
                it.copy(
                    constantBudgetAmount = trimmedValue,
                )
            }
        }
    }

    fun setBudgetRateAmount(value: String) {
        val trimmedValue = value.trimZeros()
        if (trimmedValue.isDigitsOnly() && trimmedValue.length <= MAX_DIGITS_FOR_DAILY_BUDGET_RATE) {
            updateUiBudgetData {
                it.copy(
                    budgetRateAmount = trimmedValue,
                )
            }
        }
    }

    fun setCurrency(value: Currency) {
        updateUiBudgetData {
            it.copy(
                currency = value
            )
        }
    }

    fun setDefaultPaymentDayOfMonth(value: String) {
        if (value.isDigitsOnly()) {
            val defaultPaymentDay = value.trimZeros().toIntOrNull()
            if (defaultPaymentDay == null || defaultPaymentDay in 1..31) { //TODO: Remove this in favor of error states
                updateUiBudgetData {
                    it.copy(
                        defaultPaymentDayOfMonth = value
                    )
                }
            }
        }
    }

    fun setDefaultPaymentDayOfWeek(value: DayOfWeek) {
        updateUiBudgetData {
            it.copy(
                defaultPaymentDayOfWeek = value
            )
        }
    }

    fun setBudgetType(value: BudgetType) {
        updateUiBudgetData {
            it.copy(
                budgetType = value
            )
        }
    }

    fun setStartDate(value: LocalDate) {
        updateUiBudgetData {
            it.copy(
                startDate = value
            )
        }
    }

    fun setEndDate(value: LocalDate) {
        updateUiBudgetData {
            it.copy(
                endDate = value
            )
        }
    }

    fun updateExpandedDropDown(value: DropDown) {
        currentlyExpandedDropDownStateFlow.value = value
    }

    private fun String.trimZeros(): String {
        return this.trimStart('0')
    }

    @Immutable
    data class ViewState(
        val isLoading: Boolean = true,
        val today: LocalDate = LocalDate.now(),

        val isBudgetConstant: Boolean? = null,

        val selectedCurrency: Currency? = null,
        val availableCurrencies: ImmutableList<Currency> = persistentListOf(),
        val budgetRateAmount: String? = null,
        val constantBudgetAmount: String? = null,

        val budgetType: BudgetType? = null,
        val defaultPaymentDayOfMonth: String? = null,
        val defaultPaymentDayOfWeek: DayOfWeek? = null,
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,

        val budgetTypeOptions: ImmutableList<BudgetType> = persistentListOf(),
        val dayOfWeekOptions: ImmutableList<DayOfWeek> = persistentListOf(),
        val currentlyExpandedDropDown: DropDown = DropDown.NONE,

        val showConfirmExitDialog: Boolean = false,
    )
}
