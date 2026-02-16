package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cat.company.qrreader.domain.model.SuggestedTagModel

/**
 * Display suggested tags that can be toggled
 */
@Composable
fun SuggestedTagsSection(
    suggestedTags: List<SuggestedTagModel>,
    isLoading: Boolean,
    error: String?,
    onToggleTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Suggested Tags",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        when {
            isLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generating suggestions...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            error != null -> {
                // Display user-friendly error message
                val errorMessage = when {
                    error.contains("not available on this device", ignoreCase = true) ||
                    error.contains("UnsupportedOperation", ignoreCase = true) -> {
                        "AI suggestions not supported on this device"
                    }
                    error.contains("downloading", ignoreCase = true) ||
                    error.contains("download", ignoreCase = true) -> {
                        "AI model downloading... Try again shortly"
                    }
                    else -> "Tag suggestions unavailable"
                }
                
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            suggestedTags.isEmpty() -> {
                Text(
                    text = "No tag suggestions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedTags.forEach { tag ->
                        SuggestedTagChip(
                            tag = tag,
                            onClick = { onToggleTag(tag.name) }
                        )
                    }
                }
                Text(
                    text = "Tap to remove unwanted tags",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SuggestedTagChip(
    tag: SuggestedTagModel,
    onClick: () -> Unit
) {
    val backgroundColor = if (tag.isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val contentColor = if (tag.isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val borderColor = if (tag.isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
