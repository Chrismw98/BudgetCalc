package chrismw.budgetcalc.data.currency

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test class for [CurrencyRepositoryImpl].
 */
class CurrencyRepositoryImplTest {

    private val eurJson = JsonCurrency(code = "EUR", name = "Euro", symbol = "€")
    private val usdJson = JsonCurrency(code = "USD", name = "US Dollar", symbol = "$")

    private fun repository(vararg jsonCurrencies: JsonCurrency): CurrencyRepository {
        val dataSource = mockk<CurrencyLocalDataSource> {
            every { getJsonCurrenciesFlow() } returns flowOf(jsonCurrencies.toList())
        }
        return CurrencyRepositoryImpl(dataSource)
    }

    @Test
    fun `currenciesFlow maps every JsonCurrency to a Currency`() = runTest {
        val currencies = repository(eurJson, usdJson).currenciesFlow.first()

        assertThat(currencies).containsExactly(
            eurJson.toCurrency(),
            usdJson.toCurrency(),
        ).inOrder()
    }

    @Test
    fun `codeToCurrencyMapFlow associates each currency by its code`() = runTest {
        val map = repository(eurJson, usdJson).codeToCurrencyMapFlow.first()

        assertThat(map).containsExactly(
            "EUR", eurJson.toCurrency(),
            "USD", usdJson.toCurrency(),
        )
    }
}
