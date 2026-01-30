//package chrismw.budgetcalc.screens
//
//import chrismw.budgetcalc.TestCoroutineRule
//import chrismw.budgetcalc.data.budget.BudgetDataRepository
//import chrismw.budgetcalc.helpers.BudgetDataDTO
//import chrismw.budgetcalc.helpers.BudgetType
//import io.mockk.MockKAnnotations
//import io.mockk.every
//import io.mockk.impl.annotations.RelaxedMockK
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.flowOf
//import org.junit.Before
//import org.junit.Rule
//import java.time.LocalDate
//import javax.inject.Provider
//
///**
// * Test class for [MainScreenViewModel]
// */
//class MainScreenViewModelTest2 {
//
//    private lateinit var budgetDataRepository: BudgetDataRepository
//    private lateinit var viewModel: MainScreenViewModel
//
//    @RelaxedMockK
//    private lateinit var nowDateProviderMock: Provider<LocalDate>
//
//    @RelaxedMockK
//    private lateinit var budgetDataRepositoryMock: BudgetDataRepository
//
//    @get:Rule
//    val coroutineRule = TestCoroutineRule()
//
//    companion object {
//
//        val TEST_DATE: LocalDate = LocalDate.of(2024, 4, 1)
//        val CONSTANT_MONTHLY_BUDGET = BudgetDataDTO(
//            isBudgetConstant = true,
//            constantBudgetAmount = 300F,
//            currencyCode = "EUR",
//            budgetType = BudgetType.Monthly,
//            defaultPaymentDayOfMonth = 1
//        )
//    }
//
//    @ExperimentalCoroutinesApi
//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this)
//
////        budgetDataRepository = FakeBudgetDataRepository()
//        viewModel = MainScreenViewModel(
//            budgetDataRepository = budgetDataRepositoryMock,
//            nowDateProvider = nowDateProviderMock
//        )
//    }
//
//    private suspend fun createValidUIState() {
////        budgetDataRepository.saveBudgetData(CONSTANT_MONTHLY_BUDGET)
//        every { budgetDataRepositoryMock.budgetDataFlow } returns flowOf(CONSTANT_MONTHLY_BUDGET)
//    }
//
////    @Test
////    fun `toggleDetailsExpanded toggles isExpanded`() = runTest {
////        viewModel.viewState.test {
//////            awaitItem()
////            createValidUIState()
////
////            assertThat(awaitItem().isExpanded).isTrue()
////            viewModel.toggleDetailsExpanded()
////            assertThat(awaitItem().isExpanded).isFalse()
////        }
////    }
//
////    @Test
////    fun `Target date reset to today after budget update`() = runTest {
////        val today = LocalDate.of(2024, 4, 1)
////        val startDate = LocalDate.of(2024, 4, 1)
////        val endDate = LocalDate.of(2024, 4, 30)
////        every { nowDateProviderMock.get() } returns today
////
////        val constantMonthlyBudget = BudgetDataDTO(
////            isBudgetConstant = true,
////            constantBudgetAmount = 300F,
////            currency = "EUR",
////            budgetType = BudgetType.Monthly,
////            defaultPaymentDayOfMonth = 1
////        )
////
////        val onceOnlyBudget = BudgetDataDTO(
////            isBudgetConstant = false,
////            budgetRateAmount = 100F,
////            currency = "EUR",
////            budgetType = BudgetType.OnceOnly,
////            startDate = startDate,
////            endDate = endDate,
////        )
////
////        viewModel.viewState.test {
////            awaitItem()
////
////            budgetDataRepository.saveBudgetData(constantMonthlyBudget)
////            assertThat(awaitItem().targetDate).isEqualTo(today)
////
////            budgetDataRepository.saveBudgetData(onceOnlyBudget)
////            assertThat(awaitItem().targetDate).isEqualTo(today)
////        }
////    }
//}
