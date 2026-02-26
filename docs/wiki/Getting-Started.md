# Getting Started

This page explains how to set up the project, build the app, and run the test suite locally.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | Meerkat (2024.3.1) or later recommended |
| JDK | 21 |
| Gradle | Managed by the Gradle wrapper (`gradlew`) |
| Android SDK | Min 26, Target 36 |
| Git | Required for automatic versioning |

---

## 1. Clone the Repository

```bash
git clone https://github.com/carlescs/QrReader.git
cd QrReader
```

> **Important:** The project uses Git commit history for automatic versioning. Do **not** clone with `--depth`; if you already did, run `git fetch --unshallow`.

---

## 2. Firebase Setup (Optional for Local Development)

The app integrates Firebase Analytics. A `google-services.json` file is required for the build to succeed. For local development without Firebase:

1. Create a placeholder `app/google-services.json` from the Firebase Console for your own project, **or**
2. Disable the `google-services` plugin temporarily in `app/build.gradle`.

For CI/CD, the file is provided as a GitHub Secret. See the [CI/CD Guide](../../.github/CICD.md) for details.

---

## 3. Build the App

```bash
# Assemble debug APK
./gradlew assembleDebug

# Assemble release AAB
./gradlew bundleRelease

# Install on connected device / emulator
./gradlew installDebug
```

---

## 4. Run Tests

```bash
# Unit tests only
./gradlew testDebugUnitTest

# Unit tests + JaCoCo coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Instrumented tests (requires a connected device / emulator)
./gradlew connectedAndroidTest
```

Coverage reports are generated at `app/build/reports/jacoco/`.

---

## 5. Versioning

App versions are calculated automatically from Git tags and commit count. No manual version editing is needed.

```bash
# Print the current version name
./gradlew printVersionName
```

See [VERSIONING.md](../../VERSIONING.md) for full details.

---

## 6. Google Play API Credentials (Optional)

For accurate version codes that align with the Play Store, create a `service-account.json` in the project root. See [VERSIONING.md](../../VERSIONING.md#setup-first-time-only) for instructions.

Without credentials, the build falls back to `commit_count + 365`.

---

## Useful Gradle Commands

| Command | Description |
|---------|-------------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew bundleRelease` | Build release AAB |
| `./gradlew testDebugUnitTest` | Run unit tests |
| `./gradlew jacocoTestReport` | Generate coverage report |
| `./gradlew connectedAndroidTest` | Run instrumented tests |
| `./gradlew lint` | Run Android Lint |
| `./gradlew sonar` | Run SonarCloud analysis |
| `./gradlew printVersionName` | Print current version name |
| `./gradlew clean` | Clean build outputs |
