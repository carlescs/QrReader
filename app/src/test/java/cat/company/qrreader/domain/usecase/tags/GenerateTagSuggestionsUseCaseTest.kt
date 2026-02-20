package cat.company.qrreader.domain.usecase.tags

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GenerateTagSuggestionsUseCaseTest {
    @Test
    fun parseTagNames_handlesCodeFenceJson() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val raw = """
            ```json
            {"tags": ["Work", "Travel", "Health"]}
            ```
        """.trimIndent()

        val tags = useCase.parseTagNames(raw)

        assertEquals(listOf("Work", "Travel", "Health"), tags)
    }

    @Test
    fun parseTagNames_fallsBackToCommaSeparated() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val raw = "Work, Travel , \"Health\""

        val tags = useCase.parseTagNames(raw)

        assertEquals(listOf("Work", "Travel", "Health"), tags)
    }
}
