package cat.company.testcompose.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun BottomSheetContent(lastBarcode: MutableState<List<Barcode>?>){
    Column(modifier = Modifier
        .padding(15.dp)
        .defaultMinSize(minHeight = 250.dp)) {
        if (lastBarcode.value != null) {
            LazyColumn(modifier = Modifier.fillMaxHeight()){
                items(items=lastBarcode.value!!, itemContent = { barcode ->
                    when (barcode.valueType) {
                        Barcode.TYPE_URL -> {
                            UrlBarcodeDisplay(barcode = barcode)
                        }
                        Barcode.TYPE_CONTACT_INFO -> {
                            ContactBarcodeDisplay(barcode=barcode)
                        }
                        else -> {
                            Title(title = "Other")
                            Text(text = lastBarcode.value?.first()?.displayValue?: "No")
                        }
                    }
                })
            }

        }
    }
}

