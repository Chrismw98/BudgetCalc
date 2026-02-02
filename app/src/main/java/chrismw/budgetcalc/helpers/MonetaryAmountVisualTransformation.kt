import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Locale-aware cents formatter:
 * - Uses the device's (or provided) Locale for decimal and grouping separators, and grouping rules.
 * - Assumes input is digits-only with leading zeros trimmed.
 * - Empty raw input -> shows empty.
 *
 * Examples (for en-US):
 * ""         -> ""
 * "1"        -> "0.01"
 * "12"       -> "0.12"
 * "123"      -> "1.23"
 * "12345678" -> "123,456.78"
 *
 * Examples (for de-DE):
 * "12345678" -> "123.456,78"
 *
 * Examples (for hi-IN):
 * "12345678" -> "1,23,456.78" (locale-specific grouping)
 */
class MonetaryAmountVisualTransformation(
    private val locale: Locale = Locale.getDefault()
) : VisualTransformation {

    val decimalSeparator = DecimalFormatSymbols(locale).decimalSeparator
    val groupingSeparator = DecimalFormatSymbols(locale).groupingSeparator
    val separators: Set<Char> = setOf(decimalSeparator, groupingSeparator)

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formatted = formatAsCentsLocaleAware(raw, locale)

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset == 0) return 0
                if (offset >= raw.length) return formatted.length

                // Find where the Nth digit appears in the formatted string
                var digitsFound = 0
                for (i in formatted.indices) {
                    if (formatted[i] !in separators) {
                        digitsFound++
                        if (digitsFound == offset) {
                            // Return position after this digit
                            return i + 1
                        }
                    }
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset == 0) return 0
                return formatted.take(offset).count { it !in separators }.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

/**
 * Build a BigDecimal from "digits" as cents using the provided locale for formatting.
 * This leverages DecimalFormat’s locale-dependent separators and grouping pattern.
 */
private fun formatAsCentsLocaleAware(digits: String, locale: Locale): String {
    if (digits.isEmpty()) return ""

    val normalized = when (digits.length) {
        1 -> "00$digits"
        2 -> "0$digits"
        else -> digits
    }

    val cents = normalized.takeLast(2)
    val integer = normalized.dropLast(2)

    // BigDecimal handles arbitrarily large numbers, so we avoid Long/Double overflow.
    val bd = BigDecimal("$integer.$cents")

    val nf = NumberFormat.getNumberInstance(locale) as DecimalFormat
    nf.minimumFractionDigits = 2
    nf.maximumFractionDigits = 2
    nf.isGroupingUsed = true

    return nf.format(bd)
}
