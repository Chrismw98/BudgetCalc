package chrismw.budgetcalc.data

import chrismw.budgetcalc.di.DateNow
import chrismw.budgetcalc.di.DateTimeNow
import chrismw.budgetcalc.helpers.MetricUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Provider

///**
// * Repository for [BudgetData].
// */
//public interface CurrencyRepository {
//    fun getCurrency(): Currency
//}
//
//internal class CurrencyRepositoryImpl @Inject constructor(
//    private val dataStoreManager: DataStoreManager,
//) : CurrencyRepository {
//    override fun getCurrency(): Currency {
//        dataStoreManager.
//    }
//
//}