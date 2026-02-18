# Google Play Version Code Fetching

## Overview

As of 2026-02-18, the QR Reader project uses **Google Play API-based version code fetching** as the primary versioning strategy. This approach queries the Google Play Store to get the latest published version code and increments it by 1.

## How It Works

### Primary Method: Google Play API Fetch

```kotlin
versionCode = fetch_from_google_play() + 1
```

The build system:
1. Executes `scripts/fetch_play_version.py` during build
2. Queries Google Play Developer API for the latest version code
3. Returns the next version code (latest + 1)

### Fallback Method: Git-based Versioning

If the Google Play API fetch fails (no internet, missing credentials, API error), the system automatically falls back to git-based versioning:

```kotlin
versionCode = commit_count + BASE_OFFSET + optional_branch_offset
```

## Requirements

### For Google Play API Fetch to Work

1. **Python 3**: Must be available on the build machine
2. **Google API Packages**: Required Python packages
   ```bash
   pip install google-api-python-client google-auth
   ```
3. **Service Account Credentials**: File `service-account.json` in project root
   - Must have "Release Manager" role in Google Play Console
   - Google Play Developer API must be enabled

### Service Account Setup

1. **Create Service Account** (Google Cloud Console):
   - Go to: https://console.cloud.google.com/
   - Navigate to: IAM & Admin → Service Accounts
   - Create new service account with descriptive name
   - Download JSON key file

2. **Enable Google Play Developer API**:
   - In Google Cloud Console
   - Navigate to: APIs & Services → Library
   - Search for "Google Play Developer API"
   - Click "Enable"

3. **Grant Access in Play Console**:
   - Go to: https://play.google.com/console/
   - Navigate to: Setup → API access
   - Link your Google Cloud project
   - Grant service account "Release Manager" role

4. **Configure Credentials**:
   - Save JSON key as `service-account.json` in project root
   - Add to `.gitignore` (already configured)
   - For CI/CD, store as GitHub Secret: `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

## CI/CD Configuration

### GitHub Actions

The workflow automatically handles credentials:

```yaml
- name: Write service account credentials
  run: |
    echo "${{ secrets.GOOGLE_PLAY_SERVICE_ACCOUNT_JSON }}" > service-account.json

- name: Build with Gradle
  run: ./gradlew assembleRelease
  # Version code automatically fetched from Google Play
```

### Required GitHub Secrets

- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`: Base64-encoded or plain JSON of service account key

## Local Development

### With Credentials (Recommended for Release Builds)

1. Obtain `service-account.json` from team lead
2. Place in project root
3. Build normally: `./gradlew assembleRelease`

### Without Credentials (Development Builds)

The system automatically falls back to git-based versioning:

```bash
./gradlew assembleDebug
# Uses: commit_count + 25 (+ branch offset if on feature branch)
```

## Version Code Behavior

### Master Branch with Google Play Fetch

```
Latest Play Store version: 350
Next version code: 351
```

### Feature Branch with Google Play Fetch

```
Latest Play Store version: 350
Next version code: 351
```

Note: No branch offset is applied when using Google Play fetch, as the Play Store version is already authoritative.

### Fallback Behavior (No Credentials)

**Master branch:**
```
Commits: 326
Version code: 351 (326 + 25)
```

**Feature branch:**
```
Commits: 326
Branch hash: 456
Version code: 45600351 (351 + 456 * 100000)
```

## Advantages Over Git-Based Versioning

1. **Always Accurate**: Version code reflects actual Play Store state
2. **No Branch Inflation**: Feature branches don't get huge version codes
3. **Handles History Changes**: Robust against git history rewrites or merges
4. **Unified Versioning**: Multiple repositories can publish to same app
5. **Self-Healing**: Automatically adapts if Play Store version changes

## Troubleshooting

### Build Fails with "Service account not found"

**Solution**: Add `service-account.json` to project root, or rely on fallback versioning.

### "Failed to fetch version code from Google Play"

**Possible causes**:
1. No internet connection (fallback will be used automatically)
2. Service account lacks permissions
3. Google Play API not enabled
4. Invalid credentials file

**Check logs**:
```bash
./gradlew assembleRelease --info | grep "version code"
```

### Version Code Not Incrementing

**Cause**: Using fallback versioning instead of Google Play fetch.

**Solution**: 
1. Verify `service-account.json` exists
2. Check credentials are valid
3. Ensure Python 3 is installed
4. Install required packages: `pip install google-api-python-client google-auth`

### Python Script Fails

**Debug the script directly**:
```bash
python3 scripts/fetch_play_version.py
```

**Common issues**:
- Missing Python packages (install with pip)
- Invalid credentials (re-download from Google Cloud)
- API not enabled (enable in Google Cloud Console)
- Service account lacks permissions (grant in Play Console)

## Migration Notes

### Previous Versioning Strategy

Before this change, the project used git-based versioning with:
- Base offset of 25 for historical consistency
- Large branch offsets for feature branches (e.g., 891400363)

### Why the Change?

The large version codes generated by feature branch offsets were:
- Confusing (891400363 vs expected ~350)
- Unnecessary (Play Store is the source of truth)
- Problematic for version comparisons

### Backward Compatibility

The fallback mechanism ensures builds work even without Google Play credentials, maintaining compatibility with local development workflows.

## Best Practices

1. **Always use Google Play fetch for release builds**
   - Ensures accurate version codes
   - Prevents upload conflicts

2. **Keep credentials secure**
   - Never commit `service-account.json`
   - Rotate credentials periodically
   - Use minimal required permissions

3. **Test fallback behavior**
   - Occasionally build without credentials
   - Verify git-based versioning still works

4. **Monitor version codes**
   - Check GitHub Actions logs for version info
   - Verify expected version codes before major releases

## References

- **Implementation**: `buildSrc/src/main/kotlin/GitVersioning.kt`
- **Python Script**: `scripts/fetch_play_version.py`
- **Google Play API**: https://developers.google.com/android-publisher/
- **Legacy Documentation**: `docs/VERSION_CODE_APPROACHES.md`

---

**Last Updated**: 2026-02-18  
**Status**: ✅ Implemented and Active  
**Fallback**: Git-based versioning with BASE_OFFSET=25
