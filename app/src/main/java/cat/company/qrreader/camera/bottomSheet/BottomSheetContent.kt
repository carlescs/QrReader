package cat.company.qrreader.camera.bottomSheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
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
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                        .clickable { },
                        shape = RoundedCornerShape(5.dp),
                        elevation = 10.dp) {
                        Column(modifier = Modifier.padding(15.dp)) {
                            when (barcode.valueType) {
                                Barcode.TYPE_URL -> {
                                    UrlBarcodeDisplay(barcode = barcode)
                                }
                                Barcode.TYPE_CONTACT_INFO -> {
                                    ContactBarcodeDisplay(barcode=barcode)
                                }
                                else -> {
                                    Title(title = "Other")
                                    Text(text = barcode.displayValue?: "No")
                                }
                            }
                        }
                    }
                })
            }

        }
    }
}

