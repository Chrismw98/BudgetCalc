package chrismw.budgetcalc.helpers

import androidx.annotation.StringRes
import chrismw.budgetcalc.R

data class Metric(val type: MetricType, val value: Number, val unit: MetricUnit)

sealed class MetricType(@StringRes val textRes: Int) {
    object DaysSinceStart : MetricType(R.string.metric_text_days_since_start)
    object DaysRemaining : MetricType(R.string.metric_text_days_remaining)
    object DailyBudget : MetricType(R.string.metric_text_daily_budget)
    object BudgetUntilTargetDate : MetricType(R.string.metric_text_budget_until_target_date)
    object RemainingBudget : MetricType(R.string.metric_text_remaining_budget)
    object TotalBudget : MetricType(R.string.metric_text_total_budget)
}

enum class MetricUnit {
    DAYS,
    CURRENCY_PER_DAY,
    CURRENCY,
}