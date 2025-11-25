package chrismw.budgetcalc.data.budget

import chrismw.budgetcalc.helpers.BudgetState
import chrismw.budgetcalc.helpers.Metric
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Represents a budget with an amount, currency, start date, and end date.
 */
public sealed class Budget(
    open val amount: Float,
    open val currency: String,
    open val startDate: LocalDate,
    open val endDate: LocalDate,
) {

    /**
     * Extracts the budget state and relevant metrics for a given target date.
     *
     * @param targetDate The date for which to extract the budget state and metrics.
     * @return A pair containing the [BudgetState] and a list of [Metric]s.
     */
    public fun extractBudgetStateWithMetrics(targetDate: LocalDate): Pair<BudgetState, List<Metric>> {
        val budgetState = when {
            targetDate == endDate -> BudgetState.LastDay

            targetDate.isAfter(endDate) -> {
                val daysPastEnd = ChronoUnit.DAYS.between(endDate, targetDate).toInt()
                BudgetState.Expired(
                    daysPastEnd = daysPastEnd
                )
            }

            targetDate.isBefore(startDate).not() -> BudgetState.Ongoing

            else -> {
                val daysUntilStart = ChronoUnit.DAYS.between(targetDate, startDate).toInt()
                BudgetState.HasNotStarted(
                    daysUntilStart = daysUntilStart
                )
            }
        }

        val paymentCycleLengthInDays =
            ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()
        val dailyBudget = amount / paymentCycleLengthInDays

        val daysSinceStart = ChronoUnit.DAYS.between(startDate, targetDate.plusDays(1)).toInt()
        val daysRemaining = paymentCycleLengthInDays - daysSinceStart

        val currentBudget = if (daysSinceStart <= paymentCycleLengthInDays) {
            daysSinceStart * dailyBudget
        } else {
            amount
        }
        val remainingBudget = if (daysSinceStart <= paymentCycleLengthInDays) {
            amount - currentBudget
        } else {
            0F
        }

        val metrics = when (budgetState) {
            is BudgetState.HasNotStarted -> {
                listOf(
                    Metric.DaysUntilStart(budgetState.daysUntilStart),
                    Metric.DailyBudget(dailyBudget),
                    Metric.TotalBudget(amount),
                )
            }

            is BudgetState.Expired -> {
                listOf(
                    Metric.DaysPastExpiration(budgetState.daysPastEnd),
                    Metric.DailyBudget(dailyBudget),
                    Metric.TotalBudget(amount),
                )
            }

            else -> {
                listOf(
                    Metric.DaysSinceStart(daysSinceStart),
                    Metric.DaysRemaining(daysRemaining),
                    Metric.DailyBudget(dailyBudget),
                    Metric.BudgetUntilTargetDate(currentBudget),
                    Metric.RemainingBudget(remainingBudget),
                    Metric.TotalBudget(amount),
                )
            }
        }

        return budgetState to metrics
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