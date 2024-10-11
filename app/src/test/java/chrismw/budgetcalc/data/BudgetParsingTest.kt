package chrismw.budgetcalc.data

import chrismw.budgetcalc.extensions.toBudget
import chrismw.budgetcalc.helpers.BudgetType
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

/**
 * Test class for [Budget] parsing.
 */
class BudgetParsingTest {

    @Test
    fun `Blank currency throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "  ",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(today)
        }
    }

    @Test
    fun `Missing constant budget data throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = null,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(today)
        }
    }

    @Test
    fun `Missing rate budget data throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = null,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(today)
        }
    }

    @Test
    fun `Incomplete Monthly Budget Parsing throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteMonthlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = null
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteMonthlyBudgetData.toBudget(today)
        }
    }

    @Test
    fun `Incomplete Weekly Budget Parsing throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteWeeklyBudgetData = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currency = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = null
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteWeeklyBudgetData.toBudget(today)
        }
    }

    @Test
    fun `Incomplete OnceOnly Budget Parsing throws error`() {
        val today = LocalDate.of(2024, 4, 15)
        val incompleteOnceOnlyBudgetData = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currency = "EUR",
            budgetType = BudgetType.OnceOnly,
            defaultStartDate = null,
            defaultEndDate = null
        )

        assertThrows(IllegalStateException::class.java) {
            incompleteOnceOnlyBudgetData.toBudget(today)
        }
    }

    // *********** Monthly Budget ***********

    @Test
    fun `Constant Monthly Budget correctly parsed`() {
        val today = LocalDate.of(2024, 4, 15)
        val constantMonthlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val budget = constantMonthlyBudgetData.toBudget(today)

        assertThat(budget).isInstanceOf(Budget.Monthly::class.java)
        assertThat(budget.amount).isEqualTo(300F)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 4, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 4, 30))
    }

    @Test
    fun `Constant Monthly Budget correctly parsed lower bound`() {
        val today = LocalDate.of(2024, 4, 1)
        val constantMonthlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val budget = constantMonthlyBudgetData.toBudget(today)

        assertThat(budget).isInstanceOf(Budget.Monthly::class.java)
        assertThat(budget.amount).isEqualTo(300F)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 4, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 4, 30))
    }

    @Test
    fun `Constant Monthly Budget correctly parsed upper bound`() {
        val today = LocalDate.of(2024, 4, 30)
        val constantMonthlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val budget = constantMonthlyBudgetData.toBudget(today)


        assertThat(budget).isInstanceOf(Budget.Monthly::class.java)
        assertThat(budget.amount).isEqualTo(300F)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 4, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 4, 30))
    }

    // *********** Weekly Budget ***********

    @Test
    fun `Weekly Rate Budget correctly parsed`() {
        val monday = LocalDate.of(2024, 7, 1)
        val thursday = LocalDate.of(2024, 7, 4)
        val sunday = LocalDate.of(2024, 7, 7)
        val rateWeeklyBudgetData = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currency = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = 1
        )

        val budget = rateWeeklyBudgetData.toBudget(thursday)

        assertThat(budget).isInstanceOf(Budget.Weekly::class.java)
        assertThat(budget.amount).isEqualTo(70)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(monday)
        assertThat(budget.endDate).isEqualTo(sunday)
    }

    @Test
    fun `Weekly Rate Budget correctly parsed lower bound`() {
        val monday = LocalDate.of(2024, 7, 1)
        val sunday = LocalDate.of(2024, 7, 7)
        val rateWeeklyBudgetData = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currency = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = 1
        )

        val budget = rateWeeklyBudgetData.toBudget(monday)



        assertThat(budget).isInstanceOf(Budget.Weekly::class.java)
        assertThat(budget.amount).isEqualTo(70)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(monday)
        assertThat(budget.endDate).isEqualTo(sunday)
    }

    @Test
    fun `Weekly Rate Budget correctly parsed upper bound`() {
        val monday = LocalDate.of(2024, 7, 1)
        val sunday = LocalDate.of(2024, 7, 7)
        val rateWeeklyBudgetData = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currency = "EUR",
            budgetType = BudgetType.Weekly,
            defaultPaymentDayOfWeek = 1
        )

        val budget = rateWeeklyBudgetData.toBudget(sunday)


        assertThat(budget).isInstanceOf(Budget.Weekly::class.java)
        assertThat(budget.amount).isEqualTo(70)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(monday)
        assertThat(budget.endDate).isEqualTo(sunday)
    }

    @Test
    fun `Constant OnceOnly Budget correctly parsed`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 12, 31)
        val today = LocalDate.of(2024, 4, 15)
        val constantOnceOnlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 3_660F, //2024 is a leap year
            currency = "EUR",
            budgetType = BudgetType.OnceOnly,
            defaultStartDate = startDate.toString(),
            defaultEndDate = endDate.toString()
        )

        val budget = constantOnceOnlyBudgetData.toBudget(today)

        assertThat(budget).isInstanceOf(Budget.OnceOnly::class.java)
        assertThat(budget.amount).isEqualTo(3_660F)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 12, 31))
    }

    @Test
    fun `Constant OnceOnly Budget correctly parsed lower bound`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 12, 31)
        val constantOnceOnlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 3_660F, //2024 is a leap year
            currency = "EUR",
            budgetType = BudgetType.OnceOnly,
            defaultStartDate = startDate.toString(),
            defaultEndDate = endDate.toString()
        )

        val budget = constantOnceOnlyBudgetData.toBudget(startDate)

        assertThat(budget).isInstanceOf(Budget.OnceOnly::class.java)
        assertThat(budget.amount).isEqualTo(3_660F)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 12, 31))
    }

    @Test
    fun `Constant OnceOnly Budget correctly parsed upper bound`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 12, 31)
        val constantOnceOnlyBudgetData = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 3_660F, //2024 is a leap year
            currency = "EUR",
            budgetType = BudgetType.OnceOnly,
            defaultStartDate = startDate.toString(),
            defaultEndDate = endDate.toString()
        )

        val budget = constantOnceOnlyBudgetData.toBudget(endDate)

        assertThat(budget).isInstanceOf(Budget.OnceOnly::class.java)
        assertThat(budget.amount).isEqualTo(3_660F)
        assertThat(budget.currency).isEqualTo("EUR")
        assertThat(budget.startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(budget.endDate).isEqualTo(LocalDate.of(2024, 12, 31))
    }
}
