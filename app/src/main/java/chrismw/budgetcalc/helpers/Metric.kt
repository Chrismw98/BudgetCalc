package chrismw.budgetcalc.helpers

data class Metric(val type: MetricType, val value: Number, val unit: MetricUnit)

enum class MetricType {
    DAYS_SINCE_START,
    DAYS_REMAINING,
    DAILY_BUDGET,
    BUDGET_UNTIL_TARGET_DATE,
    REMAINING_BUDGET,
}

enum class MetricUnit {
    DAYS,
    CURRENCY_PER_DAY,
    CURRENCY,
}