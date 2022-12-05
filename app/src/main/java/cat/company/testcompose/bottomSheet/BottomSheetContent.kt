package cat.company.testcompose.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun BottomSheetContent(lastBarcode: MutableState<List<Barcode>?>){
    Column(modifier = Modifier.padding(15.dp)) {
        if (lastBarcode.value != null) {
            val barcode=lastBarcode.value?.first()
            if(barcode!=null) {
                when (barcode.valueType) {
                    Barcode.TYPE_URL -> {
                        UrlBarcodeDisplay(barcode = barcode)
                    }
                    Barcode.TYPE_CONTACT_INFO -> {
                        ContactBarcodeDisplay(barcode=barcode)
                    }
                    else -> {
                        Text(text = lastBarcode.value?.first()?.displayValue?: "No")
                    }
                }
            }
        }
    }
}

