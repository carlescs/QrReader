# AGENTS.md - AI Coding Assistant Configuration

This file provides context and guidelines for AI coding assistants working on the QR Reader Android project.

## Project Overview

**QR Reader** is a modern Android application for scanning and generating QR codes and barcodes. The app is built with Jetpack Compose and follows Clean Architecture principles with clear separation between domain, data, and presentation layers.

### Key Features
- QR code and barcode scanning using CameraX and ML Kit
- QR code generation
- Barcode history management with tagging system
- User settings and preferences
- Analytics integration with Firebase

## Technology Stack

### Core Technologies
- **Language**: Kotlin (2.3.0)
- **Build System**: Gradle with version catalogs (`gradle/libs.versions.toml`)
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 36 (Android 15)
- **Java Version**: 21

### Major Libraries
- **UI Framework**: Jetpack Compose (1.10.2)
  - Material3 for design system
  - Navigation Compose for navigation
  - Accompanist Permissions for runtime permissions
- **Camera**: CameraX (1.5.3) with ML Kit Vision
- **ML/Barcode**: Google ML Kit Barcode Scanning (17.3.0)
- **Database**: Room (2.8.4) with KSP for annotation processing
- **Dependency Injection**: Koin (4.1.1)
- **Storage**: Jetpack DataStore Preferences (1.2.0)
- **Analytics**: Firebase Analytics (22.5.0)
- **QR Generation**: qrcode-kotlin (4.5.0)

### Testing
- **Unit Testing**: JUnit 4, Robolectric (4.16.1)
- **UI Testing**: Compose UI Test, Espresso
- **Coroutines Testing**: kotlinx-coroutines-test (1.10.2)
- **Coverage**: JaCoCo (0.8.12)

## Architecture

### Clean Architecture Layers

The project follows Clean Architecture with clear separation of concerns:

```
app/src/main/java/cat/company/qrreader/
├── domain/              # Business logic layer
│   ├── model/          # Domain models (pure Kotlin)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases (business operations)
├── data/               # Data layer
│   ├── mapper/         # Entity ↔ Model mappers
│   └── repository/     # Repository implementations
├── features/           # Feature modules
│   ├── camera/         # QR scanning feature
│   ├── codeCreator/    # QR generation feature
│   ├── history/        # Barcode history feature
│   ├── tags/           # Tag management feature
│   └── settings/       # App settings feature
├── db/                 # Database (Room)
│   ├── daos/          # Data Access Objects
│   ├── entities/      # Database entities
│   ├── converters/    # Type converters
│   └── Migrations.kt  # Database migrations
├── di/                # Dependency injection modules
├── ui/                # Shared UI components and theme
└── utils/             # Utility functions
```

### Design Patterns

1. **MVVM (Model-View-ViewModel)**
   - Each feature has a ViewModel for UI state management
   - ViewModels use Kotlin StateFlow and Flow for reactive state
   - Views are pure Composables that observe ViewModel state

2. **Repository Pattern**
   - Repositories abstract data sources (Room database, DataStore)
   - Domain layer depends on repository interfaces
   - Data layer provides implementations

3. **Use Case Pattern**
   - Each business operation has a dedicated use case
   - Use cases encapsulate single responsibilities
   - ViewModels orchestrate multiple use cases

4. **Dependency Injection**
   - Koin modules defined in `di/AppModule.kt`
   - Constructor injection for all dependencies
   - Separate modules: database, repository, useCase, viewModel

## Coding Standards

### Kotlin Conventions

1. **Naming**
   - Classes: PascalCase (`BarcodeRepository`, `HistoryViewModel`)
   - Functions/Variables: camelCase (`saveBarcodes`, `selectedTagId`)
   - Constants: UPPER_SNAKE_CASE in companion objects
   - Private properties: start with underscore for backing properties (e.g., `_uiState`)

2. **File Organization**
   - One public class per file
   - File name matches the class name
   - Organize imports alphabetically
   - Remove unused imports

3. **Use Cases**
   - Named with verb + noun pattern: `GetBarcodesWithTagsUseCase`, `SaveBarcodeUseCase`
   - Implement `operator fun invoke()` for clean call syntax
   - Keep use cases focused on single responsibility

