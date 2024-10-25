package chrismw.budgetcalc.extensions

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
 * Convert the [LocalDate] to epoch millis using the system default timezone.
 */
fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/**
 * Convert the epoch millis to [LocalDate] using the system default timezone.
 */
fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}