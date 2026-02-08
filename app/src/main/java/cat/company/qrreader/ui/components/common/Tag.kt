package cat.company.qrreader.ui.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.utils.Utils

/**
 * Tag composable
 */
@Composable
fun Tag(it: TagModel) {
    val color = Utils.parseColor(it.color)
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .padding(2.dp),

        colors = if (color != null) CardDefaults.cardColors(
            containerColor = color
        ) else CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = it.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                color == null || color.luminance() > 0.5f -> Color.Black
                else -> Color.White
            }
        )
    }
}

