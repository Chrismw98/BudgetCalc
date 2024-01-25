package chrismw.budgetcalc.helpers

import androidx.annotation.StringRes
import chrismw.budgetcalc.R

data class Metric(val type: MetricType, val value: Number, val unit: MetricUnit)

enum class MetricType(@StringRes val textRes: Int) {
    DAYS_SINCE_START(R.string.metric_text_days_since_start),
    DAYS_REMAINING(R.string.metric_text_days_remaining),
    DAILY_BUDGET(R.string.metric_text_daily_budget),
    BUDGET_UNTIL_TARGET_DATE(R.string.metric_text_budget_until_target_date),
    REMAINING_BUDGET(R.string.metric_text_remaining_budget),
    TOTAL_BUDGET(R.string.metric_text_total_budget),
}

enum class MetricUnit {
    DAYS,
    CURRENCY_PER_DAY,
    CURRENCY,
}