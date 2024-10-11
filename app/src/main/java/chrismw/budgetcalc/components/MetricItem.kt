package chrismw.budgetcalc.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

@Composable
fun MetricItem(
    modifier: Modifier = Modifier,
    metric: Metric,
    currency: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val metricNameString = stringResource(id = metric.textResId)
        val metricValueString = metric.getValueString()
        val metricUnitString = when (metric.unit) {
            is MetricUnit.Days -> metric.unit.getString(metric.value.toInt())
            is MetricUnit.Currency -> currency
            is MetricUnit.CurrencyPerDay -> metric.unit.getString(currency)
        }
        val metricColor = metric.getColor()

        Text(
            text = metricNameString,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(3f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = metricValueString,
            style = MaterialTheme.typography.bodyLarge,
            color = metricColor,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = metricUnitString,
            style = MaterialTheme.typography.bodyLarge,
            color = metricColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Composable
fun MetricItemPreview() {
    val testMetric = Metric.DaysSinceStart(19)
    BudgetCalcTheme {
        MetricItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            metric = testMetric,
            currency = "€"
        )
    }
}