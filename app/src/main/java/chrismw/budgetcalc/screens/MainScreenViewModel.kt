package chrismw.budgetcalc.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.extensions.dateString
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricType
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.prefdatastore.BudgetData
import chrismw.budgetcalc.prefdatastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    dataStoreManager: DataStoreManager
) : ViewModel() {

    private val budgetDataFlow: Flow<BudgetData> = dataStoreManager.getFromDataStore()
    private val isExpanded: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val targetDate: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())

    val viewState: StateFlow<UIState> = combine(
        budgetDataFlow,
        isExpanded,
        targetDate,
    ) { budgetData, isExpanded, targetDate ->
        val startDate = budgetData.defaultPaymentDay?.let {
            getLatestPaymentDate(it)
        } ?: LocalDate.now().minusDays(1)
        val targetDatePlusOne = targetDate.plusDays(1)

        //TODO: Handle IllegalStateException
        val isConstantBudget = budgetData.isBudgetConstant
        val paymentCycleLengthInDays = checkNotNull(budgetData.paymentCycleLength)
        val maxBudget = if (isConstantBudget) {
            checkNotNull(budgetData.constantBudgetAmount)
        } else {
            checkNotNull(budgetData.budgetRateAmount) * paymentCycleLengthInDays
        }
        val dailyBudget = if (isConstantBudget) {
            maxBudget / paymentCycleLengthInDays
        } else {
            checkNotNull(budgetData.budgetRateAmount)
        }

        val daysSinceStart = ChronoUnit.DAYS.between(startDate, targetDatePlusOne).toInt()
        val daysRemaining = paymentCycleLengthInDays - daysSinceStart

        val currentBudget = if (daysSinceStart <= paymentCycleLengthInDays) {
            daysSinceStart * dailyBudget
        } else {
            maxBudget
        }
        val remainingBudget = if (daysSinceStart <= paymentCycleLengthInDays) {
            maxBudget - currentBudget
        } else {
            0f
        }

        val metrics = persistentListOf(
            Metric(MetricType.DAYS_SINCE_START, daysSinceStart, MetricUnit.DAYS),
            Metric(MetricType.DAYS_REMAINING, daysRemaining, MetricUnit.DAYS),
            Metric(MetricType.DAILY_BUDGET, dailyBudget, MetricUnit.CURRENCY_PER_DAY),
            Metric(MetricType.BUDGET_UNTIL_TARGET_DATE, currentBudget, MetricUnit.CURRENCY),
            Metric(MetricType.REMAINING_BUDGET, remainingBudget, MetricUnit.CURRENCY),
        )

        UIState(
            startDate = startDate,
            targetDate = targetDate,
            targetDateInEpochMillis = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            maxBudget = maxBudget,

            remainingBudget = remainingBudget,

            currency = budgetData.currency,
            isExpanded = isExpanded,
            metrics = metrics,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UIState()
        )

    fun toggleDetailsExpanded() {
        viewModelScope.launch {
            isExpanded.emit(!isExpanded.value)
        }
    }

    fun onPickTargetDate(newDate: LocalDate) {
        viewModelScope.launch {
            targetDate.emit(newDate)
        }
    }

    @Immutable
    data class UIState(
        val startDate: LocalDate = LocalDate.now().minusDays(1),
        val targetDate: LocalDate = LocalDate.now(),
        val targetDateInEpochMillis: Long = 0,
        val maxBudget: Float = -1f,

//        val lengthOfPaymentCycleInDays: Int? = null,
//        val dailyBudget: Float? = null,
//        val daysSinceStart: Int? = null,
//        val daysRemaining: Int? = null,
//        val currentBudget: Float? = null,
        val remainingBudget: Float? = null,

        val currency: String = "",
        val metrics: ImmutableList<Metric> = persistentListOf(),
        val isExpanded: Boolean = true,

        val hasIncompleteData: Boolean = true,
//        val lengthOfPaymentCycleInDays = startDate.lengthOfMonth()
//        val dailyBudget = maxBudget / lengthOfPaymentCycleInDays
//
//        val daysSinceStart = ChronoUnit.DAYS.between(startDate, targetDate)
//        val daysRemaining = lengthOfPaymentCycleInDays - daysSinceStart
//
//        val currentBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) daysSinceStart * dailyBudget else maxBudget
//        val remainingBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) maxBudget - currentBudget else 0f
    )
}

private fun getLatestPaymentDate(paymentDayOfMonth: Int): LocalDate {
    val paymentDayOfCurrentMonth = LocalDate.now().withDayOfMonth(paymentDayOfMonth)
    return if (paymentDayOfCurrentMonth.isAfter(LocalDate.now())) {
        paymentDayOfCurrentMonth.minusMonths(1)
    } else {
        paymentDayOfCurrentMonth
    }
}