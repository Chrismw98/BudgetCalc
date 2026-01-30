package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.data.currency.Currency
import chrismw.budgetcalc.data.currency.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeCurrencyRepository() : CurrencyRepository {

    private companion object {
        val EURO = Currency(
            code = "EUR",
            name = "Euro",
            symbol = "€",
        )

        val USD = Currency(
            code = "USD",
            name = "US Dollar",
            symbol = "$",
        )

        val JPY = Currency(
            code = "JPY",
            name = "Japanese Yen",
            symbol = "¥",
        )
    }

    override val currenciesFlow: Flow<List<Currency>>
        get() = flowOf(
            listOf(
                EURO,
                USD,
                JPY,
            )
        )

    override val codeToCurrencyMapFlow: Flow<Map<String, Currency>>
        get() = currenciesFlow.map { currencies ->
            currencies.associateBy { it.code }
        }
}