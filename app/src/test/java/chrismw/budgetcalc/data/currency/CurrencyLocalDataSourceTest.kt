package chrismw.budgetcalc.data.currency

import android.content.Context
import android.content.res.AssetManager
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Test class for [CurrencyLocalDataSource].
 */
class CurrencyLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private val moshi = Moshi.Builder().build()

    private val validJson = """
        [
            {"code":"EUR","name":"Euro","symbol":"€"},
            {"code":"USD","name":"US Dollar","symbol":"${'$'}"}
        ]
    """.trimIndent()

    @Before
    fun setUp() {
        assetManager = mockk()
        context = mockk {
            every { assets } returns assetManager
        }
    }

    private fun dataSource(assetsFileName: String = "currencies.json") = CurrencyLocalDataSource(
        context = context,
        ioDispatcher = Dispatchers.Unconfined,
        moshi = moshi,
        config = CurrencyJsonConfig(assetsFileName = assetsFileName),
    )

    @Test
    fun `getJsonCurrencies parses the currencies from the assets file`() = runTest {
        every { assetManager.open("currencies.json") } returns ByteArrayInputStream(validJson.toByteArray())

        val result = dataSource().getJsonCurrencies()

        assertThat(result).containsExactly(
            JsonCurrency(code = "EUR", name = "Euro", symbol = "€"),
            JsonCurrency(code = "USD", name = "US Dollar", symbol = "$"),
        ).inOrder()
    }

    @Test
    fun `getJsonCurrencies caches the result across calls`() = runTest {
        every { assetManager.open("currencies.json") } returns ByteArrayInputStream(validJson.toByteArray())
        val dataSource = dataSource()

        dataSource.getJsonCurrencies()
        dataSource.getJsonCurrencies()

        verify(exactly = 1) { assetManager.open("currencies.json") }
    }

    @Test
    fun `invalidateCache forces the next call to re-read the assets file`() = runTest {
        every { assetManager.open("currencies.json") } answers { ByteArrayInputStream(validJson.toByteArray()) }
        val dataSource = dataSource()

        dataSource.getJsonCurrencies()
        dataSource.invalidateCache()
        dataSource.getJsonCurrencies()

        verify(exactly = 2) { assetManager.open("currencies.json") }
    }

    @Test
    fun `getJsonCurrencies returns an empty list when the JSON parses to null`() = runTest {
        every { assetManager.open("currencies.json") } returns ByteArrayInputStream("null".toByteArray())

        val result = dataSource().getJsonCurrencies()

        assertThat(result).isEmpty()
    }

    @Test
    fun `getJsonCurrencies throws when the assets file name is blank`() = runTest {
        val exception =
            runCatching { dataSource(assetsFileName = "  ").getJsonCurrencies() }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `getJsonCurrenciesFlow emits the parsed currencies`() = runTest {
        every { assetManager.open("currencies.json") } returns ByteArrayInputStream(validJson.toByteArray())

        val result = dataSource().getJsonCurrenciesFlow().first()

        assertThat(result).containsExactly(
            JsonCurrency(code = "EUR", name = "Euro", symbol = "€"),
            JsonCurrency(code = "USD", name = "US Dollar", symbol = "$"),
        ).inOrder()
    }
}