4. **ViewModels**
   - Named with feature + "ViewModel": `HistoryViewModel`, `TagsViewModel`
   - Extend `androidx.lifecycle.ViewModel`
   - Use StateFlow for UI state
   - Use Flow for continuous data streams

5. **Repositories**
   - Interface in `domain/repository/`
   - Implementation in `data/repository/` with `Impl` suffix
   - Example: `BarcodeRepository` (interface) → `BarcodeRepositoryImpl` (implementation)

### Jetpack Compose Guidelines

1. **Composable Functions**
   - PascalCase naming
   - Use preview functions with `@Preview` annotation
   - Keep composables small and focused
   - Extract reusable components to `ui/components/`

2. **State Management**
   - Use `remember` for composable-scoped state
   - Use `rememberSaveable` for configuration change persistence
   - Hoist state when sharing between composables
   - Observe ViewModels with `collectAsState()` or `collectAsStateWithLifecycle()`

3. **UI Structure**
   - Screen composables in feature's `presentation/ui/` folder
   - Shared components in `ui/components/`
   - Theme definitions in `ui/theme/`

4. **Cognitive Complexity Management (SonarQube S3776)**
   - Keep composable cognitive complexity below 15 (SonarQube threshold)
   - Extract nested UI logic into separate private composables
   - Break down complex screens into smaller composable functions
   - Use descriptive names for extracted composables (e.g., `SearchBarLeadingIcon`, `EmptySearchState`)
   - Each composable should have a single, clear responsibility
   - Example: Instead of one 200-line composable, create 10-15 focused composables of 10-20 lines each

5. **Parameter Management (SonarQube S107)**
   - Keep function parameters to 7 or fewer (SonarQube threshold)
   - When a function needs more than 7 parameters, group **logically related** parameters into focused data classes
   - **Best Practice:** Create multiple small, focused parameter groups rather than one large group
   - **Documentation:** Always add comprehensive KDoc to parameter classes explaining:
     - Purpose of the group
     - What concern it addresses (display vs interaction vs state)
     - Description of each property
     - Cross-references to related classes and usage examples
   - Example - Good approach with logical grouping and documentation:
   ```kotlin
   /**
    * Display data for component results.
    *
    * Encapsulates presentation-related data determining **what** to display.
    *
    * @property items The list of items to display
    * @property query Current search query for empty state
    * @property formatter Date formatter for consistent timestamps
    */
   private data class DisplayData(
       val items: List<Item>,
       val query: String,
       val formatter: DateFormat
   )
   
   /**
    * Interaction dependencies for user actions.
    *
    * Encapsulates dependencies enabling **how** users interact with data.
    *
    * @property clipboard For copy operations
    * @property snackbarHost For user feedback
    * @property viewModel For state management
    */
   private data class InteractionDeps(
       val clipboard: Clipboard,
       val snackbarHost: SnackbarHostState,
       val viewModel: ViewModel
   )
   
   @Composable
   private fun MyComposable(
       isActive: Boolean,           // Simple state flag
       displayData: DisplayData,    // What to display
       interactionDeps: InteractionDeps // How to interact
   )
   ```
   - **Avoid:** Grouping unrelated parameters just to reduce count
   - Benefits: Improves readability, makes relationships clear, easier refactoring, better IDE support

### Coroutines Best Practices

1. **Dispatcher Usage**
   - **Do NOT** explicitly specify `Dispatchers.IO` in ViewModel coroutines when calling suspend functions
   - Repository/DAO implementations handle their own dispatching
   - Room and DataStore automatically use appropriate dispatchers
   - Only specify dispatcher when doing CPU-intensive work (use `Dispatchers.Default`)
   - Example: `viewModelScope.launch { useCase() }` NOT `viewModelScope.launch(Dispatchers.IO) { useCase() }`

2. **ViewModel Coroutines**
   - Use `viewModelScope.launch` for fire-and-forget operations
   - Use Flow for continuous data streams
   - Let repository layer handle IO dispatching
   - Keep ViewModels dispatcher-agnostic for easier testing

### Database Conventions

1. **Room Entities**
   - Define in `db/entities/`
   - Use `@Entity`, `@PrimaryKey`, `@ColumnInfo` annotations
   - Keep entities as data classes

