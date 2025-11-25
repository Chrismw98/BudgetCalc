package chrismw.budgetcalc.data

import chrismw.budgetcalc.data.budget.Budget
import chrismw.budgetcalc.helpers.Metric
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Test class for [Metric] parsing.
 */
class MetricsTest {

    @Test
    fun `Metrics correct for Monthly Budget`() {
        val targetDate = LocalDate.of(2024, 4, 10)
        val monthlyBudget = Budget.Monthly(
            amount = 300F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(10),
            Metric.DaysRemaining(20),
            Metric.DailyBudget(10F),
            Metric.BudgetUntilTargetDate(100F),
            Metric.RemainingBudget(200F),
            Metric.TotalBudget(300F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Monthly Budget lower bound`() {
        val targetDate = LocalDate.of(2024, 4, 1)
        val monthlyBudget = Budget.Monthly(
            amount = 300F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(1),
            Metric.DaysRemaining(29),
            Metric.DailyBudget(10F),
            Metric.BudgetUntilTargetDate(10F),
            Metric.RemainingBudget(290F),
            Metric.TotalBudget(300F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Monthly Budget upper bound`() {
        val targetDate = LocalDate.of(2024, 4, 30)
        val monthlyBudget = Budget.Monthly(
            amount = 300F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(30),
            Metric.DaysRemaining(0),
            Metric.DailyBudget(10F),
            Metric.BudgetUntilTargetDate(300F),
            Metric.RemainingBudget(0F),
            Metric.TotalBudget(300F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Monthly Budget exceeding upper bound`() {
        val targetDate = LocalDate.of(2024, 5, 1)
        val monthlyBudget = Budget.Monthly(
            amount = 300F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 4, 1),
            endDate = LocalDate.of(2024, 4, 30)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysPastExpiration(1),
            Metric.DailyBudget(10F),
            Metric.TotalBudget(300F),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget`() {
        val targetDate = LocalDate.of(2024, 7, 3)
        val monthlyBudget = Budget.Weekly(
            amount = 70F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(3),
            Metric.DaysRemaining(4),
            Metric.DailyBudget(10F),
            Metric.BudgetUntilTargetDate(30F),
            Metric.RemainingBudget(40F),
            Metric.TotalBudget(70F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget lower bound`() {
        val targetDate = LocalDate.of(2024, 7, 1)
        val monthlyBudget = Budget.Weekly(
            amount = 70F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(1),
            Metric.DaysRemaining(6),
            Metric.DailyBudget(10F),
            Metric.BudgetUntilTargetDate(10F),
            Metric.RemainingBudget(60F),
            Metric.TotalBudget(70F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget upper bound`() {
        val targetDate = LocalDate.of(2024, 7, 7)
        val monthlyBudget = Budget.Weekly(
            amount = 70F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(7),
            Metric.DaysRemaining(0),
            Metric.DailyBudget(10F),
            Metric.BudgetUntilTargetDate(70F),
            Metric.RemainingBudget(0F),
            Metric.TotalBudget(70F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for Weekly Budget exceeding upper bound`() {
        val targetDate = LocalDate.of(2024, 7, 14)
        val monthlyBudget = Budget.Weekly(
            amount = 70F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 7, 1),
            endDate = LocalDate.of(2024, 7, 7)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysPastExpiration(7),
            Metric.DailyBudget(10F),
            Metric.TotalBudget(70F),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget`() {
        val targetDate = LocalDate.of(2024, 7, 3)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(185),
            Metric.DaysRemaining(181),
            Metric.DailyBudget(1F),
            Metric.BudgetUntilTargetDate(185F),
            Metric.RemainingBudget(181F),
            Metric.TotalBudget(366F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget lower bound`() {
        val targetDate = LocalDate.of(2024, 1, 1)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(1),
            Metric.DaysRemaining(365),
            Metric.DailyBudget(1F),
            Metric.BudgetUntilTargetDate(1F),
            Metric.RemainingBudget(365F),
            Metric.TotalBudget(366F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget upper bound`() {
        val targetDate = LocalDate.of(2024, 12, 31)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysSinceStart(366),
            Metric.DaysRemaining(0),
            Metric.DailyBudget(1F),
            Metric.BudgetUntilTargetDate(366F),
            Metric.RemainingBudget(0F),
            Metric.TotalBudget(366F),
        )

        assertThat(metrics).hasSize(6)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget exceeding lower bound`() {
        val targetDate = LocalDate.of(2023, 12, 31)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysUntilStart(1),
            Metric.DailyBudget(1F),
            Metric.TotalBudget(366F),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }

    @Test
    fun `Metrics correct for OnceOnly Budget exceeding upper bound`() {
        val targetDate = LocalDate.of(2025, 1, 10)
        val monthlyBudget = Budget.OnceOnly(
            amount = 366F,
            currency = "EUR",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31)
        )

        val (_, metrics) = monthlyBudget.extractBudgetStateWithMetrics(targetDate)

        val expectedMetrics = listOf(
            Metric.DaysPastExpiration(10),
            Metric.DailyBudget(1F),
            Metric.TotalBudget(366F),
        )

        assertThat(metrics).hasSize(3)
        assertThat(metrics).containsExactlyElementsIn(expectedMetrics)
    }
}
