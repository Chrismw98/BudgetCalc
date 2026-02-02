package chrismw.budgetcalc.helpers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec

/**
 * A helper class to animate Double values since [Animatable] only supports Float and Int.
 */
class DoubleAnimatable(initialValue: Double) {

    private val fraction = Animatable(1f) // always animate from 0f → 1f
    private var start = initialValue
    private var end = initialValue

    val value: Double
        get() = lerpDouble(start, end, fraction.value)

    suspend fun snapTo(newValue: Double) {
        start = newValue
        end = newValue
        fraction.snapTo(1f) // 1f = resolved end
    }

    suspend fun animateTo(
        targetValue: Double,
        animationSpec: AnimationSpec<Float>
    ) {
        start = value        // current resolved position
        end = targetValue    // new desired position

        fraction.snapTo(0f)
        fraction.animateTo(1f, animationSpec)
    }
}

fun lerpDouble(start: Double, end: Double, fraction: Float): Double {
    return start + (end - start) * fraction
}