package cat.company.qrreader.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for settings operations
 */
interface SettingsRepository {

    /**
     * Get the hide tagged when no tag selected setting
     */
    val hideTaggedWhenNoTagSelected: Flow<Boolean>

    /**
     * Set the hide tagged when no tag selected setting
     */
    suspend fun setHideTaggedWhenNoTagSelected(value: Boolean)

    /**
     * Whether searching should ignore the selected tag filter and search across all tags
     */
    val searchAcrossAllTagsWhenFiltering: Flow<Boolean>

    /**
     * Set whether searching should ignore the selected tag filter
     */
    suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean)

    /**
     * Whether AI-generated tag suggestions are enabled
     */
    val aiTagSuggestionsEnabled: Flow<Boolean>

    /**
     * Enable or disable AI-generated tag suggestions
     */
    suspend fun setAiTagSuggestionsEnabled(value: Boolean)

    /**
     * Whether AI-generated barcode descriptions are enabled
     */
    val aiDescriptionsEnabled: Flow<Boolean>

    /**
     * Enable or disable AI-generated barcode descriptions
     */
    suspend fun setAiDescriptionsEnabled(value: Boolean)
}
