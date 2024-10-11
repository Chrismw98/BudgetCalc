package chrismw.budgetcalc.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chrismw.budgetcalc.data.Budget
import chrismw.budgetcalc.data.BudgetDataRepository
import chrismw.budgetcalc.di.DateNow
import chrismw.budgetcalc.extensions.extractMetrics
import chrismw.budgetcalc.extensions.toBudget
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.helpers.Metric
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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    budgetDataRepository: BudgetDataRepository,
    @DateNow private val nowDateProvider: Provider<LocalDate>,
) : ViewModel() {

    private val isExpandedStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val todayStateFlow: MutableStateFlow<LocalDate> = MutableStateFlow(nowDateProvider.get())
    private val targetDateStateFlow: MutableStateFlow<LocalDate> = MutableStateFlow(nowDateProvider.get())

    private val todaySharedFlow: SharedFlow<LocalDate> = todayStateFlow.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        1
    )

    private val budgetSharedFlow: SharedFlow<Budget?> =
        combine(
            todaySharedFlow,
            budgetDataRepository.observeBudgetData(),
        ) { today, budget ->
            try {
                budget.toBudget(
                    today = today,
                )
            } catch (e: IllegalStateException) {
                null
            }
        }
            .shareIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                1
            )

    private val targetDateSharedFlow: Flow<LocalDate> = combine(
        budgetSharedFlow,
        todaySharedFlow,
        targetDateStateFlow
    ) { budget, today, chosenTargetDate ->
        if (budget?.startDate?.isAfter(chosenTargetDate) == true) {
            today
        } else {
            chosenTargetDate
        }
    }.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        1
    )

    private val metricsFlow: Flow<ImmutableList<Metric>?> = combine(
        budgetSharedFlow,
        targetDateSharedFlow
    ) { budget, targetDate ->
        budget?.extractMetrics(targetDate)?.toImmutableList()
    }

    val viewState: StateFlow<ViewState> = combine(
        budgetSharedFlow,
        isExpandedStateFlow,
        targetDateSharedFlow,
        metricsFlow,
    ) { budget, isExpanded, targetDate, metrics ->
        if (budget == null || metrics == null) {
            ViewState(
                isLoading = false,
                hasIncompleteData = true
            )
        } else {
            val startDate = budget.startDate
            val remainingBudget = checkNotNull(metrics.find { it is Metric.RemainingBudget }?.value).toFloat()
            val maxBudget = checkNotNull(metrics.find { it is Metric.TotalBudget }?.value).toFloat()
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

                currency = checkNotNull(budget.currency),
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

    fun updateCurrentDate() {
        todayStateFlow.value = nowDateProvider.get()
    }

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