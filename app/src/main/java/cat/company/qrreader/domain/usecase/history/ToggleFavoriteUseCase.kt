package cat.company.qrreader.domain.usecase.history

import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to toggle the favorite state of a barcode
 */
class ToggleFavoriteUseCase(private val barcodeRepository: BarcodeRepository) {

    suspend operator fun invoke(barcodeId: Int, isFavorite: Boolean) {
        barcodeRepository.toggleFavorite(barcodeId, isFavorite)
    }
}
