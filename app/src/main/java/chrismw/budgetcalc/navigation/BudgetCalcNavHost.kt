package chrismw.budgetcalc.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import chrismw.budgetcalc.extensions.popBackStackIfResumed
import chrismw.budgetcalc.helpers.LifecycleEffect
import chrismw.budgetcalc.screens.MainScreen
import chrismw.budgetcalc.screens.MainScreenViewModel
import chrismw.budgetcalc.screens.SettingsScreen
import chrismw.budgetcalc.screens.SettingsViewModel
import chrismw.budgetcalc.ui.theme.motion

private const val MAIN_SCREEN_ROUTE = "main_screen"
private const val SETTINGS_SCREEN_ROUTE = "settings_screen"

@Composable
fun BudgetCalcNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    NavHost(
        navController = navController,
        enterTransition = { MaterialTheme.motion.sharedAxis(density).targetContentEnter },
        exitTransition = { MaterialTheme.motion.sharedAxis(density).initialContentExit },
        popEnterTransition = { MaterialTheme.motion.sharedAxis(density, reverse = true).targetContentEnter },
        popExitTransition = { MaterialTheme.motion.sharedAxis(density, reverse = true).initialContentExit },
        startDestination = MAIN_SCREEN_ROUTE,
        modifier = modifier
    ) {
        composable(route = MAIN_SCREEN_ROUTE) {
            val viewModel: MainScreenViewModel = hiltViewModel()
            val state by viewModel.viewState.collectAsStateWithLifecycle()

            LifecycleEffect(
                onResume = {
                    viewModel.updateCurrentDate()
                }
            )

            MainScreen(
                viewState = state,
                onJumpToTodayClick = viewModel::onResetTargetDate,
                onSettingsClick = navController::navigateToSettingsScreen,
                toggleShowDetails = viewModel::toggleDetailsExpanded,
                onPickTargetDate = viewModel::onPickTargetDate,
                onShowDatePicker = {
                    viewModel.onSetShowDatePicker(true)
                },
                onHideDatePicker = {
                    viewModel.onSetShowDatePicker(false)
                }
            )
        }

        composable(route = SETTINGS_SCREEN_ROUTE) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val state by viewModel.viewState.collectAsStateWithLifecycle()
            SettingsScreen(
                viewState = state,
                onNavigateBack = { navController.popBackStackIfResumed() },
                onSaveChanges = viewModel::saveSettings,
                onLoadSettings = viewModel::loadSettings,
                onClickConstantBudget = { viewModel.setIsBudgetConstant(true) },
                onClickBudgetRate = { viewModel.setIsBudgetConstant(false) },
                onConstantBudgetAmountChanged = viewModel::setConstantBudgetAmount,
                onBudgetRateAmountChanged = viewModel::setBudgetRateAmount,
                onDefaultPaymentDayChanged = viewModel::setDefaultPaymentDayOfMonth,
                onCurrencyChanged = viewModel::setCurrency,
                onBudgetTypeChanged = viewModel::setBudgetType,
                onDayOfWeekChanged = viewModel::setDefaultPaymentDayOfWeek,
                onStartDateChanged = viewModel::setStartDate,
                onEndDateChanged = viewModel::setEndDate,
                onUpdateExpandedDropDown = viewModel::updateExpandedDropDown,
            )
        }

    }
}

//fun NavHostController.navigateSingleTopTo(route: String) =
//    this.navigate(route) { launchSingleTop = true }
fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

fun NavController.navigateToSettingsScreen() {
    navigate(route = SETTINGS_SCREEN_ROUTE)
}