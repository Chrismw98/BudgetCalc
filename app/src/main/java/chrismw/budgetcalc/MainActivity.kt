package chrismw.budgetcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import chrismw.budgetcalc.navigation.BudgetCalcNavHost
import chrismw.budgetcalc.ui.theme.BackgroundStatusBarColor
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BudgetCalcTheme {
                BackgroundStatusBarColor()
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