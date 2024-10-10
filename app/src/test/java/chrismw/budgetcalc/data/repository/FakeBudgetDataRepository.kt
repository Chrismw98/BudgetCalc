package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBudgetDataRepository : BudgetDataRepository {

    private val _budgetData = MutableStateFlow(BudgetData())

    override fun observeBudgetData(): Flow<BudgetData> {
        return _budgetData
    }

    override suspend fun getBudgetData(): BudgetData {
        return _budgetData.value
    }

    override suspend fun saveBudgetData(budgetData: BudgetData) {
        _budgetData.value = budgetData
    }
}