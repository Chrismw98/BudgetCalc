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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricType.DAYS_SINCE_START
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.helpers.MetricUnit.CURRENCY
import chrismw.budgetcalc.helpers.MetricUnit.CURRENCY_PER_DAY
import chrismw.budgetcalc.helpers.MetricUnit.DAYS
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import java.text.NumberFormat

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
        val metricNameString = stringResource(id = metric.type.textRes)
        val metricValueString: String
        val metricUnitString = getStringForMetricUnit(
            unit = metric.unit,
            count = metric.value.toInt(),
            currency = currency
        )
        val metricColor: Color
        when (metric.unit) {
            DAYS -> {
                metricValueString = metric.value.toString()
                metricColor = if (metric.value.toDouble() < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            }

            else -> {
                val numberFormat = NumberFormat.getNumberInstance()
                numberFormat.maximumFractionDigits = 2
                numberFormat.minimumFractionDigits = 2
                metricValueString = numberFormat.format(metric.value)
                metricColor = MaterialTheme.colorScheme.onPrimaryContainer
            }
        }

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

@ReadOnlyComposable
@Composable
fun getStringForMetricUnit(unit: MetricUnit, count: Int = 1, currency: String = "$"): String {
    return when (unit) {
        DAYS -> pluralStringResource(id = R.plurals.days, count = count)
        CURRENCY_PER_DAY -> "${currency}/${stringResource(id = R.string.day)}"
        CURRENCY -> currency
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 800)
@Composable
fun MetricItemPreview() {
    val testMetric = Metric(DAYS_SINCE_START, 19, DAYS)
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