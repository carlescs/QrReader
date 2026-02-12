package cat.company.qrreader.domain.usecase.history

import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get barcodes with tags using optional tag and text filters.
 */
class GetBarcodesWithTagsUseCase(private val barcodeRepository: BarcodeRepository) {

    /**
     * Get barcodes with tags filtered by tag and/or text.
     *
     * Behavior:
     * - When searchAcrossAllTagsWhenFiltering is true and query is non-blank, searches across all tags
     * - When searchAcrossAllTagsWhenFiltering is false, respects the tagId filter even when searching
     * - The ViewModel determines the effective tagId based on the searchAcrossAllTagsWhenFiltering setting
     *
     * @param tagId ID of the tag to filter by, or null to show all tags
     * @param query Text query used to filter barcodes
     * @param hideTaggedWhenNoTagSelected Whether to hide tagged items when no tag is selected
     * @param searchAcrossAllTagsWhenFiltering Whether to search across all tags when a query is active
     */
    operator fun invoke(
        tagId: Int?,
        query: String?,
        hideTaggedWhenNoTagSelected: Boolean,
        searchAcrossAllTagsWhenFiltering: Boolean
    ): Flow<List<BarcodeWithTagsModel>> {
        return barcodeRepository.getBarcodesWithTagsByFilter(
            tagId = tagId,
            query = query,
            hideTaggedWhenNoTagSelected = hideTaggedWhenNoTagSelected,
            searchAcrossAllTagsWhenFiltering = searchAcrossAllTagsWhenFiltering
        )
    }
}
