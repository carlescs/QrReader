package cat.company.qrreader.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.SavedBarcode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(val db: BarcodesDb):ViewModel() {
    private val TIMEOUT_MILLIS: Long = 5000

    lateinit var savedBarcodes:StateFlow<List<SavedBarcode>>

    fun loadBarcodes(){
        savedBarcodes=db.savedBarcodeDao().getAll()
            .stateIn(viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = emptyList())
    }
}
