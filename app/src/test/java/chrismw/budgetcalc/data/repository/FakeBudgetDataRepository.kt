package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository

class FakeBudgetDataRepository : BudgetDataRepository{

    private var _budgetData: BudgetData = BudgetData()

    override suspend fun getBudgetData(): BudgetData {
        return _budgetData
    }

    override suspend fun saveBudgetData(budgetData: BudgetData) {
        _budgetData = budgetData
    }
}