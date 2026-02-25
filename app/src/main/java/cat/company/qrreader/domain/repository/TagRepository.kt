package cat.company.qrreader.domain.repository

import cat.company.qrreader.domain.model.TagModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for tag operations
 */
interface TagRepository {

    /**
     * Get all tags
     */
    fun getAllTags(): Flow<List<TagModel>>

    /**
     * Insert tags
     */
    suspend fun insertTags(vararg tags: TagModel)

    /**
     * Update a tag
     */
    suspend fun updateTag(tag: TagModel)

    /**
     * Delete a tag
     */
    suspend fun deleteTag(tag: TagModel)
}

