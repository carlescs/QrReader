# CI/CD Configuration

This document describes the CI/CD pipeline configuration for the QR Reader Android application.

## Getting Started: Initial Setup

### Where is Version Information Stored?

The QR Reader project uses **Git-based automatic versioning**. Version information is stored in two places:

```
┌─────────────────────────────────────────────────────────────────┐
│                    VERSION STORAGE FLOW                          │
└─────────────────────────────────────────────────────────────────┘

  GIT REPOSITORY (Source of Truth)
  ┌──────────────────────────────────┐
  │  Git Commits                     │
  │  ├── Commit 1                    │
  │  ├── Commit 2                    │──┐
  │  ├── ...                         │  │
  │  └── Commit 267 (HEAD)           │  │  Version Code = 267
  │                                  │  │  (Total commit count)
  │  Git Tags                        │  │
  │  ├── v5.1.0                      │  │
  │  ├── v5.1.8                      │──┤
  │  └── v5.2.0 (latest)             │  │  Version Name = "5.2.0"
  │                                  │  │  (From latest tag)
  └──────────────────────────────────┘  │
                                        │
                    ┌───────────────────┘
                    │
                    ▼
  BUILD CONFIGURATION (Reads from Git)
  ┌──────────────────────────────────────────────────────────┐
  │  buildSrc/src/main/kotlin/GitVersioning.kt              │
  │  ├── getVersionCode() → git rev-list --count HEAD       │
  │  └── getVersionName() → git describe --tags             │
  └──────────────────────────────────────────────────────────┘
                    │
                    ▼
  ┌──────────────────────────────────────────────────────────┐
  │  app/build.gradle (lines 22-23)                          │
  │  versionCode = GitVersioning.getVersionCode(project)     │
  │  versionName = GitVersioning.getVersionName(project)     │
  └──────────────────────────────────────────────────────────┘
                    │
                    ▼
  ┌──────────────────────────────────────────────────────────┐
  │  BUILT APP                                               │
  │  QRReader-5.2.0.aab                                      │
  │  ├── versionCode: 267                                    │
  │  └── versionName: "5.2.0"                                │
  └──────────────────────────────────────────────────────────┘
                    │
                    ▼
  ┌──────────────────────────────────────────────────────────┐
  │  DEPLOYMENT DESTINATIONS                                 │
  │  ├── GitHub Releases (artifacts)                         │
  │  ├── Google Play Alpha (auto on master)                 │
  │  └── Google Play Production (manual approval)           │
  └──────────────────────────────────────────────────────────┘
```

#### 1. Git Repository (Primary Source)
- **Version Code**: Automatically calculated from the total number of Git commits
  - Location: Git history (`git rev-list --count HEAD`)
  - Updates: Automatically increments with every commit
  - Storage: Inherent to Git repository

- **Version Name**: Derived from Git tags following semantic versioning
  - Location: Git tags (e.g., `v5.2.0`, `v5.2.1`)
  - Format: `major.minor.patch` (following semantic versioning)
  - Storage: Git tag objects (`git tag -l`)

#### 2. Build Configuration (Derived)
- **File**: `app/build.gradle`
- **Lines 22-23**:
  ```gradle
  versionCode = GitVersioning.getVersionCode(project)
  versionName = GitVersioning.getVersionName(project)
  ```
- **Purpose**: Calls `buildSrc/src/main/kotlin/GitVersioning.kt` to extract version from Git

**Key Point**: Version numbers are **NOT** manually edited in code. They are automatically derived from Git history and tags during build time.

### Setting Up Initial Version for CI/CD

Follow these steps to initialize versioning for your repository:

#### Step 1: Ensure Full Git History
The versioning system requires full Git history (not a shallow clone):

```bash
# Check if you have full history
git log --oneline | wc -l

# If using GitHub Actions (already configured)
# The workflow uses fetch-depth: 0 in checkout action
```

**In CI/CD**: The workflow already includes `fetch-depth: 0` in `.github/workflows/android-ci-cd.yml`:
```yaml
- name: Checkout code
  uses: actions/checkout@v4
  with:
    fetch-depth: 0  # This ensures full Git history
```

#### Step 2: Create Your First Version Tag

Choose an appropriate starting version based on your project state:

```bash
# Example: Starting with version 5.2.0
git tag v5.2.0

# Push the tag to GitHub
git push origin v5.2.0
```

