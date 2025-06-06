package chrismw.budgetcalc.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

private val INDICATOR_THICKNESS_DP = 24.dp

@Composable
fun CircularTextOverview(
    modifier: Modifier = Modifier,
    text: String,
    targetDateString: String = "",
    backgroundCircleColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(250.dp)
            .clip(shape = CircleShape)
            .clickable(
                enabled = true,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        val boxWidth = maxWidth

        Surface(
            shape = CircleShape,
            color = backgroundCircleColor,
            modifier = Modifier.size(boxWidth),
            shadowElevation = 1.dp,
            content = {}
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(boxWidth - INDICATOR_THICKNESS_DP * 2),
            shadowElevation = 3.dp
        ) {
            DisplayNumberText(
                text = text,
                targetDateString = targetDateString,
            )
        }
    }
}

@Composable
private fun DisplayNumberText(
    text: String,
    targetDateString: String,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(id = R.string.label_as_of),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = targetDateString,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = false, widthDp = 100, heightDp = 100)
@Preview(showBackground = false, widthDp = 250, heightDp = 250)
@Preview(showBackground = false, widthDp = 500, heightDp = 500)
@Composable
private fun CircularTextOverviewPreview() {
    BudgetCalcTheme {
        CircularTextOverview(
            targetDateString = "Thu., 16/09/2023",
            text = "X days past",
            onClick = {}
        )
    }
}