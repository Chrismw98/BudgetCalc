package chrismw.budgetcalc.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import chrismw.budgetcalc.R
import java.time.DayOfWeek

@ReadOnlyComposable
@Composable
fun getStringForDayOfWeek(dayOfWeek: DayOfWeek): String {
    val stringRes = when (dayOfWeek) {
        DayOfWeek.MONDAY -> R.string.label_monday
        DayOfWeek.TUESDAY -> R.string.label_tuesday
        DayOfWeek.WEDNESDAY -> R.string.label_wednesday
        DayOfWeek.THURSDAY -> R.string.label_thursday
        DayOfWeek.FRIDAY -> R.string.label_friday
        DayOfWeek.SATURDAY -> R.string.label_saturday
        DayOfWeek.SUNDAY -> R.string.label_sunday
    }
    return stringResource(id = stringRes)
}