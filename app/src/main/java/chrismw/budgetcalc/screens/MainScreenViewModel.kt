package chrismw.budgetcalc.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.helpers.BudgetType
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
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import kotlin.math.abs

private const val WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS = 7

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    dataStoreManager: DataStoreManager
) : ViewModel() {

    private val isExpandedStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val targetDateStateFlow: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())

    private val budgetDataFlow: Flow<BudgetData> = dataStoreManager.getFromDataStore()

    val viewState: StateFlow<ViewState> = combine(
        budgetDataFlow,
        isExpandedStateFlow,
        targetDateStateFlow,
    ) { budgetData, isExpanded, targetDate ->

        if (budgetData.needsMoreData()) {
            ViewState(
                isLoading = false,
                hasIncompleteData = true
            )
        } else {

            val startDate = when (budgetData.budgetType) {
                BudgetType.ONCE_ONLY -> budgetData.defaultStartDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
                BudgetType.WEEKLY -> {
                    getLatestWeeklyPaymentDate(DayOfWeek.of(budgetData.defaultPaymentDayOfWeek ?: 1))
                }

                BudgetType.MONTHLY -> {
                    budgetData.defaultPaymentDayOfMonth?.let {
                        getLatestMonthlyPaymentDate(it)
                    } ?: LocalDate.now().minusDays(1)
                }
            }

            if (startDate.isAfter(targetDate)) {
                targetDateStateFlow.value = LocalDate.now()
            }

            val targetDatePlusOne = targetDate.plusDays(1)

            //TODO: Handle IllegalStateException
            val isConstantBudget = budgetData.isBudgetConstant

            val paymentCycleLengthInDays: Int = when (budgetData.budgetType) {
                BudgetType.ONCE_ONLY -> {
                    val endDate = budgetData.defaultEndDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
                    ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()
                }

                BudgetType.WEEKLY -> {
                    WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS
                }

                BudgetType.MONTHLY -> {
                    val endDate = budgetData.defaultPaymentDayOfMonth?.let {
                        getNextMonthlyPaymentDate(it)
                    } ?: LocalDate.now().minusDays(1)
                    ChronoUnit.DAYS.between(startDate, endDate).toInt()
                }
            }

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
                0F
            }

            val remainingBudgetPercentage = if (maxBudget == 0F) {
                1F
            } else {
                remainingBudget / maxBudget
            }

            val metrics = persistentListOf(
                Metric(MetricType.DAYS_SINCE_START, daysSinceStart, MetricUnit.DAYS),
                Metric(MetricType.DAYS_REMAINING, daysRemaining, MetricUnit.DAYS),
                Metric(MetricType.DAILY_BUDGET, dailyBudget, MetricUnit.CURRENCY_PER_DAY),
                Metric(MetricType.BUDGET_UNTIL_TARGET_DATE, currentBudget, MetricUnit.CURRENCY),
                Metric(MetricType.REMAINING_BUDGET, remainingBudget, MetricUnit.CURRENCY),
                Metric(MetricType.TOTAL_BUDGET, maxBudget, MetricUnit.CURRENCY),
            )

            ViewState(
                isLoading = false,
                hasIncompleteData = false,

                startDate = startDate,
                targetDate = targetDate,
                targetDateInEpochMillis = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                maxBudget = maxBudget,

                remainingBudget = remainingBudget,
                remainingBudgetPercentage = remainingBudgetPercentage,

                currency = checkNotNull(budgetData.currency),
                isExpanded = isExpanded,
                metrics = metrics,
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState()
        )

    fun toggleDetailsExpanded() {
        isExpandedStateFlow.update { isExpanded ->
            !isExpanded
        }
    }

    fun onPickTargetDate(newDate: LocalDate) {
        targetDateStateFlow.value = newDate
    }

    @Immutable
    data class ViewState(
        val isLoading: Boolean = true,
        val hasIncompleteData: Boolean = true,

        val startDate: LocalDate = LocalDate.now().minusDays(1),
        val targetDate: LocalDate = LocalDate.now(),
        val targetDateInEpochMillis: Long = 0,
        val maxBudget: Float = 0F,

        val remainingBudget: Float? = null,
        val remainingBudgetPercentage: Float = 0F,

        val currency: String = "",
        val metrics: ImmutableList<Metric> = persistentListOf(),
        val isExpanded: Boolean = true,

        )
}

private fun getLatestMonthlyPaymentDate(paymentDayOfMonth: Int): LocalDate {
    val today = LocalDate.now() //TODO: Maybe this should be a parameter?
    val paymentDayOfCurrentMonth = today.withDayOfMonth(paymentDayOfMonth)
    return if (paymentDayOfCurrentMonth.isAfter(today)) {
        paymentDayOfCurrentMonth.minusMonths(1)
    } else {
        paymentDayOfCurrentMonth
    }
}

private fun getNextMonthlyPaymentDate(paymentDayOfMonth: Int): LocalDate {
    val today = LocalDate.now()
    val paymentDayOfCurrentMonth = today.withDayOfMonth(paymentDayOfMonth)
    return if (paymentDayOfCurrentMonth.isAfter(today)) {
        paymentDayOfCurrentMonth
    } else {
        paymentDayOfCurrentMonth.plusMonths(1)
    }
}

private fun getLatestWeeklyPaymentDate(paymentDayOfWeek: DayOfWeek): LocalDate {
    val today = LocalDate.now()
    val todaysDayOfWeek = today.dayOfWeek
    val differenceInDays = abs(todaysDayOfWeek.value - paymentDayOfWeek.value)
    return if (paymentDayOfWeek <= todaysDayOfWeek) {
        today.minusDays(differenceInDays.toLong())
    } else {
        today.plusDays(differenceInDays.toLong()).minusWeeks(1)
    }
}
