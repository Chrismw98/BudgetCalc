package chrismw.budgetcalc.data.budget

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BudgetLocalDataSource @Inject constructor(
    private val dataStore: DataStore<BudgetDataPreferences>
) {

    val budgetDataFlow: Flow<BudgetDataPreferences> = dataStore.data

    suspend fun getBudgetData(): BudgetDataPreferences = dataStore.data.first()

    suspend fun setBudgetData(budgetData: BudgetDataPreferences) {
        dataStore.updateData {
            budgetData
        }
    }
}