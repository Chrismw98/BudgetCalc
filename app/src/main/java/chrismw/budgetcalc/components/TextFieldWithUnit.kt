package chrismw.budgetcalc.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.helpers.Metric
import chrismw.budgetcalc.helpers.MetricUnit
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithUnit(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String? = "Testlabel",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    unit: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onValueChange,
            label = {
                labelText?.let {
                    Text(
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                        text = labelText
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
        )

        Text(
            modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 13.dp)
                .defaultMinSize(minWidth = 46.dp),
            text = unit,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Preview(showBackground = false, widthDp = 600, heightDp = 800)
@Composable
fun TextFieldWithUnitPreview() {
    val testMetric = Metric("Days since start", 19.0, MetricUnit.DAYS)
    BudgetCalcTheme {
        TextFieldWithUnit(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            value = "Test",
            onValueChange = {},
            unit = "EUR"
        )
    }
}