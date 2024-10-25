package chrismw.budgetcalc.data.repository

import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.extensions.toLocalDate
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfMonth
import chrismw.budgetcalc.helpers.findLatestOccurrenceOfDayOfWeek
import chrismw.budgetcalc.helpers.findNextOccurrenceOfDayOfMonth
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Test class for [Utils]
 */
class UtilsTest {

    @Test
    fun `findLatestOccurrenceOfDayOfMonth returns correct day of current month`() {
        val today = LocalDate.of(2024, 5, 10)
        val expectedDate = LocalDate.of(2024, 5, 9)

        val result = findLatestOccurrenceOfDayOfMonth(
            today = today,
            targetDayOfMonth = 9
        )

        assertThat(result).isEqualTo(expectedDate)
    }

    @Test
    fun `findLatestOccurrenceOfDayOfMonth returns correct day today`() {
        val today = LocalDate.of(2024, 5, 10)

        val result = findLatestOccurrenceOfDayOfMonth(
            today = today,
            targetDayOfMonth = 10
        )

        assertThat(result).isEqualTo(today)
    }

    @Test
    fun `findLatestOccurrenceOfDayOfMonth returns correct day of last month`() {
        val today = LocalDate.of(2024, 5, 10)
        val expectedDate = LocalDate.of(2024, 4, 11)

        val result = findLatestOccurrenceOfDayOfMonth(
            today = today,
            targetDayOfMonth = 11
        )

        assertThat(result).isEqualTo(expectedDate)
    }

    @Test
    fun `findNextOccurrenceOfDayOfMonth returns correct day of current month`() {
        val today = LocalDate.of(2024, 5, 10)
        val expectedDate = LocalDate.of(2024, 5, 11)

        val result = findNextOccurrenceOfDayOfMonth(
            today = today,
            targetDayOfMonth = 11
        )

        assertThat(result).isEqualTo(expectedDate)
    }

    @Test
    fun `findNextOccurrenceOfDayOfMonth returns correct day today next month`() {
        val today = LocalDate.of(2024, 5, 10)
        val expectedDate = LocalDate.of(2024, 6, 10)

        val result = findNextOccurrenceOfDayOfMonth(
            today = today,
            targetDayOfMonth = 10
        )

        assertThat(result).isEqualTo(expectedDate)
    }

    @Test
    fun `findNextOccurrenceOfDayOfMonth returns correct day of last month`() {
        val today = LocalDate.of(2024, 5, 10)
        val expectedDate = LocalDate.of(2024, 6, 9)

        val result = findNextOccurrenceOfDayOfMonth(
            today = today,
            targetDayOfMonth = 9
        )

        assertThat(result).isEqualTo(expectedDate)
    }

    @Test
    fun `findLatestOccurrenceOfDayOfWeek returns correct day today`() {
        val todayFriday = LocalDate.of(2024, 5, 10)

        val result = findLatestOccurrenceOfDayOfWeek(
            today = todayFriday,
            targetDayOfWeek = DayOfWeek.FRIDAY
        )

        assertThat(result).isEqualTo(todayFriday)
    }

    @Test
    fun `findLatestOccurrenceOfDayOfWeek returns correct day of this week`() {
        val todayFriday = LocalDate.of(2024, 5, 10)
        val expectedPreviousThursday = LocalDate.of(2024, 5, 9)

        val result = findLatestOccurrenceOfDayOfWeek(
            today = todayFriday,
            targetDayOfWeek = DayOfWeek.THURSDAY
        )

        assertThat(result).isEqualTo(expectedPreviousThursday)
    }

    @Test
    fun `findLatestOccurrenceOfDayOfWeek returns correct day of last week`() {
        val todayFriday = LocalDate.of(2024, 5, 10)
        val expectedPreviousSaturday = LocalDate.of(2024, 5, 4)

        val result = findLatestOccurrenceOfDayOfWeek(
            today = todayFriday,
            targetDayOfWeek = DayOfWeek.SATURDAY
        )

        assertThat(result).isEqualTo(expectedPreviousSaturday)
    }

    @Test
    fun `toEpochMillis converts LocalDate to epoch millis`() {
        val dateUTC = LocalDate.of(2024, 4, 1)
        val epochMillisUTC = 1711929600000L
        assertThat(dateUTC.toEpochMillis()).isEqualTo(epochMillisUTC)
    }

    @Test
    fun `toLocalDate converts epochMillis to LocalDate`() {
        val epochMillisUTC = 1711929600000L
        val dateUTC = LocalDate.of(2024, 4, 1)
        assertThat(epochMillisUTC.toLocalDate()).isEqualTo(dateUTC)
    }
}