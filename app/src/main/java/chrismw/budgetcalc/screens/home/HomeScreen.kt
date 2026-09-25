package chrismw.budgetcalc.screens.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.CircularProgressbar
import chrismw.budgetcalc.components.CircularTextOverview
import chrismw.budgetcalc.components.EmphasisButton
import chrismw.budgetcalc.components.MetricItemCard
import chrismw.budgetcalc.components.VerticalSpacer
import chrismw.budgetcalc.extensions.accent
import chrismw.budgetcalc.extensions.accentVariant
import chrismw.budgetcalc.extensions.toEpochMillis
import chrismw.budgetcalc.extensions.toLocalDate
import chrismw.budgetcalc.helpers.BudgetState
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.dateString
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewState: HomeScreenViewModel.ViewState,
    onJumpToTodayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    toggleShowDetails: () -> Unit,
    onPickTargetDate: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.title_home))
                },
                actions = {
                    if (viewState.showJumpToTodayButton) {
                        IconButton(
                            onClick = onJumpToTodayClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null
                            )
                        }
                    }

                    IconButton(
                        onClick = onSettingsClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        if (!viewState.isLoading) {
            if (viewState.hasIncompleteData) {
                MissingDataContent(
                    contentPadding = contentPadding,
                    onSettingsClick = onSettingsClick,
                )
            } else {
                MainScreenContent(
                    contentPadding = contentPadding,
                    viewState = viewState,
                    toggleShowDetails = toggleShowDetails,
                    onPickTargetDate = onPickTargetDate,
                    onShowDatePicker = onShowDatePicker,
                    onHideDatePicker = onHideDatePicker,
                )
            }
        }
    }
}

@Composable
private fun MainScreenContent(
    contentPadding: PaddingValues,
    viewState: HomeScreenViewModel.ViewState,
    toggleShowDetails: () -> Unit,
    onPickTargetDate: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
) {
    if (viewState.showDatePicker) {
        DatePickerModal(
            targetDate = viewState.targetDate,
            minDate = viewState.datePickerMinDate,
            maxDate = viewState.datePickerMaxDate,
            onDateSelected = { selectedDate ->
                onHideDatePicker()
                selectedDate?.let {
                    onPickTargetDate(it)
                }
            },
            onDismiss = onHideDatePicker,
        )
    }

    LazyColumn(
        modifier = Modifier
            .padding(contentPadding)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item("circular_overview") {
            when (viewState.budgetState) {
                is BudgetState.Ongoing -> {
                    CircularProgressbar(
                        remainingBudget = viewState.remainingBudget ?: 0.0,
                        remainingBudgetPercentage = viewState.remainingBudgetPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        targetDateString = dateString(viewState.targetDate.toEpochMillis()),
                        currency = viewState.currencySymbol,
                        onClick = onShowDatePicker
                    )
                }

                else -> {
                    CircularTextOverview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        text = stringResource(id = viewState.budgetState.textResId),
                        targetDateString = dateString(viewState.targetDate.toEpochMillis()),
                        backgroundCircleColor = if (viewState.budgetState is BudgetState.HasNotStarted) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        onClick = onShowDatePicker
                    )
                }
            }
        }

        item("spacer") {
            VerticalSpacer(24.dp)
        }

        item("toggle_details_button") {
            ToggleDetailsButton(
                isExpanded = viewState.isExpanded,
                onClick = toggleShowDetails,
            )
        }

        item("metrics_list") {
            AnimatedVisibility(
                visible = viewState.isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    viewState.metrics.forEach { metric ->
                        MetricItemCard(
                            metric = metric,
                            currency = viewState.currencySymbol
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleDetailsButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(
            onClick = onClick
        )
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (isExpanded) stringResource(id = R.string.show_less) else stringResource(
                    id = R.string.show_more
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            HorizontalDivider(thickness = 1.dp)
        }


        Icon(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 16.dp)
                .size(40.dp),
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (isExpanded) {
                stringResource(R.string.show_less)
            } else {
                stringResource(R.string.show_more)
            }
        )
    }
}

@Composable
private fun MissingDataContent(
    contentPadding: PaddingValues,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalSpacer(24.dp)

        WelcomeHeroIcon()

        VerticalSpacer(18.dp)

        Text(
            text = stringResource(R.string.title_welcome),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        VerticalSpacer(24.dp)

        WelcomeInfoCard(
            iconResId = R.drawable.ic_wallet,
            titleResId = R.string.home_welcome_card_your_way_label,
            descriptionResId = R.string.home_welcome_card_your_way_description,
        )

        VerticalSpacer(12.dp)

        WelcomeInfoCard(
            iconResId = R.drawable.ic_tune,
            titleResId = R.string.home_welcome_card_basics_label,
            descriptionResId = R.string.home_welcome_card_basics_description,
        )

        VerticalSpacer(12.dp)

        WelcomeInfoCard(
            iconResId = R.drawable.ic_overview_circle,
            titleResId = R.string.home_welcome_card_check_in_label,
            descriptionResId = R.string.home_welcome_card_check_in_description,
        )

        VerticalSpacer(24.dp)

        EmphasisButton(
            text = stringResource(R.string.home_welcome_get_started_btn_label),
            onClick = onSettingsClick,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 16.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            elevation = 6.dp,
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
        )

        VerticalSpacer(12.dp)

        Text(
            text = stringResource(R.string.home_welcome_footer_description),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )

        VerticalSpacer(24.dp)
    }
}

@Composable
private fun WelcomeHeroIcon() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                color = MaterialTheme.colorScheme.accentVariant,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_icon_welcome),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.64f),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun WelcomeInfoCard(
    @DrawableRes iconResId: Int,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
) {
    val shape = RoundedCornerShape(12.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = shape,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.accentVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(titleResId),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = stringResource(descriptionResId),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit,
    targetDate: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
) {
    val targetDateMillis = targetDate.toEpochMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = targetDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in minDate.toEpochMillis()..maxDate.toEpochMillis()
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in minDate.year..maxDate.year
            }
        },
        initialDisplayedMonthMillis = targetDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis?.toLocalDate())
                    onDismiss()
                }) {
                Text(stringResource(id = R.string.label_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    text = stringResource(id = R.string.dialog_title_pick_target_date)
                )
            },
            showModeToggle = false,
        )
    }
}

@Preview
@Composable
fun DefaultPreview() {
    BudgetCalcTheme {
        HomeScreen(
            viewState = HomeScreenViewModel.ViewState(
                isLoading = false,

                targetDate = LocalDate.of(2023, 9, 16),

                remainingBudget = 270.0,
                currencySymbol = "€",
                metrics = persistentListOf(
                    Metric.DaysSinceStart(3),
                    Metric.DaysRemaining(27),
                    Metric.DailyBudget(10.0),
                    Metric.BudgetUntilTargetDate(270.0),
                    Metric.RemainingBudget(270.0),
                    Metric.TotalBudget(540.0),
                ),
                isExpanded = true,
                hasIncompleteData = false
            ),
            onJumpToTodayClick = {},
            onSettingsClick = {},
            toggleShowDetails = {},
            onPickTargetDate = {},
            onShowDatePicker = {},
            onHideDatePicker = {},
        )
    }
}

@Preview
@Composable
fun MissingDataPreview() {
    BudgetCalcTheme {
        HomeScreen(
            viewState = HomeScreenViewModel.ViewState(
                isLoading = false,
                hasIncompleteData = true
            ),
            onJumpToTodayClick = {},
            onSettingsClick = {},
            toggleShowDetails = {},
            onPickTargetDate = {},
            onShowDatePicker = {},
            onHideDatePicker = {},
        )
    }
}