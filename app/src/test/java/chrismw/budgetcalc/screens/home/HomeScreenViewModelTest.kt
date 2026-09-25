package chrismw.budgetcalc.screens.home

import app.cash.turbine.test
import chrismw.budgetcalc.TestCoroutineRule
import chrismw.budgetcalc.data.budget.BudgetDataRepository
import chrismw.budgetcalc.data.currency.CurrencyRepository
import chrismw.budgetcalc.data.repository.FakeBudgetDataRepository
import chrismw.budgetcalc.data.repository.FakeCurrencyRepository
import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetState
import chrismw.budgetcalc.helpers.BudgetType
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Test class for [HomeScreenViewModel]
 */
class HomeScreenViewModelTest {

    private lateinit var budgetDataRepository: BudgetDataRepository
    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var viewModel: HomeScreenViewModel

    private var nowDate: LocalDate = TEST_DATE
    private var nowDateTime: LocalDateTime = TEST_DATE_TIME

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    companion object {

        val TEST_DATE: LocalDate = LocalDate.of(2024, 4, 1)
        val TEST_DATE_TIME: LocalDateTime = LocalDateTime.of(2024, 4, 1, 12, 0)
        val CONSTANT_MONTHLY_BUDGET = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300.0,
            currencyCode = "EUR",
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
        currencyRepository = FakeCurrencyRepository()

        viewModel = HomeScreenViewModel(
            budgetDataRepository = budgetDataRepository,
            currencyRepository = currencyRepository,
            nowDateProvider = { nowDate },
        )
    }

    private suspend fun createValidUIState() {
        budgetDataRepository.saveBudgetData(CONSTANT_MONTHLY_BUDGET)
    }

    @Test
    fun `ViewModel starts with empty ViewState`() =
        runTest {
            viewModel.viewState.test {
                Truth.assertThat(awaitItem()).isEqualTo(HomeScreenViewModel.ViewState())
            }
        }

