package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.tags.DeleteTagUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteTagUseCaseTest {
    @Test
    fun `calls repository delete`() = runTest {
        val tag = TagModel(2, "B", "#111")
        var deleted: TagModel? = null
        val repo = object : TagRepository {
            override fun getAllTags(): Flow<List<TagModel>> = flowOf(emptyList())
            override fun insertTags(vararg tags: TagModel) {}
            override fun updateTag(tag: TagModel) {}
            override suspend fun deleteTag(tag: TagModel) { deleted = tag }
        }

        val uc = DeleteTagUseCase(repo)
        uc(tag)
        assertEquals(tag, deleted)
    }
}

