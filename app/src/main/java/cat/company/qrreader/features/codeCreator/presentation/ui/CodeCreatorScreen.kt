package cat.company.qrreader.features.codeCreator.presentation.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.print.PrintHelper
import cat.company.qrreader.domain.usecase.codecreator.SaveBitmapToMediaStoreUseCase
import cat.company.qrreader.events.SharedEvents
import cat.company.qrreader.features.codeCreator.presentation.CodeCreatorViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Create a QR code from a text
 */
@Composable
fun CodeCreatorScreen(viewModel: CodeCreatorViewModel = koinViewModel()) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsStateWithLifecycle()
    val isSharing by viewModel.isSharing.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val saveBitmapUseCase: SaveBitmapToMediaStoreUseCase = koinInject()
    var showWifiDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showAssistantMenu by remember { mutableStateOf(false) }

    // Update shared events based on text state
    LaunchedEffect(text) {
        val isEmpty = text.isEmpty()
        SharedEvents.onShareIsDisabled?.invoke(isEmpty)
        SharedEvents.onPrintIsDisabled?.invoke(isEmpty)
    }

    // Setup share and print handlers
    SharedEvents.onShareClick = {
        if (!isSharing && qrCodeBitmap != null) {
            shareQrCode(context, qrCodeBitmap!!, saveBitmapUseCase, viewModel)
        }
    }

    SharedEvents.onPrintClick = {
        if (!isSharing && qrCodeBitmap != null) {
            printQrCode(context, qrCodeBitmap!!, saveBitmapUseCase, viewModel)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TextField(
            value = text,
            onValueChange = { viewModel.onTextChanged(it) },
            label = { Text(stringResource(R.string.enter_text_to_encode)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusRequester(focusRequester),
            singleLine = true,
            leadingIcon = {
                Box {
                    IconButton(onClick = { showAssistantMenu = !showAssistantMenu }) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = stringResource(R.string.qr_code_assistants)
                        )
                    }
                    DropdownMenu(
                        expanded = showAssistantMenu,
                        onDismissRequest = { showAssistantMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.wifi_qr_assistant)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Wifi,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showAssistantMenu = false
                                showWifiDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.contact_qr_assistant)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Contacts,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showAssistantMenu = false
                                showContactDialog = true
                            }
                        )
                    }
                }
            },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.clearText()
                        focusRequester.requestFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White)
        ) {
            qrCodeBitmap?.let { bitmap ->
                Image(
                    bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
    }

    if (showWifiDialog) {
        WifiAssistantDialog(
            onDismiss = { showWifiDialog = false },
            onGenerate = { wifiText ->
                viewModel.onTextChanged(wifiText)
                showWifiDialog = false
            }
        )
    }

    if (showContactDialog) {
        ContactAssistantDialog(
            onDismiss = { showContactDialog = false },
            onGenerate = { contactText ->
                viewModel.onTextChanged(contactText)
                showContactDialog = false
            }
        )
    }
}

private fun shareQrCode(
    context: Context,
    bitmap: Bitmap,
    saveBitmapUseCase: SaveBitmapToMediaStoreUseCase,
    viewModel: CodeCreatorViewModel
) {
    try {
        viewModel.setSharing(true)
        val uri = saveBitmapUseCase(context, bitmap) ?: return

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
    } finally {
        viewModel.setSharing(false)
    }
}

private fun printQrCode(
    context: Context,
    bitmap: Bitmap,
    saveBitmapUseCase: SaveBitmapToMediaStoreUseCase,
    viewModel: CodeCreatorViewModel
) {
    try {
        viewModel.setSharing(true)
        val uri = saveBitmapUseCase(context, bitmap) ?: return

        PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
        }.printBitmap("QrCode", uri)
    } finally {
        viewModel.setSharing(false)
    }
}

@Composable
@Preview
fun CodeCreatorScreenPreview() {
    CodeCreatorScreen()
}
