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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    modifier: Modifier = Modifier,
//    foregroundIndicatorColor: Color = Color(0xFF35898f),
    foregroundIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    shadowColor: Color = Color.LightGray,
    indicatorThickness: Dp = 24.dp,
    remainingBudget: Float = 380f, //TODO: Handle case 0f
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

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val boxWidth = maxWidth

//        Canvas(
//            modifier = Modifier.matchParentSize()
//        ) {
//            // For shadow
//            drawCircle(
//                brush = Brush.radialGradient(
//                    colors = listOf(shadowColor, Color.White),
//                    center = Offset(x = this.size.width / 2, y = this.size.height / 2),
//                    radius = this.size.height
//                ),
//                radius = this.size.height / 2,
//                center = Offset(x = this.size.width / 2, y = this.size.height / 2)
//            )
//
//            // This is the white circle that appears on the top of the shadow circle
////            drawCircle(
////                color = Color.White,
////                radius = ((this.size.toDpSize().width / 2) - indicatorThickness).toPx(),
////                center = Offset(x = this.size.width / 2, y = this.size.height / 2)
////            )
//
//
////            // Convert the dataUsage to angle
////            val sweepAngle = (remainingBudgetAnimator.value) * 360 / maxBudget
////
////            // Foreground indicator
////            drawArc(
////                color = foregroundIndicatorColor,
////                startAngle = -90f,
////                sweepAngle = sweepAngle,
////                useCenter = false,
////                style = Stroke(width = indicatorThickness.toPx(), cap = StrokeCap.Round),
////                size = Size(
////                    width = (this.size.toDpSize().width - indicatorThickness).toPx(),
////                    height = (this.size.toDpSize().height - indicatorThickness).toPx()
////                ),
////                topLeft = Offset(
////                    x = (indicatorThickness / 2).toPx(),
////                    y = (indicatorThickness / 2).toPx()
////                )
////            )
//        }
        Surface(
            shape = CircleShape,
            color = shadowColor,
            modifier = Modifier.size(boxWidth),
            shadowElevation = 1.dp,
            content = {}
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(boxWidth - indicatorThickness*2),
            shadowElevation = 3.dp
        ){
            DisplayNumberText(
                amount = remainingBudgetAnimator.value
            )
        }


        Canvas(
            modifier = Modifier.matchParentSize()
        ){
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
                    width = (this.size.toDpSize().width - indicatorThickness).toPx(),
                    height = (this.size.toDpSize().height - indicatorThickness).toPx()
                ),
                topLeft = Offset(
                    x = (indicatorThickness / 2).toPx(),
                    y = (indicatorThickness / 2).toPx()
                )
            )
        }

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

@Preview(showBackground = false, widthDp = 100, heightDp = 100)
@Composable
fun CircularProgressBarPreviewSmall() {
    BudgetCalcTheme {
        CircularProgressbar(
            maxBudget = 100f,
            remainingBudget = 47f
        )
    }
}

@Preview(showBackground = false, widthDp = 250, heightDp = 250)
@Composable
fun CircularProgressBarPreviewNormal() {
    BudgetCalcTheme {
        CircularProgressbar(
            maxBudget = 100f,
            remainingBudget = 47f
        )
    }
}

@Preview(showBackground = false, widthDp = 500, heightDp = 500)
@Composable
fun CircularProgressBarPreviewLarge() {
    BudgetCalcTheme {
        CircularProgressbar(
            maxBudget = 100f,
            remainingBudget = 47f
        )
    }
}

@Preview(showBackground = false, widthDp = 500, heightDp = 800)
@Composable
fun CircularProgressBarPreviewCustom() {
    BudgetCalcTheme {
        CircularProgressbar(
            maxBudget = 100f,
            remainingBudget = 47f,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}