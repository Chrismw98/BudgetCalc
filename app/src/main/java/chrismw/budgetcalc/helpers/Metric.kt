package chrismw.budgetcalc.helpers

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import chrismw.budgetcalc.R
import java.text.NumberFormat

sealed class Metric(
    open val value: Number,
    val unit: MetricUnit,
    @StringRes val textResId: Int,
) {

    fun getValueString(): String = if (unit is MetricUnit.Days) {
        value.toString()
    } else {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        numberFormat.format(value)
    }

    @Composable
    @ReadOnlyComposable
    fun getColor(): Color = if (unit is MetricUnit.Days && value.toDouble() < 0) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    data class DaysSinceStart(
        override val value: Int
    ) : Metric(
        value = value,
        unit = MetricUnit.Days,
        textResId = R.string.metric_text_days_since_start
    )

    data class DaysRemaining(
        override val value: Int
    ) : Metric(
        value = value,
        unit = MetricUnit.Days,
        textResId = R.string.metric_text_days_remaining
    )

    data class DailyBudget(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.CurrencyPerDay,
        textResId = R.string.metric_text_daily_budget
    )

    data class BudgetUntilTargetDate(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.Currency,
        textResId = R.string.metric_text_budget_until_target_date
    )

    data class RemainingBudget(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.Currency,
        textResId = R.string.metric_text_remaining_budget
    )

    data class TotalBudget(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.Currency,
        textResId = R.string.metric_text_total_budget
    )
}

sealed class MetricUnit {

    object Days : MetricUnit() {

        @ReadOnlyComposable
        @Composable
        fun getString(daysCount: Int) = pluralStringResource(id = R.plurals.days, daysCount)
    }

    object CurrencyPerDay : MetricUnit() {

        @ReadOnlyComposable
        @Composable
        fun getString(currency: String) = "${currency}/${stringResource(id = R.string.day)}"
    }

    object Currency : MetricUnit()
}