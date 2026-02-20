package cat.company.qrreader.domain.usecase.tags

import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetOrCreateTagsByNameUseCase
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetOrCreateTagsByNameUseCaseTest {

    private lateinit var fakeTagRepository: FakeTagRepository
    private lateinit var useCase: GetOrCreateTagsByNameUseCase

    // Fake TagRepository implementation
    private class FakeTagRepository : TagRepository {
        private val tagsFlow = MutableStateFlow<List<TagModel>>(emptyList())
        private val tags = mutableListOf<TagModel>()
        val insertedTags = mutableListOf<TagModel>()
        private var nextId = 1

        override fun getAllTags(): Flow<List<TagModel>> = tagsFlow

        override fun insertTags(vararg tags: TagModel) {
            tags.forEach { tag ->
                val newTag = tag.copy(id = nextId++)
                this@FakeTagRepository.tags.add(newTag)
                insertedTags.add(newTag)
            }
            tagsFlow.value = this@FakeTagRepository.tags.toList()
        }

        override fun updateTag(tag: TagModel) {
            val index = tags.indexOfFirst { it.id == tag.id }
            if (index >= 0) {
                tags[index] = tag
                tagsFlow.value = tags.toList()
            }
        }

        override fun deleteTag(tag: TagModel) {
            tags.removeAll { it.id == tag.id }
            tagsFlow.value = tags.toList()
        }

        fun emitTags(newTags: List<TagModel>) {
            tags.clear()
            tags.addAll(newTags)
            tagsFlow.value = tags.toList()
        }
    }

    @Before
    fun setup() {
        fakeTagRepository = FakeTagRepository()
        useCase = GetOrCreateTagsByNameUseCase(fakeTagRepository)
    }

    @Test
    fun invoke_existingTag_returnsExistingTag() = runTest {
        val existingTag = TagModel(id = 1, name = "Shopping", color = "#FF0000")
        fakeTagRepository.emitTags(listOf(existingTag))

        val result = useCase(listOf("Shopping"))

        assertEquals(1, result.size)
        assertEquals(existingTag.id, result[0].id)
        assertEquals("Shopping", result[0].name)
        assertEquals("#FF0000", result[0].color)
        assertTrue(fakeTagRepository.insertedTags.isEmpty())
    }

    @Test
    fun invoke_newTag_createsTag() = runTest {
        val result = useCase(listOf("NewTag"))

        assertEquals(1, result.size)
        assertEquals("NewTag", result[0].name)
        assertEquals("#2196F3", result[0].color)
        assertEquals(1, fakeTagRepository.insertedTags.size)
        assertEquals("NewTag", fakeTagRepository.insertedTags[0].name)
    }

    @Test
    fun invoke_caseInsensitive_returnsExistingTag() = runTest {
        val existingTag = TagModel(id = 1, name = "Shopping", color = "#FF0000")
        fakeTagRepository.emitTags(listOf(existingTag))

        val result = useCase(listOf("shopping"))

        assertEquals(1, result.size)
        assertEquals(existingTag.id, result[0].id)
        assertEquals("Shopping", result[0].name)
        assertTrue(fakeTagRepository.insertedTags.isEmpty())
    }

    @Test
    fun invoke_mixedTags_handlesCorrectly() = runTest {
        val existingTag = TagModel(id = 1, name = "Work", color = "#00FF00")
        fakeTagRepository.emitTags(listOf(existingTag))

        val result = useCase(listOf("Work", "Personal"))

        assertEquals(2, result.size)
        assertEquals(existingTag.id, result[0].id)
        assertEquals("Work", result[0].name)
        assertEquals("Personal", result[1].name)
        assertEquals("#2196F3", result[1].color)
        assertEquals(1, fakeTagRepository.insertedTags.size)
    }

    @Test
    fun invoke_emptyTagName_skipsIt() = runTest {
        val result = useCase(listOf("", "  ", "ValidTag"))

        assertEquals(1, result.size)
        assertEquals("ValidTag", result[0].name)
    }

    @Test
    fun invoke_emptyList_returnsEmptyList() = runTest {
        val result = useCase(emptyList())

        assertTrue(result.isEmpty())
        assertTrue(fakeTagRepository.insertedTags.isEmpty())
    }

    @Test
    fun invoke_newTagWithProvidedColor_usesProvidedColor() = runTest {
        val tagColors = mapOf("NewTag" to "#FF5733")

        val result = useCase(listOf("NewTag"), tagColors)

        assertEquals(1, result.size)
        assertEquals("NewTag", result[0].name)
        assertEquals("#FF5733", result[0].color)
        assertEquals(1, fakeTagRepository.insertedTags.size)
        assertEquals("#FF5733", fakeTagRepository.insertedTags[0].color)
    }

    @Test
    fun invoke_newTagWithoutProvidedColor_usesDefaultColor() = runTest {
        val result = useCase(listOf("NewTag"), emptyMap())

        assertEquals(1, result.size)
        assertEquals("NewTag", result[0].name)
        assertEquals("#2196F3", result[0].color)
    }

    @Test
    fun invoke_mixedTagsWithColors_usesProvidedColorForNewTag() = runTest {
        val existingTag = TagModel(id = 1, name = "Work", color = "#00FF00")
        fakeTagRepository.emitTags(listOf(existingTag))
        val tagColors = mapOf("Work" to "#AABBCC", "Personal" to "#FF5733")

        val result = useCase(listOf("Work", "Personal"), tagColors)

        assertEquals(2, result.size)
        // Existing tag keeps its original color
        assertEquals("#00FF00", result[0].color)
        // New tag uses the provided color
        assertEquals("#FF5733", result[1].color)
    }
}