**Semantic Versioning Guidelines**:
- **v1.0.0**: First stable release
- **v5.2.0**: If continuing from previous manual version (current project uses v5.x.x)
- Format: `v{major}.{minor}.{patch}`

#### Step 3: Verify Version Calculation

Test that versions are calculated correctly:

```bash
# Check version code (commit count)
git rev-list --count HEAD
# Output: 267 (or your current commit count)

# Check version name (from tag)
git describe --tags --abbrev=0
# Output: v5.2.0

# Test with Gradle
./gradlew printVersionName
# Output: versionCode=267, versionName=5.2.0
```

#### Step 4: Understand Development vs Release Versions

After tagging, subsequent commits create **development versions**:

**On a tagged commit** (e.g., commit abc123 tagged as v5.2.0):
- Version Code: `267`
- Version Name: `5.2.0` (clean version)

**3 commits after the tag** (e.g., commit def456):
- Version Code: `270` (267 + 3 commits)
- Version Name: `5.2.0-dev.3+def456`
  - `5.2.0`: Last tag version
  - `dev.3`: 3 commits since tag
  - `def456`: Short commit hash

**Benefits**:
- Every build has a unique, traceable version
- Feature branches get unique versions for parallel testing
- No manual version management needed

#### Step 5: Configure CI/CD Secrets

For automated deployment to Google Play, configure these secrets in GitHub:

**Repository Settings → Secrets and variables → Actions → New repository secret**

1. **KEYSTORE_BASE64**: Base64-encoded keystore for signing
   ```bash
   base64 -w 0 your-keystore.jks
   ```

2. **KEYSTORE_PASSWORD**: Your keystore password

3. **KEY_ALIAS**: Signing key alias in keystore

4. **GOOGLE_PLAY_SERVICE_ACCOUNT_JSON**: Service account JSON for Play Store API
   - See "Setting Up Google Play Publishing" section below

5. **CODECOV_TOKEN** (Optional): For coverage reporting

6. **SONAR_TOKEN** (Optional): For code quality analysis

#### Step 6: First CI/CD Run

After setup, trigger your first CI/CD run:

**Automatic** (recommended for testing):
```bash
# Push to master to trigger automatic run
git checkout master
git push origin master
```

**Manual** (for testing without deploying):
- Go to GitHub Actions → "Android CI/CD" workflow
- Click "Run workflow"
- Select branch: `master`
- Upload to Google Play Alpha: `☐` (uncheck for first test)
- Click "Run workflow"

The workflow will:
1. ✅ Run unit tests
2. ✅ Build release bundle (AAB)
3. ✅ Calculate version from Git (versionCode=267, versionName=5.2.0)
4. ✅ Sign bundle (if secrets configured)
5. ✅ Upload to Play Store Alpha (if enabled and on master)

### Subsequent Deployments: How It Works

After initial setup, version management is fully automatic:

#### Regular Development Workflow
```bash
# 1. Make changes
git add .
git commit -m "Add new feature"
git push origin master

# Version automatically increments:
# - versionCode: 268 (was 267)
# - versionName: 5.2.0-dev.1+abc123
```

#### Creating a New Release
```bash
# 1. When ready for a new release version
git tag v5.2.1
git push origin v5.2.1

# Version becomes clean:
# - versionCode: 268
# - versionName: 5.2.1 (clean, no -dev suffix)

# 2. Next commit after tag
git commit -m "Start next development cycle"
git push

# Version becomes development again:
# - versionCode: 269
# - versionName: 5.2.1-dev.1+def456
```

#### Feature Branch Deployments
```bash
# 1. Create and work on feature branch
git checkout -b feature/new-scanner
git commit -m "Implement new scanner"
git push origin feature/new-scanner

# Version on this branch:
# - versionCode: 269 (commit count)
# - versionName: 5.2.1-dev.2+xyz789

# 2. Deploy to Alpha for testing (manual trigger)
# Go to GitHub Actions → "Android CI/CD" → "Run workflow"
# Select branch: feature/new-scanner
# Check: "Upload to Google Play Alpha"
# Click: "Run workflow"
```

### Storage Summary

