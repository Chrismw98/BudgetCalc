package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository
import chrismw.budgetcalc.helpers.BudgetType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Test class for [BudgetDataRepository]
 */
class BudgetDataRepositoryTest {

    private lateinit var budgetDataRepository: BudgetDataRepository

    companion object {

        val CONSTANT_MONTHLY_BUDGET = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 500F,
            currency = "EUR",
            budgetType = BudgetType.MONTHLY,
            defaultPaymentDayOfMonth = 1
        )
    }

    @Before
    fun setUp() {
        budgetDataRepository = FakeBudgetDataRepository()

        val budgetDataToSave = CONSTANT_MONTHLY_BUDGET.copy()

        runBlocking {
            budgetDataRepository.saveBudgetData(budgetDataToSave)
        }
    }

    @Test
    fun `Saved BudgetData equals retrieved BudgetData`() = runBlocking {
        val budgetData = budgetDataRepository.getBudgetData()
        assertThat(budgetData).isEqualTo(CONSTANT_MONTHLY_BUDGET)
    }
}