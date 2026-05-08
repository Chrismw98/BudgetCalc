package chrismw.budgetcalc.screens.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chrismw.budgetcalc.R
import chrismw.budgetcalc.extensions.accent
import chrismw.budgetcalc.ui.theme.BudgetCalcTheme

@Composable
internal fun BudgetDefinitionCard(
    modifier: Modifier = Modifier,
    @StringRes labelResId: Int,
    @StringRes descriptionResId: Int,
    @DrawableRes iconResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.White
    }
    val outlineColor = if (selected) {
        MaterialTheme.colorScheme.accent
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    key(selected) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(84.dp),
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            border = BorderStroke(
                width = 1.dp,
                color = outlineColor,
            ),
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconBubble(
                    selected = selected,
                    drawableResId = iconResId,
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(labelResId),
                            style = MaterialTheme.typography.labelLarge,
                        )

                        if (selected) {
                            Badge(
                                modifier = Modifier.padding(top = 1.dp),
                                containerColor = MaterialTheme.colorScheme.accent
                            )
                        }
                    }

                    Text(
                        text = stringResource(descriptionResId),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun IconBubble(
    selected: Boolean,
    @DrawableRes drawableResId: Int,
) {
    val bubbleColor = if (selected) {
        MaterialTheme.colorScheme.accent
    } else {
        MaterialTheme.colorScheme.surfaceDim
    }
    val iconColor = if (selected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = bubbleColor,
                shape = CircleShape,
            ),
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(drawableResId),
            contentDescription = null,
            tint = iconColor,
        )
    }
}

@Composable
@Preview
private fun PreviewSelectedBudgetDefinitionCard() {
    BudgetCalcTheme {
        BudgetDefinitionCard(
            labelResId = R.string.settings_definition_constant_label,
            descriptionResId = R.string.settings_definition_constant_description,
            iconResId = R.drawable.ic_event,
            selected = true,
            onClick = {},
        )
    }
}

@Composable
@Preview
private fun PreviewUnselectedBudgetDefinitionCard() {
    BudgetCalcTheme {
        BudgetDefinitionCard(
            labelResId = R.string.settings_definition_constant_label,
            descriptionResId = R.string.settings_definition_constant_description,
            iconResId = R.drawable.ic_event,
            selected = false,
            onClick = {},
        )
    }
}