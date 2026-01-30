package chrismw.budgetcalc.helpers

import chrismw.budgetcalc.data.budget.Budget
import chrismw.budgetcalc.data.budget.BudgetDataPreferences
import chrismw.budgetcalc.data.budget.BudgetTypePreferences
import chrismw.budgetcalc.data.currency.Currency
import chrismw.budgetcalc.helpers.Constants.WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class BudgetDataDTO(
    val isBudgetConstant: Boolean? = false,

    val constantBudgetAmount: Float? = null,
    val budgetRateAmount: Float? = null,
    val currencyCode: String? = null,

    val budgetType: BudgetType? = BudgetType.Monthly,
    val defaultPaymentDayOfMonth: Int? = null,
    val defaultPaymentDayOfWeek: DayOfWeek? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) {

    fun toBudgetDataPreferences(): BudgetDataPreferences {
        return BudgetDataPreferences.newBuilder()
            .apply {
                this@BudgetDataDTO.isBudgetConstant?.let { setIsBudgetConstant(it) } ?: clearIsBudgetConstant()

                this@BudgetDataDTO.constantBudgetAmount?.let { setConstantBudgetAmount(it) } ?: clearConstantBudgetAmount()
                this@BudgetDataDTO.budgetRateAmount?.let { setBudgetRateAmount(it) } ?: clearBudgetRateAmount()
                this@BudgetDataDTO.currencyCode?.let { setCurrencyCode(it) } ?: clearCurrencyCode()

                this@BudgetDataDTO.budgetType?.toBudgetTypePreferences()?.let { setBudgetType(it) } ?: clearBudgetType()
                this@BudgetDataDTO.defaultPaymentDayOfMonth?.let { setDefaultPaymentDayOfMonth(it) } ?: clearDefaultPaymentDayOfMonth()
                this@BudgetDataDTO.defaultPaymentDayOfWeek?.value?.let { setDefaultPaymentDayOfWeek(it) } ?: clearDefaultPaymentDayOfWeek()
                this@BudgetDataDTO.startDate?.toString()?.let { setDefaultStartDate(it) } ?: clearDefaultStartDate()
                this@BudgetDataDTO.endDate?.toString()?.let { setDefaultEndDate(it) } ?: clearDefaultEndDate()
            }
            .build()
    }

    fun toBudget(
        today: LocalDate,
        currenciesMap: Map<String, Currency>,
    ): Budget {
        val isBudgetConstant = isBudgetConstant.checkNotNullOrThrow("isBudgetConstant")
        val currency = this@BudgetDataDTO.currencyCode
            .checkNotNullOrThrow("currency code")
            .let { currenciesMap[it] }
            .checkNotNullOrThrow("currency")

        val budgetType = budgetType.checkNotNullOrThrow("budgetType")

        val startDate = when (budgetType) {
            BudgetType.OnceOnly -> startDate.checkNotNullOrThrow("startDate")

            BudgetType.Weekly -> {
                findLatestOccurrenceOfDayOfWeek(
                    today = today,
                    targetDayOfWeek = defaultPaymentDayOfWeek.checkNotNullOrThrow("defaultPaymentDayOfWeek")
                )
            }

            BudgetType.Monthly -> {
                findLatestOccurrenceOfDayOfMonth(
                    today = today,
                    targetDayOfMonth = defaultPaymentDayOfMonth.checkNotNullOrThrow("defaultPaymentDayOfMonth")
                )
            }
        }

        val endDate = when (budgetType) {
            BudgetType.OnceOnly -> endDate.checkNotNullOrThrow("startDate")

            BudgetType.Weekly -> {
                startDate.plusDays(WEEKLY_BUDGET_PAYMENT_CYCLE_LENGTH_IN_DAYS - 1L)
            }

            BudgetType.Monthly -> {
                findNextOccurrenceOfDayOfMonth(
                    today = today,
                    targetDayOfMonth = defaultPaymentDayOfMonth.checkNotNullOrThrow("defaultPaymentDayOfMonth")
                ).minusDays(1)
            }
        }

        val amount = if (isBudgetConstant) {
            constantBudgetAmount.checkNotNullOrThrow("constantBudgetAmount")
        } else {
            val paymentCycleLengthInDays =
                ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()
            budgetRateAmount.checkNotNullOrThrow("budgetRateAmount") * paymentCycleLengthInDays
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

private fun BudgetType.toBudgetTypePreferences() = when (this) {
    BudgetType.OnceOnly -> BudgetTypePreferences.ONCE_ONLY
    BudgetType.Weekly -> BudgetTypePreferences.WEEKLY
    BudgetType.Monthly -> BudgetTypePreferences.MONTHLY
}

private fun <T> T?.checkNotNullOrThrow(propertyName: String): T {
    return this ?: throw IllegalStateException("$propertyName cannot be null!")
}