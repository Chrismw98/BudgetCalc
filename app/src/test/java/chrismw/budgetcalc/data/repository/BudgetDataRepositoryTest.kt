package chrismw.budgetcalc.data.repository

import app.cash.turbine.test
import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository
import chrismw.budgetcalc.helpers.BudgetType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Test class for [BudgetDataRepository]
 */
class BudgetDataRepositoryTest {

    private lateinit var budgetDataRepository: BudgetDataRepository

    companion object {

        val EMPTY_MONTHLY_BUDGET = BudgetData()

        val CONSTANT_MONTHLY_BUDGET = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 500F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )
    }

    @Before
    fun setUp() {
        budgetDataRepository = FakeBudgetDataRepository()
    }

    @Test
    fun `Default BudgetData equals empty BudgetData`() = runTest {
        val budgetData = budgetDataRepository.getBudgetData()
        assertThat(budgetData).isEqualTo(EMPTY_MONTHLY_BUDGET)
    }

    @Test
    fun `Saved BudgetData equals observed BudgetData`() = runTest {
        budgetDataRepository.observeBudgetData().test {
            assertThat(awaitItem()).isEqualTo(EMPTY_MONTHLY_BUDGET)

            budgetDataRepository.saveBudgetData(CONSTANT_MONTHLY_BUDGET.copy())

            assertThat(awaitItem()).isEqualTo(CONSTANT_MONTHLY_BUDGET)
        }
    }
}