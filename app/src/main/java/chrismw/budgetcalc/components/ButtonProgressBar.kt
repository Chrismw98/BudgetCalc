package chrismw.budgetcalc.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

@Composable
fun ButtonProgressbar(
    backgroundColor: Color = Color(0xFF35898f),
    onClickButton: () -> Unit
) {
    Button(
        onClick = {
            onClickButton()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        )
    ) {
        Text(
            text = "Animate with Random Value",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
fun ButtonProgressbarPreview() {
    BudgetCalcTheme {
        ButtonProgressbar {

        }
    }
}