package chrismw.budgetcalc.helpers

import chrismw.budgetcalc.data.BudgetData
import java.time.DayOfWeek
import java.time.LocalDate

data class BudgetDataDTO(
    val isBudgetConstant: Boolean = false,

    val constantBudgetAmount: String? = null,
    val budgetRateAmount: String? = null,
    val currency: String? = null,

    val budgetType: BudgetType = BudgetType.MONTHLY,
    val defaultPaymentDayOfMonth: String? = null,
    val defaultPaymentDayOfWeek: DayOfWeek? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) {

    fun toBudgetData(): BudgetData {
        return BudgetData(
            isBudgetConstant = isBudgetConstant,

            constantBudgetAmount = constantBudgetAmount?.toFloatOrNull(),
            budgetRateAmount = budgetRateAmount?.toFloatOrNull(),
            currency = currency,

            budgetType = budgetType,
            defaultPaymentDayOfMonth = defaultPaymentDayOfMonth?.toIntOrNull(),
            defaultPaymentDayOfWeek = defaultPaymentDayOfWeek?.value,
            defaultStartDate = startDate?.toString(),
            defaultEndDate = endDate?.toString(),
        )
    }
}