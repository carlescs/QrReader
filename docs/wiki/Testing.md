# Testing

QR Reader has two test source sets: **unit tests** (`app/src/test/`) and **instrumented tests** (`app/src/androidTest/`). Code coverage is measured with JaCoCo and reported to Codecov and SonarCloud via CI.

---

## Running Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Unit tests + JaCoCo coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Instrumented tests (requires a connected device or emulator)
./gradlew connectedAndroidTest
```

Coverage reports are generated at `app/build/reports/jacoco/`.

---

## Unit Tests (`app/src/test/`)

Unit tests run on the JVM using JUnit 4, Robolectric, and `kotlinx-coroutines-test`.

### What is Covered

| Area | Example Test File |
|------|------------------|
| ViewModels | `QrCameraViewModelTest.kt`, `HistoryViewModelTest.kt`, `TagsViewModelTest.kt` |
| Use cases | Individual use-case test files in the respective `usecase/` packages |
| Repositories | `SettingsRepositoryTest.kt` (Robolectric for Android dependencies) |
| Utilities | `BarcodeUtilsTest.kt` |

### Test Structure Conventions

- **Naming:** `functionName_scenario_expectedResult`  
  Example: `saveBarcode_validInput_returnsNewId`
- **Setup:** Use `@Before` for shared fixture setup.
- **Teardown:** Use `@After` for cleanup (e.g. Koin `stopKoin()`).
- **Coroutines:** Wrap tests in `runTest { }` from `kotlinx-coroutines-test`.
- **Fakes over mocks:** Prefer fake implementations of repositories and use cases for cleaner, faster tests.

### Testing ViewModels

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

@Test
fun `loadBarcodes emits barcodes from repository`() = runTest {
    val viewModel = HistoryViewModel(fakeUseCase)
    // ... assert state changes
}
```

### Testing AI Features

Gemini Nano is unavailable in unit test environments. Use fake use-case subclasses that override `invoke`, `downloadModelIfNeeded`, and `cleanup` with no-op implementations:

```kotlin
class FakeGenerateTagSuggestionsUseCase : GenerateTagSuggestionsUseCase() {
    override suspend fun invoke(...): Result<List<SuggestedTagModel>> =
        Result.success(emptyList())
    override suspend fun downloadModelIfNeeded() { }
    override fun cleanup() { }
}
```

Mark any test that requires the real model with `@Ignore`.

---

## Instrumented Tests (`app/src/androidTest/`)

Instrumented tests run on a real device or emulator using Compose UI Test and Espresso.

### What is Covered

| Area | Example Test File |
|------|------------------|
| Camera screen UI | `QrCameraComposeTest.kt` |
| End-to-end flows | `AppTests.kt` |
| Room database | In-memory database tests |

### Compose UI Test Example

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun scanScreen_cameraPermissionGranted_showsViewfinder() {
    composeTestRule.setContent { CameraScreen(...) }
    composeTestRule.onNodeWithTag("viewfinder").assertIsDisplayed()
}
```

---

## Code Coverage

- **Tool:** JaCoCo (v0.8.12)
- **Reporting:** Uploaded to Codecov and SonarCloud in CI
- **Command:** `./gradlew testDebugUnitTest jacocoTestReport`
- **Report location:** `app/build/reports/jacoco/`

---

## Testing Checklist

When adding a new feature, ensure the following are covered:

- [ ] **ViewModel** – State changes for happy path and error cases
- [ ] **Use cases** – Business logic with mocked repositories
- [ ] **Repository** – Data operations using an in-memory Room database (if applicable)
- [ ] **UI** – Compose UI test for the main screen interaction (if applicable)
