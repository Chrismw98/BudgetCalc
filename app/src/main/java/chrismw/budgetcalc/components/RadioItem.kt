package chrismw.budgetcalc.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

@Composable
fun RadioItem(
    modifier: Modifier = Modifier,
    text: String = "",
    isSelected: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.clickable(
            onClick = onClick
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Text(
                modifier = Modifier.weight(1f),
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
fun RadioItemPreview() {
    BudgetCalcTheme {
        RadioItem(
            text = "Test",
            isSelected = false,
            onClick = {}
        )
    }
}