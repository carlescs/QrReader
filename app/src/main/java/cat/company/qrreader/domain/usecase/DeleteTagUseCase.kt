package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository

/**
 * Use case to delete a tag
 */
class DeleteTagUseCase(private val tagRepository: TagRepository) {

    operator fun invoke(tag: TagModel) {
        tagRepository.deleteTag(tag)
    }
}

