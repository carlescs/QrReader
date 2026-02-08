package cat.company.qrreader.features.codeCreator.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.usecase.GenerateQrCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for QR Code Creator screen
 */
class CodeCreatorViewModel(
    private val generateQrCodeUseCase: GenerateQrCodeUseCase
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()

    private val _isSharing = MutableStateFlow(false)
    val isSharing: StateFlow<Boolean> = _isSharing.asStateFlow()

    fun onTextChanged(newText: String) {
        _text.value = newText
        generateQrCode(newText)
    }

    fun clearText() {
        _text.value = ""
        _qrCodeBitmap.value = null
    }

    private fun generateQrCode(text: String) {
        viewModelScope.launch {
            _qrCodeBitmap.value = generateQrCodeUseCase(text)
        }
    }

    fun setSharing(isSharing: Boolean) {
        _isSharing.value = isSharing
    }
}

