package cat.company.qrreader.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.utils.Utils

/**
 * Tag composable
 */
private const val LUMINANCE_THRESHOLD = 0.5f
private const val DARKEN_FACTOR = 0.4f

@Composable
fun Tag(it: TagModel) {
    val color = Utils.parseColor(it.color) ?: MaterialTheme.colorScheme.primary
    // Darken very light colors so the border and icon remain visible against the card background.
    val accentColor = if (color.luminance() > LUMINANCE_THRESHOLD) lerp(color, Color.Black, DARKEN_FACTOR) else color
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, accentColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = it.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

