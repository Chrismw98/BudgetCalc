package chrismw.budgetcalc.data

import chrismw.budgetcalc.data.budget.Budget
import chrismw.budgetcalc.data.currency.Currency
import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetType
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Test class for [Budget] parsing.
 */
class BudgetParsingTest {

    private val EURO_CURRENCY: Currency = Currency(
        code = "EUR",
        name = "Euro",
        symbol = "€"
    )
    private val CURRENCY_MAP: Map<String, Currency> = mapOf(
        "EUR" to EURO_CURRENCY,
    )

    @Test
    fun `Blank currency code throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currencyCode = "  ",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(
                today = today,
                currenciesMap = CURRENCY_MAP,
            )
        }
    }

    @Test
    fun `Currency code not found in currency map throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currencyCode = "NOT_EXISTING_CODE",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(
                today = today,
                currenciesMap = CURRENCY_MAP,
            )
        }
    }

    @Test
    fun `Missing constant budget data throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = null,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(
                today = today,
                currenciesMap = CURRENCY_MAP,
            )
        }
    }

    @Test
    fun `Missing rate budget data throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = null,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(
                today = today,
                currenciesMap = CURRENCY_MAP,
            )
        }
    }

    @Test
    fun `Incomplete Monthly Budget Parsing throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = null,
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(
                today = today,
                currenciesMap = CURRENCY_MAP,
            )
        }
    }

    @Test
    fun `Incomplete Weekly Budget Parsing throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteWeeklyBudgetData = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currencyCode = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = null
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteWeeklyBudgetData.toBudget(
                today = today,
                currenciesMap = CURRENCY_MAP,
            )
        }
    }

    @Test
    fun `Incomplete OnceOnly Budget Parsing throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteOnceOnlyBudgetData = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currencyCode = "EUR",
            budgetType = BudgetType.OnceOnly,
            startDate = null,
            endDate = null
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteOnceOnlyBudgetData.toBudget(
                today = today,
                currenciesMap = CURRENCY_MAP,
            )
        }
    }

    // *********** Monthly Budget ***********

    @Test
    fun `Constant Monthly Budget correctly parsed`() {
        val today = LocalDate.of(2024, 4, 15)
        val constantMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val budget = constantMonthlyBudgetData.toBudget(
            today = today,
            currenciesMap = CURRENCY_MAP,
        )

        assertThat(budget).isInstanceOf(Budget.Monthly::class.java)
        assertThat(budget.amount).isEqualTo(300F)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 4, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 4, 30))
    }

    @Test
    fun `Constant Monthly Budget correctly parsed lower bound`() {
        val today = LocalDate.of(2024, 4, 1)
        val constantMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val budget = constantMonthlyBudgetData.toBudget(
            today = today,
            currenciesMap = CURRENCY_MAP,
        )

        assertThat(budget).isInstanceOf(Budget.Monthly::class.java)
        assertThat(budget.amount).isEqualTo(300F)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 4, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 4, 30))
    }

    @Test
    fun `Constant Monthly Budget correctly parsed upper bound`() {
        val today = LocalDate.of(2024, 4, 30)
        val constantMonthlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val budget = constantMonthlyBudgetData.toBudget(
            today = today,
            currenciesMap = CURRENCY_MAP,
        )


        assertThat(budget).isInstanceOf(Budget.Monthly::class.java)
        assertThat(budget.amount).isEqualTo(300F)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 4, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 4, 30))
    }

    // *********** Weekly Budget ***********

    @Test
    fun `Weekly Rate Budget correctly parsed`() {
        val monday = LocalDate.of(2024, 7, 1)
        val thursday = LocalDate.of(2024, 7, 4)
        val sunday = LocalDate.of(2024, 7, 7)
        val rateWeeklyBudgetData = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currencyCode = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = DayOfWeek.MONDAY,
        )

        val budget = rateWeeklyBudgetData.toBudget(
            today = thursday,
            currenciesMap = CURRENCY_MAP,
        )

        assertThat(budget).isInstanceOf(Budget.Weekly::class.java)
        assertThat(budget.amount).isEqualTo(70)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(monday)
        assertThat(budget.endDate).isEqualTo(sunday)
    }

    @Test
    fun `Weekly Rate Budget correctly parsed lower bound`() {
        val monday = LocalDate.of(2024, 7, 1)
        val sunday = LocalDate.of(2024, 7, 7)
        val rateWeeklyBudgetData = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currencyCode = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = DayOfWeek.MONDAY,
        )

        val budget = rateWeeklyBudgetData.toBudget(
            today = monday,
            currenciesMap = CURRENCY_MAP,
        )



        assertThat(budget).isInstanceOf(Budget.Weekly::class.java)
        assertThat(budget.amount).isEqualTo(70)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(monday)
        assertThat(budget.endDate).isEqualTo(sunday)
    }

    @Test
    fun `Weekly Rate Budget correctly parsed upper bound`() {
        val monday = LocalDate.of(2024, 7, 1)
        val sunday = LocalDate.of(2024, 7, 7)
        val rateWeeklyBudgetData = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currencyCode = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = DayOfWeek.MONDAY,
        )

        val budget = rateWeeklyBudgetData.toBudget(
            today = sunday,
            currenciesMap = CURRENCY_MAP,
        )


        assertThat(budget).isInstanceOf(Budget.Weekly::class.java)
        assertThat(budget.amount).isEqualTo(70)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(monday)
        assertThat(budget.endDate).isEqualTo(sunday)
    }

    @Test
    fun `Constant OnceOnly Budget correctly parsed`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 12, 31)
        val today = LocalDate.of(2024, 4, 15)
        val constantOnceOnlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 3_660F, //2024 is a leap year
            currencyCode = "EUR",
            budgetType = BudgetType.OnceOnly,
            startDate = startDate,
            endDate = endDate,
        )

        val budget = constantOnceOnlyBudgetData.toBudget(
            today = today,
            currenciesMap = CURRENCY_MAP,
        )

        assertThat(budget).isInstanceOf(Budget.OnceOnly::class.java)
        assertThat(budget.amount).isEqualTo(3_660F)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 12, 31))
    }

    @Test
    fun `Constant OnceOnly Budget correctly parsed lower bound`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 12, 31)
        val constantOnceOnlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 3_660F, //2024 is a leap year
            currencyCode = "EUR",
            budgetType = BudgetType.OnceOnly,
            startDate = startDate,
            endDate = endDate,
        )

        val budget = constantOnceOnlyBudgetData.toBudget(
            today = startDate,
            currenciesMap = CURRENCY_MAP,
        )

        assertThat(budget).isInstanceOf(Budget.OnceOnly::class.java)
        assertThat(budget.amount).isEqualTo(3_660F)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 12, 31))
    }

    @Test
    fun `Constant OnceOnly Budget correctly parsed upper bound`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 12, 31)
        val constantOnceOnlyBudgetData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 3_660F, //2024 is a leap year
            currencyCode = "EUR",
            budgetType = BudgetType.OnceOnly,
            startDate = startDate,
            endDate = endDate,
        )

        val budget = constantOnceOnlyBudgetData.toBudget(
            today = endDate,
            currenciesMap = CURRENCY_MAP,
        )

        assertThat(budget).isInstanceOf(Budget.OnceOnly::class.java)
        assertThat(budget.amount).isEqualTo(3_660F)
        assertThat(budget.currency).isEqualTo(EURO_CURRENCY)
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 12, 31))
    }
}
