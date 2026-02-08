package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get all tags
 */
class GetAllTagsUseCase(private val tagRepository: TagRepository) {

    operator fun invoke(): Flow<List<TagModel>> {
        return tagRepository.getAllTags()
    }
}

