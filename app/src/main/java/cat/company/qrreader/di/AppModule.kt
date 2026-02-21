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
import cat.company.qrreader.domain.usecase.codecreator.GenerateQrCodeUseCase
import cat.company.qrreader.domain.usecase.codecreator.SaveBitmapToMediaStoreUseCase
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.SwitchBarcodeTagUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.SetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.SetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.tags.DeleteTagUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import cat.company.qrreader.features.camera.presentation.QrCameraViewModel
import cat.company.qrreader.features.codeCreator.presentation.CodeCreatorViewModel
import cat.company.qrreader.features.history.presentation.HistoryViewModel
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
                Migrations.MIGRATION_4_5
            )
            .build()
    }
}

val repositoryModule = module {
    single<BarcodeRepository> { BarcodeRepositoryImpl(get()) }
    single<TagRepository> { TagRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }
}

val useCaseModule = module {
    factory { GetBarcodesWithTagsUseCase(get()) }
    factory { SaveBarcodeUseCase(get()) }
    factory { SaveBarcodeWithTagsUseCase(get()) }
    factory { UpdateBarcodeUseCase(get()) }
    factory { DeleteBarcodeUseCase(get()) }
    factory { SwitchBarcodeTagUseCase(get()) }
    factory { GetAllTagsUseCase(get()) }
    factory { GetOrCreateTagsByNameUseCase(get()) }
    factory { GenerateBarcodeAiDataUseCase() }
    factory { DeleteTagUseCase(get()) }
    factory { GetHideTaggedSettingUseCase(get()) }
    factory { SetHideTaggedSettingUseCase(get()) }
    factory { GetSearchAcrossAllTagsUseCase(get()) }
    factory { SetSearchAcrossAllTagsUseCase(get()) }
    factory { GetAiGenerationEnabledUseCase(get()) }
    factory { SetAiGenerationEnabledUseCase(get()) }
    factory { GetAiLanguageUseCase(get()) }
    factory { SetAiLanguageUseCase(get()) }
    factory { GenerateQrCodeUseCase() }
    factory { SaveBitmapToMediaStoreUseCase() }
}

val viewModelModule = module {
    viewModel { HistoryViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { TagsViewModel(get(), get()) }
    viewModel { QrCameraViewModel(get<GenerateBarcodeAiDataUseCase>(), get<GetAllTagsUseCase>(), get<GetAiGenerationEnabledUseCase>(), get<GetAiLanguageUseCase>()) }
    viewModel { CodeCreatorViewModel(get<GenerateQrCodeUseCase>()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}

// Combine all modules
val appModules = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
