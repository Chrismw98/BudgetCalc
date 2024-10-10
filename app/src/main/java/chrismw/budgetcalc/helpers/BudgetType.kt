package chrismw.budgetcalc.helpers

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import chrismw.budgetcalc.R

@Immutable
public sealed class BudgetType(
    @StringRes val textResId: Int,
    val name: String
) {

    object OnceOnly : BudgetType(
        textResId = R.string.label_once_only,
        name = "ONCE_ONLY"
    )

    object Weekly : BudgetType(
        R.string.label_weekly,
        name = "WEEKLY"
    )

    object Monthly : BudgetType(
        R.string.label_monthly,
        name = "MONTHLY"
    )
}

public fun getBudgetTypeFromName(name: String): BudgetType {
    return when (name) {
        "ONCE_ONLY" -> BudgetType.OnceOnly
        "WEEKLY" -> BudgetType.Weekly
        "MONTHLY" -> BudgetType.Monthly
        else -> throw IllegalArgumentException("Unknown budget type name: $name")
    }
}