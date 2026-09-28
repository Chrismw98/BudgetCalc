package chrismw.budgetcalc.data.budget

import chrismw.budgetcalc.data.DateWithTimestamp
import chrismw.budgetcalc.helpers.BudgetDataDTO
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Provider

/**
 * Test class for [BudgetDataRepositoryImpl].
 */
class BudgetDataRepositoryImplTest {

    private val initialDate = LocalDate.of(2024, 4, 1)
    private val initialDateTime = LocalDateTime.of(2024, 4, 1, 12, 0)

    private lateinit var budgetLocalDataSource: BudgetLocalDataSource
    private lateinit var nowDateProvider: Provider<LocalDate>
    private lateinit var nowDateTimeProvider: Provider<LocalDateTime>

    @Before
    fun setUp() {
        budgetLocalDataSource = mockk {
            // BudgetDataRepositoryImpl reads this eagerly during construction.
            every { budgetDataFlow } returns flowOf(BudgetDataPreferences.getDefaultInstance())
        }
        nowDateProvider = mockk { every { get() } returns initialDate }
        nowDateTimeProvider = mockk { every { get() } returns initialDateTime }
    }

    private fun repository() = BudgetDataRepositoryImpl(
        budgetLocalDataSource = budgetLocalDataSource,
        nowDateProvider = nowDateProvider,
        nowDateTimeProvider = nowDateTimeProvider,
    )

    @Test
    fun `targetDateFlow starts out at the current date and time`() = runTest {
        val targetDate = repository().targetDateFlow.first()

        assertThat(targetDate).isEqualTo(
            DateWithTimestamp(
                date = initialDate,
                createdAt = initialDateTime
            )
        )
    }

    @Test
    fun `setTargetDate updates targetDateFlow with the new date and the current time`() = runTest {
        val repository = repository()
        val newDate = LocalDate.of(2024, 5, 10)
        val newDateTime = LocalDateTime.of(2024, 5, 10, 8, 30)
        every { nowDateTimeProvider.get() } returns newDateTime

        repository.setTargetDate(newDate)

        assertThat(repository.targetDateFlow.first())
            .isEqualTo(DateWithTimestamp(date = newDate, createdAt = newDateTime))
    }

    @Test
    fun `budgetDataFlow maps the local data source's preferences to a DTO`() = runTest {
        val prefs = BudgetDataPreferences.newBuilder().setCurrencyCode("EUR").build()
        every { budgetLocalDataSource.budgetDataFlow } returns flowOf(prefs)

        val result = repository().budgetDataFlow.first()

        assertThat(result).isEqualTo(prefs.toBudgetDataDTO())
    }

    @Test
    fun `observeBudgetDataWithNowDate pairs the budget data with the current date`() = runTest {
        val prefs = BudgetDataPreferences.newBuilder().setCurrencyCode("EUR").build()
        every { budgetLocalDataSource.budgetDataFlow } returns flowOf(prefs)

        val (budgetData, dateWithTimestamp) = repository().observeBudgetDataWithNowDate().first()

        assertThat(budgetData).isEqualTo(prefs.toBudgetDataDTO())
        assertThat(dateWithTimestamp).isEqualTo(
            DateWithTimestamp(
                date = initialDate,
                createdAt = initialDateTime
            )
        )
    }

    @Test
    fun `getBudgetData delegates to the local data source and maps the result`() = runTest {
        val prefs = BudgetDataPreferences.newBuilder().setCurrencyCode("EUR").build()
        coEvery { budgetLocalDataSource.getBudgetData() } returns prefs

        val result = repository().getBudgetData()

        assertThat(result).isEqualTo(prefs.toBudgetDataDTO())
    }

    @Test
    fun `saveBudgetData maps the DTO and delegates to the local data source`() = runTest {
        coEvery { budgetLocalDataSource.setBudgetData(any()) } returns Unit
        val dto = BudgetDataDTO(currencyCode = "EUR")

        repository().saveBudgetData(dto)

        coVerify(exactly = 1) { budgetLocalDataSource.setBudgetData(dto.toBudgetDataPreferences()) }
    }
}
