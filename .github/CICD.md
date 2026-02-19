# CI/CD Configuration

This document describes the CI/CD pipeline configuration for the QR Reader Android application.

## Quick Start for Solo Developers

**As a solo developer**, you can configure a streamlined approval process:

1. **Environment Setup**: Add yourself as the sole reviewer for the `PlayStore` environment
   - This creates a manual approval gate you control
   - You can self-approve deployments when ready
   - Provides a final safety check before production

2. **Simplified Flow**: Use the default 2-tier flow (Alpha → Production)
   - Manually trigger deployment via workflow_dispatch → Alpha deployment
   - Self-approve Production promotion when ready
   - No need for Beta track or multiple reviewers

3. **Optional**: Skip approval gates entirely by not configuring environment protection
   - Deployments will proceed automatically after manual trigger
   - **Not recommended** for production apps (no safety gate)

See "Setting Up Environment Protection" below for configuration instructions.

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
- Signs the bundle when manually triggered for deployment (requires secrets)
- Uploads artifacts (both unsigned and signed bundles)

### 3. Release Job (Alpha Track)
Publishes to Google Play Alpha track:
- **Manual Only**: Via workflow_dispatch from any branch (must check "Upload to Google Play Alpha")
- **No longer automatic** on master branch push
- Downloads signed bundle
- Validates bundle and secrets
- Uploads to Google Play Console Alpha track

### 4. Beta Job (Optional - 3-Tier Flow)
Promotes from Alpha to Beta track (OPTIONAL):
- **Only runs when**: `use_beta_track` input is set to `true` via workflow_dispatch
- **Purpose**: Enables 3-tier deployment flow (Alpha → Beta → Production)
- **Trigger**: Manual workflow dispatch with "Use Beta track" checkbox enabled
- **Approval**: Requires manual approval via PlayStore-Beta environment
- Downloads latest version from Alpha track
- Promotes to Beta track for external beta testing
- See "Deployment Flows" section below for when to use Beta

### 5. Promote Job (**MANUAL APPROVAL REQUIRED**)
Promotes to Production track (supports both flows):
- **2-Tier Flow** (default): Promotes directly from Alpha to Production
- **3-Tier Flow** (optional): Promotes from Beta to Production (when Beta job ran)
- **Runs after release job** (or after Beta if 3-tier) when manually triggered
- **Pauses and waits for manual approval** before executing (via GitHub Environment protection)
- **Approval requirements**: Configurable based on team size
  - Solo developers: Can add themselves as single reviewer for self-approval
  - Teams: Recommend 2+ reviewers for redundancy
- Intelligently detects source track (Alpha or Beta) based on workflow inputs
- Uses Google Play API to promote the release to Production
- See "Manual Approval for Production Promotion" section below for setup and usage instructions

## Deployment Flows

The CI/CD pipeline supports **two flexible deployment paths** to accommodate different release scenarios:

### Flow 1: Fast-Track (2-Tier) - **DEFAULT**
```
Code → CI → Alpha (Internal) → Production (Public)
```

**When to Use:**
- ✅ Hotfixes and critical bug fixes
- ✅ Minor updates and patches
- ✅ Changes with thorough CI/CD testing
- ✅ Low-risk releases
- ✅ When speed is important

**Timeline:** 2-3 days (Alpha testing + Production approval)

**How to Trigger:**
1. Navigate to: GitHub → Actions → "Android CI/CD"
2. Click "Run workflow" button
3. Select branch (usually master)
4. ✅ Check "Upload to Google Play Alpha"
5. Click "Run workflow"
6. Wait for Alpha testing validation
7. Approve Production promotion (self-approve if solo developer, or wait for team reviewers)

### Flow 2: Cautious (3-Tier) - **OPT-IN**
```
Code → CI → Alpha (Internal) → Beta (External) → Production (Public)
```

**When to Use:**
- ✅ Major feature releases
- ✅ Significant architectural changes
- ✅ High-risk updates
- ✅ Need external beta tester feedback
- ✅ Regulatory/compliance-sensitive releases
- ✅ When extra validation is needed

**Timeline:** 5-8 days (Alpha 2-3d + Beta 3-5d + Production approval)

**How to Trigger:**
1. Navigate to: GitHub → Actions → "Android CI/CD"
2. Click "Run workflow" button
3. Select branch (usually master)
4. ✅ Check "Upload to Google Play Alpha"
5. ✅ Check "Use Beta track before Production" ← **KEY STEP**
6. Click "Run workflow"
7. Approve Beta promotion (if configured)
8. Approve Production promotion (self-approve if solo developer)

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