| Information | Storage Location | Updated By | Example |
|-------------|-----------------|------------|---------|
| **Version Code** | Git commit history | Git (automatic) | `267` |
| **Version Name** | Git tags | Developer (manual tagging) | `v5.2.0` |
| **Build Config** | `app/build.gradle` lines 22-23 | Never (calls GitVersioning) | `versionCode = GitVersioning.getVersionCode(project)` |
| **Version Logic** | `buildSrc/src/main/kotlin/GitVersioning.kt` | Never (unless changing logic) | Logic to extract from Git |
| **CI/CD Config** | `.github/workflows/android-ci-cd.yml` | Never (unless changing pipeline) | `fetch-depth: 0` for full history |
| **Release Artifacts** | GitHub Releases (for tags) | GitHub Actions (automatic) | APK/AAB files |
| **Play Store Alpha** | Google Play Console | GitHub Actions (automatic on master) | Uploaded AAB |
| **Play Store Prod** | Google Play Console | Manual promotion approval | Promoted from Alpha |

**Key Insight**: 
- Version **code** is stored in Git commit history (never edited manually)
- Version **name** is stored in Git tags (created when releasing)
- Everything else derives from these two sources automatically

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
- Downloads signed bundle
- Validates bundle and secrets
- Uploads to Google Play Console Alpha track

### 4. Promote Job (**MANUAL APPROVAL REQUIRED**)
Promotes from Alpha to Production track:
- **Runs automatically after release job** on master branch pushes
- **Pauses and waits for manual approval** before executing (via GitHub Environment protection)
- Requires at least one reviewer to approve in the Actions UI
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
2. Connect to Google Play Console using service account credentials
3. Fetch the latest version from the Alpha track
4. Promote that version to the Production track
5. Mark the release as "completed" (fully rolled out)

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

## Quick Reference: Version Management Commands

### Checking Current Version
```bash
# View current version code and name
./gradlew printVersionName

# Check commit count (version code)
git rev-list --count HEAD

# Check last tag (version name base)
git describe --tags --abbrev=0

# Check current commit hash
git rev-parse --short HEAD
```

### Creating Releases
```bash
# Check current status
git status
git log --oneline -5

# Create and push a release tag
git tag v5.3.0
git push origin v5.3.0

# This triggers:
# 1. GitHub Release workflow (creates release with artifacts)
# 2. CI/CD workflow (if on master, auto-deploys to Alpha)
```

### Managing Tags
```bash
# List all tags
git tag -l

# Delete a local tag (if mistake)
git tag -d v5.3.0

# Delete a remote tag (if pushed by mistake)
git push origin :refs/tags/v5.3.0

# Create an annotated tag with message
git tag -a v5.3.0 -m "Release version 5.3.0 with new features"
git push origin v5.3.0
```

### Viewing Version History
```bash
# Show all tags with dates
git log --tags --simplify-by-decoration --pretty="format:%ai %d"

# Show commits between tags
git log v5.2.0..v5.3.0 --oneline

# Show what will be in next release
git log v5.3.0..HEAD --oneline
```

## Troubleshooting: Initial Setup Issues

### Issue: "No tags found" or version shows 0.0.1-dev

**Symptom**: 
```
versionName: 0.0.1-dev+abc1234
```

**Cause**: No Git tags exist in the repository.

**Solution**:
```bash
# Create your first tag
git tag v5.2.0
git push origin v5.2.0

# Verify
./gradlew printVersionName
# Should now show: versionName: 5.2.0
```

### Issue: "Failed to get Git commit count"

**Symptom**: Version code defaults to 1, warning in build logs.

**Cause**: Repository is a shallow clone (missing Git history).

**Solution**:
```bash
# Convert shallow clone to full clone
git fetch --unshallow

# Verify full history
git log --oneline | wc -l
# Should show complete commit count (not just recent commits)
```

**For CI/CD**: Ensure workflow has `fetch-depth: 0`:
```yaml
- uses: actions/checkout@v4
  with:
    fetch-depth: 0  # This is already configured in android-ci-cd.yml
```

### Issue: Version not incrementing after commits

**Symptom**: Version code stays the same after new commits.

**Cause**: Git repository may not be updating, or build cache issue.

**Solution**:
```bash
# Verify Git is tracking commits
git log --oneline -5

# Clean Gradle cache and rebuild
./gradlew clean
./gradlew printVersionName

# If still issues, check .git directory exists
ls -la .git
```

### Issue: CI/CD fails with "unable to sign bundle"

**Symptom**: Build job fails at signing step.

**Cause**: Missing or incorrect signing secrets.

