package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.settings.GetAiDescriptionsEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiTagSuggestionsEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiDescriptionsEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiTagSuggestionsEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.SetSearchAcrossAllTagsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUseCasesTest {
    @Test
    fun `get and set hide tagged and search-across-all flags`() = runTest {
        val hideFlow = MutableStateFlow(false)
        val searchFlow = MutableStateFlow(false)
        val aiTagsFlow = MutableStateFlow(true)
        val aiDescFlow = MutableStateFlow(true)
        val settingsRepo = object : SettingsRepository {
            override val hideTaggedWhenNoTagSelected = hideFlow
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) { hideFlow.value = value }
            override val searchAcrossAllTagsWhenFiltering = searchFlow
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) { searchFlow.value = value }
            override val aiTagSuggestionsEnabled = aiTagsFlow
            override suspend fun setAiTagSuggestionsEnabled(value: Boolean) { aiTagsFlow.value = value }
            override val aiDescriptionsEnabled = aiDescFlow
            override suspend fun setAiDescriptionsEnabled(value: Boolean) { aiDescFlow.value = value }
        }

        val getHide = GetHideTaggedSettingUseCase(settingsRepo)
        val setHide = SetHideTaggedSettingUseCase(settingsRepo)
        val getSearch = GetSearchAcrossAllTagsUseCase(settingsRepo)
        val setSearch = SetSearchAcrossAllTagsUseCase(settingsRepo)

        assertEquals(false, getHide().first())
        assertEquals(false, getSearch().first())

        setHide(true)
        setSearch(true)

        assertEquals(true, getHide().first())
        assertEquals(true, getSearch().first())
    }

    @Test
    fun `get and set AI tag suggestions and descriptions flags`() = runTest {
        val aiTagsFlow = MutableStateFlow(true)
        val aiDescFlow = MutableStateFlow(true)
        val settingsRepo = object : SettingsRepository {
            override val hideTaggedWhenNoTagSelected = MutableStateFlow(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering = MutableStateFlow(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
            override val aiTagSuggestionsEnabled = aiTagsFlow
            override suspend fun setAiTagSuggestionsEnabled(value: Boolean) { aiTagsFlow.value = value }
            override val aiDescriptionsEnabled = aiDescFlow
            override suspend fun setAiDescriptionsEnabled(value: Boolean) { aiDescFlow.value = value }
        }

        val getAiTags = GetAiTagSuggestionsEnabledUseCase(settingsRepo)
        val setAiTags = SetAiTagSuggestionsEnabledUseCase(settingsRepo)
        val getAiDesc = GetAiDescriptionsEnabledUseCase(settingsRepo)
        val setAiDesc = SetAiDescriptionsEnabledUseCase(settingsRepo)

        assertEquals(true, getAiTags().first())
        assertEquals(true, getAiDesc().first())

        setAiTags(false)
        setAiDesc(false)

        assertEquals(false, getAiTags().first())
        assertEquals(false, getAiDesc().first())

        setAiTags(true)
        assertEquals(true, getAiTags().first())
        assertEquals(false, getAiDesc().first())
    }
}

