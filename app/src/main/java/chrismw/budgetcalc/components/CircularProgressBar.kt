package chrismw.budgetcalc.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.helpers.DoubleAnimatable
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.NumberFormat

private val INDICATOR_THICKNESS_DP = 24.dp

@Composable
fun CircularProgressbar(
    modifier: Modifier = Modifier,
    remainingBudgetPercentage: Float,
    remainingBudget: Double,
    currency: String,
    targetDateString: String = "",
    onClick: () -> Unit,
) {
    val remainingBudgetAnimated = remember {
        DoubleAnimatable(0.0)
    }

    val remainingBudgetPercentageAnimated = remember {
        Animatable(0F)
    }

    LaunchedEffect(remainingBudget, remainingBudgetPercentage) {

        awaitAll(
            async { remainingBudgetAnimated.snapTo(0.0) },
            async { remainingBudgetPercentageAnimated.snapTo(0F) }
        )

        awaitAll(
            async {
                remainingBudgetAnimated.animateTo(
                    targetValue = remainingBudget,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            async {
                remainingBudgetPercentageAnimated.animateTo(
                    targetValue = remainingBudgetPercentage.coerceIn(0.0F, 1.0F),
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        )

    }

    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(250.dp)
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val boxWidth = maxWidth

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(boxWidth),
            shadowElevation = 1.dp,
            content = {}
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            onClick = onClick,
            enabled = true,
            modifier = Modifier.size(boxWidth - INDICATOR_THICKNESS_DP * 2),
            shadowElevation = 3.dp
        ) {
            DisplayNumberText(
                amount = remainingBudgetAnimated.value,
                targetDateString = targetDateString,
                currency = currency,
            )
        }

        val indicatorColor = MaterialTheme.colorScheme.tertiary
        Canvas(
            modifier = Modifier
                .progressSemantics(remainingBudgetPercentage)
                .matchParentSize()
        ) {
            // Convert the dataUsage to angle
            val sweepAngle = (remainingBudgetPercentageAnimated.value) * 360

            // Foreground indicator
            drawArc(
                color = indicatorColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = INDICATOR_THICKNESS_DP.toPx(), cap = StrokeCap.Round),
                size = Size(
                    width = (this.size.toDpSize().width - INDICATOR_THICKNESS_DP).toPx(),
                    height = (this.size.toDpSize().height - INDICATOR_THICKNESS_DP).toPx()
                ),
                topLeft = Offset(
                    x = (INDICATOR_THICKNESS_DP / 2).toPx(),
                    y = (INDICATOR_THICKNESS_DP / 2).toPx()
                )
            )
        }

    }
}

@Composable
private fun DisplayNumberText(
    amount: Double,
    dataTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    remainingTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    targetDateString: String,
    currency: String,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        val amountString = numberFormat.format(amount)
        Text(
            text = "$amountString $currency",
            style = dataTextStyle
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(id = R.string.label_remaining_as_of),
            style = remainingTextStyle
        )

        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = targetDateString,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true, widthDp = 100, heightDp = 100)
@Composable
private fun CircularProgressBarPreviewSmall() {
    BudgetCalcTheme {
        CircularProgressbar(
            remainingBudget = 47.0,
            remainingBudgetPercentage = 0.47f,
            targetDateString = "Thu., 16/09/2023",
            currency = "€",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 250, heightDp = 250)
@Composable
private fun CircularProgressBarPreviewNormal() {
    BudgetCalcTheme {
        CircularProgressbar(
            remainingBudget = 47.0,
            remainingBudgetPercentage = 0.47f,
            targetDateString = "Thu., 16/09/2023",
            currency = "€",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
private fun CircularProgressBarPreviewLarge() {
    BudgetCalcTheme {
        CircularProgressbar(
            remainingBudget = 47.0,
            remainingBudgetPercentage = 0.47f,
            targetDateString = "Thu., 16/09/2023",
            currency = "€",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 800)
@Composable
private fun CircularProgressBarPreviewCustom() {
    BudgetCalcTheme {
        CircularProgressbar(
            remainingBudget = 47.0,
            remainingBudgetPercentage = 0.47f,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            targetDateString = "Thu., 16/09/2023",
            currency = "€",
            onClick = {}
        )
    }
}
