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
}

