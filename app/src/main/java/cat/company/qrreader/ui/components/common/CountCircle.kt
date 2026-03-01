package cat.company.qrreader.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MIN_CIRCLE_SIZE = 16.dp

/**
 * A circular or pill-shaped count indicator that displays a number beside an icon.
 *
 * Stays round for counts 1–99 (fixed [MIN_CIRCLE_SIZE] minimum size). For counts above 99 the
 * display text is capped at "99+" to prevent the indicator from growing unbounded.
 *
 * @param count The numeric count to display.
 * @param countDescription Accessibility description for the count (e.g. "5 barcodes").
 */
@Composable
fun CountCircle(count: Int, countDescription: String? = null) {
    val displayText = if (count > 99) "99+" else count.toString()
    val semanticsModifier = if (countDescription != null) {
        Modifier.semantics { contentDescription = countDescription }
    } else {
        Modifier
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = semanticsModifier
            .height(MIN_CIRCLE_SIZE)
            .widthIn(min = MIN_CIRCLE_SIZE)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = displayText,
            color = MaterialTheme.colorScheme.onPrimary,
            style = TextStyle(
                fontSize = 9.sp,
                lineHeight = 9.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}
