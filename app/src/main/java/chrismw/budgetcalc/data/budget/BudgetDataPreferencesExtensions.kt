package chrismw.budgetcalc.data.budget

import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetType
import java.time.DayOfWeek
import java.time.LocalDate

private inline fun <T> Boolean.ifTrue(block: () -> T): T? = takeIf { it }?.let { block() }

fun BudgetDataPreferences.toBudgetDataDTO(): BudgetDataDTO {
    return BudgetDataDTO(
        isBudgetConstant = hasIsBudgetConstant().ifTrue { isBudgetConstant },

        constantBudgetAmount = hasConstantBudgetAmount().ifTrue { constantBudgetAmount },
        budgetRateAmount = hasBudgetRateAmount().ifTrue { budgetRateAmount },
        currencyCode = hasCurrencyCode().ifTrue { currencyCode },

        budgetType = hasBudgetType().ifTrue { budgetType.toBudgetType() },
        defaultPaymentDayOfMonth = hasDefaultPaymentDayOfMonth().ifTrue { defaultPaymentDayOfMonth },
        defaultPaymentDayOfWeek = hasDefaultPaymentDayOfWeek().ifTrue { DayOfWeek.of(defaultPaymentDayOfWeek) },
        startDate = hasDefaultStartDate().ifTrue { LocalDate.parse(defaultStartDate) },
        endDate = hasDefaultEndDate().ifTrue { LocalDate.parse(defaultEndDate) },
    )
}

private fun BudgetTypePreferences.toBudgetType() = when (this) {
    BudgetTypePreferences.ONCE_ONLY -> BudgetType.OnceOnly
    BudgetTypePreferences.WEEKLY -> BudgetType.Weekly
    BudgetTypePreferences.MONTHLY -> BudgetType.Monthly
    BudgetTypePreferences.UNRECOGNIZED -> throw IllegalStateException("Unrecognized value for BudgetTypePreferences!")
}