**Solution**:
1. Verify secrets are configured in GitHub:
   - Go to: Repository Settings → Secrets and variables → Actions
   - Check for: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`

2. Verify keystore encoding:
   ```bash
   # Re-encode keystore if needed
   base64 -w 0 your-keystore.jks > keystore.txt
   # Copy content of keystore.txt to KEYSTORE_BASE64 secret
   ```

3. Test locally first:
   ```bash
   # Test signing locally before pushing
   ./gradlew bundleRelease
   ```

### Issue: Upload to Play Store fails

**Symptom**: Release job fails with authentication or permission errors.

**Common Causes and Solutions**:

1. **"App not found" or "Package name not found"**:
   - ⚠️ **You must manually upload your app to Play Console at least once**
   - Go to Play Console → Create app → Upload AAB manually
   - Then automation will work for subsequent uploads

2. **"Service account does not have permission"**:
   - Go to Play Console → Users & Permissions
   - Find your service account email
   - Grant "Release Manager" role (minimum)

3. **"Invalid JSON key"**:
   - Verify `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` secret
   - Ensure entire JSON file content is pasted (including { })
   - Check for no extra spaces or newlines

4. **"Track not found"**:
   - Ensure the Alpha track exists in Play Console
   - Some apps need internal track created first

### Issue: Feature branch shows wrong version

**Symptom**: Feature branch version doesn't match expected format.

**Expected**: `5.2.0-dev.5+abc123`  
**Got**: `5.1.8-dev.15+xyz789`

**Explanation**: This is correct! The version is based on the **last tag** in Git history, not the master branch version. The feature branch may have branched from an earlier tag.

**Solution**: This is normal behavior and doesn't affect functionality. Each branch gets a unique hash anyway.

### Issue: Multiple feature branches have same version code

**Symptom**: Two feature branches both show versionCode=268.

**Explanation**: Version code is based on commit count, which can be similar for branches created around the same time.

**Is this a problem?**: No! Google Play allows same version code for Alpha/Beta tracks. The version **name** includes the unique commit hash (e.g., `5.2.0-dev.5+abc123` vs `5.2.0-dev.5+def456`), ensuring uniqueness.

**For Production**: Only production releases need unique version codes, which is guaranteed because they increment with each commit to master.

## FAQ: Initial Setup and Storage

### Q: Where is the version number stored?
**A**: Version information is stored in **Git**, not in code files:
- **Version Code**: Calculated from Git commit history (`git rev-list --count HEAD`)
- **Version Name**: Derived from Git tags (`v5.2.0`, `v5.3.0`, etc.)

The `app/build.gradle` file **calls** the versioning logic but doesn't store the numbers.

### Q: Do I need to edit version numbers manually?
**A**: No! Never edit version numbers manually. They are automatically calculated from Git:
- Version code increments automatically with each commit
- Version name comes from Git tags (you create tags when releasing)

### Q: How do I release version 5.3.0?
**A**: Create and push a Git tag:
```bash
git tag v5.3.0
git push origin v5.3.0
```

### Q: What happens if I forget to create a tag?
**A**: The build will use a development version (e.g., `5.2.0-dev.8+abc123`). This is fine for testing but not recommended for production releases.

### Q: Can I go back to manual versioning?
**A**: Yes, but not recommended. You would need to:
1. Edit `app/build.gradle` to set static values
2. Manually increment version for each release
3. Lose automatic version tracking and traceability

The Git-based approach is more reliable and automatic.

### Q: What version should I start with?
**A**: Choose based on your current state:
- **New project**: Start with `v1.0.0`
- **Existing project**: Use your current version (e.g., `v5.2.0` if you were at 5.1.8)
- **Major rewrite**: Consider bumping major version (e.g., `v6.0.0`)

### Q: Where can I see all versions that have been deployed?
**A**: Multiple places:
- **GitHub Releases**: All tagged releases with artifacts
- **Google Play Console**: All versions uploaded to Alpha/Beta/Production
- **Git Tags**: `git tag -l` shows all version tags
- **GitHub Actions**: Past workflow runs show version numbers in logs

### Q: How do I know what changed between versions?
**A**: Use Git:
```bash
# Show commits between two tags
git log v5.2.0..v5.3.0 --oneline

# Show detailed changes
git log v5.2.0..v5.3.0 --stat

# What will be in next release
git log v5.3.0..HEAD --oneline
```

## References

- [Git-based Versioning Details](../VERSIONING.md) - Complete versioning guide
- [Gradle Actions Documentation](https://github.com/gradle/actions)
- [r0adkll/upload-google-play Action](https://github.com/r0adkll/upload-google-play)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [GitHub Actions Best Practices](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Semantic Versioning Specification](https://semver.org/)
