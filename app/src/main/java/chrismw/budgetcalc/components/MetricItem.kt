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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chrismw.budgetcalc.Constants
import chrismw.budgetcalc.Metric
import chrismw.budgetcalc.MetricUnit
import chrismw.budgetcalc.R
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme
import java.text.NumberFormat
import kotlin.math.abs

@Composable
fun MetricItem(
    modifier: Modifier = Modifier,
    metric: Metric
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        //TODO: Enable usage of Settings
        val metricNameString = metric.name
        val metricValueString: String
        val metricUnitString: String
        val metricColor: Color
        when (metric.unit) {
            MetricUnit.DAYS -> {
//                metricValueString = String.format("%.0f", metric.value)
                metricValueString = metric.value.toString()
                metricUnitString = if (abs(metric.value.toDouble()) == 1.0) {
                    stringResource(R.string.day)
                } else {
                    stringResource(R.string.days)
                }
                metricColor = if (metric.value.toDouble() < 0) {
                    Color.Red
                } else {
//                    colorResource(id = R.color.text_normal)
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            }

            else -> {
                val numberFormat = NumberFormat.getNumberInstance()
                numberFormat.maximumFractionDigits = 2
                numberFormat.minimumFractionDigits = 2
                metricValueString = numberFormat.format(metric.value)
                metricUnitString = when (metric.unit) {
                    MetricUnit.CURRENCY_PER_DAY -> "${Constants.defaultCurrency}/${stringResource(id = R.string.day)}"
                    MetricUnit.CURRENCY -> Constants.defaultCurrency
                    else -> throw IllegalArgumentException("Invalid metric unit provided: ${metric.unit} for metric $metric")
                }
//                metricColor = colorResource(id = R.color.text_normal)
                metricColor = MaterialTheme.colorScheme.onPrimaryContainer
            }
        }

        Text(
            text = metricNameString,
            fontSize = 20.sp,
//            color = colorResource(id = R.color.text_normal),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(3f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = metricValueString,
            fontSize = 20.sp,
            color = metricColor,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = metricUnitString,
            fontSize = 20.sp,
            color = metricColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = false, widthDp = 600, heightDp = 800)
@Composable
fun MetricItemPreview() {
    val testMetric = Metric("Days since start", 19.0, MetricUnit.DAYS)
    BudgetCalcTheme {
        MetricItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            metric = testMetric
        )
    }
}