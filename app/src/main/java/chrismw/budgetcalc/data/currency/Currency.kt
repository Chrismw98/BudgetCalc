package chrismw.budgetcalc.data.currency

/**
 * Data class representing a currency.
 */
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
) {
    fun toJsonEntity() = JsonCurrency(
        code = code,
        name = name,
        symbol = symbol,
    )

    fun toDisplayName(): String {
        return "$code - $name"
    }
}