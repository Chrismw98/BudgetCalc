package chrismw.budgetcalc.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository
import chrismw.budgetcalc.di.DateNow
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricType
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfMonth
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfWeek
import chrismw.budgetcalc.helpers.findNextOccurrenceOfDayOfMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Provider

private const val WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS = 7

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    budgetDataRepository: BudgetDataRepository,
    @DateNow private val nowDateProvider: Provider<LocalDate>,
) : ViewModel() {

    private val isExpandedStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val targetDateStateFlow: MutableStateFlow<LocalDate> = MutableStateFlow(nowDateProvider.get())

    private val budgetDataSharedFlow: SharedFlow<BudgetData> = budgetDataRepository.observeBudgetData() //TODO: Refactor - budget should be sealed class respecting each Budget Type -> Nullability can be eradicated that way
        .shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            1
        )

    private val startDateSharedFlow: SharedFlow<LocalDate> = budgetDataSharedFlow.map { budgetData ->
        val today = nowDateProvider.get()

        when (budgetData.budgetType) {
            is BudgetType.OnceOnly -> budgetData.defaultStartDate?.let { LocalDate.parse(it) } ?: nowDateProvider.get()
            is BudgetType.Weekly -> {
                findLatestOccurrenceOfDayOfWeek(
                    today = today,
                    targetDayOfWeek = DayOfWeek.of(budgetData.defaultPaymentDayOfWeek ?: 1)
                )
            }

            is BudgetType.Monthly -> {
                budgetData.defaultPaymentDayOfMonth?.let {
                    findLatestOccurrenceOfDayOfMonth(
                        today = today,
                        targetDayOfMonth = it
                    )
                } ?: nowDateProvider.get().minusDays(1)
            }
        }
    }.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        1
    )

    private val targetDateSharedFlow: Flow<LocalDate> = combine(
        startDateSharedFlow,
        targetDateStateFlow
    ) { startDate, chosenTargetDate ->
        if (startDate.isAfter(chosenTargetDate)) {
            nowDateProvider.get()
        } else {
            chosenTargetDate
        }
    }.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        1
    )

    private val metricsFlow: Flow<ImmutableList<Metric>?> = combine(
        budgetDataSharedFlow,
        startDateSharedFlow,
        targetDateSharedFlow
    ) { budgetData, startDate, targetDate ->
        val targetDatePlusOne = targetDate.plusDays(1)
        //TODO: Handle IllegalStateException (?)
        val isConstantBudget = budgetData.isBudgetConstant
        val today = nowDateProvider.get()

        val paymentCycleLengthInDays: Int = when (budgetData.budgetType) {
            is BudgetType.OnceOnly -> {
                val endDate = budgetData.defaultEndDate?.let { LocalDate.parse(it) } ?: today
                ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()
            }

            is BudgetType.Weekly -> {
                WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS
            }

            is BudgetType.Monthly -> {
                val endDate = budgetData.defaultPaymentDayOfMonth?.let {
                    findNextOccurrenceOfDayOfMonth(
                        today = today,
                        targetDayOfMonth = it
                    )
                } ?: today.minusDays(1)
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
        val metrics = persistentListOf(
            Metric(MetricType.DaysSinceStart, daysSinceStart, MetricUnit.DAYS),
            Metric(MetricType.DaysRemaining, daysRemaining, MetricUnit.DAYS),
            Metric(MetricType.DailyBudget, dailyBudget, MetricUnit.CURRENCY_PER_DAY),
            Metric(MetricType.BudgetUntilTargetDate, currentBudget, MetricUnit.CURRENCY),
            Metric(MetricType.RemainingBudget, remainingBudget, MetricUnit.CURRENCY),
            Metric(MetricType.TotalBudget, maxBudget, MetricUnit.CURRENCY),
        )
        metrics.toImmutableList()
    }

    val viewState: StateFlow<ViewState> = combine(
        budgetDataSharedFlow,
        startDateSharedFlow,
        isExpandedStateFlow,
        targetDateSharedFlow,
        metricsFlow,
    ) { budgetData, startDate, isExpanded, targetDate, metrics ->
        if (budgetData.needsMoreData() || metrics.isNullOrEmpty()) {
            ViewState(
                isLoading = false,
                hasIncompleteData = true
            )
        } else {

            val remainingBudget = checkNotNull(metrics.find { it.type is MetricType.RemainingBudget }?.value).toFloat()
            val maxBudget = checkNotNull(metrics.find { it.type is MetricType.TotalBudget }?.value).toFloat()
            val remainingBudgetPercentage = if (maxBudget == 0F) {
                1F
            } else {
                remainingBudget / maxBudget
            }

            ViewState(
                isLoading = false,
                hasIncompleteData = false,

                startDate = startDate,
                targetDate = targetDate,
                targetDateInEpochMillis = targetDate.toEpochMillis(),

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

        val remainingBudget: Float? = null,
        val remainingBudgetPercentage: Float = 0F,

        val currency: String = "",
        val metrics: ImmutableList<Metric> = persistentListOf(),
        val isExpanded: Boolean = true,
    )
}