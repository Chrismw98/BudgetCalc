package chrismw.budgetcalc

data class Metric(var name: String, var value: Double, val unit: MetricUnit) {

}

enum class MetricUnit() {
    DAYS,
    CURRENCY_PER_DAY,
    CURRENCY,
}
