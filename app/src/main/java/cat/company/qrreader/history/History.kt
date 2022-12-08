package cat.company.qrreader.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cat.company.qrreader.db.BarcodesDb

@Composable
fun History(db: BarcodesDb, viewModel: HistoryViewModel=HistoryViewModel(db = db)){
    viewModel.loadBarcodes()
    val state by viewModel.savedBarcodes.collectAsState(initial = emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize()){
        items(items = state){
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(text = it.barcode)
            }
        }
    }
}