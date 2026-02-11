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
     * - If `query` is null or blank (i.e. `query == null || query.isBlank()`), the `tagId`
     *   is forwarded to the repository and can be used to filter by tag (no active text search).
     * - If `query` contains non-whitespace characters (is non-blank), `tagId` is set to `null`
     *   and ignored by the repository; filtering is then based only on `query`
     *   (and `hideTaggedWhenNoTagSelected`).
     *
     * @param tagId ID of the tag to filter by when no non-blank query is provided.
     * @param query Text query used to filter barcodes; when non-blank, tag filtering is disabled.
     * @param hideTaggedWhenNoTagSelected Whether to hide tagged items when no tag is effectively selected.
     */
    operator fun invoke(
        tagId: Int?,
        query: String?,
        hideTaggedWhenNoTagSelected: Boolean,
        searchAcrossAllTagsWhenFiltering: Boolean
    ): Flow<List<BarcodeWithTagsModel>> {
        return barcodeRepository.getBarcodesWithTagsByFilter(
            tagId = if (query == null || query.isBlank()) tagId else null,
            query = query,
            hideTaggedWhenNoTagSelected = hideTaggedWhenNoTagSelected,
            searchAcrossAllTagsWhenFiltering = searchAcrossAllTagsWhenFiltering
        )
    }
}
