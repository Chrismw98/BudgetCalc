package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBudgetDataRepository : BudgetDataRepository {

    private var _budgetData: BudgetData = BudgetData()

    override fun observeBudgetData(): Flow<BudgetData> {
        return flowOf(_budgetData)
    }

    override suspend fun getBudgetData(): BudgetData {
        return _budgetData
    }

    override suspend fun saveBudgetData(budgetData: BudgetData) {
        _budgetData = budgetData
    }
}