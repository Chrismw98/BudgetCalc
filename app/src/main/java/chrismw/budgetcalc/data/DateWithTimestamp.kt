package chrismw.budgetcalc.data

import java.time.LocalDate
import java.time.LocalDateTime

public data class DateWithTimestamp(
    val date: LocalDate,
    val createdAt: LocalDateTime,
)
