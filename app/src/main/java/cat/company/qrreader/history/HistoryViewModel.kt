package cat.company.qrreader.history

import androidx.lifecycle.ViewModel
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import kotlinx.coroutines.flow.Flow

class HistoryViewModel(val db: BarcodesDb) : ViewModel() {

    lateinit var savedBarcodes: Flow<List<SavedBarcodeWithTags>>

    fun loadBarcodesByTagId(tagId: Int?) {
        savedBarcodes = db.savedBarcodeDao().getSavedBarcodesWithTagsByTagId(tagId)
    }
}
