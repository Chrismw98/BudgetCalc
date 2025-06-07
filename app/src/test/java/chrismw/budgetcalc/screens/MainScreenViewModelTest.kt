package chrismw.budgetcalc.screens

import app.cash.turbine.test
import chrismw.budgetcalc.TestCoroutineRule
import chrismw.budgetcalc.data.BudgetData
import chrismw.budgetcalc.data.BudgetDataRepository
import chrismw.budgetcalc.data.repository.FakeBudgetDataRepository
import chrismw.budgetcalc.helpers.BudgetState
import chrismw.budgetcalc.helpers.BudgetType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Test class for [MainScreenViewModel]
 */
class MainScreenViewModelTest {

    private lateinit var budgetDataRepository: BudgetDataRepository
    private lateinit var viewModel: MainScreenViewModel

    private var nowDate: LocalDate = TEST_DATE
    private var nowDateTime: LocalDateTime = TEST_DATE_TIME

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    companion object {

        val TEST_DATE: LocalDate = LocalDate.of(2024, 4, 1)
        val TEST_DATE_TIME: LocalDateTime = LocalDateTime.of(2024, 4, 1, 12, 0)
        val CONSTANT_MONTHLY_BUDGET = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )
    }

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        nowDate = TEST_DATE
        nowDateTime = TEST_DATE_TIME

        budgetDataRepository = FakeBudgetDataRepository(
            nowDateProvider = { nowDate },
            nowDateTimeProvider = { nowDateTime }
        )
        viewModel = MainScreenViewModel(
            budgetDataRepository = budgetDataRepository,
            nowDateProvider = { nowDate }
        )
    }

    private suspend fun createValidUIState() {
        budgetDataRepository.saveBudgetData(CONSTANT_MONTHLY_BUDGET)
    }

    @Test
    fun `ViewModel starts with empty ViewState`() =
        runTest { //TODO: Test metrics calculation individually
            viewModel.viewState.test {
                assertThat(awaitItem()).isEqualTo(MainScreenViewModel.ViewState())
            }
        }

    @Test
    fun `toggleDetailsExpanded toggles isExpanded`() = runTest {
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()
            assertThat(awaitItem().isExpanded).isTrue()

            viewModel.toggleDetailsExpanded()
            assertThat(awaitItem().isExpanded).isFalse()
        }
    }

    @Test
    fun `onPickTargetDate updates targetDate`() = runTest {
        val newDate = LocalDate.of(2024, 4, 5)
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()

            assertThat(awaitItem().targetDate).isEqualTo(nowDate)

            nowDateTime = nowDateTime.plusMinutes(1)

            viewModel.onPickTargetDate(newDate)
            assertThat(awaitItem().targetDate).isEqualTo(newDate)
        }
    }

    @Test
    fun `ViewModel creates correct ViewState for constant monthly budget`() =
        runTest { //TODO: Test metrics calculation individually
            viewModel.viewState.test {
                awaitItem()

                val constantMonthlyBudget = BudgetData(
                    isBudgetConstant = true,
                    constantBudgetAmount = 300F,
                    currency = "EUR",
                    budgetType = BudgetType.Monthly,
                    defaultPaymentDayOfMonth = 1
                )
                budgetDataRepository.saveBudgetData(constantMonthlyBudget)

                val resultingViewState: MainScreenViewModel.ViewState = awaitItem()

                assertThat(resultingViewState.isLoading).isFalse()
                assertThat(resultingViewState.hasIncompleteData).isFalse()
                assertThat(resultingViewState.datePickerMinDate).isEqualTo(TEST_DATE)
                assertThat(resultingViewState.targetDate).isEqualTo(TEST_DATE)
                assertThat(resultingViewState.datePickerMaxDate).isEqualTo(
                    TEST_DATE.plusMonths(1).minusDays(1)
                )
                assertThat(resultingViewState.remainingBudget).isEqualTo(290F)
                assertThat(resultingViewState.remainingBudgetPercentage).isEqualTo(290F / 300F)
                assertThat(resultingViewState.currency).isEqualTo("EUR")
                assertThat(resultingViewState.metrics).isNotEmpty()//TODO: Specify the exact metrics when metrics are refactored
                assertThat(resultingViewState.isExpanded).isTrue()
            }
        }

    @Test
    fun `Target date reset to today after budget update - target date after start date, before end date`() =
        runTest {
            val constantMonthlyBudget = BudgetData(
                isBudgetConstant = true,
                constantBudgetAmount = 300F,
                currency = "EUR",
                budgetType = BudgetType.Monthly,
                defaultPaymentDayOfMonth = 25
            )

            val onceOnlyBudget = BudgetData(
                isBudgetConstant = false,
                budgetRateAmount = 10F,
                currency = "EUR",
                budgetType = BudgetType.OnceOnly,
                defaultStartDate = LocalDate.of(2024, 3, 20).toString(),
                defaultEndDate = LocalDate.of(2024, 4, 10).toString()
            )

            viewModel.viewState.test {
                awaitItem()

                budgetDataRepository.saveBudgetData(constantMonthlyBudget)
                assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

                nowDateTime = nowDateTime.plusMinutes(1)

                val newTargetDate = nowDate.minusDays(2)
                viewModel.onPickTargetDate(newTargetDate)
                assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

                nowDateTime = nowDateTime.plusMinutes(1)

                budgetDataRepository.saveBudgetData(onceOnlyBudget)
                with(awaitItem()) {
                    assertThat(targetDate).isEqualTo(TEST_DATE)
                    assertThat(budgetState).isInstanceOf(BudgetState.Ongoing::class.java)
                }
            }
        }

    @Test
    fun `Target date reset to today after budget update - target date before start date`() =
        runTest {
            val constantMonthlyBudget = BudgetData(
                isBudgetConstant = true,
                constantBudgetAmount = 300F,
                currency = "EUR",
                budgetType = BudgetType.Monthly,
                defaultPaymentDayOfMonth = 1
            )

            val onceOnlyBudget = BudgetData(
                isBudgetConstant = false,
                budgetRateAmount = 10F,
                currency = "EUR",
                budgetType = BudgetType.OnceOnly,
                defaultStartDate = LocalDate.of(2024, 4, 10).toString(),
                defaultEndDate = LocalDate.of(2024, 4, 30).toString()
            )

            viewModel.viewState.test {
                awaitItem()

                budgetDataRepository.saveBudgetData(constantMonthlyBudget)
                assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

                nowDateTime = nowDateTime.plusMinutes(1)

                val newTargetDate = nowDate.plusDays(2)
                viewModel.onPickTargetDate(newTargetDate)
                assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

                nowDateTime = nowDateTime.plusMinutes(1)

                budgetDataRepository.saveBudgetData(onceOnlyBudget)
                with(awaitItem()) {
                    assertThat(targetDate).isEqualTo(TEST_DATE)
                    assertThat(budgetState).isInstanceOf(BudgetState.HasNotStarted::class.java)
                }
            }
        }

    @Test
    fun `Target date reset to today after budget update - target date after end date`() = runTest {
        val constantMonthlyBudget = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val onceOnlyBudget = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currency = "EUR",
            budgetType = BudgetType.OnceOnly,
            defaultStartDate = LocalDate.of(2024, 3, 20).toString(),
            defaultEndDate = LocalDate.of(2024, 3, 30).toString()
        )

        viewModel.viewState.test {
            awaitItem()

            budgetDataRepository.saveBudgetData(constantMonthlyBudget)
            assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

            nowDateTime = nowDateTime.plusMinutes(1)

            val newTargetDate = nowDate.plusDays(10)
            viewModel.onPickTargetDate(newTargetDate)
            assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

            nowDateTime = nowDateTime.plusMinutes(1)

            budgetDataRepository.saveBudgetData(onceOnlyBudget)
            with(awaitItem()) {
                assertThat(targetDate).isEqualTo(TEST_DATE)
                assertThat(budgetState).isInstanceOf(BudgetState.Expired::class.java)
            }
        }
    }

    @Test
    fun `Target date reset to today after budget update - target date equals end date`() = runTest {
        val constantMonthlyBudget = BudgetData(
            isBudgetConstant = true,
            constantBudgetAmount = 300F,
            currency = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val onceOnlyBudget = BudgetData(
            isBudgetConstant = false,
            budgetRateAmount = 10F,
            currency = "EUR",
            budgetType = BudgetType.OnceOnly,
            defaultStartDate = LocalDate.of(2024, 3, 20).toString(),
            defaultEndDate = LocalDate.of(2024, 4, 1).toString()
        )

        viewModel.viewState.test {
            awaitItem()

            budgetDataRepository.saveBudgetData(constantMonthlyBudget)
            assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

            nowDateTime = nowDateTime.plusMinutes(1)

            val newTargetDate = nowDate.plusDays(10)
            viewModel.onPickTargetDate(newTargetDate)
            assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

            nowDateTime = nowDateTime.plusMinutes(1)

            budgetDataRepository.saveBudgetData(onceOnlyBudget)
            with(awaitItem()) {
                assertThat(targetDate).isEqualTo(TEST_DATE)
                assertThat(budgetState).isInstanceOf(BudgetState.LastDay::class.java)
            }
        }
    }
}
