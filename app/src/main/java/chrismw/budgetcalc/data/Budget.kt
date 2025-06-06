package chrismw.budgetcalc.data

import chrismw.budgetcalc.helpers.BudgetState
import java.time.LocalDate
import java.time.temporal.ChronoUnit

public sealed class Budget(
    open val amount: Float,
    open val currency: String,
    open val startDate: LocalDate,
    open val endDate: LocalDate,
) {

    public fun extractBudgetState(targetDate: LocalDate): BudgetState {
        return when {

            targetDate == endDate -> BudgetState.LastDay

            targetDate.isAfter(endDate) -> {
                val daysPastEnd = ChronoUnit.DAYS.between(endDate, targetDate).toInt()
                BudgetState.HasEnded(
                    daysPastEnd = daysPastEnd
                )
            }

            targetDate.isBefore(startDate).not() -> BudgetState.Ongoing

            else -> BudgetState.HasNotStarted
        }
    }

    internal data class OnceOnly(
        override val amount: Float,
        override val currency: String,
        override val startDate: LocalDate,
        override val endDate: LocalDate,
    ) : Budget(
        amount = amount,
        currency = currency,
        startDate = startDate,
        endDate = endDate
    )

    internal data class Weekly(
        override val amount: Float,
        override val currency: String,
        override val startDate: LocalDate,
        override val endDate: LocalDate,
    ) : Budget(
        amount = amount,
        currency = currency,
        startDate = startDate,
        endDate = endDate
    )

    internal data class Monthly(
        override val amount: Float,
        override val currency: String,
        override val startDate: LocalDate,
        override val endDate: LocalDate,
    ) : Budget(
        amount = amount,
        currency = currency,
        startDate = startDate,
        endDate = endDate
    )
}