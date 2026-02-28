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
     * Whether AI-generated features (tag suggestions and barcode descriptions) are enabled
     */
    val aiGenerationEnabled: Flow<Boolean>

    /**
     * Enable or disable AI-generated features (tag suggestions and barcode descriptions)
     */
    suspend fun setAiGenerationEnabled(value: Boolean)

    /**
     * The language code for AI-generated texts and tags (e.g., "en", "es", "fr").
     * The special value `"device"` (the default) resolves to the current device locale language
     * at prompt-generation time.
     */
    val aiLanguage: Flow<String>

    /**
     * Set the language for AI-generated texts and tags
     */
    suspend fun setAiLanguage(value: String)

    /**
     * Whether AI-generated barcode descriptions should use a humorous tone.
     * When `true`, Gemini Nano is asked to write funny descriptions instead of factual ones.
     * Defaults to `false`.
     */
    val aiHumorousDescriptions: Flow<Boolean>

    /**
     * Enable or disable humorous AI-generated barcode descriptions
     */
    suspend fun setAiHumorousDescriptions(value: Boolean)
}
