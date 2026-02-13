package chrismw.budgetcalc.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import chrismw.budgetcalc.extensions.popBackStackIfResumed
import chrismw.budgetcalc.helpers.LifecycleEffect
import chrismw.budgetcalc.screens.home.HomeScreen
import chrismw.budgetcalc.screens.home.HomeScreenViewModel
import chrismw.budgetcalc.screens.settings.SettingsScreen
import chrismw.budgetcalc.screens.settings.SettingsViewModel
import chrismw.budgetcalc.ui.theme.motion

private const val HOME_SCREEN_ROUTE = "home_screen"
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
        startDestination = HOME_SCREEN_ROUTE,
        modifier = modifier
    ) {
        composable(route = HOME_SCREEN_ROUTE) {
            val viewModel: HomeScreenViewModel = hiltViewModel()
            val state by viewModel.viewState.collectAsStateWithLifecycle()

            LifecycleEffect(
                onResume = {
                    viewModel.updateCurrentDate()
                }
            )

            HomeScreen(
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

fun NavController.navigateToSettingsScreen() {
    navigate(route = SETTINGS_SCREEN_ROUTE)
}