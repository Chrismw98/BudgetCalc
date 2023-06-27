package chrismw.budgetcalc

data class Metric(var name: String, var value: Number, val unit: MetricUnit) {

}

enum class MetricUnit() {
    DAYS,
    CURRENCY_PER_DAY,
    CURRENCY,
}
