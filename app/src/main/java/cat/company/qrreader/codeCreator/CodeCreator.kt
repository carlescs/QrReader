package cat.company.qrreader.codeCreator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore.Images.Media
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.print.PrintHelper
import cat.company.qrreader.events.SharedEvents
import qrcode.QRCode
import java.io.ByteArrayOutputStream

/**
 * Create a QR code from a text

 */
@Composable
fun CodeCreator() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val text = remember { mutableStateOf("") }
        val image: MutableState<Bitmap?> = remember { mutableStateOf(null) }
        val context = LocalContext.current
        val sharing = remember { mutableStateOf(false) }
        val focusRequester=remember{FocusRequester()}
        SharedEvents.onShareIsDisabled?.invoke(text.value.isEmpty())
        SharedEvents.onShareClick = {
            if (!sharing.value) {
                try {
                    sharing.value = true
                    shareImage(context, image.value)
                } finally {
                    sharing.value = false
                }
            }
        }
        SharedEvents.onPrintIsDisabled?.invoke(text.value.isEmpty())
        SharedEvents.onPrintClick = {
            if (!sharing.value) {
                try {
                    sharing.value = true
                    printImage(context, image.value)
                } finally {
                    sharing.value = false
                }
            }
        }
        TextField(
            value = text.value, onValueChange = {
                text.value = it
                if (text.value.isEmpty()) {
                    image.value = null
                    SharedEvents.onShareIsDisabled?.invoke(true)
                    SharedEvents.onPrintIsDisabled?.invoke(true)
                } else {
                    val bos = ByteArrayOutputStream()
                    QRCode(it).render().writeImage(bos)
                    image.value =
                        BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)
                    SharedEvents.onShareIsDisabled?.invoke(false)
                    SharedEvents.onPrintIsDisabled?.invoke(false)
                }
            },
            label = { Text("Enter text to encode") },
            modifier = Modifier.fillMaxWidth()
                .padding(vertical=8.dp)
                .focusRequester(focusRequester), singleLine = true,
            trailingIcon = {
                if (text.value.isNotEmpty())
                    IconButton(onClick = {
                        text.value = ""
                        image.value = null
                        SharedEvents.onShareIsDisabled?.invoke(true)
                        focusRequester.requestFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear"
                        )
                    }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White)
        ) {
            if (image.value != null)
                Image(
                    image.value!!.asImageBitmap(), contentDescription = null, modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
        }
    }
}

private fun shareImage(
    context: Context,
    bitmap: Bitmap?
) {
    if(bitmap == null) return
    val share = Intent(Intent.ACTION_SEND)
    share.type = "image/jpeg"
    val uri= generateUriFromBitmap(context, bitmap) ?: return
    share.putExtra(Intent.EXTRA_STREAM, uri)
    context.startActivity(Intent.createChooser(share, "Share Image"))
}

private fun printImage(context: Context,bitmap: Bitmap?){
    if(bitmap == null) return
    val uri= generateUriFromBitmap(context, bitmap) ?: return
    PrintHelper(context).apply {
        scaleMode = PrintHelper.SCALE_MODE_FIT
    }.also {
        it.printBitmap("QrCode", uri)
    }
}

private fun generateUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
    val values = ContentValues()
    values.put(Media.TITLE, "QrCode")
    values.put(Media.MIME_TYPE, "image/jpeg")
    val uri = context.contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values) ?: return null
    val outputStream = context.contentResolver.openOutputStream(uri) ?: return null
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    return uri
}
@Composable
@Preview
fun CodeCreatorPreview() {
    CodeCreator()
}
