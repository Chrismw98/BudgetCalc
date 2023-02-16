package chrismw.budgetcalc

object Constants {

    const val defaultBudgetAmountInEurosPerDay = 20
    const val defaultPaymentDayOfMonth = 25
    const val defaultCurrency = "€"

    const val SHARED_PREFERENCES_FILENAME = "shared_preferences_file"

    //These are the entered values the app will remember
    const val LATEST_BUDGET_AMOUNT = "latest_budget_amount"
    const val LATEST_PAYMENT_CYCLE_LENGTH = "latest_payment_cycle_length"

    //These are the values that are configurable through the settings
    const val LATEST_PAYMENT_DAY_OF_MONTH = "latest_payment_day_of_month"
    const val LATEST_BUDGET_AMOUNT_IN_EUROS_PER_DAY = "latest_budget_amount_in_euros_per_day"
    const val LATEST_CURRENCY = "latest_currency"
}