package chrismw.budgetcalc.data.budget

import chrismw.budgetcalc.data.DateWithTimestamp
import chrismw.budgetcalc.di.DateNow
import chrismw.budgetcalc.di.DateTimeNow
import chrismw.budgetcalc.helpers.BudgetDataDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Provider

/**
 * Repository for [BudgetData].
 */
public interface BudgetDataRepository {

    public val targetDateFlow: Flow<DateWithTimestamp>

    public val budgetDataFlow: Flow<BudgetDataDTO>

    public fun setTargetDate(date: LocalDate)

    public fun observeBudgetDataWithNowDate(): Flow<Pair<BudgetDataDTO, DateWithTimestamp>>

    public suspend fun getBudgetData(): BudgetDataDTO

    public suspend fun saveBudgetData(budgetData: BudgetDataDTO)
}

internal class BudgetDataRepositoryImpl @Inject constructor(
    private val budgetLocalDataSource: BudgetLocalDataSource,
    @DateNow private val nowDateProvider: Provider<LocalDate>,
    @DateTimeNow private val nowDateTimeProvider: Provider<LocalDateTime>,
) : BudgetDataRepository {

    private var targetDateStateFlow: MutableStateFlow<DateWithTimestamp> =
        MutableStateFlow(
            DateWithTimestamp(
                date = nowDateProvider.get(),
                createdAt = nowDateTimeProvider.get()
            )
        )

    override val targetDateFlow: Flow<DateWithTimestamp> = targetDateStateFlow.asStateFlow()

    override val budgetDataFlow: Flow<BudgetDataDTO> = budgetLocalDataSource.budgetDataFlow.map {
        it.toBudgetDataDTO()
    }

    override fun setTargetDate(date: LocalDate) {
        targetDateStateFlow.value = DateWithTimestamp(
            date = date,
            createdAt = nowDateTimeProvider.get()
        )
    }

    override fun observeBudgetDataWithNowDate(): Flow<Pair<BudgetDataDTO, DateWithTimestamp>> =
        budgetDataFlow.map {
            it to DateWithTimestamp(
                date = nowDateProvider.get(),
                createdAt = nowDateTimeProvider.get()
            )
        }

    override suspend fun getBudgetData(): BudgetDataDTO {
        return budgetLocalDataSource.getBudgetData().toBudgetDataDTO()
    }

    override suspend fun saveBudgetData(budgetData: BudgetDataDTO) {
        budgetLocalDataSource.setBudgetData(budgetData.toBudgetDataPreferences())
    }
}