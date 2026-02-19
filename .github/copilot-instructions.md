# AI Coding Assistant Instructions for QR Reader

This file provides context and guidelines for AI coding assistants working on the QR Reader Android project.

## Project Overview

**QR Reader** is a modern Android application for scanning and generating QR codes and barcodes. The app is built with Jetpack Compose and follows Clean Architecture principles with clear separation between domain, data, and presentation layers.

### Key Features
- QR code and barcode scanning using CameraX and ML Kit
- QR code generation
- Barcode history management with tagging system
- **AI-powered tag suggestions** using ML Kit GenAI (Gemini Nano)
- **AI-generated barcode descriptions** using ML Kit GenAI (Gemini Nano)
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
- **AI/GenAI**: Google ML Kit GenAI Prompt API (1.0.0-beta1) for Gemini Nano
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
   - Current database version: 5 (see `BarcodesDb` class)
   - **Recent migrations**:
     - v4→v5 (2026): Added `aiGeneratedDescription VARCHAR(200)` column to `saved_barcodes` table

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

## AI Features (ML Kit GenAI)

The app uses **Google ML Kit GenAI Prompt API** with Gemini Nano for on-device AI features.

### Overview

- **Model**: Gemini Nano (lightweight on-device LLM)
- **Size**: ~150-200MB download on first use
- **Device Requirements**: Pixel 9+, Galaxy Z Fold7+, Xiaomi 15, and devices with AICore service
- **API**: ML Kit GenAI Prompt API 1.0.0-beta1

### AI Use Cases

#### 1. Tag Suggestions (`GenerateTagSuggestionsUseCase`)
- **Location**: `domain/usecase/tags/GenerateTagSuggestionsUseCase.kt`
- **Purpose**: Generate 1-3 relevant tags for categorizing scanned barcodes
- **Input**: Barcode content, type, format, existing tags
- **Output**: List of `SuggestedTagModel` with selection state
- **Prompt Engineering**: 
  - Low temperature (0.3) for consistent results
  - Prioritizes existing tags if relevant
  - Returns comma-separated tag names only

#### 2. Barcode Descriptions (`GenerateBarcodeDescriptionUseCase`)
- **Location**: `domain/usecase/barcode/GenerateBarcodeDescriptionUseCase.kt`
- **Purpose**: Generate human-readable descriptions for scanned barcodes
- **Input**: Barcode content, type, format
- **Output**: 1-2 sentence description (max 200 characters)
- **Prompt Engineering**:
  - Moderate temperature (0.5) for creative but accurate descriptions
  - Explains what the barcode is and what it's used for
  - Automatically truncated if response exceeds limit

### Implementation Patterns

**1. Model Initialization:**
```kotlin
private var model: GenerativeModel? = null

// In use case invoke():
if (model == null) {
    model = Generation.getClient()
}
```

**2. Status Checking:**
```kotlin
val status = model?.checkStatus()
when (status) {
    FeatureStatus.UNAVAILABLE -> // Device not supported
    FeatureStatus.DOWNLOADABLE -> // Model needs download
    FeatureStatus.DOWNLOADING -> // Download in progress
    FeatureStatus.AVAILABLE -> // Ready to use
}
```

**3. Content Generation:**
```kotlin
val request = generateContentRequest(TextPart(promptText)) {
    temperature = 0.5f
    topK = 20
    candidateCount = 1
    maxOutputTokens = 100
}

val response = model?.generateContent(request)
val text = response?.candidates?.firstOrNull()?.text?.trim()
```

**4. Model Download (in ViewModel init):**
```kotlin
viewModelScope.launch {
    generateTagSuggestionsUseCase.downloadModelIfNeeded()
}
```

### Error Handling

AI use cases return `Result<T>` with specific error types:

1. **UnsupportedOperationException**: Device doesn't support Gemini Nano
2. **IllegalStateException**: Model downloading or temporarily unavailable
3. **Exception**: General errors (network, parsing, etc.)

**UI should parse and display user-friendly messages:**
- "Not available on this device" → Gemini Nano unavailable
- "AI model downloading..." → Download in progress
- "Temporarily unavailable" → Transient errors

### State Management

