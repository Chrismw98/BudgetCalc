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

//    suspend fun getLastAchievedBudgetId(): UUID? {
//        return lastAchievedBudgetIdFlow.first()
//    }
//
//    suspend fun setLastAchievedBudgetId(id: UUID) {
//        dataStore.updateData { Data ->
//            Data.toBuilder()
//                .setLastAchievedBudgetId(id.toString())
//                .build()
//        }
//    }
//
//    suspend fun setInitialized() {
//        dataStore.updateData { Data ->
//            Data.toBuilder()
//                .setIsInitialized(true)
//                .build()
//        }
//    }
//
//    suspend fun isInitialized(): Boolean {
//        return dataStore.data.first().getIsInitialized()
//    }
//
//    suspend fun clear() {
//        dataStore.updateData { Data ->
//            Data.toBuilder().clear().build()
//        }
//    }

//    private val lastAchievedBudgetIdFlow: Flow<UUID?>
//        get() = dataStore.data.map { Data ->
//            with(Data.lastAchievedBudgetId) {
//                if (this == null || isEmpty()) {
//                    null
//                } else {
//                    UUID.fromString(this)
//                }
//            }
//        }
}