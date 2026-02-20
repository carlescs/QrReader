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

    @Test
    fun parseTagNames_handlesEmptyResponse() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val raw = ""

        val tags = useCase.parseTagNames(raw)

        assertEquals(emptyList<String>(), tags)
    }

    @Test
    fun parseTagNames_handlesWhitespaceOnlyResponse() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val raw = "   \n\t  "

        val tags = useCase.parseTagNames(raw)

        assertEquals(emptyList<String>(), tags)
    }

    @Test
    fun parseTagNames_handlesExtraTextBeforeJsonFence() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val raw = """
            Here's your result:
            ```json
            {"tags":["Work"]}
            ```
        """.trimIndent()

        val tags = useCase.parseTagNames(raw)

        assertEquals(listOf("Work"), tags)
    }

    @Test
    fun parseTagNames_handlesJsonWithPreambleWithoutFence() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val raw = "Sure! Here are the tags: {\"tags\":[\"Work\"]}"

        val tags = useCase.parseTagNames(raw)

        assertEquals(listOf("Work"), tags)
    }

    @Test
    fun parseTagNames_handlesMalformedJsonInsideFenceGracefully() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val raw = """
            ```json
            {"tags": ["Work"
            ```
        """.trimIndent()

        val tags = useCase.parseTagNames(raw)

        assertEquals(emptyList<String>(), tags)
    }
}
