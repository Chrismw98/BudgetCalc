package chrismw.budgetcalc.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.data.Budget
import chrismw.budgetcalc.data.BudgetDataRepository
import chrismw.budgetcalc.di.DateNow
import chrismw.budgetcalc.extensions.toBudget
import chrismw.budgetcalc.helpers.BudgetState
import chrismw.budgetcalc.helpers.Metric
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val budgetDataRepository: BudgetDataRepository,
    @DateNow private val nowDateProvider: Provider<LocalDate>,
) : ViewModel() {

    private val isExpandedStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val todayStateFlow: MutableStateFlow<LocalDate> =
        MutableStateFlow(nowDateProvider.get())

    private val budgetWithTargetDateFlow: Flow<Pair<Budget, LocalDate>?> =
        combine(
            todayStateFlow,
            budgetDataRepository.targetDateFlow,
            budgetDataRepository.observeBudgetDataWithNowDate(),
        ) { today, customTargetDate, (budget, defaultTargetDate) ->
            try {
                val budget = budget.toBudget(
                    today = today,
                )
                val targetDate =
                    if (customTargetDate.createdAt.isAfter(defaultTargetDate.createdAt)) {
                        customTargetDate.date
                    } else {
                        defaultTargetDate.date
                    }

                budget to targetDate
            } catch (e: IllegalStateException) {
                null
            }
        }.shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            1
        )

    val viewState: StateFlow<ViewState> = combine(
        budgetWithTargetDateFlow,
        isExpandedStateFlow,
        todayStateFlow,
    ) { budgetWithTargetDate, isExpanded, today ->
        val budget = budgetWithTargetDate?.first
        val targetDate = budgetWithTargetDate?.second ?: today

        val metrics = budget?.extractMetrics(targetDate)?.toImmutableList()

        if (budget == null || metrics == null) {
            ViewState(
                isLoading = false,
                hasIncompleteData = true
            )
        } else {
            val remainingBudget =
                checkNotNull(metrics.find { it is Metric.RemainingBudget }?.value).toFloat()
            val maxBudget = checkNotNull(metrics.find { it is Metric.TotalBudget }?.value).toFloat()
            val remainingBudgetPercentage = if (maxBudget == 0F) {
                1F
            } else {
                remainingBudget / maxBudget
            }

            ViewState(
                isLoading = false,
                hasIncompleteData = false,
                today = today,

                budgetState = budget.extractBudgetState(targetDate),

                datePickerMinDate = budget.startDate,
                datePickerMaxDate = budget.endDate,
                targetDate = targetDate,

                remainingBudget = remainingBudget,
                remainingBudgetPercentage = remainingBudgetPercentage,

                currency = checkNotNull(budget.currency),
                isExpanded = isExpanded,
                metrics = metrics,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ViewState()
    )

    fun updateCurrentDate() {
        todayStateFlow.value = nowDateProvider.get()
    }

    fun toggleDetailsExpanded() {
        isExpandedStateFlow.update { isExpanded ->
            !isExpanded
        }
    }

    fun onResetTargetDate() {
        budgetDataRepository.setTargetDate(nowDateProvider.get())
    }

    fun onPickTargetDate(targetDate: LocalDate) {
        budgetDataRepository.setTargetDate(targetDate)
    }

    @Immutable
    data class ViewState(
        val isLoading: Boolean = true,
        val hasIncompleteData: Boolean = true,
        val today: LocalDate = LocalDate.now(),

        val budgetState: BudgetState = BudgetState.HasNotStarted,

        val targetDate: LocalDate = LocalDate.now(),
        val datePickerMinDate: LocalDate = LocalDate.now().minusDays(1),
        val datePickerMaxDate: LocalDate = LocalDate.now(),

        val remainingBudget: Float? = null,
        val remainingBudgetPercentage: Float = 0F,

        val currency: String = "",
        val metrics: ImmutableList<Metric> = persistentListOf(),
        val isExpanded: Boolean = true,
    ) {
        val showJumpToTodayButton: Boolean = !hasIncompleteData && today != targetDate
    }
}