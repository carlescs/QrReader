# GitHub Copilot Instructions for QR Reader

This repository contains comprehensive instructions for AI coding assistants in the [`AGENTS.md`](../AGENTS.md) file at the root of the repository.

**Please refer to [`AGENTS.md`](../AGENTS.md) for complete project guidelines including:**

- Project overview and architecture
- Technology stack and dependencies
- Coding standards and conventions
- Testing guidelines
- Build and deployment instructions
- Common development tasks

## Quick Reference

### Technology Stack
- **Language**: Kotlin 2.3.0
- **UI**: Jetpack Compose 1.10.2 with Material3
- **Architecture**: Clean Architecture (Domain, Data, Presentation layers)
- **DI**: Koin 4.1.1
- **Database**: Room 2.8.4
- **Build**: Gradle with version catalogs

### Key Commands
```bash
# Build
./gradlew assembleDebug

# Test
./gradlew testDebugUnitTest

# Coverage
./gradlew jacocoTestReport

# Lint
./gradlew lint
```

### Architecture Layers
```
app/src/main/java/cat/company/qrreader/
├── domain/      # Business logic (models, repositories, use cases)
├── data/        # Data layer (repository implementations)
├── features/    # Feature modules (camera, history, settings, etc.)
├── db/          # Room database (DAOs, entities, migrations)
├── di/          # Koin dependency injection
└── ui/          # Shared UI components and theme
```

### Critical Guidelines
1. **Follow Clean Architecture**: Separate domain, data, and presentation layers
2. **Use Koin for DI**: Register all dependencies in `di/AppModule.kt`
3. **No explicit dispatchers in ViewModels**: Let repositories handle `Dispatchers.IO`
4. **Test database migrations**: Always test schema changes
5. **Keep composables focused**: Extract complex UI into smaller composables (SonarQube S3776)
6. **Limit function parameters**: Use data classes for >7 parameters (SonarQube S107)

### Before Making Changes
- Run existing tests to understand baseline: `./gradlew testDebugUnitTest`
- Check SonarQube rules for complexity and maintainability
- Review similar features in the codebase for consistency

For complete details, conventions, and best practices, see [`AGENTS.md`](../AGENTS.md).
