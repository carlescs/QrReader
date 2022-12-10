package cat.company.qrreader.history

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cat.company.qrreader.camera.bottomSheet.Title
import cat.company.qrreader.db.entities.SavedBarcode
import java.text.SimpleDateFormat

@Composable
fun OtherHistoryContent(sdf:SimpleDateFormat, barcode:SavedBarcode){
    Title(title = "Other")
    Text(text = sdf.format(barcode.date))
    Text(text = barcode.barcode)
}