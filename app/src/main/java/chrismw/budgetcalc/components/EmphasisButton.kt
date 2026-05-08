package chrismw.budgetcalc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.extensions.accent

private val ColorScheme.accentButtonGradient: Color
    @Composable
    get() = Color(0xFFFD08B7)

@Composable
fun EmphasisButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp),
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.accent,
                        MaterialTheme.colorScheme.accentButtonGradient,
                    )
                ),
                shape = RoundedCornerShape(8.dp),
            ),
        onClick = onClick,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
@Preview
private fun EmphasisButtonPreview() {
    EmphasisButton(
        text = "Emphasis Button",
        onClick = {},
    )
}
