package cat.company.qrreader.features.tags.presentation
import androidx.lifecycle.ViewModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.usecase.tags.DeleteTagUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import cat.company.qrreader.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent.inject
/**
 * ViewModel for the tags
 */
class TagsViewModel(
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val deleteTagUseCase: DeleteTagUseCase
) : ViewModel() {
    private val tagRepository: TagRepository by inject(TagRepository::class.java)
    lateinit var tags: Flow<List<TagModel>>
    fun loadTags() {
        tags = getAllTagsUseCase()
    }
    fun deleteTag(tag: TagModel) {
        deleteTagUseCase(tag)
    }
    fun insertTags(vararg tags: TagModel) {
        tagRepository.insertTags(*tags)
    }
    fun updateTag(tag: TagModel) {
        tagRepository.updateTag(tag)
    }
}
