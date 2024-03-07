package chrismw.budgetcalc.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import chrismw.budgetcalc.R

/**
 * Full screen loading overlay.
 */
@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean,
    loadingText: String = stringResource(id = R.string.text_loading)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .alpha(0.95f)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.inverseOnSurface
            ) {}
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
