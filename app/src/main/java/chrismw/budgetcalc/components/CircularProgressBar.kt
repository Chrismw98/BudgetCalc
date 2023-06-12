package chrismw.budgetcalc.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import java.text.NumberFormat

@Composable
fun CircularProgressbar(
    size: Dp = 260.dp,
    foregroundIndicatorColor: Color = Color(0xFF35898f),
    shadowColor: Color = Color.LightGray,
    indicatorThickness: Dp = 24.dp,
    remainingBudget: Float = 380f,
    maxBudget: Float = 600f,
    animationDurationInMs: Int = 750
) {
    val remainingBudgetAnimator = remember {
        Animatable(-1f)
    }

    // This is to animate the foreground indicator
//    val remainingBudgetAnimate = animateFloatAsState(
////        targetValue = remainingBudgetAnimationFloat,
////        targetValue = remainingBudget,
//        targetValue = remainingBudgetRemember.value,
//        animationSpec = tween(
//            durationMillis = animationDurationInMs,
//            easing = FastOutSlowInEasing
//        )
//    )

    // This is to start the animation when the activity is opened
//    LaunchedEffect(Unit) {
//        remainingBudgetAnimationFloat = remainingBudget
//    }
    LaunchedEffect(remainingBudget) {
        remainingBudgetAnimator.snapTo(0f)
        remainingBudgetAnimator.animateTo(
            targetValue = remainingBudget,
            animationSpec = tween(
                durationMillis = animationDurationInMs,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = Modifier
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
        ) {
            // For shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(shadowColor, Color.White),
                    center = Offset(x = this.size.width / 2, y = this.size.height / 2),
                    radius = this.size.height / 2
                ),
                radius = this.size.height / 2,
                center = Offset(x = this.size.width / 2, y = this.size.height / 2)
            )

            // This is the white circle that appears on the top of the shadow circle
            drawCircle(
                color = Color.White,
                radius = (size / 2 - indicatorThickness).toPx(),
                center = Offset(x = this.size.width / 2, y = this.size.height / 2)
            )

            // Convert the dataUsage to angle
            val sweepAngle = (remainingBudgetAnimator.value) * 360 / maxBudget

            // Foreground indicator
            drawArc(
                color = foregroundIndicatorColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = indicatorThickness.toPx(), cap = StrokeCap.Round),
                size = Size(
                    width = (size - indicatorThickness).toPx(),
                    height = (size - indicatorThickness).toPx()
                ),
                topLeft = Offset(
                    x = (indicatorThickness / 2).toPx(),
                    y = (indicatorThickness / 2).toPx()
                )
            )
        }

        DisplayNumberText(
            amount = remainingBudgetAnimator.value
        )
    }
}

@Composable
private fun DisplayNumberText(
    amount: Float,
    dataTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    remainingTextStyle: TextStyle = MaterialTheme.typography.bodyLarge
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
            text = "$amountString €",
            style = dataTextStyle
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Remaining",
            style = remainingTextStyle
        )
    }
}

@Preview
@Composable
fun DefaultPreview() {
    BudgetCalcTheme {
        CircularProgressbar(maxBudget = 100f, remainingBudget = 47f)
    }
}