## CodeQL Security Scanning

This project uses CodeQL for automated security vulnerability detection. CodeQL analyzes the code for common security issues and coding errors.

### Workflow Configuration

CodeQL scanning is configured in `.github/workflows/codeql.yml` and runs:
- **On every push** to master branch
- **On every pull request** to master branch
- **Weekly** on Mondays at 00:00 UTC (scheduled scan)

### What CodeQL Scans For

- **Security vulnerabilities**: SQL injection, XSS, path traversal, etc.
- **Code quality issues**: Code smells, anti-patterns
- **Android-specific issues**: Security best practices for Android apps
- **Kotlin/Java issues**: Language-specific vulnerabilities

### Key Features

- **Automated Analysis**: Runs automatically on code changes
- **Security Alerts**: Creates security alerts in GitHub Security tab
- **Pull Request Integration**: Shows findings directly in PR reviews
- **Weekly Scans**: Regular scheduled scans catch new vulnerabilities
- **Extended Query Set**: Uses `security-extended` and `security-and-quality` query packs

### Viewing Results

1. Navigate to repository → **Security** tab
2. Click **Code scanning alerts**
3. Review any findings and remediate as needed
4. CodeQL provides detailed explanations and fix suggestions

### Build Integration

CodeQL builds the project during analysis to understand the code:
```yaml
- name: Build project
  run: ./gradlew assembleDebug --no-configuration-cache
```

This ensures accurate analysis of compiled languages like Kotlin/Java.

### No Additional Setup Required

CodeQL is fully configured and requires no secrets or tokens. It uses GitHub's built-in integration and runs automatically.

## Dependabot Dependency Updates

Automated dependency updates are configured via `.github/dependabot.yml`:

### What Dependabot Monitors

1. **GitHub Actions**: Weekly checks for action updates
2. **Gradle Dependencies**: Weekly checks for library updates

### Configuration Highlights

- **Weekly Schedule**: Runs every Monday
- **Pull Request Limits**: Max 5 for actions, 10 for Gradle
- **Grouped Patch Updates**: Minor patches grouped into single PR
- **Smart Ignoring**: Ignores Kotlin major version bumps (requires manual review)
- **Auto-labeling**: PRs labeled with "dependencies" for easy filtering

### Review Process

1. Dependabot creates PRs for dependency updates
2. CI/CD runs automatically on the PR
3. Review changes and test results
4. Merge if tests pass and changes are acceptable

No additional configuration needed - Dependabot is ready to use!

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

## Deploying to Alpha Track

The workflow supports deploying versions from any branch to the Alpha track for testing via manual workflow trigger. This is useful for:
- Testing features in production-like environment before merging
- QA testing of specific feature branches or master
- Beta testing with stakeholders

### How to Deploy to Alpha

1. **Push your branch to GitHub** (if not already pushed):
   ```bash
   git push origin feature/my-new-feature  # or master
   ```

2. **Trigger the workflow manually**:
   - Go to GitHub repository → **Actions** tab
   - Select **"Android CI/CD"** workflow from the left sidebar
   - Click **"Run workflow"** button (top right)
   - In the dropdown:
     - **Branch**: Select your branch (e.g., `feature/my-new-feature` or `master`)
     - **Upload to Google Play Alpha**: ✅ Check this box
   - Click **"Run workflow"** button

3. **Monitor the workflow**:
   - The workflow will run tests, build, sign, and deploy to Alpha track
   - Development version format: `5.2.0-dev.3+abc1234` (includes commit hash)
   - Each branch gets a unique version name for parallel testing

### What Happens

✅ **Test job**: Runs unit tests and generates coverage
✅ **Build job**: Builds release bundle (AAB)
✅ **Sign bundle**: Signs the bundle using keystore secrets
✅ **Release to Alpha**: Uploads to Google Play Alpha track
✅ **Promote to Production**: Available with manual approval (any branch)

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
| **Trigger** | Manual via workflow_dispatch | Manual via workflow_dispatch |
| **Alpha Deploy** | ✅ Manual (must check box) | ✅ Manual (must check box) |
| **Production Promote** | ✅ Available (with approval) | ✅ Available (with approval) |
| **Version Name** | Clean (e.g., `5.2.0`) or dev | Always dev (e.g., `5.2.0-dev.8+hash`) |

## Manual Approval for Production Promotion

The promote job runs after a successful alpha release (via manual workflow trigger) but **pauses and waits for manual approval** before executing. This provides control over production deployments while keeping everything in a single pipeline run.