2. **DAOs (Data Access Objects)**
   - Define in `db/daos/`
   - Return Flow for observable queries
   - Use suspend functions for one-shot operations
   - Name with entity + "Dao": `SavedBarcodeDao`, `TagDao`

3. **Migrations**
   - Add all migrations to `db/Migrations.kt`
   - Test migrations thoroughly
   - Document schema changes in migration code
   - Current database version: Check `BarcodesDb` class

4. **Type Converters**
   - Define in `db/converters/Converters.kt`
   - Register in `BarcodesDb` with `@TypeConverters`

### Dependency Injection (Koin)

All dependency injection is managed in `di/AppModule.kt` with these modules:

1. **databaseModule** - Room database instance
2. **repositoryModule** - Repository implementations (use `single<Interface> { Implementation }`)
3. **useCaseModule** - Use cases (use `factory { UseCase() }`)
4. **viewModelModule** - ViewModels (use `viewModel { ViewModel() }`)

**Adding New Dependencies:**
```kotlin
// In appropriate module
val repositoryModule = module {
    single<NewRepository> { NewRepositoryImpl(get()) }
}

val useCaseModule = module {
    factory { NewUseCase(get()) }
}

val viewModelModule = module {
    viewModel { NewViewModel(get(), get()) }
}
```

## Testing Guidelines

### Unit Tests (app/src/test/)

1. **ViewModels**
   - Test state changes
   - Test user interactions
   - Use fake repositories
   - Use `kotlinx-coroutines-test` for coroutine testing
   - Example: `HistoryViewModelTest.kt`, `TagsViewModelTest.kt`

2. **Use Cases**
   - Test business logic
   - Mock repositories
   - Test edge cases

3. **Repositories**
   - Test with Robolectric for Android dependencies
   - Use in-memory database for Room tests
   - Example: `SettingsRepositoryTest.kt`

4. **Test Structure**
   - Follow naming: `functionName_scenario_expectedResult`
   - Use `@Before` for setup
   - Use `@After` for cleanup
   - Use `runTest` for coroutine tests

5. **Important: Test Imports**
   - Always import the actual implementation classes being tested
   - Test files may be in different packages than the source files
   - Common imports needed:
     - ViewModels from `features/{feature}/presentation/`
     - Use cases from `domain/usecase/`
     - Repository implementations from `data/repository/`
     - UI utilities from `features/{feature}/presentation/ui/components/`

### Instrumented Tests (app/src/androidTest/)

1. **Compose UI Tests**
   - Use `createComposeRule()`
   - Test user interactions
   - Test navigation flows
   - Example: `QrCameraComposeTest.kt`, `AppTests.kt`

2. **Integration Tests**
   - Test end-to-end workflows
   - Use real database with `Room.inMemoryDatabaseBuilder()`

### Code Coverage

- JaCoCo configured for test coverage reporting
- Run: `./gradlew testDebugUnitTest jacocoTestReport`
- Reports generated in `app/build/reports/jacoco/`
- Coverage reports integrated with SonarQube

## Build Configuration

### Version Management

All dependency versions are managed in `gradle/libs.versions.toml`:
- Update versions in `[versions]` section
- Add new libraries in `[libraries]` section
- Define bundles in `[bundles]` for related dependencies

### Build Variants

- **Debug**: Test coverage enabled, debug symbols included
- **Release**: Minification disabled (can be enabled in `app/build.gradle`)

### Gradle Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Test
./gradlew testDebugUnitTest              # Unit tests
./gradlew connectedAndroidTest           # Instrumented tests
./gradlew jacocoTestReport              # Coverage report

# Analysis
./gradlew sonar                         # SonarQube analysis
./gradlew lint                          # Android Lint

# Clean
./gradlew clean
```

## CI/CD

### GitHub Actions

Primary CI/CD configured in `.github/workflows/android-ci-cd.yml`:
- **Test Job**: Runs unit tests, generates JaCoCo coverage, uploads to Codecov
- **Build Job**: Builds release bundle (AAB), runs SonarCloud analysis, signs bundle
- **Release Job**: Automatically publishes to Google Play Alpha track (master branch only)
- **Promote Job**: Promotes to Production track (requires manual approval)
- **Triggers**: On push to master and pull requests
- **VM Image**: ubuntu-latest
- **Java Version**: 21

**Required Secrets for Publishing:**
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` - Service account for Play Store API
- `KEYSTORE_BASE64` - Base64-encoded keystore for signing
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias in keystore

