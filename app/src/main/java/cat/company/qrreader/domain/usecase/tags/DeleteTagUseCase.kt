package cat.company.qrreader.domain.usecase.tags

import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository

/**
 * Use case to delete a tag
 */
class DeleteTagUseCase(private val tagRepository: TagRepository) {

    suspend operator fun invoke(tag: TagModel) {
        tagRepository.deleteTag(tag)
    }
}

