package chrismw.budgetcalc.data.budget

import androidx.datastore.core.DataStore
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test class for [BudgetLocalDataSource].
 */
class BudgetLocalDataSourceTest {

    private val prefs: BudgetDataPreferences = BudgetDataPreferences.newBuilder()
        .setCurrencyCode("EUR")
        .build()

    @Test
    fun `budgetDataFlow forwards the DataStore's data flow`() = runTest {
        val dataStore = mockk<DataStore<BudgetDataPreferences>> {
            every { data } returns flowOf(prefs)
        }

        val result = BudgetLocalDataSource(dataStore).budgetDataFlow.first()

        assertThat(result).isEqualTo(prefs)
    }

    @Test
    fun `getBudgetData returns the first emission from the DataStore`() = runTest {
        val dataStore = mockk<DataStore<BudgetDataPreferences>> {
            every { data } returns flowOf(prefs)
        }

        val result = BudgetLocalDataSource(dataStore).getBudgetData()

        assertThat(result).isEqualTo(prefs)
    }

    @Test
    fun `setBudgetData updates the DataStore with the new value`() = runTest {
        val transformSlot = slot<suspend (BudgetDataPreferences) -> BudgetDataPreferences>()
        val dataStore = mockk<DataStore<BudgetDataPreferences>> {
            every { data } returns flowOf(prefs)
            coEvery { updateData(capture(transformSlot)) } coAnswers { transformSlot.captured(prefs) }
        }

        BudgetLocalDataSource(dataStore).setBudgetData(prefs)

        coVerify(exactly = 1) { dataStore.updateData(any()) }
        assertThat(transformSlot.captured(BudgetDataPreferences.getDefaultInstance())).isEqualTo(
            prefs
        )
    }
}
