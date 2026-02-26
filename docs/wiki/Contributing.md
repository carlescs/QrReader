# Contributing

Thank you for contributing to QR Reader! This page covers coding standards, project conventions, and step-by-step guides for common development tasks.

---

## Development Workflow

1. **Fork / branch** – Create a feature branch from `master` (e.g. `feature/my-feature`).
2. **Develop** – Follow the coding standards below.
3. **Test** – Run unit tests locally (`./gradlew testDebugUnitTest`).
4. **Push** – Push your branch and open a pull request against `master`.
5. **CI** – The GitHub Actions pipeline runs tests, lint, and SonarCloud analysis automatically.
6. **Review** – Address review feedback and get approval.
7. **Merge** – Squash-merge into `master`.

---

## Coding Standards

### Kotlin Naming

| Element | Convention | Example |
|---------|-----------|---------|
| Class | PascalCase | `BarcodeRepository` |
| Function / variable | camelCase | `saveBarcodes`, `selectedTagId` |
| Constant (companion object) | UPPER_SNAKE_CASE | `MAX_DESCRIPTION_LENGTH` |
| Backing state property | underscore prefix | `_uiState` |

### One Class Per File

Each public class lives in its own file, named after the class.

### Use Cases

- Named `VerbNounUseCase` (e.g. `GetBarcodesWithTagsUseCase`, `SaveBarcodeUseCase`).
- Implement `operator fun invoke(...)` for clean call syntax.
- Single responsibility – one operation per use case.

### ViewModels

- Named `FeatureViewModel` (e.g. `HistoryViewModel`, `TagsViewModel`).
- Extend `androidx.lifecycle.ViewModel`.
- Expose state as `StateFlow<UiState>`.
- Use `viewModelScope.launch { }` – **do not** specify `Dispatchers.IO` (repositories handle dispatching).

### Repositories

- Interface in `domain/repository/`.
- Implementation in `data/repository/` with the `Impl` suffix.

---

## Adding a New Feature

Follow these steps to add a complete feature:

1. **Domain model** – `domain/model/MyModel.kt`
2. **Repository interface** – `domain/repository/MyRepository.kt`
3. **Repository implementation** – `data/repository/MyRepositoryImpl.kt`
4. **Use cases** – `domain/usecase/my/MyUseCase.kt`
5. **ViewModel** – `features/my/presentation/MyViewModel.kt`
6. **Screen + components** – `features/my/presentation/ui/`
7. **DI registration** – `di/AppModule.kt` (repository, use case, viewModel modules)
8. **Navigation** – Register the screen in `MainScreen.kt` if it needs a nav destination
9. **Tests** – ViewModel tests, use-case tests, and at minimum a smoke UI test

---

## Adding a Database Field

1. Update the entity in `db/entities/`.
2. Increment `version` in `@Database(version = ...)` in `BarcodesDb.kt`.
3. Add a `Migration(from, to)` in `db/Migrations.kt`.
4. Register the migration in the Room builder (`di/AppModule.kt`).
5. Update mappers in `data/mapper/` as needed.
6. Update domain models if the new field is part of the business logic.
7. Write a migration test.

---

## Adding a String Resource

1. Open `app/src/main/res/values/strings.xml`.
2. Find the appropriate feature comment block (e.g. `<!-- History screen -->`).
3. Add the entry: `<string name="my_new_string">My text</string>`.
4. Reference it in Compose: `stringResource(R.string.my_new_string)`.
5. If adding an AI error string, also update `parseErrorMessageRes()` in `BarcodeDescriptionSection.kt`.

---

## Jetpack Compose Guidelines

- **Cognitive complexity:** Keep composables below 15 (SonarQube S3776). Extract nested logic into separate private composables.
- **Parameter count:** Keep functions to 7 or fewer parameters (SonarQube S107). Group logically related parameters into focused data classes when needed.
- **State:** Use `remember` for composable-scoped state; `rememberSaveable` when state must survive configuration changes.
- **Previews:** Add `@Preview` functions for every non-trivial composable.
- **Accessibility:** Provide `contentDescription` for all icons and images.

---

## CI/CD

The project uses GitHub Actions. See [CI/CD Guide](../../.github/CICD.md) for a full description of the pipeline jobs (test, build, release, promote).

### Creating a Release

```bash
git checkout master && git pull
git tag v5.3.0
git push origin v5.3.0
```

This triggers the release pipeline, which builds a signed AAB and publishes it to Google Play Alpha. A manual approval step is required before promotion to Production.

---

## Dependency Management

All dependency versions are managed in `gradle/libs.versions.toml`:

- Add new versions to the `[versions]` section.
- Define new libraries in the `[libraries]` section.
- Group related libraries into `[bundles]` if appropriate.
- Check for known vulnerabilities before adding a dependency.
