package cat.company.qrreader.di

import androidx.room.Room
import cat.company.qrreader.data.repository.BarcodeRepositoryImpl
import cat.company.qrreader.data.repository.SettingsRepositoryImpl
import cat.company.qrreader.data.repository.TagRepositoryImpl
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.Migrations
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeUseCase
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeWithTagsUseCase
import cat.company.qrreader.domain.usecase.camera.ScanImageUseCase
import cat.company.qrreader.domain.usecase.codecreator.GenerateQrCodeUseCase
import cat.company.qrreader.domain.usecase.codecreator.SaveBitmapToMediaStoreUseCase
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.SwitchBarcodeTagUseCase
import cat.company.qrreader.domain.usecase.history.ToggleFavoriteUseCase
import cat.company.qrreader.domain.usecase.history.ToggleLockBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetBiometricLockEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetBiometricLockEnabledUseCase
import cat.company.qrreader.domain.usecase.camera.CheckDuplicateBarcodeUseCase
import cat.company.qrreader.domain.usecase.settings.GetDuplicateCheckEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetDuplicateCheckEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.GetShowTagCountersUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.SetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.SetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetShowTagCountersUseCase
import cat.company.qrreader.domain.usecase.tags.DeleteTagUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import cat.company.qrreader.domain.usecase.update.CheckAppUpdateUseCase
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import cat.company.qrreader.features.camera.presentation.QrCameraViewModel
import cat.company.qrreader.features.codeCreator.presentation.CodeCreatorViewModel
import cat.company.qrreader.features.history.presentation.HistoryAiUseCases
import cat.company.qrreader.features.history.presentation.HistoryBarcodeUseCases
import cat.company.qrreader.features.history.presentation.HistoryViewModel
import cat.company.qrreader.features.settings.presentation.AiSettingsUseCases
import cat.company.qrreader.features.settings.presentation.HistoryFilterSettingsUseCases
import cat.company.qrreader.features.settings.presentation.HistoryPrivacySettingsUseCases
import cat.company.qrreader.features.settings.presentation.SettingsViewModel
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin modules for dependency injection
 */

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            BarcodesDb::class.java,
            "barcodes_db"
        )
            .addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3,
                Migrations.MIGRATION_3_4,
                Migrations.MIGRATION_4_5,
                Migrations.MIGRATION_5_6,
                Migrations.MIGRATION_6_7
            )
            .build()
    }
}

val repositoryModule = module {
    single<BarcodeRepository> { BarcodeRepositoryImpl(get()) }
    single<TagRepository> { TagRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }
    single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
}

val useCaseModule = module {
    factory { GetBarcodesWithTagsUseCase(get()) }
    factory { SaveBarcodeUseCase(get()) }
    factory { SaveBarcodeWithTagsUseCase(get()) }
    factory { ScanImageUseCase() }
    factory { CheckDuplicateBarcodeUseCase(get()) }
    factory { UpdateBarcodeUseCase(get()) }
    factory { DeleteBarcodeUseCase(get()) }
    factory { SwitchBarcodeTagUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { ToggleLockBarcodeUseCase(get()) }
    factory { GetBiometricLockEnabledUseCase(get()) }
    factory { SetBiometricLockEnabledUseCase(get()) }
    factory { GetDuplicateCheckEnabledUseCase(get()) }
    factory { SetDuplicateCheckEnabledUseCase(get()) }
    factory { GetAllTagsUseCase(get()) }
    factory { GetOrCreateTagsByNameUseCase(get()) }
    factory { GenerateBarcodeAiDataUseCase() }
    factory { DeleteTagUseCase(get()) }
    factory { GetHideTaggedSettingUseCase(get()) }
    factory { SetHideTaggedSettingUseCase(get()) }
    factory { GetSearchAcrossAllTagsUseCase(get()) }
    factory { SetSearchAcrossAllTagsUseCase(get()) }
    factory { GetShowTagCountersUseCase(get()) }
    factory { SetShowTagCountersUseCase(get()) }
    factory { GetAiGenerationEnabledUseCase(get()) }
    factory { SetAiGenerationEnabledUseCase(get()) }
    factory { GetAiLanguageUseCase(get()) }
    factory { SetAiLanguageUseCase(get()) }
    factory { GetAiHumorousDescriptionsUseCase(get()) }
    factory { SetAiHumorousDescriptionsUseCase(get()) }
    factory { GenerateQrCodeUseCase() }
    factory { SaveBitmapToMediaStoreUseCase() }
    factory { CheckAppUpdateUseCase(get<AppUpdateManager>()) }
}

val viewModelModule = module {
    viewModel {
        HistoryViewModel(
            barcodeUseCases = HistoryBarcodeUseCases(get(), get(), get(), get(), get()),
            settingsRepository = get(),
            aiUseCases = HistoryAiUseCases(get(), get(), get()),
            processLifecycleOwner = ProcessLifecycleOwner.get()
        )
    }
    viewModel { TagsViewModel(get(), get(), get(), get()) }
    viewModel { QrCameraViewModel(get<GenerateBarcodeAiDataUseCase>(), get<GetAllTagsUseCase>(), get<GetAiGenerationEnabledUseCase>(), get<GetAiLanguageUseCase>(), get<GetAiHumorousDescriptionsUseCase>()) }
    viewModel { CodeCreatorViewModel(get<GenerateQrCodeUseCase>()) }
    viewModel {
        SettingsViewModel(
            filterSettings = HistoryFilterSettingsUseCases(get(), get(), get(), get(), get(), get()),
            privacySettings = HistoryPrivacySettingsUseCases(get(), get(), get(), get()),
            aiSettings = AiSettingsUseCases(get(), get(), get(), get(), get(), get(), get<GenerateBarcodeAiDataUseCase>()),
            checkAppUpdateUseCase = get()
        )
    }
}

// Combine all modules
val appModules = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
