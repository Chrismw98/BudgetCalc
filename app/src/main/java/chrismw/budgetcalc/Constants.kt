package chrismw.budgetcalc

object Constants {

    val defaultBudgetAmount = 600
    val defaultNumberOfDays = 30
    val defaultPaymentDayOfMonth = 25
    val hintText =
        "* The budget will start counting from the next day.\n" +
                "** Easy Adjust will snap the budget start date to the 25th of the last month. " +
                "If this falls on a weekend, then to the Friday before that weekend.\n" +
                "*** The start date has to be before the target date."

}