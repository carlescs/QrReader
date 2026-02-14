package cat.company.qrreader.domain.usecase.tags

import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import kotlinx.coroutines.flow.first

/**
 * Use case to get or create tags by name
 * If a tag with the given name doesn't exist, creates it with a default color
 */
class GetOrCreateTagsByNameUseCase(
    private val tagRepository: TagRepository
) {
    /**
     * Get or create tags by name
     * @param tagNames List of tag names to get or create
     * @return List of TagModel (existing or newly created)
     */
    suspend operator fun invoke(tagNames: List<String>): List<TagModel> {
        val existingTags = tagRepository.getAllTags().first()
        val result = mutableListOf<TagModel>()
        
        tagNames.forEach { name ->
            val normalizedName = name.trim()
            if (normalizedName.isEmpty()) return@forEach
            
            // Try to find existing tag (case-insensitive)
            val existingTag = existingTags.find { 
                it.name.equals(normalizedName, ignoreCase = true) 
            }
            
            if (existingTag != null) {
                result.add(existingTag)
            } else {
                // Create new tag with default color (blue)
                val newTag = TagModel(
                    name = normalizedName,
                    color = "#2196F3" // Material Blue 500
                )
                tagRepository.insertTags(newTag)
                // Get the newly created tag with its ID
                val updatedTags = tagRepository.getAllTags().first()
                val createdTag = updatedTags.find { 
                    it.name.equals(normalizedName, ignoreCase = true) 
                }
                if (createdTag != null) {
                    result.add(createdTag)
                }
            }
        }
        
        return result
    }
}
