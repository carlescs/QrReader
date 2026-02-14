# CI/CD Configuration

This document describes the CI/CD pipeline configuration for the QR Reader Android application.

## Overview

The project uses GitHub Actions for continuous integration and deployment. The main workflow is defined in `.github/workflows/android-ci-cd.yml`.

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
- Signs the bundle (on master branch only, requires secrets)
- Uploads artifacts

### 3. Release Job (Master Branch Only)
Automatically publishes to Google Play Alpha track:
- Downloads signed bundle
- Validates bundle and secrets
- Uploads to Google Play Console Alpha track

### 4. Promote Job (Master Branch Only)
Promotes from Alpha to Production track:
- Requires manual approval via environment protection
- Uses Google Play API to promote the release

## Required Secrets

The following secrets must be configured in GitHub repository settings:

### For Bundle Signing (Master Branch Only)

1. **KEYSTORE_BASE64**
   - Base64-encoded Android keystore file
   - Generate: `base64 -w 0 your-keystore.jks`
   - Used to sign the release bundle

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

5. **Initial Manual Upload**
   - Upload your app manually at least once
   - This creates the necessary API connections

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
- Check that signing secrets are configured (for master branch)
- Verify the artifact was uploaded in the build job

**Secret not found:**
- The release and promote jobs will fail with clear error messages if secrets are missing
- Add the required secrets following the instructions above

## Environment Protection

The workflow uses GitHub Environments for deployment gates:

- **PlayStore-Alpha**: Automatically deploys to alpha track
- **PlayStore**: Requires manual approval before promoting to production

Configure these in repository Settings → Environments.

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

## References

- [r0adkll/upload-google-play Action](https://github.com/r0adkll/upload-google-play)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
