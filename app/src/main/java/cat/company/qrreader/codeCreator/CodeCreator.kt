package cat.company.qrreader.codeCreator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore.Images.Media
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cat.company.qrreader.events.SharedEvents
import io.github.g0dkar.qrcode.QRCode
import java.io.ByteArrayOutputStream


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
        TextField(
            value = text.value, onValueChange = {
                text.value = it
                if (text.value.isEmpty()) {
                    image.value = null
                } else {
                    val bos = ByteArrayOutputStream()
                    QRCode(it).render().writeImage(bos)
                    image.value =
                        BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)
                }
            },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        if (image.value != null)
            Image(
                image.value!!.asImageBitmap(), contentDescription = null, modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
    }
}

private fun shareImage(
    context: Context,
    bitmap: Bitmap?
) {
    if(bitmap == null) return
    val share = Intent(Intent.ACTION_SEND)
    share.type = "image/jpeg"
    val values = ContentValues()
    values.put(Media.TITLE, "QrCode")
    values.put(Media.MIME_TYPE, "image/jpeg")
    val uri = context.contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values) ?: return
    val outputStream = context.contentResolver.openOutputStream(uri) ?: return
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    share.putExtra(Intent.EXTRA_STREAM, uri)
    context.startActivity(Intent.createChooser(share, "Share Image"))
}