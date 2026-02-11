package cat.company.qrreader.features.history.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview

/**
 * ViewModel for the history
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getBarcodesWithTagsUseCase: GetBarcodesWithTagsUseCase,
    private val updateBarcodeUseCase: UpdateBarcodeUseCase,
    private val deleteBarcodeUseCase: DeleteBarcodeUseCase,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedTagId = MutableStateFlow<Int?>(null)
    val selectedTagId = _selectedTagId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Debounce input to avoid querying DB on every keystroke
    @OptIn(FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(250)
        .map { it.trim() }
        .distinctUntilChanged()

    val savedBarcodes: Flow<List<BarcodeWithTagsModel>> =
        combine(
            _selectedTagId,
            debouncedQuery,
            settingsRepository.hideTaggedWhenNoTagSelected,
            settingsRepository.searchAcrossAllTagsWhenFiltering
        ) { tagId, query, hideTagged, searchAcrossAll ->
            Quad(tagId, query, hideTagged, searchAcrossAll)
        }
            .flatMapLatest { (tagId, query, hideTagged, searchAcrossAll) ->
                val q = query.takeIf { it.isNotBlank() }
                val effectiveTagId = if (searchAcrossAll && q != null) null else tagId
                getBarcodesWithTagsUseCase(effectiveTagId, q, hideTagged, searchAcrossAll)
            }

    fun onTagSelected(tagId: Int?) {
        _selectedTagId.value = tagId
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun updateBarcode(barcode: BarcodeModel) {
        viewModelScope.launch {
            val rows = updateBarcodeUseCase(barcode)
            Log.d("HistoryViewModel", "updateBarcode id=${barcode.id} rows=$rows")
        }
    }

    fun deleteBarcode(barcode: BarcodeModel) {
        viewModelScope.launch {
            deleteBarcodeUseCase(barcode)
        }
    }

    // small data holder for combine result
    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
