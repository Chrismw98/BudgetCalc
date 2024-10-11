package chrismw.budgetcalc.data

import chrismw.budgetcalc.extensions.extractMetrics
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(10)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(20)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(100F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(200F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(300F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(1)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(29)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(10F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(290F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(300F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(30)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(0)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(300F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(0F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(300F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(31)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(-1)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(300F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(0F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(300F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(3)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(4)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(30F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(40F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(70F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(1)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(6)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(10F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(60F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(70F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(7)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(0)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(70F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(0F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(70F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(14)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(-7)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(10F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(70F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(0F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(70F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(185)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(181)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(1F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(185F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(181F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(366F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(1)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(365)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(1F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(1F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(365F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(366F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(366)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(0)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(1F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(366F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(0F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(366F)
            }
        }
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

        val metrics = monthlyBudget.extractMetrics(targetDate)

        assertThat(metrics).isNotEmpty()
        metrics.forEach {
            when (it) {
                is Metric.DaysSinceStart -> assertThat(it.value).isEqualTo(376)
                is Metric.DaysRemaining -> assertThat(it.value).isEqualTo(-10)
                is Metric.DailyBudget -> assertThat(it.value).isEqualTo(1F)
                is Metric.BudgetUntilTargetDate -> assertThat(it.value).isEqualTo(366F)
                is Metric.RemainingBudget -> assertThat(it.value).isEqualTo(0F)
                is Metric.TotalBudget -> assertThat(it.value).isEqualTo(366F)
            }
        }
    }
}
