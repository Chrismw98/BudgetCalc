package chrismw.budgetcalc.helpers

import chrismw.budgetcalc.data.currency.Currency
import java.time.DayOfWeek
import java.time.LocalDate

data class UiBudgetData(
    val isBudgetConstant: Boolean? = null,
    val constantBudgetAmount: String? = null,
    val budgetRateAmount: String? = null,

    val currency: Currency? = null,

    val budgetType: BudgetType? = null,
    val defaultPaymentDayOfMonth: String? = null,
    val defaultPaymentDayOfWeek: DayOfWeek? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) {

    fun toDTO(): BudgetDataDTO = BudgetDataDTO(
        isBudgetConstant = isBudgetConstant,
        constantBudgetAmount = constantBudgetAmount?.toDoubleOrNull()?.div(100.0),
        budgetRateAmount = budgetRateAmount?.toDoubleOrNull()?.div(100.0),
        currencyCode = currency?.code,
        budgetType = budgetType,
        defaultPaymentDayOfMonth = defaultPaymentDayOfMonth?.toIntOrNull(),
        defaultPaymentDayOfWeek = defaultPaymentDayOfWeek,
        startDate = startDate,
        endDate = endDate,
    )
}