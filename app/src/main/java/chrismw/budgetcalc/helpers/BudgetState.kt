package chrismw.budgetcalc.helpers

import androidx.annotation.StringRes
import chrismw.budgetcalc.R

public sealed class BudgetState(
    @StringRes val textResId: Int,
) {

    internal object HasNotStarted : BudgetState(
        textResId = R.string.budget_state_not_started
    )

    internal object Ongoing : BudgetState(0)

    internal object LastDay : BudgetState(
        textResId = R.string.budget_state_last_day
    )

    internal data class HasEnded(
        val daysPastEnd: Int,
    ) : BudgetState(
        textResId = R.string.budget_state_has_ended
    )
}