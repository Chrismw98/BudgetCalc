package chrismw.budgetcalc.helpers

import androidx.annotation.StringRes
import chrismw.budgetcalc.R

enum class DayOfWeek(@StringRes val textRes: Int) { //TODO: Check if this can be deleted 2023-12-07
    ONCE_ONLY(R.string.label_once_only),
    WEEKLY(R.string.label_weekly),
    MONTHLY(R.string.label_monthly);
//    MONDAY,
//    TUESDAY,
//    WEDNESDAY,
//    THURSDAY,
//    FRIDAY,
//    SATURDAY,
//    SUNDAY
}