package cat.company.qrreader.codeCreator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.unit.dp
import io.github.g0dkar.qrcode.QRCode
import java.io.ByteArrayOutputStream

@Composable
fun CodeCreator() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)) {
        val text = remember { mutableStateOf("") }
        val image: MutableState<Bitmap?> = remember { mutableStateOf(null) }
        TextField(
            value = text.value, onValueChange = {
                text.value = it
                if(text.value.isEmpty()) {
                    image.value = null
                }
                else {
                    val bos = ByteArrayOutputStream()
                    QRCode(it).render().writeImage(bos)
                    image.value =
                        BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)
                }
            },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        if(image.value != null)
            Image(image.value!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().fillMaxHeight())
    }
}