    @Test
    fun `toggleDetailsExpanded toggles isExpanded`() = runTest {
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()
            Truth.assertThat(awaitItem().isExpanded).isTrue()

            viewModel.toggleDetailsExpanded()
            Truth.assertThat(awaitItem().isExpanded).isFalse()
        }
    }

    @Test
    fun `onPickTargetDate updates targetDate`() = runTest {
        val newDate = LocalDate.of(2024, 4, 5)
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()

            Truth.assertThat(awaitItem().targetDate).isEqualTo(nowDate)

            nowDateTime = nowDateTime.plusMinutes(1)

            viewModel.onPickTargetDate(newDate)
            Truth.assertThat(awaitItem().targetDate).isEqualTo(newDate)
        }
    }

    @Test
    fun `ViewModel creates correct ViewState for constant monthly budget`() =
        runTest { //TODO: Test metrics calculation individually
            viewModel.viewState.test {
                awaitItem()

                val constantMonthlyBudget = BudgetDataDTO(
                    isBudgetConstant = true,
                    constantBudgetAmount = 300.0,
                    currencyCode = "EUR",
                    budgetType = BudgetType.Monthly,
                    defaultPaymentDayOfMonth = 1
                )
                budgetDataRepository.saveBudgetData(constantMonthlyBudget)

                val resultingViewState: HomeScreenViewModel.ViewState = awaitItem()

                Truth.assertThat(resultingViewState.isLoading).isFalse()
                Truth.assertThat(resultingViewState.hasIncompleteData).isFalse()
                Truth.assertThat(resultingViewState.datePickerMinDate).isEqualTo(TEST_DATE)
                Truth.assertThat(resultingViewState.targetDate).isEqualTo(TEST_DATE)
                Truth.assertThat(resultingViewState.datePickerMaxDate).isEqualTo(
                    TEST_DATE.plusMonths(1).minusDays(1)
                )
                Truth.assertThat(resultingViewState.remainingBudget).isEqualTo(290.0)
                Truth.assertThat(resultingViewState.remainingBudgetPercentage).isWithin(0.0001F).of(290F / 300F)
                Truth.assertThat(resultingViewState.currencySymbol).isEqualTo("€")
                Truth.assertThat(resultingViewState.metrics).isNotEmpty()//TODO: Specify the exact metrics when metrics are refactored
                Truth.assertThat(resultingViewState.isExpanded).isTrue()
            }
        }

    @Test
    fun `Target date reset to today after budget update - target date after start date, before end date`() =
        runTest {
            val constantMonthlyBudget = BudgetDataDTO(
                isBudgetConstant = true,
                constantBudgetAmount = 300.0,
                currencyCode = "EUR",
                budgetType = BudgetType.Monthly,
                defaultPaymentDayOfMonth = 25
            )

            val onceOnlyBudget = BudgetDataDTO(
                isBudgetConstant = false,
                budgetRateAmount = 10.0,
                currencyCode = "EUR",
                budgetType = BudgetType.OnceOnly,
                startDate = LocalDate.of(2024, 3, 20),
                endDate = LocalDate.of(2024, 4, 10),
            )

            viewModel.viewState.test {
                awaitItem()

                budgetDataRepository.saveBudgetData(constantMonthlyBudget)
                Truth.assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

                nowDateTime = nowDateTime.plusMinutes(1)

                val newTargetDate = nowDate.minusDays(2)
                viewModel.onPickTargetDate(newTargetDate)
                Truth.assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

                nowDateTime = nowDateTime.plusMinutes(1)

                budgetDataRepository.saveBudgetData(onceOnlyBudget)
                with(awaitItem()) {
                    Truth.assertThat(targetDate).isEqualTo(TEST_DATE)
                    Truth.assertThat(budgetState).isInstanceOf(BudgetState.Ongoing::class.java)
                }
            }
        }

    @Test
    fun `Target date reset to today after budget update - target date before start date`() =
        runTest {
            val constantMonthlyBudget = BudgetDataDTO(
                isBudgetConstant = true,
                constantBudgetAmount = 300.0,
                currencyCode = "EUR",
                budgetType = BudgetType.Monthly,
                defaultPaymentDayOfMonth = 1
            )

            val onceOnlyBudget = BudgetDataDTO(
                isBudgetConstant = false,
                budgetRateAmount = 10.0,
                currencyCode = "EUR",
                budgetType = BudgetType.OnceOnly,
                startDate = LocalDate.of(2024, 4, 10),
                endDate = LocalDate.of(2024, 4, 30),
            )

            viewModel.viewState.test {
                awaitItem()

                budgetDataRepository.saveBudgetData(constantMonthlyBudget)
                Truth.assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

                nowDateTime = nowDateTime.plusMinutes(1)

                val newTargetDate = nowDate.plusDays(2)
                viewModel.onPickTargetDate(newTargetDate)
                Truth.assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

                nowDateTime = nowDateTime.plusMinutes(1)

                budgetDataRepository.saveBudgetData(onceOnlyBudget)
                with(awaitItem()) {
                    Truth.assertThat(targetDate).isEqualTo(TEST_DATE)
                    Truth.assertThat(budgetState).isInstanceOf(BudgetState.HasNotStarted::class.java)
                }
            }
        }

    @Test
    fun `Target date reset to today after budget update - target date after end date`() = runTest {
        val constantMonthlyBudget = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300.0,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val onceOnlyBudget = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = 10.0,
            currencyCode = "EUR",
            budgetType = BudgetType.OnceOnly,
            startDate = LocalDate.of(2024, 3, 20),
            endDate = LocalDate.of(2024, 3, 30),
        )

        viewModel.viewState.test {
            awaitItem()

            budgetDataRepository.saveBudgetData(constantMonthlyBudget)
            Truth.assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

            nowDateTime = nowDateTime.plusMinutes(1)

            val newTargetDate = nowDate.plusDays(10)
            viewModel.onPickTargetDate(newTargetDate)
            Truth.assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

            nowDateTime = nowDateTime.plusMinutes(1)

            budgetDataRepository.saveBudgetData(onceOnlyBudget)
            with(awaitItem()) {
                Truth.assertThat(targetDate).isEqualTo(TEST_DATE)
                Truth.assertThat(budgetState).isInstanceOf(BudgetState.Expired::class.java)
            }
        }
    }

    @Test
    fun `Target date reset to today after budget update - target date equals end date`() = runTest {
        val constantMonthlyBudget = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300.0,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 1
        )

        val onceOnlyBudget = BudgetDataDTO(
            isBudgetConstant = false,
            budgetRateAmount = 10.0,
            currencyCode = "EUR",
            budgetType = BudgetType.OnceOnly,
            startDate = LocalDate.of(2024, 3, 20),
            endDate = LocalDate.of(2024, 4, 1),
        )

        viewModel.viewState.test {
            awaitItem()

            budgetDataRepository.saveBudgetData(constantMonthlyBudget)
            Truth.assertThat(awaitItem().targetDate).isEqualTo(TEST_DATE)

            nowDateTime = nowDateTime.plusMinutes(1)

            val newTargetDate = nowDate.plusDays(10)
            viewModel.onPickTargetDate(newTargetDate)
            Truth.assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

            nowDateTime = nowDateTime.plusMinutes(1)

            budgetDataRepository.saveBudgetData(onceOnlyBudget)
            with(awaitItem()) {
                Truth.assertThat(targetDate).isEqualTo(TEST_DATE)
                Truth.assertThat(budgetState).isInstanceOf(BudgetState.LastDay::class.java)
            }
        }
    }

    @Test
    fun `ViewModel reports incomplete data when budget data is invalid`() = runTest {
        viewModel.viewState.test {
            Truth.assertThat(awaitItem()).isEqualTo(HomeScreenViewModel.ViewState())

            with(awaitItem()) {
                Truth.assertThat(isLoading).isFalse()
                Truth.assertThat(hasIncompleteData).isTrue()
            }
        }
    }

    @Test
    fun `updateCurrentDate updates today`() = runTest {
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()
            Truth.assertThat(awaitItem().today).isEqualTo(TEST_DATE)

            nowDate = TEST_DATE.plusDays(3)
            viewModel.updateCurrentDate()
            Truth.assertThat(awaitItem().today).isEqualTo(nowDate)
        }
    }

    @Test
    fun `onResetTargetDate resets target date to today`() = runTest {
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()
            awaitItem()

            nowDateTime = nowDateTime.plusMinutes(1)
            val newTargetDate = nowDate.plusDays(3)
            viewModel.onPickTargetDate(newTargetDate)
            Truth.assertThat(awaitItem().targetDate).isEqualTo(newTargetDate)

            nowDateTime = nowDateTime.plusMinutes(1)
            viewModel.onResetTargetDate()
            Truth.assertThat(awaitItem().targetDate).isEqualTo(nowDate)
        }
    }

    @Test
    fun `onSetShowDatePicker toggles showDatePicker`() = runTest {
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()
            Truth.assertThat(awaitItem().showDatePicker).isFalse()

            viewModel.onSetShowDatePicker(true)
            Truth.assertThat(awaitItem().showDatePicker).isTrue()

            viewModel.onSetShowDatePicker(false)
            Truth.assertThat(awaitItem().showDatePicker).isFalse()
        }
    }

    @Test
    fun `showJumpToTodayButton is true only once target date diverges from today`() = runTest {
        viewModel.viewState.test {
            awaitItem()

            createValidUIState()
            Truth.assertThat(awaitItem().showJumpToTodayButton).isFalse()

            nowDateTime = nowDateTime.plusMinutes(1)
            viewModel.onPickTargetDate(nowDate.plusDays(3))
            Truth.assertThat(awaitItem().showJumpToTodayButton).isTrue()

            nowDateTime = nowDateTime.plusMinutes(1)
            viewModel.onResetTargetDate()
            Truth.assertThat(awaitItem().showJumpToTodayButton).isFalse()
        }
    }

    @Test
    fun `remainingBudgetPercentage defaults to full when total budget is zero`() = runTest {
        viewModel.viewState.test {
            awaitItem()

            val zeroBudget = BudgetDataDTO(
                isBudgetConstant = true,
                constantBudgetAmount = 0.0,
                currencyCode = "EUR",
                budgetType = BudgetType.Monthly,
                defaultPaymentDayOfMonth = 1
            )
            budgetDataRepository.saveBudgetData(zeroBudget)

            with(awaitItem()) {
                Truth.assertThat(remainingBudget).isEqualTo(0.0)
                Truth.assertThat(remainingBudgetPercentage).isEqualTo(1F)
            }
        }
    }
}