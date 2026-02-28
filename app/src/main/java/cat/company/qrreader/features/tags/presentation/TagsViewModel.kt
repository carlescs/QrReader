package cat.company.qrreader.features.tags.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.tags.DeleteTagUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
/**
 * ViewModel for the tags
 */
class TagsViewModel(
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val barcodeRepository: BarcodeRepository,
    private val tagRepository: TagRepository
) : ViewModel() {
    val tags: Flow<List<TagModel>> = getAllTagsUseCase()
    val tagBarcodeCounts: Flow<Map<Int, Int>> = barcodeRepository.getTagBarcodeCounts()
    val favoritesCount: Flow<Int> = barcodeRepository.getFavoritesCount()
    /** No-op kept for call-site compatibility; [tags] is now a stable Flow. */
    fun loadTags() {}
    fun deleteTag(tag: TagModel) {
        viewModelScope.launch {
            deleteTagUseCase(tag)
        }
    }
    fun insertTags(vararg tags: TagModel) {
        viewModelScope.launch {
            tagRepository.insertTags(*tags)
        }
    }
    fun updateTag(tag: TagModel) {
        viewModelScope.launch {
            tagRepository.updateTag(tag)
        }
    }
}
