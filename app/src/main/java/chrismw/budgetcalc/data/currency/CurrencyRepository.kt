package chrismw.budgetcalc.data.currency

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for [Currency].
 */
interface CurrencyRepository {
    val currenciesFlow: Flow<List<Currency>>

    val codeToCurrencyMapFlow: Flow<Map<String, Currency>>
}

/**
 * Implementation of [CurrencyRepository].
 */
internal class CurrencyRepositoryImpl @Inject constructor(
    private val dataSource: CurrencyLocalDataSource,
) : CurrencyRepository {

    override val currenciesFlow: Flow<List<Currency>>
        get() = dataSource.getJsonCurrenciesFlow().map { jsonCurrencies ->
            jsonCurrencies.map { it.toCurrency() }
        }

    override val codeToCurrencyMapFlow: Flow<Map<String, Currency>> = currenciesFlow.map { currencies ->
        currencies.associateBy { it.code }
    }
}
