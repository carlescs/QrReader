package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllTagsUseCaseTest {
    @Test
    fun `returns repository flow`() = runTest {
        val tags = listOf(TagModel(1, "A", "#000"))
        val repo = object : TagRepository {
            override fun getAllTags(): Flow<List<TagModel>> = flowOf(tags)
            override fun insertTags(vararg tags: TagModel) {}
            override fun updateTag(tag: TagModel) {}
            override fun deleteTag(tag: TagModel) {}
        }

        val uc = GetAllTagsUseCase(repo)
        val emitted = uc().first()
        assertEquals(tags, emitted)
    }
}

