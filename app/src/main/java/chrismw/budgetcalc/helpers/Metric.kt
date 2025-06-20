package chrismw.budgetcalc.helpers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import chrismw.budgetcalc.R
import java.text.NumberFormat

sealed class Metric(
    open val value: Number,
    val unit: MetricUnit,
    @StringRes val textResId: Int,
    @DrawableRes val iconDrawableRes: Int? = null,
    val iconImageVector: ImageVector? = null,
) {

    fun getValueString(): String = if (unit is MetricUnit.Days) {
        value.toString()
    } else {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        numberFormat.format(value)
    }

    data class DaysSinceStart(
        override val value: Int
    ) : Metric(
        value = value,
        unit = MetricUnit.Days,
        textResId = R.string.metric_text_days_since_start,
        iconImageVector = Icons.Outlined.CalendarMonth,
    )

    data class DaysRemaining(
        override val value: Int
    ) : Metric(
        value = value,
        unit = MetricUnit.Days,
        textResId = R.string.metric_text_days_remaining,
        iconDrawableRes = R.drawable.ic_event_upcoming,
    )

    data class DailyBudget(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.CurrencyPerDay,
        textResId = R.string.metric_text_daily_budget,
        iconDrawableRes = R.drawable.ic_clock,
    )

    data class BudgetUntilTargetDate(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.Currency,
        textResId = R.string.metric_text_budget_until_target_date,
        iconImageVector = Icons.Outlined.LocalMall,
    )

    data class RemainingBudget(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.Currency,
        textResId = R.string.metric_text_remaining_budget,
        iconImageVector = Icons.Outlined.Savings,
    )

    data class TotalBudget(
        override val value: Float
    ) : Metric(
        value = value,
        unit = MetricUnit.Currency,
        textResId = R.string.metric_text_total_budget,
        iconDrawableRes = R.drawable.ic_money_bag,
    )

    data class DaysUntilStart(
        override val value: Int
    ) : Metric(
        value = value,
        unit = MetricUnit.Days,
        textResId = R.string.metric_text_budget_start_in,
        iconImageVector = Icons.Outlined.SportsScore
    )

    data class DaysPastExpiration(
        override val value: Int
    ) : Metric(
        value = value,
        unit = MetricUnit.Days,
        textResId = R.string.metric_text_days_past_expiration,
        iconDrawableRes = R.drawable.ic_history
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