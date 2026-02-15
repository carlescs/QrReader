package cat.company.qrreader.features.camera.presentation.ui.components

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import cat.company.qrreader.features.camera.presentation.BarcodeState
import cat.company.qrreader.features.camera.presentation.QrCameraViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Content of the bottom sheet - displays scanned barcodes
 */
@Composable
fun BottomSheetContent(
    lastBarcode: List<Barcode>?,
    snackbarHostState: SnackbarHostState,
    viewModel: QrCameraViewModel = koinViewModel()
) {
    Column(
        modifier = Modifier
            .padding(15.dp)
            .defaultMinSize(minHeight = 250.dp)
    ) {
        val clipboard: Clipboard = LocalClipboard.current
        val coroutineScope = rememberCoroutineScope()
        
        if (lastBarcode != null) {
            LazyColumn(modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
                items(
                    items = lastBarcode,
                    key = { it.hashCode() },
                    itemContent = { barcode ->
                        val barcodeHash = barcode.hashCode()
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    if (barcode.displayValue != null) {
                                        coroutineScope.launch {
                                            clipboard.setClipEntry(
                                                ClipEntry(
                                                    ClipData.newPlainText(
                                                        "Barcode",
                                                        barcode.displayValue
                                                    )
                                                )
                                            )
                                        }
                                    }
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Copied!") }
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                        ) {
                            Column(modifier = Modifier.padding(15.dp)) {
                                // Show suggested tags for THIS specific barcode
                                SuggestedTagsSection(
                                    suggestedTags = viewModel.getSuggestedTags(barcodeHash),
                                    isLoading = viewModel.isLoadingTags(barcodeHash),
                                    error = viewModel.getTagError(barcodeHash),
                                    onToggleTag = { tagName ->
                                        viewModel.toggleTagSelection(barcodeHash, tagName)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                                
                                if (viewModel.getSuggestedTags(barcodeHash).isNotEmpty()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                                
                                val selectedTagNames = viewModel.getSelectedTagNames(barcodeHash)
                                when (barcode.valueType) {
                                    Barcode.TYPE_URL -> {
                                        UrlBarcodeDisplay(
                                            barcode = barcode,
                                            selectedTagNames = selectedTagNames
                                        )
                                    }
                                    Barcode.TYPE_CONTACT_INFO -> {
                                        ContactBarcodeDisplay(barcode = barcode)
                                    }
                                    else -> {
                                        OtherContent(
                                            barcode = barcode,
                                            selectedTagNames = selectedTagNames
                                        )
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }
}

