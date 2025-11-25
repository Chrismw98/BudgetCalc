package chrismw.budgetcalc.di

import javax.inject.Qualifier

/**
 * DI qualifier for the OffsetDateTime for now.
 */
@Qualifier
public annotation class OffsetDateTimeNow

/**
 * DI qualifier for the LocalDateTime for now.
 */

@Qualifier
public annotation class DateTimeNow

/**
 * DI qualifier for the LocalDate for today.
 */
@Qualifier
public annotation class DateNow

/**
 * DI qualifier for the default coroutines dispatcher.
 */
@Qualifier
public annotation class DefaultDispatcher

/**
 * DI qualifier for the I/O coroutines dispatcher.
 */
@Qualifier
public annotation class IODispatcher

/**
 * DI qualifier for the Main UI thread coroutines dispatcher.
 */
@Qualifier
public annotation class MainDispatcher

/**
 * DI qualifier for the unconfined coroutines dispatcher.
 */
@Qualifier
public annotation class UnconfinedDispatcher

/**
 * DI qualifier for the application coroutines scope.
 */
@Qualifier
public annotation class ApplicationScope