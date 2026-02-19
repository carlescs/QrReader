package cat.company.qrreader.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A text composable that truncates at [collapsedMaxLines] lines and shows a
 * "Show more" / "Show less" toggle when the content overflows.
 *
 * @param text The text to display.
 * @param modifier Modifier for the outer [Column].
 * @param collapsedMaxLines Maximum number of lines shown in the collapsed state.
 * @param style Typography style applied to the text.
 */
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 3,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var expanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = style,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) {
                    hasOverflow = result.hasVisualOverflow
                }
            }
        )
        if (hasOverflow || expanded) {
            Text(
                text = if (expanded) "Show less" else "Show more",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 2.dp)
            )
        }
    }
}
