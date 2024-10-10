package chrismw.budgetcalc.helpers

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.abs

internal fun findLatestOccurrenceOfDayOfMonth(today: LocalDate, targetDayOfMonth: Int): LocalDate {
    val currentMonthAtTargetDayOfMonth = today.withDayOfMonth(targetDayOfMonth)
    return if (currentMonthAtTargetDayOfMonth.isAfter(today)) {
        currentMonthAtTargetDayOfMonth.minusMonths(1)
    } else {
        currentMonthAtTargetDayOfMonth
    }
}

internal fun findNextOccurrenceOfDayOfMonth(today: LocalDate, targetDayOfMonth: Int): LocalDate {
    val currentMonthAtTargetDayOfMonth = today.withDayOfMonth(targetDayOfMonth)
    return if (currentMonthAtTargetDayOfMonth.isAfter(today)) {
        currentMonthAtTargetDayOfMonth
    } else {
        currentMonthAtTargetDayOfMonth.plusMonths(1)
    }
}

internal fun findLatestOccurrenceOfDayOfWeek(today: LocalDate, targetDayOfWeek: DayOfWeek): LocalDate {
    val currentDayOfWeek = today.dayOfWeek
    val differenceInDays = abs(currentDayOfWeek.value - targetDayOfWeek.value).toLong()
    return if (targetDayOfWeek <= currentDayOfWeek) {
        today.minusDays(differenceInDays)
    } else {
        today.plusDays(differenceInDays).minusWeeks(1)
    }
}