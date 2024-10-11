package chrismw.budgetcalc.data

import java.time.LocalDate

public sealed class Budget(
    open val amount: Float,
    open val currency: String,
    open val startDate: LocalDate,
    open val endDate: LocalDate,
) {

    internal data class OnceOnly(
        override val amount: Float,
        override val currency: String,
        override val startDate: LocalDate,
        override val endDate: LocalDate,
    ) : Budget(
        amount = amount,
        currency = currency,
        startDate = startDate,
        endDate = endDate
    )

    internal data class Weekly(
        override val amount: Float,
        override val currency: String,
        override val startDate: LocalDate,
        override val endDate: LocalDate,
    ) : Budget(
        amount = amount,
        currency = currency,
        startDate = startDate,
        endDate = endDate
    )

    internal data class Monthly(
        override val amount: Float,
        override val currency: String,
        override val startDate: LocalDate,
        override val endDate: LocalDate,
    ) : Budget(
        amount = amount,
        currency = currency,
        startDate = startDate,
        endDate = endDate
    )
}