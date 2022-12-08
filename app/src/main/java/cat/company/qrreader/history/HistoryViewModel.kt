package cat.company.qrreader.history

import androidx.lifecycle.ViewModel
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.SavedBarcode
import kotlinx.coroutines.flow.Flow

class HistoryViewModel(val db: BarcodesDb):ViewModel() {
    lateinit var savedBarcodes:Flow<List<SavedBarcode>>

    fun loadBarcodes(){
        savedBarcodes=db.savedBarcodeDao().getAll()
    }
}
