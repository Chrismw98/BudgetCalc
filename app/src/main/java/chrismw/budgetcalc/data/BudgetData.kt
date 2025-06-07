package chrismw.budgetcalc.data

import chrismw.budgetcalc.decimalFormat
import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.Constants.WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfMonth
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfWeek
import chrismw.budgetcalc.helpers.findNextOccurrenceOfDayOfMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class BudgetData(
    val isBudgetConstant: Boolean = false,

    val constantBudgetAmount: Float? = null,
    val budgetRateAmount: Float? = null,
    val currency: String? = null,

    val budgetType: BudgetType = BudgetType.Monthly,
    val defaultPaymentDayOfMonth: Int? = null,
    val defaultPaymentDayOfWeek: Int? = null,
    val defaultStartDate: String? = null,
    val defaultEndDate: String? = null,
) {

    fun toBudgetDataDTO(): BudgetDataDTO {
        return BudgetDataDTO(
            isBudgetConstant = isBudgetConstant,

            constantBudgetAmount = constantBudgetAmount?.let { decimalFormat.format(it) },
            budgetRateAmount = budgetRateAmount?.let { decimalFormat.format(it) },
            currency = currency,

            budgetType = budgetType,
            defaultPaymentDayOfMonth = defaultPaymentDayOfMonth?.toString(),
            defaultPaymentDayOfWeek = defaultPaymentDayOfWeek?.let { DayOfWeek.of(it) },
            startDate = defaultStartDate?.let { LocalDate.parse(it) },
            endDate = defaultEndDate?.let { LocalDate.parse(it) },
        )
    }

    fun toBudget(today: LocalDate): Budget {
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
                ).minusDays(1)
            }
        }

        val amount = if (isBudgetConstant) {
            checkNotNull(constantBudgetAmount)
        } else {
            val paymentCycleLengthInDays =
                ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()
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
}