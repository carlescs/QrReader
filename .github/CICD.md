# CI/CD Configuration

This document describes the CI/CD pipeline configuration for the QR Reader Android application.

## Overview

The project uses GitHub Actions for continuous integration and deployment. The main workflow is defined in `.github/workflows/android-ci-cd.yml`.

## Key Technologies

### Gradle Build Actions
The workflow uses the official [Gradle Actions](https://github.com/gradle/actions) for optimal build performance:

- **gradle/actions/wrapper-validation@v4**: Validates the Gradle wrapper for security
- **gradle/actions/setup-gradle@v4**: Sets up Gradle with intelligent caching
  - Automatically caches Gradle User Home directory
  - Caches configuration cache for faster incremental builds
  - Provides dependency graph for Dependabot alerts
  - Superior to manual caching with `actions/cache`

### Action Versions (2026 Best Practices)
All actions are pinned to specific versions for security and reproducibility:

- Core Actions: `actions/checkout@v4`, `actions/setup-java@v4`, `actions/upload-artifact@v4`
- Testing: `EnricoMi/publish-unit-test-result-action@v2.22.0`
- Coverage: `codecov/codecov-action@v4`
- Publishing: `r0adkll/upload-google-play@v1.1.3`

## Workflow Jobs

### 1. Test Job
Runs on every push and pull request:
- Executes unit tests
- Generates JaCoCo coverage reports
- Uploads coverage to Codecov
- Publishes test results

### 2. Build Job
Runs after tests pass:
- Builds release bundle (AAB)
- Runs SonarCloud analysis (if configured)
  - Configuration centralized in `sonar-project.properties`
  - Uses Gradle SonarQube plugin for Android project analysis
  - Automatically uploads coverage reports from test job
- Signs the bundle when deploying to Play Store (master auto-deploy or manual trigger from any branch, requires secrets)
- Uploads artifacts (both unsigned and signed bundles)

### 3. Release Job (Alpha Track)
Publishes to Google Play Alpha track:
- **Automatic**: On master branch push
- **Manual**: Via workflow_dispatch from any branch (must check "Upload to Google Play Alpha")
- **Gets version information**: Extracts version name and version code from Git-based versioning
- **Generates release notes**: Creates localized release notes with version information
- Downloads signed bundle
- Validates bundle and secrets
- Uploads to Google Play Console Alpha track with release notes

### 4. Promote Job (**MANUAL APPROVAL REQUIRED**)
Promotes from Alpha to Production track:
- **Runs automatically after release job** on master branch pushes
- **Pauses and waits for manual approval** before executing (via GitHub Environment protection)
- Requires at least one reviewer to approve in the Actions UI
- **Gets version information**: Extracts version name and version code for release notes
- **Copies release notes**: Transfers release notes from Alpha to Production, or creates new ones
- Uses Google Play API to promote the release from Alpha to Production
- See "Manual Approval for Production Promotion" section below for setup and usage instructions

## Required Secrets

The following secrets must be configured in GitHub repository settings:

### For Bundle Signing

1. **KEYSTORE_BASE64**
   - Base64-encoded Android keystore file
   - Generate: `base64 -w 0 your-keystore.jks`
   - Used to sign the release bundle when deploying to Play Store
   - Required for both automatic (master) and manual (any branch) deployments

2. **KEYSTORE_PASSWORD**
   - Password for the keystore file

3. **KEY_ALIAS**
   - Alias of the signing key in the keystore

### For Google Play Publishing

4. **GOOGLE_PLAY_SERVICE_ACCOUNT_JSON**
   - Service account JSON key from Google Cloud Console
   - Required permissions: "Release Manager" role in Play Console
   - The app must be manually uploaded at least once before automation works

### Optional Secrets

5. **CODECOV_TOKEN**
   - Token for uploading coverage reports to Codecov
   - Optional but recommended for coverage tracking

6. **SONAR_TOKEN**
   - Token for SonarCloud code quality analysis
   - Optional but recommended for code quality tracking
   - Get from [SonarCloud](https://sonarcloud.io/) → Account → Security → Generate Token
   - Configuration stored in `sonar-project.properties` at repository root

## SonarCloud Configuration

This project uses SonarCloud for continuous code quality and security analysis. The configuration follows best practices for Android/Gradle projects.

### Architecture

**Gradle-based Analysis** (Recommended for Android Projects):
- The project uses the official `org.sonarqube` Gradle plugin
- This is the [recommended approach](https://docs.sonarsource.com/sonarcloud/advanced-setup/ci-based-analysis/sonarscanner-for-gradle/) for Gradle/Android projects
- Configuration is centralized in `sonar-project.properties` at the repository root
- Analysis is triggered via `./gradlew sonar` command in the CI/CD pipeline

### Why Gradle Plugin Instead of Generic Action?

SonarSource officially recommends using the Gradle plugin for Gradle-based projects because:
1. **Better Integration**: Native understanding of Gradle project structure
2. **Automatic Configuration**: Picks up source sets, dependencies, and build outputs automatically
3. **Coverage Integration**: Seamlessly integrates with JaCoCo coverage reports
4. **Android-Specific**: Handles Android-specific files and module structure correctly

The generic `sonarsource/sonarqube-scan-action` is designed for non-Gradle projects (e.g., pure JavaScript, Python, etc.).

### Configuration Files

**sonar-project.properties** (Repository Root):
```properties
sonar.projectKey=carles-cs_QrReader
sonar.projectName=QrReader
sonar.organization=carles-cs
sonar.host.url=https://sonarcloud.io
sonar.sources=app/src/main/java
sonar.tests=app/src/test/java,app/src/androidTest/java
sonar.coverage.jacoco.xmlReportPaths=app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
# ... (see file for complete configuration)
```

**build.gradle** (Root):
```gradle
plugins {
    alias(libs.plugins.sonarqube)  // Applies the SonarQube plugin
}
```

**gradle/libs.versions.toml**:
```toml
[versions]
sonarqube = "7.2.2.6593"

[plugins]
sonarqube = { id = "org.sonarqube", version.ref = "sonarqube" }
```

### GitHub Actions Integration

The workflow runs SonarCloud analysis in the Build job:

```yaml
- name: Run SonarCloud analysis
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  if: env.SONAR_TOKEN != ''
  continue-on-error: true
  run: |
    if [ "${{ github.event_name }}" == "pull_request" ]; then
      ./gradlew sonar -Dsonar.branch.name=${{ github.head_ref }} --no-configuration-cache
    else
      ./gradlew sonar --no-configuration-cache
    fi
```

**Key Features**:
- Only runs if `SONAR_TOKEN` secret is configured
- Uses `continue-on-error: true` to prevent blocking deployments if SonarCloud is temporarily unavailable
- Branch analysis for pull requests (uses `sonar.branch.name`)
- All other configuration comes from `sonar-project.properties`

### Setup Instructions

1. **Create SonarCloud Project**
   - Go to [SonarCloud](https://sonarcloud.io/)
   - Sign in with your GitHub account
   - Click "+" → "Analyze new project"
   - Select your repository
   - Follow the setup wizard

2. **Generate Token**
   - Go to Account → Security → Generate Tokens
   - Name: "GitHub Actions"
   - Type: "User Token" or "Project Analysis Token"
   - Copy the generated token

3. **Add Token to GitHub**
   - Repository Settings → Secrets and variables → Actions
   - New repository secret: `SONAR_TOKEN`
   - Paste the token value

4. **Verify Configuration**
   - Check that `sonar-project.properties` matches your SonarCloud project
   - Ensure `sonar.projectKey` and `sonar.organization` are correct
   - Push to master or create a pull request to trigger analysis

### Troubleshooting

**"Could not find project":**
- Verify `sonar.projectKey` in `sonar-project.properties` matches SonarCloud
- Ensure the project exists in your SonarCloud organization

**"Unauthorized" or "Forbidden":**
- Check that `SONAR_TOKEN` is correctly set in GitHub secrets
- Regenerate the token if it's expired
- Verify token has appropriate permissions

**Coverage not showing:**
- Ensure the test job runs before the build job
- Verify JaCoCo reports are generated: `./gradlew jacocoTestReport`
- Check that `sonar.coverage.jacoco.xmlReportPaths` points to the correct file
- The workflow downloads coverage artifacts from the test job

**Analysis skipped:**
- The step only runs if `SONAR_TOKEN` is set
- Check workflow logs to confirm the step was executed
- Look for errors in Gradle output

### Local Analysis

To run SonarCloud analysis locally:

```bash
# Ensure you have SONAR_TOKEN environment variable set
export SONAR_TOKEN=your-token-here

# Run tests and generate coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run SonarCloud analysis
./gradlew sonar
```

View results at: https://sonarcloud.io/project/overview?id=carles-cs_QrReader

## Setting Up Google Play Publishing

### Prerequisites

1. **Create a Google Cloud Project**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or use an existing one

2. **Enable Google Play Developer API**
   - Enable the "Google Play Android Developer API" for your project

3. **Create a Service Account**
   - Go to IAM & Admin → Service Accounts
   - Create a new service account
   - Download the JSON key file

4. **Grant Permissions in Play Console**
   - Go to [Google Play Console](https://play.google.com/console)
   - Navigate to Users & Permissions
   - Invite the service account email
   - Grant "Release Manager" permissions

5. **⚠️ IMPORTANT: Initial Manual Upload Required**
   - **You must upload your app manually at least once before automation will work**
   - This creates the necessary API connections and initializes your app in the Play Console
   - Without this step, the automated upload will fail with authentication errors

6. **Add Secret to GitHub**
   - Go to repository Settings → Secrets and variables → Actions
   - Add new secret named `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
   - Paste the entire JSON key file content

### Troubleshooting

**"Unknown error occurred" in upload step:**
- Verify the service account JSON is correctly formatted
- Ensure the service account has "Release Manager" role
- Confirm the app was manually uploaded at least once
- Check that the package name matches exactly

**Bundle not found:**
- Ensure the build job completed successfully
- Check that signing secrets are configured
- Verify the artifact was uploaded in the build job
- For feature branches, ensure "Upload to Google Play Alpha" was checked when triggering the workflow

**Secret not found:**
- The release and promote jobs will fail with clear error messages if secrets are missing
- Add the required secrets following the instructions above

## Deploying from Feature Branches

The workflow supports deploying development versions from any branch to the Alpha track for testing. This is useful for:
- Testing features in production-like environment before merging
- QA testing of specific feature branches
- Beta testing with stakeholders

### How to Deploy a Feature Branch to Alpha

1. **Push your feature branch to GitHub**:
   ```bash
   git push origin feature/my-new-feature
   ```

2. **Trigger the workflow manually**:
   - Go to GitHub repository → **Actions** tab
   - Select **"Android CI/CD"** workflow from the left sidebar
   - Click **"Run workflow"** button (top right)
   - In the dropdown:
     - **Branch**: Select your feature branch (e.g., `feature/my-new-feature`)
     - **Upload to Google Play Alpha**: ✅ Check this box
   - Click **"Run workflow"** button

3. **Monitor the workflow**:
   - The workflow will run tests, build, sign, and deploy to Alpha track
   - Development version format: `5.2.0-dev.3+abc1234` (includes commit hash)
   - Each branch gets a unique version name for parallel testing

### What Happens

✅ **Test job**: Runs unit tests and generates coverage
✅ **Build job**: Builds release bundle (AAB)
✅ **Sign bundle**: Signs the bundle using keystore secrets (same as master)
✅ **Release to Alpha**: Uploads to Google Play Alpha track
❌ **Promote to Production**: Does NOT run (master only)

### Version Naming

Feature branch deployments use development version names:
- Format: `{version}-dev.{commits}+{hash}`
- Example: `5.2.0-dev.8+a1b2c3d`
  - `5.2.0`: Last git tag version
  - `dev.8`: 8 commits since that tag
  - `a1b2c3d`: Unique commit hash

This ensures each branch has a unique, traceable version.

### Requirements

- All signing secrets must be configured (see "Required Secrets" section)
- Feature branch must be pushed to GitHub remote
- You must have permission to trigger workflows in the repository

### Differences from Master Branch Deployment

| Aspect | Master Branch | Feature Branch |
|--------|--------------|----------------|
| **Trigger** | Automatic on push | Manual via workflow_dispatch |
| **Alpha Deploy** | ✅ Automatic | ✅ Manual (must check box) |
| **Production Promote** | ✅ Available (with approval) | ❌ Not available |
| **Version Name** | Clean (e.g., `5.2.0`) or dev | Always dev (e.g., `5.2.0-dev.8+hash`) |

## Manual Approval for Production Promotion

The promote job runs automatically after a successful alpha release but **pauses and waits for manual approval** before executing. This provides control over production deployments while keeping everything in a single pipeline run.

### Pipeline Behavior

When you push to master:
1. ✅ **Test job** runs automatically
2. ✅ **Build job** runs automatically (after test)
3. ✅ **Release job** runs automatically (publishes to Alpha track)
4. ⏸️ **Promote job** starts but **waits for approval** before executing
5. ✅ **After approval**: Promotes to Production track

### How to Approve/Reject Production Promotion

When a workflow is waiting for approval:

1. **Navigate to Actions tab** in GitHub repository
2. **Click on the running workflow** (you'll see a yellow dot indicating "waiting")
3. **Click on "Promote to Production" job** - you'll see "This workflow is waiting for approval"
4. **Review the details**:
   - Check that Alpha release succeeded
   - Verify the app version and build
   - Review any test results
5. **Click "Review deployments"** button
6. **Select "PlayStore" environment** (check the box)
7. **Add a comment** (optional but recommended)
8. **Click "Approve and deploy"** to proceed or **"Reject"** to cancel

### Setting Up Environment Protection (Required for Approval Gate)

⚠️ **Important**: Environment protection rules must be configured for the approval gate to work.

1. Go to repository **Settings → Environments**
2. **If "PlayStore" environment doesn't exist**:
   - Click **"New environment"**
   - Name it **"PlayStore"** (exact name, case-sensitive)
3. Click on **PlayStore** environment
4. Under **Environment protection rules**, check **"Required reviewers"**
5. **Add reviewers**: Click **"Add reviewers"** and select team members who can approve production deployments
   - Recommend: Add at least 2 reviewers for redundancy
   - Only selected reviewers can approve deployments
6. (Optional) Configure **Wait timer**: Add a delay (e.g., 5 minutes) to give time for review
7. **Click "Save protection rules"**

### What Happens During Promotion

The promote job will:
1. Wait for manual approval from designated reviewers
2. Extract version name and version code from Git-based versioning
3. Connect to Google Play Console using service account credentials
4. Fetch the latest version from the Alpha track (including release notes)
5. Promote that version to the Production track with release notes
6. Mark the release as "completed" (fully rolled out)

**Version Information in Release Notes:**
- The workflow automatically extracts version name and version code using `printVersionName` and `printVersionCode` Gradle tasks
- Release notes include: `Version X.X.X (Build XXX)`
- Alpha track: Release notes are generated from a template and include version information
- Production track: Release notes are copied from the Alpha track, or new ones are generated if not available

### Prerequisites

- The release job must have successfully published to Alpha track
- All required secrets must be configured (see Required Secrets section)
- Environment protection rules must be configured (see above)
- The app must pass Google Play's review process

### Notifications

GitHub will notify designated reviewers via:
- Email notification
- GitHub notification bell
- Mobile notifications (if GitHub mobile app is configured)

Reviewers can approve from:
- GitHub web interface
- GitHub mobile app
- GitHub CLI: `gh run approve <run-id>`

## Environment Protection

The workflow uses GitHub Environments for deployment gates:

- **PlayStore-Alpha**: Automatically deploys to alpha track (no protection rules required)
- **PlayStore**: Used for production promotion (**MUST** be configured with required reviewers for manual approval)

**Critical**: The PlayStore environment **must** have "Required reviewers" configured for the manual approval gate to work. Without this configuration, the promote job will run automatically without waiting for approval.

See "Setting Up Environment Protection" in the "Manual Approval for Production Promotion" section above for detailed setup instructions.

Configure environments in repository **Settings → Environments**.

## Release Notes and Version Information

The CI/CD pipeline automatically includes version information in release notes for both Alpha and Production deployments.

### Automatic Version Extraction

The workflow extracts version information from Git-based versioning:
- **Version Name**: Retrieved using `./gradlew -q printVersionName`
  - Example: `5.2.0` (tagged release) or `5.2.0-dev.3+abc1234` (development build)
- **Version Code**: Retrieved using `./gradlew -q printVersionCode`
  - Example: `250` (based on Git commit count)

### Release Notes Format

**Alpha Track:**
```
Version 5.2.0 (Build 250)

What's new in this release:
- Latest updates and improvements
- Bug fixes and performance enhancements
```

**Production Track:**
- Copies release notes from Alpha track automatically
- Falls back to generated notes if Alpha notes are unavailable

### Customizing Release Notes

To customize release notes for a specific release:

1. **Option 1: Modify the workflow (recommended for consistent changes)**
   - Edit `.github/workflows/android-ci-cd.yml`
   - Locate the "Generate release notes" step in the `release` job
   - Modify the template text

2. **Option 2: Create release notes files (for one-time or localized notes)**
   - Create `distribution/whatsnew/` directory
   - Add files: `whatsnew-en-US`, `whatsnew-es-ES`, etc. (BCP 47 format)
   - Commit and push before the release
   - The workflow will use these files if present

### Testing Version Extraction Locally

```bash
# Test version name extraction
./gradlew -q printVersionName

# Test version code extraction
./gradlew -q printVersionCode
```

## Local Testing

To test the build locally:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release bundle
./gradlew bundleRelease

# Run tests
./gradlew testDebugUnitTest

# Generate coverage report
./gradlew jacocoTestReport
```

## GitHub Actions Best Practices (2026)

### Caching Strategy
The workflow uses the official Gradle Actions for optimal caching:
- **gradle/actions/setup-gradle**: Provides superior caching compared to manual `actions/cache`
- Automatically caches Gradle User Home directory
- Caches configuration cache for faster incremental builds
- Generates dependency graphs for Dependabot security alerts

**Why not `actions/setup-java` with `cache: 'gradle'`?**
- The Gradle-specific action provides more efficient caching
- Supports configuration cache (critical for Android builds)
- Better cache hit rates and faster builds
- Recommended by both Gradle and GitHub communities

### Security Practices
- **Gradle Wrapper Validation**: Validates wrapper JARs against known checksums to prevent supply chain attacks
- **Version Pinning**: All actions pinned to specific versions (e.g., `@v4`, `@v2.22.0`)
- **Minimal Permissions**: Workflow uses principle of least privilege for permissions
- **Secret Handling**: Secrets validated before use, never logged or exposed

### Alternative Publishing Options

While the workflow uses `r0adkll/upload-google-play@v1.1.3`, alternatives include:

**Fastlane**
- More flexible and feature-rich
- Supports complex release workflows
- Better for teams managing iOS and Android
- Supports Workload Identity Federation (keyless auth)
- Requires Ruby environment and Fastfile configuration

**Custom Scripts**
- Maximum control over publishing process
- Uses Google Play Developer API directly
- Higher maintenance overhead
- Best for specific enterprise requirements

**Current Choice: r0adkll/upload-google-play**
- Native GitHub Actions integration
- Easy to configure and maintain
- Well-maintained and widely adopted
- Sufficient for most Android projects

## Performance Optimizations

### Build Time Improvements
1. **Gradle caching**: Configured via `gradle/actions/setup-gradle`
2. **Parallel execution**: Gradle runs tests and builds in parallel where possible
3. **Incremental builds**: Configuration cache enabled for faster subsequent builds
4. **Artifact reuse**: Build artifacts shared between jobs to avoid rebuilds

### CI/CD Pipeline Efficiency
- Test and build jobs run in parallel where dependencies allow
- Artifacts uploaded/downloaded only when needed
- Conditional job execution (release/promote only on master branch)
- SonarCloud analysis runs with `continue-on-error` to prevent blocking deployments

## Maintenance Guidelines

### Updating Actions
Check for new versions quarterly:
```bash
# Check current versions
grep 'uses:' .github/workflows/android-ci-cd.yml

# Update to latest
# - Review changelogs for breaking changes
# - Test in a branch before merging to master
# - Pin to specific versions, not tags like @latest
```

### Monitoring Build Performance
- Review Gradle Build Scans for performance insights
- Monitor cache hit rates in workflow logs
- Track build times trends over time

## References

- [Gradle Actions Documentation](https://github.com/gradle/actions)
- [r0adkll/upload-google-play Action](https://github.com/r0adkll/upload-google-play)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [GitHub Actions Best Practices](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
