package chrismw.budgetcalc.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.R
import chrismw.budgetcalc.components.CircularProgressbar
import chrismw.budgetcalc.components.MetricItem
import chrismw.budgetcalc.components.StartToTargetDate
import chrismw.budgetcalc.components.TextFieldWithUnit
import chrismw.budgetcalc.decimalFormat
import chrismw.budgetcalc.decimalFormatSymbols
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    onClickSettingsButton: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "BudgetCalc")
                },
                actions = {
                    IconButton(onClick = onClickSettingsButton
//                        {val intent = Intent(context, ComposableSettingsActivity::class.java)
//                        context.startActivity(intent)}
                    ) {
                        Icon(imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { contentPadding ->

        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Column(
                modifier = Modifier
                    .padding(6.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                //TODO: Add top bar with settings navigation (2023-06-13)
                var startDate by rememberSaveable {
                    mutableStateOf(LocalDate.now())
                }
                var targetDate by rememberSaveable {
                    mutableStateOf(LocalDate.now().plusDays(1))
                }

                var maxBudgetString by rememberSaveable {
                    mutableStateOf("600")
                }

                val maxBudget by rememberSaveable(maxBudgetString) {
                    mutableStateOf(maxBudgetString.toFloatOrNull() ?: 0f)
                }

//            var remainingBudget by rememberSaveable {
//                mutableStateOf(246.672f)
//            }

//            val maxBudget = 600f
//            val maxBudget = Float.MAX_VALUE

                val lengthOfPaymentCycleInDays = startDate.lengthOfMonth()
                val dailyBudget = maxBudget / lengthOfPaymentCycleInDays

                val daysSinceStart = ChronoUnit.DAYS.between(startDate, targetDate)
                val daysRemaining = lengthOfPaymentCycleInDays - daysSinceStart

                val currentBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) daysSinceStart * dailyBudget else maxBudget
                val remainingBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) maxBudget - currentBudget else 0f

                CircularProgressbar(
                    maxBudget = maxBudget,
                    remainingBudget = remainingBudget,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )

                Spacer(modifier = Modifier.height(30.dp))

//            ButtonProgressbar {
//                remainingBudget = (Random.nextDouble() * (maxBudget + 1)).toFloat()
//            }

//            Spacer(modifier = Modifier.height(20.dp))

                StartToTargetDate(
                    startDate = startDate,
                    endDate = targetDate,
                    onClickStartDate = {
                        startDate = it
                        if (!startDate.isBefore(targetDate)) {
                            targetDate = startDate.plusDays(1)
                        }
                    },
                    onClickTargetDate = {
                        targetDate = it
                        if (!startDate.isBefore(targetDate)) {
                            startDate = targetDate.minusDays(1)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                decimalFormat
                TextFieldWithUnit(
                    modifier = Modifier.fillMaxWidth(),
                    value = maxBudgetString,
                    onValueChange = { newValueString ->
                        if (validateNumberInputString(
                                numberInputString = newValueString,
                                allowDecimalSeparator = true)
                        ) {
                            maxBudgetString = newValueString
                        }
                    },
                    labelText = stringResource(id = R.string.budget_amount),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                    unit = "EUR"
                )

                Spacer(modifier = Modifier.height(6.dp))

                TextFieldWithUnit(
                    modifier = Modifier.fillMaxWidth(),
                    value = "30",
                    onValueChange = {},
                    labelText = stringResource(id = R.string.payment_cycle_length),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    unit = "Days"
                )

                val exampleMetrics = arrayListOf(
                    Metric(stringResource(id = R.string.days_since_start), daysSinceStart, MetricUnit.DAYS),
                    Metric(stringResource(id = R.string.days_remaining), daysRemaining, MetricUnit.DAYS),
                    Metric(stringResource(id = R.string.daily_budget), dailyBudget, MetricUnit.CURRENCY_PER_DAY),
                    Metric(stringResource(id = R.string.budget_until_target_date), currentBudget, MetricUnit.CURRENCY),
                    Metric(stringResource(id = R.string.remaining_budget), remainingBudget, MetricUnit.CURRENCY),
                )
                var expanded by rememberSaveable {
                    mutableStateOf(true)
                }

                Column(
                    modifier = Modifier
                        .padding(vertical = 24.dp),
//                    .animateContentSize(
//                        animationSpec = tween(
//                            durationMillis = 300,
//                            easing = FastOutSlowInEasing
////                            dampingRatio = Spring.DampingRatioMediumBouncy,
////                            stiffness = Spring.StiffnessLow
//                        )
//                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable(
                            onClick = { expanded = !expanded }
                        )
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 24.dp),
                            text = if (expanded) stringResource(id = R.string.show_less) else stringResource(id = R.string.show_more),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start
                        )

                        Divider(thickness = 1.dp)

//                    IconButton(
//                        modifier = Modifier.padding(top = 30.dp),
//                        onClick = { expanded = !expanded }
//                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            modifier = Modifier
                                .size(size = 64.dp) //TODO: Check if this is the right way to set the size (2023-06-14)
                                .padding(top = 30.dp),
//                            tint = colorResource(id = R.color.color_accent), //TODO: Use Material UI Colors (2023-06-14), //TODO: Use Material UI Colors (2023-06-14)
                            contentDescription = if (expanded) {
                                stringResource(R.string.show_less)
                            } else {
                                stringResource(R.string.show_more)
                            }
                        )
//                    }
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.heightIn(max = 250.dp)) {
                            items(items = exampleMetrics) { metric ->
                                Card(
                                    shape = MaterialTheme.shapes.small,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    MetricItem(metric = metric, modifier = Modifier.padding(6.dp))
                                }

                            }
                        }
                    }
                }

            }

        }
    }
}

private fun validateNumberInputString(numberInputString: String, allowDecimalSeparator: Boolean = false): Boolean {
    val maxDecimalSeparatorCount = if (allowDecimalSeparator) 1 else 0
    var decimalSeparatorCounter = 0
    for (char in numberInputString) {
        if (char == decimalFormatSymbols.decimalSeparator) {
            if (decimalSeparatorCounter < maxDecimalSeparatorCount) {
                decimalSeparatorCounter++
            } else {
                return false
            }
        } else if (!char.isDigit()) {
            return false
        }
    }
    return true
}

@Preview
@Composable
fun DefaultPreview() {
    BudgetCalcTheme {
        MainScreen(
            onClickSettingsButton = {}
        )
    }
}