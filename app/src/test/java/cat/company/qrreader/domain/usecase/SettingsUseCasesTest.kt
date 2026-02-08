package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
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
        val settingsRepo = object : SettingsRepository {
            override val hideTaggedWhenNoTagSelected = hideFlow
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) { hideFlow.value = value }
            override val searchAcrossAllTagsWhenFiltering = searchFlow
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) { searchFlow.value = value }
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
}

