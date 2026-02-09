package cat.company.qrreader.domain.usecase.history

import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get barcodes with tags using optional tag and text filters.
 *
 * Behavior:
 * - If [query] is a non-null blank string (i.e. `query.trim() == ""`), the [tagId]
 *   is forwarded to the repository and can be used to filter by tag.
 * - If [query] is non-blank or `null`, [tagId] is set to `null` and ignored by the
 *   repository; filtering is then based only on [query] (and [hideTaggedWhenNoTagSelected]).
 *
 * @param tagId ID of the tag to filter by when no non-blank query is provided.
 * @param query Text query used to filter barcodes; when non-blank or `null`, tag filtering is disabled.
 * @param hideTaggedWhenNoTagSelected Whether to hide tagged items when no tag is effectively selected.
 */
class GetBarcodesWithTagsUseCase(private val barcodeRepository: BarcodeRepository) {

    operator fun invoke(
        tagId: Int?,
        query: String?,
        hideTaggedWhenNoTagSelected: Boolean
    ): Flow<List<BarcodeWithTagsModel>> {
        return barcodeRepository.getBarcodesWithTagsByFilter(
            tagId = if (query == null || query.isBlank()) tagId else null,
            query = query,
            hideTaggedWhenNoTagSelected = hideTaggedWhenNoTagSelected
        )
    }
}

