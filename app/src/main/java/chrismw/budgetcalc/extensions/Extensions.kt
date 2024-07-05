package chrismw.budgetcalc.extensions

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
@ReadOnlyComposable
fun relativeDateString(timeInMillis: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timeInMillis,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

@Composable
@ReadOnlyComposable
fun dateTimeString(timeInEpochMillis: Long): String {
    val context = LocalContext.current
    return DateUtils.formatDateTime(
        context,
        timeInEpochMillis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
            or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NUMERIC_DATE
    )
}

@Composable
@ReadOnlyComposable
fun dateString(timeInEpochMillis: Long): String {
    val context = LocalContext.current
    return DateUtils.formatDateTime(
        context,
        timeInEpochMillis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_WEEKDAY
        or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_YEAR
        or DateUtils.FORMAT_NUMERIC_DATE
    )
}

/**
 * Format number to shows decimal separators
 * Example: 1234567 -> 1.234.567
 */
@Composable
@ReadOnlyComposable
fun currencyFormat(amount: Long): String {
    return String.format("%1\$,d", amount)
}

/**
 * Pops the back stack of the current entry, if its state is [Lifecycle.State.RESUMED].
 * Background: After popping the back stack with [NavController.popBackStack] the navigation will
 * start and the current screen will change its lifecycle state away from RESUMED.
 */
public fun NavController.popBackStackIfResumed() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}

/**
 * Convert the LocalDate to epoch millis using the system default timezone. The time is the start of the day.
 */
fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}