@file:OptIn(ExperimentalFoundationApi::class)

package chrismw.budgetcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import chrismw.budgetcalc.navigation.BudgetCalcNavHost
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

//Compose variables/constants
var decimalFormatSymbols = DecimalFormatSymbols(Locale.getDefault())
var decimalFormat = DecimalFormat("#,##0.00", decimalFormatSymbols)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BudgetCalcTheme(darkTheme = false) {
                BudgetCalcApp()
            }
        }
    }
}

@Composable
private fun BudgetCalcApp() {
    val navController = rememberNavController()
    BudgetCalcNavHost(
        navController = navController
    )
}