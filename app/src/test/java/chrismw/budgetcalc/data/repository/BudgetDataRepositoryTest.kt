package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.data.budget.BudgetDataRepository
import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Test class for [BudgetDataRepository]
 */
class BudgetDataRepositoryTest {

    private lateinit var budgetDataRepository: BudgetDataRepository

    companion object {

        val EMPTY_MONTHLY_BUDGET = BudgetDataDTO()

        val CONSTANT_MONTHLY_BUDGET = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 500F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )
    }

    @Before
    fun setUp() {
        budgetDataRepository = FakeBudgetDataRepository(
            { LocalDate.of(2024, 4, 1) },
            { LocalDateTime.of(2024, 4, 1, 12, 0) },
        )
    }

    @Test
    fun `Default BudgetDataDTO equals empty BudgetDataDTO`() = runTest {
        val budgetData = budgetDataRepository.getBudgetData()
        assertThat(budgetData).isEqualTo(EMPTY_MONTHLY_BUDGET)
    }

//    @Test
//    fun `Saved BudgetDataDTO equals observed BudgetDataDTO`() = runTest {
//        budgetDataRepository.observeBudgetData().test {
//            assertThat(awaitItem()).isEqualTo(EMPTY_MONTHLY_BUDGET)
//
//            budgetDataRepository.saveBudgetData(CONSTANT_MONTHLY_BUDGET.copy())
//
//            assertThat(awaitItem()).isEqualTo(CONSTANT_MONTHLY_BUDGET)
//        }
//    }
}