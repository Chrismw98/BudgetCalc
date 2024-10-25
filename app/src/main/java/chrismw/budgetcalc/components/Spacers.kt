package chrismw.budgetcalc.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
public fun VerticalSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
public fun HorizontalSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(24.dp))
}