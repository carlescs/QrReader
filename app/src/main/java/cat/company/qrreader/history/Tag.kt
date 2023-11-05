package cat.company.qrreader.history

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
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.utils.Utils

@Composable
fun Tag(it: Tag) {
    val color = Utils.parseColor(it.color)
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .padding(5.dp),

        colors = if (color != null) CardDefaults.cardColors(
            containerColor = color
        ) else CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            modifier = Modifier.padding(5.dp),
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