### Pipeline Behavior

When you manually trigger a deployment to Alpha:
1. ✅ **Test job** runs automatically
2. ✅ **Build job** runs automatically (after test)
3. ✅ **Release job** runs (publishes to Alpha track)
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

#### Required Environments

The workflow uses three GitHub Environments for deployment control:

1. **PlayStore-Alpha** (No protection required)
   - Used for automatic Alpha track deployments
   - No approval needed (automatic from master branch)
   - Configure in: Repository Settings → Environments

2. **PlayStore-Beta** (Optional - For 3-tier flow)
   - Used when deploying via Beta track
   - Recommend: 1 reviewer required
   - Only needed if using 3-tier deployment flow
   - Configure in: Repository Settings → Environments

3. **PlayStore** (Production - **CRITICAL**)
   - Used for Production track deployments
   - **MUST** have "Required reviewers" configured
   - Recommend: At least 2 reviewers for redundancy
   - Configure in: Repository Settings → Environments

#### Setup Instructions

1. Go to repository **Settings → Environments**

2. **Create PlayStore-Alpha environment** (if doesn't exist):
   - Click **"New environment"**
   - Name it **"PlayStore-Alpha"** (exact name, case-sensitive)
   - Click **"Configure environment"**
   - No protection rules needed (leave empty)
   - Click **"Save protection rules"**

3. **Create PlayStore-Beta environment** (if doesn't exist - optional):
   - Click **"New environment"**
   - Name it **"PlayStore-Beta"** (exact name, case-sensitive)
   - Click **"Configure environment"**
   - **For solo developers**: Add yourself as the reviewer (allows self-approval)
   - **For teams**: Add 1 reviewer (e.g., Product Owner or Tech Lead)
   - Under **Environment protection rules**, check **"Required reviewers"**
   - **Add reviewers**: Select team member(s)
   - (Optional) Configure **Wait timer**: Add a delay (e.g., 5 minutes)
   - Click **"Save protection rules"**

4. **Create PlayStore environment** (if doesn't exist - **REQUIRED**):
   - Click **"New environment"**
   - Name it **"PlayStore"** (exact name, case-sensitive)
   - Click **"Configure environment"**
   - Under **Environment protection rules**, check **"Required reviewers"**
   - **Add reviewers**: 
     - **For solo developers**: Add yourself as the sole reviewer (enables self-approval with manual gate)
     - **For small teams**: Add 1-2 reviewers
     - **For larger teams**: Add 2+ reviewers for redundancy
   - (Optional) Configure **Wait timer**: Add a delay (e.g., 10 minutes) to give time for final checks
   - Click **"Save protection rules"**

#### Verification

After setup, verify environments exist:
- Navigate to: Repository → Settings → Environments
- You should see:
  - ✅ PlayStore-Alpha (no protection)
  - ✅ PlayStore-Beta (1 reviewer - optional, only for 3-tier flow)
  - ✅ PlayStore (at least 1 reviewer - required for manual approval gate)

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

- **PlayStore-Alpha**: Deploys to alpha track via manual workflow trigger (no protection rules required)
  - Used for: Internal testing deployments
  - Trigger: Manual workflow dispatch only
  - Approval: None (deploys after manual trigger)

- **PlayStore-Beta** (Optional): Used for beta track promotion in 3-tier flow
  - Used for: External beta testing (optional deployment path)
  - Trigger: Manual workflow dispatch with "Use Beta track" enabled
  - Approval: 1 reviewer (can be yourself for solo development)
  - Only needed if using 3-tier deployment flow

- **PlayStore** (Production): Used for production promotion (**IMPORTANT**)
  - Used for: Production releases to all users
  - Trigger: After Alpha (2-tier) or Beta (3-tier) deployment via manual workflow dispatch
  - Approval: **Configurable** based on team size
    - **Solo developers**: Add yourself as reviewer (enables self-approval with safety gate)
    - **Small teams**: 1-2 reviewers
    - **Larger teams**: 2+ reviewers for redundancy
  - **Without any reviewers configured**: Promote job will run automatically (provides no safety gate)

**Note for Solo Developers**: Adding yourself as the sole reviewer provides a final manual approval gate while allowing you to self-approve when ready. This is recommended over having no protection at all.

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

## References

- [Gradle Actions Documentation](https://github.com/gradle/actions)
- [r0adkll/upload-google-play Action](https://github.com/r0adkll/upload-google-play)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [GitHub Actions Best Practices](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
