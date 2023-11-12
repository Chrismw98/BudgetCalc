package chrismw.budgetcalc.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.core.Easing as ComposeEasing

/**
 * Copied from androidx.compose.material3.tokens.MotionTokens
 */
@Stable
public object Motion {

    /**
     * Material Fade Through Transition.
     */
    @OptIn(ExperimentalAnimationApi::class)
    @Stable
    public val fadeThrough: ContentTransform
        get() = fadeIn(
            animationSpec = tween(
                durationMillis = Duration.Medium2,
                delayMillis = Duration.Short2,
                easing = Easing.EmphasizedDecelerate
            )
        ).plus(
            scaleIn(initialScale = 0.92f)
        ).with(
            fadeOut(
                animationSpec = tween(
                    durationMillis = Duration.Short2,
                    easing = Easing.EmphasizedAccelerate
                )
            )
        )

    /**
     * Material Shared Axis transition.
     */
    @OptIn(ExperimentalAnimationApi::class)
    @Stable
    public fun sharedAxis(
        density: Density,
        axis: SharedAxis = SharedAxis.X,
        reverse: Boolean = false
    ): ContentTransform {
        val direction = if (reverse) -1 else 1
        val offset = (30 * density.density * direction).roundToInt()
        val slideXYAnimationSpec = tween<IntOffset>(
                durationMillis = Duration.Medium2,
                easing = Easing.Standard
            )
        val slideZAnimationSpec = tween<Float>(
                durationMillis = Duration.Medium2,
                easing = Easing.Standard
            )

        val enterFade = fadeIn(
            animationSpec = tween(
                durationMillis = Duration.Medium2,
                delayMillis = Duration.Short2,
                easing = Easing.EmphasizedDecelerate
            )
        )
        val exitFade = fadeOut(
            animationSpec = tween(
                durationMillis = Duration.Short2,
                easing = Easing.StandardAccelerate
            )
        )
        return when (axis) {
            SharedAxis.X -> slideInHorizontally(
                animationSpec = slideXYAnimationSpec,
                initialOffsetX = { offset }
            ).plus(
                enterFade
            ).with(
                slideOutHorizontally(
                    animationSpec = slideXYAnimationSpec,
                    targetOffsetX = { -offset }
                ).plus(
                    exitFade
                )
            )
            SharedAxis.Y -> slideInVertically(
                animationSpec = slideXYAnimationSpec,
                initialOffsetY = { offset }
            ).plus(
                enterFade
            ).with(
                slideOutVertically(
                    animationSpec = slideXYAnimationSpec,
                    targetOffsetY = { -offset }
                ).plus(
                    exitFade
                )
            )
            SharedAxis.Z -> scaleIn(
                initialScale = 0.8f,
                animationSpec = slideZAnimationSpec,
            ).plus(
                enterFade
            ).with(
                scaleOut(
                    targetScale = 1.1f,
                    animationSpec = slideZAnimationSpec,
                ).plus(
                    exitFade
                )
            )
        }
    }

    /**
     * Axis for shared axis transition.
     */
    public enum class SharedAxis {
        X, Y, Z
    }

    internal object Easing {

        val Emphasized: ComposeEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val EmphasizedAccelerate: ComposeEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
        val EmphasizedDecelerate: ComposeEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        val Legacy: ComposeEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val LegacyAccelerate: ComposeEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        val LegacyDecelerate: ComposeEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        val Linear: ComposeEasing = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
        val Standard: ComposeEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val StandardAccelerate: ComposeEasing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
        val StandardDecelerate: ComposeEasing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    }

    internal object Duration {

        const val ExtraLong1: Int = 700
        const val ExtraLong2: Int = 800
        const val ExtraLong3: Int = 900
        const val ExtraLong4: Int = 1000
        const val Long1: Int = 450
        const val Long2: Int = 500
        const val Long3: Int = 550
        const val Long4: Int = 600
        const val Medium1: Int = 250
        const val Medium2: Int = 300
        const val Medium3: Int = 350
        const val Medium4: Int = 400
        const val Short1: Int = 50
        const val Short2: Int = 100
        const val Short3: Int = 150
        const val Short4: Int = 200
    }
}

public val MaterialTheme.motion: Motion
    get() {
        return Motion
    }

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
private fun PreviewSharedAxis() {
    MaterialTheme {
        val contentTransform = MaterialTheme.motion.sharedAxis(density = LocalDensity.current, reverse = false)
        AnimatedVisibility(
            visible = true,
            enter = contentTransform.targetContentEnter,
            exit = contentTransform.initialContentExit,
            label = "first layer"
        ) {
            Text(text = "FIRST LAYER")
        }
        AnimatedVisibility(
            visible = true,
            enter = contentTransform.targetContentEnter,
            exit = contentTransform.initialContentExit,
            label = "second layer"
        ) {
            Text(text = "SECOND LAYER")
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
private fun PreviewFadeThrough() {
    MaterialTheme {
        val contentTransform = MaterialTheme.motion.fadeThrough
        AnimatedVisibility(
            visible = true,
            enter = contentTransform.targetContentEnter,
            exit = contentTransform.initialContentExit,
            label = "first layer"
        ) {
            Text(text = "FIRST AXIS")
        }
        AnimatedVisibility(
            visible = true,
            enter = contentTransform.targetContentEnter,
            exit = contentTransform.initialContentExit,
            label = "second layer"
        ) {
            Text(text = "SECOND AXIS")
        }
    }
}