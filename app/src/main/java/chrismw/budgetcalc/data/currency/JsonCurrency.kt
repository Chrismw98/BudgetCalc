package chrismw.budgetcalc.data.currency

import com.squareup.moshi.JsonClass


/**
 * Data class representing a currency in JSON format.
 */
@JsonClass(generateAdapter = true)
data class JsonCurrency(
    val code: String,
    val name: String,
    val symbol: String,
) {
    fun toCurrency() = Currency(
        code = code,
        name = name,
        symbol = symbol,
    )
}
