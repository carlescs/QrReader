package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.ui.components.common.ExpandableText

/**
 * Section displaying AI-generated barcode description with loading and error states
 */
@Composable
fun BarcodeDescriptionSection(
    description: String?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    // Only show if there's content, loading, or error
    if (description == null && !isLoading && error == null) {
        return
    }
    
    Column(modifier = modifier) {
        // Header with AI icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = stringResource(R.string.ai_generated),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.ai_description),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Content based on state
        when {
            isLoading -> {
                LoadingDescription()
            }
            error != null -> {
                ErrorDescription(error)
            }
            description != null -> {
                DescriptionText(description)
            }
        }
    }
}

@Composable
private fun LoadingDescription() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.generating_description),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorDescription(error: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = stringResource(R.string.error),
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(parseErrorMessageRes(error)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DescriptionText(description: String) {
    ExpandableText(
        text = description,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Parse error message to show user-friendly text, returning a string resource ID
 */
@StringRes
private fun parseErrorMessageRes(error: String): Int {
    return when {
        error.contains("not available on this device", ignoreCase = true) ->
            R.string.not_available_on_device
        error.contains("downloading", ignoreCase = true) ||
        error.contains("download", ignoreCase = true) ->
            R.string.ai_model_downloading
        error.contains("temporarily unavailable", ignoreCase = true) ->
            R.string.temporarily_unavailable
        else -> R.string.could_not_generate
    }
}
