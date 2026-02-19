package cat.company.qrreader.features.history.presentation.ui.components

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import cat.company.qrreader.features.history.presentation.ui.content.OtherHistoryContent
import cat.company.qrreader.features.history.presentation.ui.content.UrlHistoryContent
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import org.koin.androidx.compose.koinViewModel
import cat.company.qrreader.ui.components.common.Tag
import cat.company.qrreader.ui.components.common.DeleteConfirmDialog
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
    val coroutineScope = rememberCoroutineScope()
    val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    val copiedMsg = stringResource(R.string.copied)

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
                    else
                        OtherHistoryContent(sdf = sdf, barcode = barcode.barcode)
                }
            }
        }

        Row(modifier = Modifier.padding(5.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val menuOpen = remember { mutableStateOf(false) }
            IconButton(onClick = { menuOpen.value = true }) {
                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = stringResource(R.string.manage_tags))
            }
            Spacer(modifier = Modifier.width(4.dp))
            barcode.tags.forEach {
                Tag(it)
            }
            DropdownMenu(
                expanded = menuOpen.value,
                onDismissRequest = { menuOpen.value = false }) {
                val tags = tagsViewModel.tags.collectAsState(initial = emptyList())
                tags.value.forEach { tag ->
                    DropdownMenuItem(text = { Text(text = tag.name) },
                        leadingIcon = {
                            if(barcode.tags.contains(tag))
                                Icon(imageVector = Icons.Filled.Check, contentDescription = stringResource(R.string.check))
                        },
                        onClick = {
                            ioCoroutineScope.launch {
                                // Use injected use case to switch tag
                                switchBarcodeTagUseCase.invoke(barcode, tag)
                                menuOpen.value = false
                            }
                        })
                 }
             }
         }
        Row {
            TextButton(onClick = {
                confirmDeleteOpen.value = true
            }) {
                Text(text = stringResource(R.string.delete))
            }
            TextButton(onClick = {
                editOpen.value = true
            }) {
                Text(text = stringResource(R.string.edit))
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
    }
}
