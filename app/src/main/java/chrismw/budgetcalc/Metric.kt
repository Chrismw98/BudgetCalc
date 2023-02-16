package chrismw.budgetcalc

data class Metric(var name: String, var value: Double, val unit: MetricUnit) {

}

enum class MetricUnit() {
    DAYS,
    EURO_PER_DAY,
    EURO,
}
