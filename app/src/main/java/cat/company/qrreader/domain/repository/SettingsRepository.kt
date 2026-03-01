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

    /**
     * Whether tag counters (barcode counts) should be shown next to tags in the history filter.
     * Defaults to `true`.
     */
    val showTagCounters: Flow<Boolean>

    /**
     * Enable or disable tag counter badges in the history filter
     */
    suspend fun setShowTagCounters(value: Boolean)

    /**
     * Whether biometric lock for sensitive barcodes is enabled
     */
    val biometricLockEnabled: Flow<Boolean>

    /**
     * Enable or disable the biometric lock feature
     */
    suspend fun setBiometricLockEnabled(value: Boolean)

    /**
     * Whether duplicate scan detection is enabled. When true, the user is prompted before
     * saving a barcode that already exists in history (case-insensitive match).
     * Defaults to true.
     */
    val duplicateCheckEnabled: Flow<Boolean>

    /**
     * Enable or disable duplicate scan detection
     */
    suspend fun setDuplicateCheckEnabled(value: Boolean)
}
