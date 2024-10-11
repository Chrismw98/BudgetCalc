package chrismw.budgetcalc.extensions

import chrismw.budgetcalc.data.Budget
import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.Constants.WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfMonth
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfWeek
import chrismw.budgetcalc.helpers.findNextOccurrenceOfDayOfMonth
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal fun Budget.extractMetrics(targetDate: LocalDate): PersistentList<Metric> {
    val paymentCycleLengthInDays = ChronoUnit.DAYS.between(startDate, endDate).toInt()
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

    val metrics = persistentListOf(
        Metric.DaysSinceStart(daysSinceStart),
        Metric.DaysRemaining(daysRemaining),
        Metric.DailyBudget(dailyBudget),
        Metric.BudgetUntilTargetDate(currentBudget),
        Metric.RemainingBudget(remainingBudget),
        Metric.TotalBudget(amount),
    )

    return metrics
}

fun BudgetData.toBudget(today: LocalDate): Budget {
    //TODO: There might be a better way to handle the currency 2024-10-11
    val currency = checkNotNull(currency)
    currency.ifBlank { throw IllegalStateException("Currency cannot be blank!") }

    val startDate = when (budgetType) {
        is BudgetType.OnceOnly -> {
            LocalDate.parse(checkNotNull(defaultStartDate))
        }

        is BudgetType.Weekly -> {
            findLatestOccurrenceOfDayOfWeek(
                today = today,
                targetDayOfWeek = DayOfWeek.of(checkNotNull(defaultPaymentDayOfWeek))
            )
        }

        is BudgetType.Monthly -> {
            findLatestOccurrenceOfDayOfMonth(
                today = today,
                targetDayOfMonth = checkNotNull(defaultPaymentDayOfMonth)
            )
        }
    }

    val endDate = when (budgetType) {
        BudgetType.OnceOnly -> {
            LocalDate.parse(checkNotNull(defaultEndDate))
        }

        BudgetType.Weekly -> {
            startDate.plusDays(WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS - 1L)
        }

        BudgetType.Monthly -> {
            findNextOccurrenceOfDayOfMonth(
                today = today,
                targetDayOfMonth = checkNotNull(defaultPaymentDayOfMonth)
            )
        }
    }

    val paymentCycleLengthInDays = ChronoUnit.DAYS.between(startDate, endDate).toInt()

    val amount = if (isBudgetConstant) {
        checkNotNull(constantBudgetAmount)
    } else {
        checkNotNull(budgetRateAmount) * paymentCycleLengthInDays
    }

    return when (budgetType) {
        BudgetType.OnceOnly -> Budget.OnceOnly(
            amount = amount,
            currency = currency,
            startDate = startDate,
            endDate = endDate,
        )

        BudgetType.Weekly -> Budget.Weekly(
            amount = amount,
            currency = currency,
            startDate = startDate,
            endDate = endDate
        )

        BudgetType.Monthly -> Budget.Monthly(
            amount = amount,
            currency = currency,
            startDate = startDate,
            endDate = endDate
        )
    }
}