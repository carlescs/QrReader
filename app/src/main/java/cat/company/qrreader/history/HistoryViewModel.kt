package cat.company.qrreader.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

/**
 * ViewModel for the history
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(val db: BarcodesDb) : ViewModel() {

    private val _selectedTagId = MutableStateFlow<Int?>(null)
    val selectedTagId = _selectedTagId.asStateFlow()

    val savedBarcodes: Flow<List<SavedBarcodeWithTags>> = _selectedTagId.flatMapLatest { tagId ->
        db.savedBarcodeDao().getSavedBarcodesWithTagsByTagId(tagId)
    }

    fun onTagSelected(tagId: Int?) {
        _selectedTagId.value = tagId
    }

    fun updateBarcode(barcode: SavedBarcode) {
        viewModelScope.launch(Dispatchers.IO) {
            val rows = db.savedBarcodeDao().updateItem(barcode)
            Log.d("HistoryViewModel", "updateBarcode id=${barcode.id} rows=$rows")
        }
    }
}

class HistoryViewModelFactory(private val db: BarcodesDb) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
