package chrismw.budgetcalc.data

import chrismw.budgetcalc.data.budget.Budget
import chrismw.budgetcalc.data.currency.Currency
import chrismw.budgetcalc.helpers.Metric
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Test class for [Metric] parsing.
 */
class MetricsTest {

    private val EURO_CURRENCY = Currency(
        code = "EUR",
        name = "Euro",
        symbol = "€"
    )

    @Test
    fun `Metrics correct for Monthly Budget`() {
        val targetDate = LocalDate.of(2024, 4, 10)
        val monthlyBudget = Budget.Monthly(
            amount = 300.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(10),
            Metric.DaysRemaining(20),
            Metric.DailyBudget(10.0),
            Metric.BudgetUntilTargetDate(100.0),
            Metric.RemainingBudget(200.0),
            Metric.TotalBudget(300.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Monthly Budget lower bound`() {
        val targetDate = LocalDate.of(2024, 4, 1)
        val monthlyBudget = Budget.Monthly(
            amount = 300.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(1),
            Metric.DaysRemaining(29),
            Metric.DailyBudget(10.0),
            Metric.BudgetUntilTargetDate(10.0),
            Metric.RemainingBudget(290.0),
            Metric.TotalBudget(300.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Monthly Budget upper bound`() {
        val targetDate = LocalDate.of(2024, 4, 30)
        val monthlyBudget = Budget.Monthly(
            amount = 300.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(30),
            Metric.DaysRemaining(0),
            Metric.DailyBudget(10.0),
            Metric.BudgetUntilTargetDate(300.0),
            Metric.RemainingBudget(0.0),
            Metric.TotalBudget(300.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Monthly Budget exceeding upper bound`() {
        val targetDate = LocalDate.of(2024, 5, 1)
        val monthlyBudget = Budget.Monthly(
            amount = 300.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysPastExpiration(1),
            Metric.DailyBudget(10.0),
            Metric.TotalBudget(300.0),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget`() {
        val targetDate = LocalDate.of(2024, 7, 3)
        val monthlyBudget = Budget.Weekly(
            amount = 70.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(3),
            Metric.DaysRemaining(4),
            Metric.DailyBudget(10.0),
            Metric.BudgetUntilTargetDate(30.0),
            Metric.RemainingBudget(40.0),
            Metric.TotalBudget(70.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget lower bound`() {
        val targetDate = LocalDate.of(2024, 7, 1)
        val monthlyBudget = Budget.Weekly(
            amount = 70.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(1),
            Metric.DaysRemaining(6),
            Metric.DailyBudget(10.0),
            Metric.BudgetUntilTargetDate(10.0),
            Metric.RemainingBudget(60.0),
            Metric.TotalBudget(70.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget upper bound`() {
        val targetDate = LocalDate.of(2024, 7, 7)
        val monthlyBudget = Budget.Weekly(
            amount = 70.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(7),
            Metric.DaysRemaining(0),
            Metric.DailyBudget(10.0),
            Metric.BudgetUntilTargetDate(70.0),
            Metric.RemainingBudget(0.0),
            Metric.TotalBudget(70.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget exceeding upper bound`() {
        val targetDate = LocalDate.of(2024, 7, 14)
        val monthlyBudget = Budget.Weekly(
            amount = 70.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysPastExpiration(7),
            Metric.DailyBudget(10.0),
            Metric.TotalBudget(70.0),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget`() {
        val targetDate = LocalDate.of(2024, 7, 3)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(185),
            Metric.DaysRemaining(181),
            Metric.DailyBudget(1.0),
            Metric.BudgetUntilTargetDate(185.0),
            Metric.RemainingBudget(181.0),
            Metric.TotalBudget(366.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget lower bound`() {
        val targetDate = LocalDate.of(2024, 1, 1)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(1),
            Metric.DaysRemaining(365),
            Metric.DailyBudget(1.0),
            Metric.BudgetUntilTargetDate(1.0),
            Metric.RemainingBudget(365.0),
            Metric.TotalBudget(366.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget upper bound`() {
        val targetDate = LocalDate.of(2024, 12, 31)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(366),
            Metric.DaysRemaining(0),
            Metric.DailyBudget(1.0),
            Metric.BudgetUntilTargetDate(366.0),
            Metric.RemainingBudget(0.0),
            Metric.TotalBudget(366.0),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget exceeding lower bound`() {
        val targetDate = LocalDate.of(2023, 12, 31)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysUntilStart(1),
            Metric.DailyBudget(1.0),
            Metric.TotalBudget(366.0),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget exceeding upper bound`() {
        val targetDate = LocalDate.of(2025, 1, 10)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366.0,
            currency = EURO_CURRENCY,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysPastExpiration(10),
            Metric.DailyBudget(1.0),
            Metric.TotalBudget(366.0),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }
}
