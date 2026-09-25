package chrismw.budgetcalc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
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
    shape: Shape = RoundedCornerShape(8.dp),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    textStyle: TextStyle? = null,
    elevation: Dp = 2.dp,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    TextButton(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.accent,
                        MaterialTheme.colorScheme.accentButtonGradient,
                    )
                ),
                shape = shape,
            ),
        onClick = onClick,
        shape = shape,
        contentPadding = contentPadding,
    ) {
        if (textStyle != null) {
            Text(
                text = text,
                style = textStyle,
                color = Color.White,
            )
        } else {
            Text(
                text = text,
                color = Color.White,
            )
        }

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                trailingIcon()
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun EmphasisButtonPreview() {
    EmphasisButton(
        text = "Emphasis Button",
        onClick = {},
    )
}
