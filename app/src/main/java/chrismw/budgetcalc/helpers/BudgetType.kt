package chrismw.budgetcalc.helpers

import androidx.annotation.StringRes
import chrismw.budgetcalc.R

enum class BudgetType(@StringRes val textRes: Int) {
    ONCE_ONLY(R.string.label_once_only),
    WEEKLY(R.string.label_weekly),
    MONTHLY(R.string.label_monthly);
}