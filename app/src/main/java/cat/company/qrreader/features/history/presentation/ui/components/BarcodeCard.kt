package cat.company.qrreader.features.history.presentation.ui.components

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.features.history.presentation.HistoryViewModel
import cat.company.qrreader.features.history.presentation.ui.content.ContactHistoryContent
import cat.company.qrreader.features.history.presentation.ui.content.OtherHistoryContent
import cat.company.qrreader.features.history.presentation.ui.content.UrlHistoryContent
import cat.company.qrreader.features.history.presentation.ui.content.WifiHistoryContent
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import org.koin.androidx.compose.koinViewModel
import cat.company.qrreader.ui.components.common.DeleteConfirmDialog
import cat.company.qrreader.ui.components.common.Tag
import cat.company.qrreader.domain.usecase.history.SwitchBarcodeTagUseCase
import org.koin.compose.koinInject
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

/**
 * Card for displaying a barcode
 */
@Composable
fun BarcodeCard(
    clipboardManager: Clipboard,
    barcode: BarcodeWithTagsModel,
    snackBarHostState: SnackbarHostState,
    sdf: SimpleDateFormat,
    historyViewModel: HistoryViewModel,
    tagsViewModel: TagsViewModel = koinViewModel(),
    switchBarcodeTagUseCase: SwitchBarcodeTagUseCase = koinInject()
) {
    val editOpen = remember { mutableStateOf(false) }
    val confirmDeleteOpen = remember { mutableStateOf(false) }
    val tagEditOpen = remember { mutableStateOf(false) }
    val aiDescriptionOpen = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    val copiedMsg = stringResource(R.string.copied)
    val aiGenerationEnabled by historyViewModel.aiGenerationEnabled.collectAsState()
    val allTags by tagsViewModel.tags.collectAsState(initial = emptyList())

    // Load tags
    tagsViewModel.loadTags()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable {
                coroutineScope.launch {
                    clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("barcode", barcode.barcode.barcode)))
                    snackBarHostState.showSnackbar(copiedMsg)
                }
            },
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = getBarcodeIcon(barcode.barcode.type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (barcode.barcode.type == Barcode.TYPE_URL)
                        UrlHistoryContent(sdf = sdf, barcode = barcode.barcode)
                    else if (barcode.barcode.type == Barcode.TYPE_WIFI)
                        WifiHistoryContent(sdf = sdf, barcode = barcode.barcode)
                    else if (barcode.barcode.type == Barcode.TYPE_CONTACT_INFO)
                        ContactHistoryContent(sdf = sdf, barcode = barcode.barcode)
                    else
                        OtherHistoryContent(sdf = sdf, barcode = barcode.barcode)
                }
            }
        }

        // Tag section: collapsed shows only assigned tags; expanded shows all tags as FilterChips
        if (tagEditOpen.value && allTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allTags.forEach { tag ->
                    val isSelected = barcode.tags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            ioCoroutineScope.launch {
                                switchBarcodeTagUseCase.invoke(barcode, tag)
                            }
                        },
                        label = { Text(tag.name) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
            }
        } else if (barcode.tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                barcode.tags.forEach { Tag(it) }
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { confirmDeleteOpen.value = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete_barcode)
                )
            }
            IconButton(onClick = { editOpen.value = true }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.edit_barcode)
                )
            }
            if (allTags.isNotEmpty()) {
                IconButton(onClick = { tagEditOpen.value = !tagEditOpen.value }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = stringResource(R.string.manage_tags),
                        tint = if (barcode.tags.isNotEmpty() || tagEditOpen.value)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (aiGenerationEnabled && !barcode.barcode.aiGeneratedDescription.isNullOrBlank()) {
                IconButton(onClick = { aiDescriptionOpen.value = true }) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = stringResource(R.string.ai_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { historyViewModel.toggleFavorite(barcode.barcode.id, !barcode.barcode.isFavorite) }) {
                Icon(
                    imageVector = if (barcode.barcode.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(
                        if (barcode.barcode.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
                    ),
                    tint = if (barcode.barcode.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (editOpen.value) {
            EditBarcodeDialog(
                savedBarcode = barcode.barcode,
                viewModel = historyViewModel,
                onRequestClose = { editOpen.value = false }
            )
        }
        if (confirmDeleteOpen.value) {
            DeleteConfirmDialog(
                confirmDeleteOpen,
                item = barcode.barcode
            ) {
                ioCoroutineScope.launch {
                    historyViewModel.deleteBarcode(it)
                }
            }
        }
        val description = barcode.barcode.aiGeneratedDescription
        if (aiDescriptionOpen.value && description != null) {
            AiDescriptionDialog(
                description = description,
                onDismiss = { aiDescriptionOpen.value = false }
            )
        }
    }
}