See [.github/CICD.md](.github/CICD.md) for detailed setup instructions.

### Azure Pipelines (Legacy)

Alternative CI/CD configured in `devops/azure-pipelines.yml`:
- **Test Stage**: Runs unit tests, generates JaCoCo coverage
- **Build Stage**: Builds APK
- **Triggers**: On push to master branch
- **VM Image**: ubuntu-latest
- **Java Version**: 21

### Code Security

Advanced security scanning configured in `devops/code-security.yml`

## Common Tasks for AI Assistants

### Adding a New Feature

1. **Create domain models** in `domain/model/`
2. **Define repository interface** in `domain/repository/`
3. **Implement repository** in `data/repository/`
4. **Create use cases** in `domain/usecase/`
5. **Create ViewModel** in `features/{feature}/presentation/`
6. **Create UI composables** in `features/{feature}/presentation/ui/`
7. **Register dependencies** in `di/AppModule.kt`
8. **Add navigation** in `MainScreen.kt` if needed
9. **Write tests** for all layers

### Adding a Database Field

1. **Update entity** in `db/entities/`
2. **Increment database version** in `BarcodesDb`
3. **Create migration** in `db/Migrations.kt`
4. **Update DAO queries** if needed
5. **Update mappers** in `data/mapper/`
6. **Update domain models** if needed
7. **Write migration test**

### Adding a New Dependency

1. **Add version** to `[versions]` in `gradle/libs.versions.toml`
2. **Add library** to `[libraries]` section
3. **Add to appropriate bundle** if applicable
4. **Sync Gradle files**
5. **Update this document** if it's a major dependency

### Modifying UI

1. **Locate screen composable** in `features/{feature}/presentation/ui/`
2. **Check for shared components** in `ui/components/`
3. **Follow Material3 design patterns**
4. **Ensure accessibility** (content descriptions, semantics)
5. **Add preview functions** for design validation
6. **Test on multiple screen sizes** if possible

## Firebase Analytics

The app uses Firebase Analytics for event tracking:
- **Configuration**: `google-services.json` in `app/`
- **Instance**: Injected via Koin or created in activities
- **Usage**: Track user interactions and feature usage
- **Privacy**: Follow GDPR and data privacy guidelines

## Resources

### Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [CameraX](https://developer.android.com/training/camerax)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Koin DI](https://insert-koin.io/)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)

### Key Files
- `app/build.gradle` - App-level build configuration
- `gradle/libs.versions.toml` - Centralized dependency versions
- `di/AppModule.kt` - Dependency injection setup
- `MainScreen.kt` - Main navigation and app structure
- `BarcodesDb.kt` - Database configuration

## Important Notes

1. **Package Name**: `cat.company.qrreader` - maintain consistency across all new files
2. **Kotlin Version**: Keep in sync with Compose Compiler version
3. **Database Migrations**: Always test migrations - data loss is unacceptable
4. **Permissions**: Camera permission is critical - handle gracefully
5. **Proguard**: Currently disabled for release builds
6. **Git Hooks**: Consider adding pre-commit hooks for lint/format checks
7. **Code Review**: All changes should maintain architecture patterns and test coverage
8. **Deprecated Code Handling**: 
   - When overriding deprecated abstract methods (e.g., `Drawable.getOpacity()`), add `@Deprecated` annotation with message
   - Include KDoc explaining why the method must remain and when it can be removed
   - Example: "This will be removed when minSdk is raised to API XX"
   - Do NOT use `@Suppress("OVERRIDE_DEPRECATION")` without documentation

## Questions or Issues?

When uncertain about implementation details:
1. Check existing similar features in the codebase
2. Follow the architecture patterns established in other features
3. Maintain consistency with existing code style
4. Prioritize testability and maintainability
5. Ask for clarification on business requirements before implementing

---

**Last Updated**: 2026-02-08  
**Project Version**: 5.0.0 (versionCode 250)  
**Maintained by**: QR Reader Development Team

