package chrismw.budgetcalc.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

@Composable
fun MetricItemCard(
    modifier: Modifier = Modifier,
    metric: Metric,
    currency: String,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val metricNameString = stringResource(id = metric.textResId)
            val metricValueString = metric.getValueString()
            val metricUnitString = when (metric.unit) {
                is MetricUnit.Days -> metric.unit.getString(metric.value.toInt())
                is MetricUnit.Currency -> currency
                is MetricUnit.CurrencyPerDay -> metric.unit.getString(currency)
            }

            if (metric.iconImageVector != null){
                Icon(
                    imageVector = metric.iconImageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else if (metric.iconDrawableRes != null) {
                Icon(
                    painter = painterResource(metric.iconDrawableRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f),
                text = metricNameString,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Text(
                text = metricValueString,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Text(
                text = metricUnitString,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MetricItemWithVectorIconPreview() {
    val testMetric = Metric.DaysRemaining(19)
    BudgetCalcTheme {
        MetricItemCard(
            modifier = Modifier.fillMaxWidth(),
            metric = testMetric,
            currency = "€"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MetricItemWithDrawableResPreview() {
    val testMetric = Metric.DaysUntilStart(10)
    BudgetCalcTheme {
        MetricItemCard(
            modifier = Modifier.fillMaxWidth(),
            metric = testMetric,
            currency = "€"
        )
    }
}