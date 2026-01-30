package chrismw.budgetcalc.data.currency

import android.content.Context
import chrismw.budgetcalc.di.IODispatcher
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class CurrencyLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineContext,
    private val moshi: Moshi,
    private val config: CurrencyJsonConfig,
) {

    @Volatile
    private var cached: List<JsonCurrency>? = null

    /**
     * Parses JSON from the respective .json currencies file.
     */
    suspend fun getJsonCurrencies(): List<JsonCurrency> = withContext(ioDispatcher) {
        cached ?: run {
            require(config.assetsFileName.isNotBlank()) {
                "assetsFileName must be set for ASSETS source"
            }

            val jsonText = context.assets.open(config.assetsFileName)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            val listType = Types.newParameterizedType(List::class.java, JsonCurrency::class.java)
            val adapter = moshi.adapter<List<JsonCurrency>>(listType)

            val parsed = adapter.fromJson(jsonText) ?: emptyList()
            cached = parsed
            parsed
        }
    }

    fun getJsonCurrenciesFlow(): Flow<List<JsonCurrency>> = flow {
        emit(getJsonCurrencies())
    }

    fun invalidateCache() {
        cached = null
    }
}
