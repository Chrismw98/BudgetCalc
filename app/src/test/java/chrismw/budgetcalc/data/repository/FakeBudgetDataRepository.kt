package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository
import chrismw.budgetcalc.data.DateWithTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Provider

class FakeBudgetDataRepository(
    private val nowDateProvider: Provider<LocalDate>,
    private val nowDateTimeProvider: Provider<LocalDateTime>,
) : BudgetDataRepository {

    private val budgetDataStateFlow = MutableStateFlow(BudgetData())
    private val targetDateStateFlow = MutableStateFlow(
        DateWithTimestamp(
            date = nowDateProvider.get(),
            createdAt = nowDateTimeProvider.get()
        )
    )

    override val targetDateFlow: Flow<DateWithTimestamp> = targetDateStateFlow.asStateFlow()

    override val budgetDataFlow: Flow<BudgetData> = budgetDataStateFlow.asStateFlow()

    override fun setTargetDate(date: LocalDate) {
        targetDateStateFlow.value = DateWithTimestamp(
            date = date,
            createdAt = nowDateTimeProvider.get()
        )
    }

    override fun observeBudgetDataWithNowDate(): Flow<Pair<BudgetData, DateWithTimestamp>> {
        return budgetDataStateFlow.map {
            it to DateWithTimestamp(
                date = nowDateProvider.get(),
                createdAt = nowDateTimeProvider.get()
            )
        }
    }

    override suspend fun getBudgetData(): BudgetData {
        return budgetDataStateFlow.value
    }

    override suspend fun saveBudgetData(budgetData: BudgetData) {
        budgetDataStateFlow.value = budgetData
    }
}