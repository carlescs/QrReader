package cat.company.qrreader.camera.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.SavedBarcode
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun OtherContent(barcode: Barcode, db: BarcodesDb){
    val coroutineScope= CoroutineScope(Dispatchers.IO)
    val saved = remember{ mutableStateOf(false) }
    Title(title = "Other")
    Text(text = barcode.displayValue ?: "No")
    Spacer(modifier = Modifier.height(20.dp))
    TextButton(onClick = {
        coroutineScope.launch { db.savedBarcodeDao().insertAll(SavedBarcode(type = barcode.valueType, barcode = barcode.displayValue!!)) }
        saved.value=true
    }, enabled = !saved.value) {
        Text(text = if (!saved.value) "Save" else "Saved")
    }
}