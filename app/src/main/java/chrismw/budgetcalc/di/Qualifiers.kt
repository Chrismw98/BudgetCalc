package chrismw.budgetcalc.di

import javax.inject.Qualifier

/**
 * DI qualifier for the OffsetDateTime for now.
 */
@Qualifier
public annotation class DateTimeNow

/**
 * DI qualifier for the LocalDate for today.
 */
@Qualifier
public annotation class DateNow