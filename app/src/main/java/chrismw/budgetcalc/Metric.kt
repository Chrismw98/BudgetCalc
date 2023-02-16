package chrismw.budgetcalc

data class Metric(val name: String, var value: Double, val unit: MetricUnit) {

}

enum class MetricUnit(val unitString: String) {
    DAYS("Days"),
    EURO_PER_DAY("€/Day"),
    EURO("€"),
}
