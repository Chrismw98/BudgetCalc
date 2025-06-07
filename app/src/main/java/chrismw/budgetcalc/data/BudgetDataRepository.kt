package chrismw.budgetcalc.data

import chrismw.budgetcalc.di.DateNow
import chrismw.budgetcalc.di.DateTimeNow
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

    public val budgetDataFlow: Flow<BudgetData>

    public fun setTargetDate(date: LocalDate)

    public fun observeBudgetDataWithNowDate(): Flow<Pair<BudgetData, DateWithTimestamp>>

    public suspend fun getBudgetData(): BudgetData

    public suspend fun saveBudgetData(budgetData: BudgetData)
}

internal class BudgetDataRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager,
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

    override val budgetDataFlow: Flow<BudgetData> = dataStoreManager.getFromDataStore()

    override fun setTargetDate(date: LocalDate) {
        targetDateStateFlow.value = DateWithTimestamp(
                date = date,
                createdAt = nowDateTimeProvider.get()
            )
    }

    override fun observeBudgetDataWithNowDate(): Flow<Pair<BudgetData, DateWithTimestamp>> =
        budgetDataFlow.map {
            it to DateWithTimestamp(
                date = nowDateProvider.get(),
                createdAt = nowDateTimeProvider.get()
            )
        }

    override suspend fun getBudgetData(): BudgetData {
        return dataStoreManager.getBudgetData()
    }

    override suspend fun saveBudgetData(budgetData: BudgetData) {
        dataStoreManager.saveToDataStore(budgetData)
    }
}