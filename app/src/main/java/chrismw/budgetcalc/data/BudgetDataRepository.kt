package chrismw.budgetcalc.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for [BudgetData].
 */
public interface BudgetDataRepository {

    public fun observeBudgetData(): Flow<BudgetData>

    public suspend fun getBudgetData(): BudgetData

    public suspend fun saveBudgetData(budgetData: BudgetData)
}

internal class BudgetDataRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : BudgetDataRepository {

    override fun observeBudgetData(): Flow<BudgetData> {
        return dataStoreManager.getFromDataStore()
    }

    override suspend fun getBudgetData(): BudgetData {
        return dataStoreManager.getBudgetData()
    }

    override suspend fun saveBudgetData(budgetData: BudgetData) {
        dataStoreManager.saveToDataStore(budgetData)
    }
}
