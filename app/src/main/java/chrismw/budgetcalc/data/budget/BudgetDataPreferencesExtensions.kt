package chrismw.budgetcalc.data.budget

import chrismw.budgetcalc.data.budget.Budget.Monthly
import chrismw.budgetcalc.data.budget.Budget.OnceOnly
import chrismw.budgetcalc.data.budget.Budget.Weekly
import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.Constants.WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfMonth
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfWeek
import chrismw.budgetcalc.helpers.findNextOccurrenceOfDayOfMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private inline fun <T> Boolean.ifTrue(block: () -> T): T? = takeIf { it }?.let { block() }

private inline fun <T> Boolean.ifTrueOrThrow(block: () -> T): T = takeIf { it }?.let { block() }
    ?: throw IllegalStateException("Expected condition to be true, but was false.")

fun BudgetDataPreferences.toBudgetDataDTO(): BudgetDataDTO {
    return BudgetDataDTO(
        isBudgetConstant = hasIsBudgetConstant().ifTrue { isBudgetConstant },

        constantBudgetAmount = hasConstantBudgetAmount().ifTrue { constantBudgetAmount },
        budgetRateAmount = hasBudgetRateAmount().ifTrue { budgetRateAmount },
        currency = hasCurrency().ifTrue { currency },

        budgetType = hasBudgetType().ifTrue { budgetType.toBudgetType() },
        defaultPaymentDayOfMonth = hasDefaultPaymentDayOfMonth().ifTrue { defaultPaymentDayOfMonth },
        defaultPaymentDayOfWeek = hasDefaultPaymentDayOfWeek().ifTrue { DayOfWeek.of(defaultPaymentDayOfWeek) },
        startDate = hasDefaultStartDate().ifTrue { LocalDate.parse(defaultStartDate) },
        endDate = hasDefaultEndDate().ifTrue { LocalDate.parse(defaultEndDate) },
    )
}

fun BudgetDataPreferences.toBudget(today: LocalDate): Budget {
    //TODO: There might be a better way to handle the currency 2024-10-11
    val currency = hasCurrency().ifTrueOrThrow {
        currency.ifBlank {
            throw IllegalStateException("Currency cannot be blank!")
        }
    }

    val startDate = when (budgetType) {
        BudgetTypePreferences.ONCE_ONLY -> {
            hasDefaultStartDate().ifTrueOrThrow { LocalDate.parse(defaultStartDate) }
        }

        BudgetTypePreferences.WEEKLY -> {
            findLatestOccurrenceOfDayOfWeek(
                today = today,
                targetDayOfWeek = hasDefaultPaymentDayOfWeek().ifTrueOrThrow { DayOfWeek.of(defaultPaymentDayOfWeek) },
            )
        }

        BudgetTypePreferences.MONTHLY -> {
            findLatestOccurrenceOfDayOfMonth(
                today = today,
                targetDayOfMonth = hasDefaultPaymentDayOfMonth().ifTrueOrThrow { defaultPaymentDayOfMonth },
            )
        }

        BudgetTypePreferences.UNRECOGNIZED -> throw IllegalStateException("Unrecognized value for BudgetTypePreferences!")
    }

    val endDate = when (budgetType) {
        BudgetTypePreferences.ONCE_ONLY -> {
            hasDefaultEndDate().ifTrueOrThrow { LocalDate.parse(defaultEndDate) }
        }

        BudgetTypePreferences.WEEKLY -> {
            startDate.plusDays(WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS - 1L)
        }

        BudgetTypePreferences.MONTHLY -> {
            findNextOccurrenceOfDayOfMonth(
                today = today,
                targetDayOfMonth = hasDefaultPaymentDayOfMonth().ifTrueOrThrow { defaultPaymentDayOfMonth },
            ).minusDays(1)
        }

        BudgetTypePreferences.UNRECOGNIZED -> throw IllegalStateException("Unrecognized value for BudgetTypePreferences!")
    }

    val isBudgetConstant = hasIsBudgetConstant().ifTrueOrThrow { isBudgetConstant }
    val amount = if (isBudgetConstant) {
        hasConstantBudgetAmount().ifTrueOrThrow { constantBudgetAmount }
    } else {
        val paymentCycleLengthInDays =
            ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()
        val budgetRateAmount = hasBudgetRateAmount().ifTrueOrThrow { budgetRateAmount }

        budgetRateAmount * paymentCycleLengthInDays
    }

    return when (budgetType) {
        BudgetTypePreferences.ONCE_ONLY -> OnceOnly(
            amount = amount,
            currency = currency,
            startDate = startDate,
            endDate = endDate,
        )

        BudgetTypePreferences.WEEKLY -> Weekly(
            amount = amount,
            currency = currency,
            startDate = startDate,
            endDate = endDate
        )

        BudgetTypePreferences.MONTHLY -> Monthly(
            amount = amount,
            currency = currency,
            startDate = startDate,
            endDate = endDate
        )

        BudgetTypePreferences.UNRECOGNIZED -> throw IllegalStateException("Unrecognized value for BudgetTypePreferences!")
    }
}

private fun BudgetTypePreferences.toBudgetType() = when (this) {
    BudgetTypePreferences.ONCE_ONLY -> BudgetType.OnceOnly
    BudgetTypePreferences.WEEKLY -> BudgetType.Weekly
    BudgetTypePreferences.MONTHLY -> BudgetType.Monthly
    BudgetTypePreferences.UNRECOGNIZED -> throw IllegalStateException("Unrecognized value for BudgetTypePreferences!")
}