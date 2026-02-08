package cat.company.qrreader.features.history.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import java.text.SimpleDateFormat

/**
 * Simplified list item for displaying a barcode in search suggestions
 */
@Composable
fun SimpleBarcodeCard(
    barcode: BarcodeWithTagsModel,
    sdf: SimpleDateFormat,
    onDismissSearch: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = getTitle(barcode.barcode),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = barcode.barcode.barcode,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = sdf.format(barcode.barcode.date),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = getBarcodeIcon(barcode.barcode.type),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onDismissSearch()
            },
        colors = ListItemDefaults.colors()
    )
}

