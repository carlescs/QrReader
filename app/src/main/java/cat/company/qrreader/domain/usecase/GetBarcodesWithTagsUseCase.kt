package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get barcodes with tags filtered by tag ID and search query
 */
class GetBarcodesWithTagsUseCase(private val barcodeRepository: BarcodeRepository) {

    operator fun invoke(
        tagId: Int?,
        query: String?,
        hideTaggedWhenNoTagSelected: Boolean
    ): Flow<List<BarcodeWithTagsModel>> {
        return barcodeRepository.getBarcodesWithTagsByFilter(
            tagId = tagId,
            query = query,
            hideTaggedWhenNoTagSelected = hideTaggedWhenNoTagSelected
        )
    }
}