ViewModels track AI generation state for each barcode:

```kotlin
data class BarcodeState(
    val barcodeTags: Map<Int, List<SuggestedTagModel>>,
    val isLoadingTags: Set<Int>,
    val tagSuggestionErrors: Map<Int, String>,
    val barcodeDescriptions: Map<Int, String>,
    val isLoadingDescriptions: Set<Int>,
    val descriptionErrors: Map<Int, String>
)
```

Use barcode.hashCode() as key for tracking per-barcode state.

### Data Persistence

AI-generated data is persisted to database:

- **Tags**: Stored in `tags` table with many-to-many relationship via `barcode_tag_cross_ref`
- **Descriptions**: Stored in `aiGeneratedDescription` column in `saved_barcodes` table (added in migration 4→5)

### Testing AI Features

**Challenge**: Gemini Nano unavailable in test environments

**Strategy**:
1. Create fake use cases that override `invoke()`, `downloadModelIfNeeded()`, and `cleanup()`
2. Test ViewModel logic with null/empty AI responses
3. Test data class behavior (equality, copy) separately
4. Mark Gemini Nano-dependent tests with `@Ignore`

**Example Fake:**
```kotlin
class FakeGenerateTagSuggestionsUseCase : GenerateTagSuggestionsUseCase() {
    override suspend fun invoke(...): Result<List<SuggestedTagModel>> {
        return Result.success(emptyList())
    }
    override suspend fun downloadModelIfNeeded() { /* no-op */ }
    override fun cleanup() { /* no-op */ }
}
```

### Best Practices

1. **Always download model proactively** in ViewModel `init` block
2. **Never block UI** on AI generation - use loading states
3. **Provide fallbacks** for unsupported devices
4. **Let repositories handle dispatching** - ViewModels should be dispatcher-agnostic
5. **Cleanup models** in ViewModel `onCleared()`
6. **Store AI outputs** to avoid regenerating on every view

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

App versioning is Git-based and automatic:
- **Version Code**: Calculated as `commit_count + 25` (base offset for historical consistency with Google Play)
- **Version Name**: Derived from Git tags with branch-aware formatting:
  - **Master/main branch**: Clean version from last tag (e.g., `5.2.0`) - no `-dev` suffix
  - **Feature branches**: Development version with commit info (e.g., `5.2.0-dev.3+abc1234`)
  - **Tagged commits**: Clean version on all branches (e.g., `5.2.0`)
- Configured in `buildSrc/src/main/kotlin/GitVersioning.kt`
- Base offset of 25 accounts for historical repository restructuring
- All branches use same version code formula (no branch offsets)

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
./gradlew sonar                         # SonarCloud analysis (requires SONAR_TOKEN)
./gradlew lint                          # Android Lint

# Clean
./gradlew clean
```

## CI/CD

### GitHub Actions

Primary CI/CD configured in `.github/workflows/android-ci-cd.yml`:
- **Test Job**: Runs unit tests, generates JaCoCo coverage, uploads to Codecov
- **Build Job**: Builds release bundle (AAB), runs SonarCloud analysis (Gradle-based), signs bundle
- **Release Job**: Automatically publishes to Google Play Alpha track (master branch only)
- **Promote Job**: Promotes to Production track (requires manual approval)
- **Triggers**: On push to master and pull requests
- **VM Image**: ubuntu-latest
- **Java Version**: 21

**SonarCloud Configuration:**
- Uses official `org.sonarqube` Gradle plugin (recommended for Android projects)
- Configuration centralized in `sonar-project.properties`
- Automatically integrates with JaCoCo coverage reports
- See [CICD.md](./CICD.md) for detailed SonarCloud setup instructions

**Build Optimization (2026):**
- Uses `gradle/actions/setup-gradle@v4` for intelligent Gradle caching
- Uses `gradle/actions/wrapper-validation@v4` for security validation
- Configuration cache enabled for faster incremental builds
- Superior performance compared to manual `actions/cache`

**Required Secrets for Publishing:**
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` - Service account for Play Store API
- `KEYSTORE_BASE64` - Base64-encoded keystore for signing
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias in keystore

See [CICD.md](./CICD.md) for detailed setup instructions and best practices.

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
