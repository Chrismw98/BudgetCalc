package chrismw.budgetcalc.screens.settings

import android.text.TextUtils
import app.cash.turbine.test
import chrismw.budgetcalc.TestCoroutineRule
import chrismw.budgetcalc.data.budget.BudgetDataRepository
import chrismw.budgetcalc.data.currency.Currency
import chrismw.budgetcalc.data.currency.CurrencyRepository
import chrismw.budgetcalc.data.repository.FakeBudgetDataRepository
import chrismw.budgetcalc.data.repository.FakeCurrencyRepository
import chrismw.budgetcalc.helpers.BudgetDataDTO
import chrismw.budgetcalc.helpers.BudgetType
import chrismw.budgetcalc.helpers.DropDown
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Test class for [SettingsViewModel]: loading existing data, the individual field setters, and
 * the error/validation states surfaced through [SettingsViewModel.ViewState.errors].
 */
class SettingsViewModelTest {

    private lateinit var budgetDataRepository: BudgetDataRepository
    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var viewModel: SettingsViewModel

    private var nowDate: LocalDate = TEST_DATE

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    companion object {

        val TEST_DATE: LocalDate = LocalDate.of(2026, 1, 26)
        val TEST_DATE_TIME: LocalDateTime = LocalDateTime.of(2026, 1, 26, 9, 30)

        val EUR = Currency(code = "EUR", name = "Euro", symbol = "€")

        // Fully empty budget data, as it would be for a brand-new user who never configured a budget.
        val EMPTY_BUDGET_DATA = BudgetDataDTO(
            isBudgetConstant = null,
            budgetType = null,
        )

        val MONTHLY_BUDGET_DATA = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = 300.0,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = 15,
        )
    }

    @Before
    fun setUp() {
        nowDate = TEST_DATE

        budgetDataRepository = FakeBudgetDataRepository(
            nowDateProvider = { nowDate },
            nowDateTimeProvider = { TEST_DATE_TIME },
        )
        currencyRepository = FakeCurrencyRepository()

        // TextUtils isn't available on the JVM unit test runtime, but the ViewModel relies on
        // androidx.core.text.isDigitsOnly(), which delegates to it.
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers {
            (firstArg<CharSequence>()).all { it.isDigit() }
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(TextUtils::class)
    }

    private suspend fun TestScope.createViewModel(initialData: BudgetDataDTO = EMPTY_BUDGET_DATA) {
        budgetDataRepository.saveBudgetData(initialData)
        viewModel = SettingsViewModel(
            budgetDataRepository = budgetDataRepository,
            currencyRepository = currencyRepository,
            nowDateProvider = { nowDate },
        )
        // Let the ViewModel's one-shot initial load complete before the test starts mutating state.
        advanceUntilIdle()
    }

    @Test
    fun `ViewModel starts with default ViewState`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            assertThat(awaitItem()).isEqualTo(SettingsViewModel.ViewState())
        }
    }

    @Test
    fun `viewState reports no errors before a save is attempted, even when nothing has been filled in`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem() // Default seed value.

            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
            assertThat(loadedState.errors.hasAnyError).isFalse()
        }
    }

    @Test
    fun `onSaveClicked reports definition, details and period sections as required when nothing has been filled in`() =
        runTest {
            createViewModel()

            val errors = viewModel.onSaveClicked()

            assertThat(errors.isBudgetMethodMissing).isTrue()
            assertThat(errors.isBudgetDetailsMissing).isTrue()
            assertThat(errors.isCurrencyMissing).isFalse()
            assertThat(errors.isBudgetAmountMissing).isFalse()
            assertThat(errors.isBudgetPeriodMissing).isTrue()
            assertThat(errors.hasAnyError).isTrue()
        }

    @Test
    fun `onSaveClicked reports currency and amount as individually required once a budget method is chosen`() =
        runTest {
            createViewModel()

            viewModel.setIsBudgetConstant(true)

            val errors = viewModel.onSaveClicked()

            assertThat(errors.isBudgetMethodMissing).isFalse()
            assertThat(errors.isBudgetDetailsMissing).isFalse()
            assertThat(errors.isCurrencyMissing).isTrue()
            assertThat(errors.isBudgetAmountMissing).isTrue()
        }

    @Test
    fun `onSaveClicked does not report amount as missing once it has been entered`() = runTest {
        createViewModel()

        viewModel.setIsBudgetConstant(true)
        viewModel.setCurrency(EUR)
        viewModel.setConstantBudgetAmount("30000")

        val errors = viewModel.onSaveClicked()

        assertThat(errors.isCurrencyMissing).isFalse()
        assertThat(errors.isBudgetAmountMissing).isFalse()
    }

    @Test
    fun `onSaveClicked reports payment day of month as required for a monthly budget`() = runTest {
        createViewModel()

        viewModel.setBudgetType(BudgetType.Monthly)

        val errors = viewModel.onSaveClicked()

        assertThat(errors.isBudgetPeriodMissing).isFalse()
        assertThat(errors.isPaymentDayOfMonthMissing).isTrue()
        assertThat(errors.isPaymentDayOfWeekMissing).isFalse()
        assertThat(errors.isStartDateMissing).isFalse()
        assertThat(errors.isEndDateMissing).isFalse()

        viewModel.setDefaultPaymentDayOfMonth("15")

        val updatedErrors = viewModel.onSaveClicked()
        assertThat(updatedErrors.isPaymentDayOfMonthMissing).isFalse()
    }

    @Test
    fun `onSaveClicked reports payment day of week as required for a weekly budget`() = runTest {
        createViewModel()

        viewModel.setBudgetType(BudgetType.Weekly)

        val errors = viewModel.onSaveClicked()

        assertThat(errors.isBudgetPeriodMissing).isFalse()
        assertThat(errors.isPaymentDayOfWeekMissing).isTrue()

        viewModel.setDefaultPaymentDayOfWeek(DayOfWeek.MONDAY)

        val updatedErrors = viewModel.onSaveClicked()
        assertThat(updatedErrors.isPaymentDayOfWeekMissing).isFalse()
    }

    @Test
    fun `onSaveClicked reports start and end date as required for a once-only budget`() = runTest {
        createViewModel()

        viewModel.setBudgetType(BudgetType.OnceOnly)

        val errors = viewModel.onSaveClicked()

        assertThat(errors.isBudgetPeriodMissing).isFalse()
        assertThat(errors.isStartDateMissing).isTrue()
        assertThat(errors.isEndDateMissing).isTrue()

        viewModel.setStartDate(TEST_DATE)

        val errorsWithStartDateOnly = viewModel.onSaveClicked()
        assertThat(errorsWithStartDateOnly.isStartDateMissing).isFalse()
        assertThat(errorsWithStartDateOnly.isEndDateMissing).isTrue()

        viewModel.setEndDate(TEST_DATE.plusDays(4))

        val errorsFullyFilled = viewModel.onSaveClicked()
        assertThat(errorsFullyFilled.isStartDateMissing).isFalse()
        assertThat(errorsFullyFilled.isEndDateMissing).isFalse()
    }

    @Test
    fun `onSaveClicked returns no errors and persists the data when a monthly budget is fully filled in`() =
        runTest {
            createViewModel()

            viewModel.setIsBudgetConstant(true)
            viewModel.setCurrency(EUR)
            viewModel.setConstantBudgetAmount("30000")
            viewModel.setBudgetType(BudgetType.Monthly)
            viewModel.setDefaultPaymentDayOfMonth("15")

            val errors = viewModel.onSaveClicked()
            assertThat(errors.hasAnyError).isFalse()

            advanceUntilIdle()

            val persisted = budgetDataRepository.getBudgetData()
            assertThat(persisted.isBudgetConstant).isTrue()
            assertThat(persisted.currencyCode).isEqualTo("EUR")
            assertThat(persisted.constantBudgetAmount).isEqualTo(300.0)
            assertThat(persisted.budgetType).isEqualTo(BudgetType.Monthly)
            assertThat(persisted.defaultPaymentDayOfMonth).isEqualTo(15)
        }

    @Test
    fun `onSaveClicked does not persist data when errors are present`() = runTest {
        createViewModel()

        viewModel.setIsBudgetConstant(true)
        // Currency and amount are left empty on purpose.

        val errors = viewModel.onSaveClicked()
        assertThat(errors.hasAnyError).isTrue()

        advanceUntilIdle()

        val persisted = budgetDataRepository.getBudgetData()
        assertThat(persisted).isEqualTo(EMPTY_BUDGET_DATA)
    }

    @Test
    fun `a newly revealed field does not show an error until it has been checked by a save attempt`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem() // Default seed value.
            runCurrent()
            expectMostRecentItem() // Loaded state, before any save attempt.

            viewModel.onSaveClicked() // Nothing filled in yet - budgetType is still null.
            runCurrent()
            expectMostRecentItem()

            viewModel.setBudgetType(BudgetType.Monthly)
            runCurrent()
            val afterPickingMonthly = expectMostRecentItem()
            assertThat(afterPickingMonthly.errors.isBudgetPeriodMissing).isFalse()
            // The day-of-month field just appeared and hasn't been checked by a save attempt yet.
            assertThat(afterPickingMonthly.errors.isPaymentDayOfMonthMissing).isFalse()

            viewModel.onSaveClicked() // This attempt actually checks the day-of-month field.
            runCurrent()
            val afterSecondSave = expectMostRecentItem()
            assertThat(afterSecondSave.errors.isPaymentDayOfMonthMissing).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a field already checked by a save attempt keeps updating live, without requiring another attempt`() =
        runTest {
            createViewModel()

            viewModel.viewState.test {
                awaitItem() // Default seed value.
                runCurrent()
                expectMostRecentItem() // Loaded state, before any save attempt.

                viewModel.setIsBudgetConstant(true)
                viewModel.setCurrency(EUR)
                viewModel.setConstantBudgetAmount("30000")

                viewModel.onSaveClicked() // Checks the details section; period section is still missing.
                runCurrent()
                val afterSave = expectMostRecentItem()
                assertThat(afterSave.errors.isCurrencyMissing).isFalse()
                assertThat(afterSave.errors.isBudgetAmountMissing).isFalse()

                viewModel.setConstantBudgetAmount("")
                runCurrent()
                val afterAmountCleared = expectMostRecentItem()
                assertThat(afterAmountCleared.errors.isBudgetAmountMissing).isTrue()

                viewModel.setConstantBudgetAmount("30000")
                runCurrent()
                val afterAmountReEntered = expectMostRecentItem()
                assertThat(afterAmountReEntered.errors.isBudgetAmountMissing).isFalse()

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadSettings populates the view state from previously saved data`() = runTest {
        createViewModel(MONTHLY_BUDGET_DATA)

        viewModel.viewState.test {
            awaitItem() // Default seed value.
            runCurrent()
            val loadedState = expectMostRecentItem()

            assertThat(loadedState.isBudgetConstant).isTrue()
            assertThat(loadedState.selectedCurrency).isEqualTo(EUR)
            assertThat(loadedState.constantBudgetAmount).isEqualTo("30000")
            assertThat(loadedState.budgetType).isEqualTo(BudgetType.Monthly)
            assertThat(loadedState.defaultPaymentDayOfMonth).isEqualTo("15")
            assertThat(loadedState.showConfirmExitDialog).isFalse()
            assertThat(loadedState.errors.hasAnyError).isFalse()
        }
    }

    @Test
    fun `viewState exposes the available currencies from the currency repository`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            val loadedState = expectMostRecentItem()

            assertThat(loadedState.availableCurrencies).containsExactly(
                EUR,
                Currency(code = "USD", name = "US Dollar", symbol = "$"),
                Currency(code = "JPY", name = "Japanese Yen", symbol = "¥"),
            )
        }
    }

    @Test
    fun `loadSettings handles inconsistent persisted data without crashing`() = runTest {
        // A method and period were chosen at some point, but the amount and payment day were
        // never actually persisted - e.g. leftover data from an older app version.
        val inconsistentData = BudgetDataDTO(
            isBudgetConstant = true,
            constantBudgetAmount = null,
            currencyCode = "EUR",
            budgetType = BudgetType.Monthly,
            defaultPaymentDayOfMonth = null,
        )

        createViewModel(inconsistentData)

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            val loadedState = expectMostRecentItem()

            assertThat(loadedState.isBudgetConstant).isTrue()
            assertThat(loadedState.constantBudgetAmount).isNull()
            assertThat(loadedState.budgetType).isEqualTo(BudgetType.Monthly)
            assertThat(loadedState.defaultPaymentDayOfMonth).isNull()
            assertThat(loadedState.errors.hasAnyError).isFalse() // Nothing checked yet.
        }

        val errors = viewModel.onSaveClicked()
        assertThat(errors.isBudgetAmountMissing).isTrue()
        assertThat(errors.isPaymentDayOfMonthMissing).isTrue()
    }

    @Test
    fun `setConstantBudgetAmount ignores non-digit input`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            viewModel.setConstantBudgetAmount("12a3")
            runCurrent()
            // Rejected input causes no state change, so there's nothing new to await here.
            assertThat(viewModel.viewState.value.constantBudgetAmount).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setConstantBudgetAmount trims leading zeros`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            viewModel.setConstantBudgetAmount("00500")
            runCurrent()
            assertThat(expectMostRecentItem().constantBudgetAmount).isEqualTo("500")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setConstantBudgetAmount ignores input longer than the max allowed digits`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            // MAX_DIGITS_FOR_CONSTANT_BUDGET_AMOUNT is 15, so 16 digits should be rejected.
            viewModel.setConstantBudgetAmount("1234567890123456")
            runCurrent()
            assertThat(viewModel.viewState.value.constantBudgetAmount).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setBudgetRateAmount ignores non-digit input`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            viewModel.setBudgetRateAmount("12a3")
            runCurrent()
            // Rejected input causes no state change, so there's nothing new to await here.
            assertThat(viewModel.viewState.value.budgetRateAmount).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setBudgetRateAmount ignores input longer than the max allowed digits`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            // MAX_DIGITS_FOR_DAILY_BUDGET_RATE is 14, so 15 digits should be rejected.
            viewModel.setBudgetRateAmount("123456789012345")
            runCurrent()
            assertThat(viewModel.viewState.value.budgetRateAmount).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setDefaultPaymentDayOfMonth ignores non-digit input`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            viewModel.setDefaultPaymentDayOfMonth("1a")
            runCurrent()
            assertThat(viewModel.viewState.value.defaultPaymentDayOfMonth).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setDefaultPaymentDayOfMonth accepts a value outside 1 to 31, deferring range validation to the error state`() =
        runTest {
            createViewModel()

            viewModel.viewState.test {
                awaitItem()
                runCurrent()
                expectMostRecentItem()

                viewModel.setDefaultPaymentDayOfMonth("35")
                runCurrent()
                assertThat(expectMostRecentItem().defaultPaymentDayOfMonth).isEqualTo("35")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSaveClicked reports the payment day of month as invalid when it is outside 1 to 31`() = runTest {
        createViewModel()

        viewModel.setBudgetType(BudgetType.Monthly)
        viewModel.setDefaultPaymentDayOfMonth("35")

        val errors = viewModel.onSaveClicked()

        assertThat(errors.isPaymentDayOfMonthMissing).isFalse()
        assertThat(errors.isPaymentDayOfMonthInvalid).isTrue()

        viewModel.setDefaultPaymentDayOfMonth("15")

        val updatedErrors = viewModel.onSaveClicked()
        assertThat(updatedErrors.isPaymentDayOfMonthInvalid).isFalse()
    }

    @Test
    fun `onSaveClicked accepts the boundary values 1 and 31 as a valid payment day of month`() = runTest {
        createViewModel()

        viewModel.setBudgetType(BudgetType.Monthly)
        viewModel.setDefaultPaymentDayOfMonth(SettingsViewModel.MIN_PAYMENT_DAY_OF_MONTH.toString())

        val errorsAtMin = viewModel.onSaveClicked()
        assertThat(errorsAtMin.isPaymentDayOfMonthMissing).isFalse()
        assertThat(errorsAtMin.isPaymentDayOfMonthInvalid).isFalse()

        viewModel.setDefaultPaymentDayOfMonth(SettingsViewModel.MAX_PAYMENT_DAY_OF_MONTH.toString())

        val errorsAtMax = viewModel.onSaveClicked()
        assertThat(errorsAtMax.isPaymentDayOfMonthMissing).isFalse()
        assertThat(errorsAtMax.isPaymentDayOfMonthInvalid).isFalse()
    }

    @Test
    fun `onSaveClicked reports an invalid payment day of month for 0, 32 and an overflowing value`() = runTest {
        createViewModel()

        viewModel.setBudgetType(BudgetType.Monthly)

        viewModel.setDefaultPaymentDayOfMonth((SettingsViewModel.MIN_PAYMENT_DAY_OF_MONTH - 1).toString())
        assertThat(viewModel.onSaveClicked().isPaymentDayOfMonthInvalid).isTrue()

        viewModel.setDefaultPaymentDayOfMonth((SettingsViewModel.MAX_PAYMENT_DAY_OF_MONTH + 1).toString())
        assertThat(viewModel.onSaveClicked().isPaymentDayOfMonthInvalid).isTrue()

        // Larger than Int.MAX_VALUE, so toIntOrNull() overflows to null and must still be treated as invalid.
        viewModel.setDefaultPaymentDayOfMonth("99999999999999999999")
        assertThat(viewModel.onSaveClicked().isPaymentDayOfMonthInvalid).isTrue()
    }

    @Test
    fun `payment day of month invalid error clears live once fixed, without requiring another save attempt`() =
        runTest {
            createViewModel()

            viewModel.viewState.test {
                awaitItem()
                runCurrent()
                expectMostRecentItem()

                viewModel.setBudgetType(BudgetType.Monthly)
                viewModel.setDefaultPaymentDayOfMonth("35")

                viewModel.onSaveClicked()
                runCurrent()
                val afterSave = expectMostRecentItem()
                assertThat(afterSave.errors.isPaymentDayOfMonthInvalid).isTrue()

                viewModel.setDefaultPaymentDayOfMonth("15")
                runCurrent()
                val afterFix = expectMostRecentItem()
                assertThat(afterFix.errors.isPaymentDayOfMonthInvalid).isFalse()
                assertThat(afterFix.errors.isPaymentDayOfMonthMissing).isFalse()

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `switching budget type clears validation errors that belonged to the previous type`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            viewModel.setBudgetType(BudgetType.Monthly)
            viewModel.setDefaultPaymentDayOfMonth("35")

            viewModel.onSaveClicked()
            runCurrent()
            val afterSave = expectMostRecentItem()
            assertThat(afterSave.errors.isPaymentDayOfMonthInvalid).isTrue()

            viewModel.setBudgetType(BudgetType.Weekly)
            runCurrent()
            val afterSwitch = expectMostRecentItem()
            assertThat(afterSwitch.errors.isPaymentDayOfMonthInvalid).isFalse()
            assertThat(afterSwitch.errors.isPaymentDayOfMonthMissing).isFalse()
            assertThat(afterSwitch.errors.isBudgetPeriodMissing).isFalse()
            // The weekly field just appeared, so it isn't flagged until checked by a save attempt.
            assertThat(afterSwitch.errors.isPaymentDayOfWeekMissing).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `basic field setters update the corresponding view state values`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            viewModel.setCurrency(EUR)
            runCurrent()
            assertThat(expectMostRecentItem().selectedCurrency).isEqualTo(EUR)

            viewModel.setDefaultPaymentDayOfWeek(DayOfWeek.MONDAY)
            runCurrent()
            assertThat(expectMostRecentItem().defaultPaymentDayOfWeek).isEqualTo(DayOfWeek.MONDAY)

            viewModel.setStartDate(TEST_DATE)
            runCurrent()
            assertThat(expectMostRecentItem().startDate).isEqualTo(TEST_DATE)

            viewModel.setEndDate(TEST_DATE.plusDays(4))
            runCurrent()
            assertThat(expectMostRecentItem().endDate).isEqualTo(TEST_DATE.plusDays(4))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateExpandedDropDown updates which dropdown is expanded`() = runTest {
        createViewModel()

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            expectMostRecentItem()

            viewModel.updateExpandedDropDown(DropDown.CURRENCY)
            runCurrent()
            assertThat(expectMostRecentItem().currentlyExpandedDropDown).isEqualTo(DropDown.CURRENCY)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showConfirmExitDialog reflects unsaved changes and clears again after a successful save`() = runTest {
        createViewModel(MONTHLY_BUDGET_DATA)

        viewModel.viewState.test {
            awaitItem()
            runCurrent()
            assertThat(expectMostRecentItem().showConfirmExitDialog).isFalse()

            viewModel.setConstantBudgetAmount("40000")
            runCurrent()
            assertThat(expectMostRecentItem().showConfirmExitDialog).isTrue()

            viewModel.onSaveClicked()
            runCurrent()
            assertThat(expectMostRecentItem().showConfirmExitDialog).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveClicked persists a weekly budget`() = runTest {
        createViewModel()

        viewModel.setIsBudgetConstant(false)
        viewModel.setCurrency(EUR)
        viewModel.setBudgetRateAmount("1000")
        viewModel.setBudgetType(BudgetType.Weekly)
        viewModel.setDefaultPaymentDayOfWeek(DayOfWeek.MONDAY)

        val errors = viewModel.onSaveClicked()
        assertThat(errors.hasAnyError).isFalse()

        advanceUntilIdle()

        val persisted = budgetDataRepository.getBudgetData()
        assertThat(persisted.isBudgetConstant).isFalse()
        assertThat(persisted.currencyCode).isEqualTo("EUR")
        assertThat(persisted.budgetRateAmount).isEqualTo(10.0)
        assertThat(persisted.budgetType).isEqualTo(BudgetType.Weekly)
        assertThat(persisted.defaultPaymentDayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    }

    @Test
    fun `onSaveClicked persists a once-only budget`() = runTest {
        createViewModel()

        viewModel.setIsBudgetConstant(true)
        viewModel.setCurrency(EUR)
        viewModel.setConstantBudgetAmount("30000")
        viewModel.setBudgetType(BudgetType.OnceOnly)
        viewModel.setStartDate(TEST_DATE)
        viewModel.setEndDate(TEST_DATE.plusDays(10))

        val errors = viewModel.onSaveClicked()
        assertThat(errors.hasAnyError).isFalse()

        advanceUntilIdle()

        val persisted = budgetDataRepository.getBudgetData()
        assertThat(persisted.budgetType).isEqualTo(BudgetType.OnceOnly)
        assertThat(persisted.startDate).isEqualTo(TEST_DATE)
        assertThat(persisted.endDate).isEqualTo(TEST_DATE.plusDays(10))
    }

}
