# Architecture

QR Reader is built with **Clean Architecture** and the **MVVM** presentation pattern, written entirely in Kotlin with Jetpack Compose.

---

## Layer Overview

```
app/src/main/java/cat/company/qrreader/
├── domain/          # Business logic – pure Kotlin, no Android dependencies
│   ├── model/       # Domain models
│   ├── repository/  # Repository interfaces
│   └── usecase/     # Use cases (single-responsibility business operations)
├── data/            # Data layer – implements domain interfaces
│   ├── mapper/      # Entity ↔ domain model converters
│   └── repository/  # Repository implementations (Room, DataStore)
├── features/        # Feature modules – UI + ViewModel per feature
│   ├── camera/      # Barcode scanning
│   ├── codeCreator/ # QR code generation
│   ├── history/     # Barcode history
│   ├── tags/        # Tag management
│   └── settings/    # App settings
├── db/              # Room database – entities, DAOs, migrations
├── di/              # Koin dependency injection modules
├── ui/              # Shared Compose components and Material3 theme
└── utils/           # Utility functions (barcode parsing, formatting)
```

---

## Design Patterns

### 1. Clean Architecture

Dependencies flow inward only:

```
features (presentation)
    │
    ▼
domain (use cases + interfaces)
    │
    ▼
data (repository implementations + Room)
```

- The **domain** layer has zero Android dependencies.
- The **data** layer implements domain interfaces and owns all I/O.
- **Features** depend on domain use cases and models, never on the data layer directly.

### 2. MVVM (Model-View-ViewModel)

Each feature follows the same structure:

```
features/{feature}/
├── presentation/
│   ├── {Feature}ViewModel.kt   # UI state + business orchestration
│   └── ui/
│       ├── {Feature}Screen.kt  # Root composable (screen)
│       └── components/         # Smaller composables
```

- **ViewModels** hold `StateFlow` / `Flow` state and call use cases.
- **Composables** observe ViewModel state with `collectAsStateWithLifecycle()`.
- No business logic lives inside composables.

### 3. Repository Pattern

```kotlin
// domain/repository/BarcodeRepository.kt  (interface)
interface BarcodeRepository {
    fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>>
    suspend fun saveBarcode(barcode: BarcodeModel): Int
}

// data/repository/BarcodeRepositoryImpl.kt  (implementation)
class BarcodeRepositoryImpl(private val dao: SavedBarcodeDao) : BarcodeRepository { ... }
```

Repositories abstract all data sources. ViewModels never touch DAOs directly.

### 4. Use Case Pattern

Each business operation has its own use case class with an `invoke` operator:

```kotlin
class SaveBarcodeUseCase(private val repository: BarcodeRepository) {
    suspend operator fun invoke(barcode: BarcodeModel): Int =
        repository.saveBarcode(barcode)
}
```

### 5. Dependency Injection (Koin)

All wiring is done in `di/AppModule.kt` with four Koin modules:

| Module | Contents |
|--------|----------|
| `databaseModule` | Room database singleton |
| `repositoryModule` | Repository implementations (`single<Interface> { Impl(get()) }`) |
| `useCaseModule` | Use cases (`factory { UseCase(get()) }`) |
| `viewModelModule` | ViewModels (`viewModel { ViewModel(get(), get()) }`) |

---

## State Management

ViewModels expose state as `StateFlow<UiState>`:

```kotlin
private val _uiState = MutableStateFlow(HistoryUiState())
val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
```

Screens collect state with lifecycle-awareness:

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

---

## Navigation

App-level navigation is managed by `MainScreen.kt` using `androidx.navigation.compose`. Each feature is a top-level destination registered in the nav graph. The bottom navigation bar provides access to Camera, History, Code Creator, Tags, and Settings.

---

## Coroutine Guidelines

- **ViewModels** use `viewModelScope.launch { }` for fire-and-forget operations.
- **Do not** specify `Dispatchers.IO` in ViewModels – repositories handle their own dispatching (Room and DataStore dispatch automatically).
- Use `Dispatchers.Default` only for CPU-intensive work.
- Flows are collected in the UI layer with `collectAsStateWithLifecycle()`.
