package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiLanguageUseCase
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
        val aiFlow = MutableStateFlow(true)
        val settingsRepo = object : SettingsRepository {
            override val hideTaggedWhenNoTagSelected = hideFlow
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) { hideFlow.value = value }
            override val searchAcrossAllTagsWhenFiltering = searchFlow
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) { searchFlow.value = value }
            override val aiGenerationEnabled = aiFlow
            override suspend fun setAiGenerationEnabled(value: Boolean) { aiFlow.value = value }
            override val aiLanguage = MutableStateFlow("en")
            override suspend fun setAiLanguage(value: String) {}
            override val aiHumorousDescriptions = MutableStateFlow(false)
            override suspend fun setAiHumorousDescriptions(value: Boolean) {}
            override val biometricLockEnabled: kotlinx.coroutines.flow.Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setBiometricLockEnabled(value: Boolean) {}
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
    fun `get and set AI generation flag`() = runTest {
        val aiFlow = MutableStateFlow(true)
        val settingsRepo = object : SettingsRepository {
            override val hideTaggedWhenNoTagSelected = MutableStateFlow(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering = MutableStateFlow(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
            override val aiGenerationEnabled = aiFlow
            override suspend fun setAiGenerationEnabled(value: Boolean) { aiFlow.value = value }
            override val aiLanguage = MutableStateFlow("en")
            override suspend fun setAiLanguage(value: String) {}
            override val aiHumorousDescriptions = MutableStateFlow(false)
            override suspend fun setAiHumorousDescriptions(value: Boolean) {}
            override val biometricLockEnabled: kotlinx.coroutines.flow.Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setBiometricLockEnabled(value: Boolean) {}
        }

        val getAi = GetAiGenerationEnabledUseCase(settingsRepo)
        val setAi = SetAiGenerationEnabledUseCase(settingsRepo)

        assertEquals(true, getAi().first())

        setAi(false)
        assertEquals(false, getAi().first())

        setAi(true)
        assertEquals(true, getAi().first())
    }

    @Test
    fun `get and set AI language`() = runTest {
        val languageFlow = MutableStateFlow("en")
        val settingsRepo = object : SettingsRepository {
            override val hideTaggedWhenNoTagSelected = MutableStateFlow(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering = MutableStateFlow(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
            override val aiGenerationEnabled = MutableStateFlow(true)
            override suspend fun setAiGenerationEnabled(value: Boolean) {}
            override val aiLanguage = languageFlow
            override suspend fun setAiLanguage(value: String) { languageFlow.value = value }
            override val aiHumorousDescriptions = MutableStateFlow(false)
            override suspend fun setAiHumorousDescriptions(value: Boolean) {}
            override val biometricLockEnabled: kotlinx.coroutines.flow.Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setBiometricLockEnabled(value: Boolean) {}
        }

        val getLanguage = GetAiLanguageUseCase(settingsRepo)
        val setLanguage = SetAiLanguageUseCase(settingsRepo)

        assertEquals("en", getLanguage().first())

        setLanguage("es")
        assertEquals("es", getLanguage().first())

        setLanguage("fr")
        assertEquals("fr", getLanguage().first())
    }

    @Test
    fun `get and set AI humorous descriptions flag`() = runTest {
        val humorousFlow = MutableStateFlow(false)
        val settingsRepo = object : SettingsRepository {
            override val hideTaggedWhenNoTagSelected = MutableStateFlow(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering = MutableStateFlow(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
            override val aiGenerationEnabled = MutableStateFlow(true)
            override suspend fun setAiGenerationEnabled(value: Boolean) {}
            override val aiLanguage = MutableStateFlow("en")
            override suspend fun setAiLanguage(value: String) {}
            override val aiHumorousDescriptions = humorousFlow
            override suspend fun setAiHumorousDescriptions(value: Boolean) { humorousFlow.value = value }
        }

        val getHumorous = GetAiHumorousDescriptionsUseCase(settingsRepo)
        val setHumorous = SetAiHumorousDescriptionsUseCase(settingsRepo)

        assertEquals(false, getHumorous().first())

        setHumorous(true)
        assertEquals(true, getHumorous().first())

        setHumorous(false)
        assertEquals(false, getHumorous().first())
    }
}

