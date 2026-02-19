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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.SuggestedTagModel
import androidx.core.graphics.toColorInt

/**
 * Display suggested tags that can be toggled.
 * The section is hidden entirely when AI features are not available on this device.
 */
@Composable
fun SuggestedTagsSection(
    suggestedTags: List<SuggestedTagModel>,
    isLoading: Boolean,
    error: String?,
    onToggleTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAiUnavailable = error != null && (
        error.contains("not available on this device", ignoreCase = true) ||
        error.contains("UnsupportedOperation", ignoreCase = true)
    )
    if (isAiUnavailable) return

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.suggested_tags),
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
                        text = stringResource(R.string.generating_suggestions),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            error != null -> {
                val errorMessage = when {
                    error.contains("not available on this device", ignoreCase = true) ||
                    error.contains("UnsupportedOperation", ignoreCase = true) ->
                        stringResource(R.string.ai_suggestions_not_supported)
                    error.contains("downloading", ignoreCase = true) ||
                    error.contains("download", ignoreCase = true) ->
                        stringResource(R.string.ai_model_downloading_retry)
                    else -> stringResource(R.string.tag_suggestions_unavailable)
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
                    text = stringResource(R.string.no_tag_suggestions),
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
                    text = stringResource(R.string.tap_to_remove_unwanted_tags),
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
    // Parse the tag's hex color
    val tagColor = try {
        Color(tag.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primaryContainer
    }

    val backgroundColor = if (tag.isSelected) {
        tagColor
    } else {
        tagColor.copy(alpha = 0.3f)
    }
    
    // Calculate luminance to determine if we need dark or light text
    val luminance = (0.299f * tagColor.red + 0.587f * tagColor.green + 0.114f * tagColor.blue)
    val contentColor = if (tag.isSelected) {
        if (luminance > 0.5f) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val borderColor = if (tag.isSelected) {
        tagColor.copy(alpha = 0.8f)
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
