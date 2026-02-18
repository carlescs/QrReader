# Version Code Calculation: Approaches Comparison

## Overview
This document compares two approaches for calculating version codes to maintain consistency with Google Play Store.

---

## Approach 1: Static Base Offset (Current Implementation)

### Description
Add a fixed `BASE_VERSION_CODE_OFFSET = 25` to all version code calculations.

**Formula:**
```kotlin
version_code = commit_count + BASE_OFFSET
```

### Implementation
```kotlin
// buildSrc/src/main/kotlin/GitVersioning.kt
private const val BASE_VERSION_CODE_OFFSET = 25

fun getVersionCode(project: Project): Int {
    val count = getCommitCount()
    val baseVersionCode = count + BASE_VERSION_CODE_OFFSET
    // ... branch offset logic
    return baseVersionCode
}
```

### Pros ✓
1. **Simple**: Single constant, easy to understand
2. **Fast**: No network calls, instant calculation
3. **Reliable**: No external dependencies or API rate limits
4. **Offline-friendly**: Works without internet connection
5. **Predictable**: Always produces same result for same commit
6. **Build speed**: Zero impact on build time
7. **No credentials needed**: No Google API setup required
8. **No security concerns**: No API keys to manage
9. **Easy testing**: Can test locally without Play Store access
10. **Transparent**: Clear, auditable calculation

### Cons ✗
1. **Manual calculation**: Need to calculate offset once during setup
2. **Fixed offset**: Doesn't adapt if Play Store version changes independently
3. **Assumes linear history**: Works best with linear git history

### Use Cases
- ✓ Normal development workflow
- ✓ Most Android projects
- ✓ When build speed is important
- ✓ When offline builds are needed
- ✓ When simplicity is preferred

---

## Approach 2: Dynamic Google Play API Fetch

### Description
Query Google Play Developer API at build time to get the latest version code, then add 1.

**Formula:**
```kotlin
version_code = fetch_from_play_store() + 1
```

### Implementation Options

#### Option A: Using Gradle Play Publisher Plugin
```gradle
// build.gradle
plugins {
    id 'com.github.triplet.play' version '3.9.1'
}

play {
    serviceAccountCredentials = file("service-account.json")
}

// Custom task to fetch and increment version
task fetchPlayStoreVersion {
    doLast {
        // Use GPP's bootstrap or API to fetch current version
        def playVersion = getLatestPlayStoreVersion()
        android.defaultConfig.versionCode = playVersion + 1
    }
}
```

#### Option B: Direct API Call (Python/Bash Script)
```bash
#!/bin/bash
# Script to fetch latest version code from Play Store

PACKAGE_NAME="cat.company.qrreader"
CREDENTIALS="service-account.json"

# Use Google Play Developer API
VERSION_CODE=$(python3 << EOF
from googleapiclient.discovery import build
from google.oauth2 import service_account

credentials = service_account.Credentials.from_service_account_file(
    '$CREDENTIALS',
    scopes=['https://www.googleapis.com/auth/androidpublisher']
)

service = build('androidpublisher', 'v3', credentials=credentials)
edit_request = service.edits().insert(body={}, packageName='$PACKAGE_NAME')
edit_response = edit_request.execute()
edit_id = edit_response['id']

track = service.edits().tracks().get(
    editId=edit_id,
    track='production',
    packageName='$PACKAGE_NAME'
).execute()

# Get highest version code from production track
version_codes = []
for release in track.get('releases', []):
    version_codes.extend(release.get('versionCodes', []))

print(max(version_codes) if version_codes else 0)
EOF
)

echo $((VERSION_CODE + 1))
```

#### Option C: Using Gradle with API Client
```kotlin
// buildSrc/src/main/kotlin/PlayStoreVersioning.kt
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.androidpublisher.AndroidPublisher

object PlayStoreVersioning {
    fun getLatestVersionCode(
        packageName: String,
        credentialsFile: File
    ): Int {
        val credential = GoogleCredential
            .fromStream(credentialsFile.inputStream())
            .createScoped(setOf("https://www.googleapis.com/auth/androidpublisher"))
        
        val publisher = AndroidPublisher.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(packageName).build()
        
        val editRequest = publisher.edits().insert(packageName, null)
        val edit = editRequest.execute()
        
        val track = publisher.edits().tracks()
            .get(packageName, edit.id, "production")
            .execute()
        
        val versionCodes = track.releases
            ?.flatMap { it.versionCodes ?: emptyList() }
            ?.map { it.toLongOrNull() ?: 0L }
            ?: emptyList()
        
        return versionCodes.maxOrNull()?.toInt() ?: 0
    }
}

// In build.gradle.kts
android {
    defaultConfig {
        versionCode = try {
            PlayStoreVersioning.getLatestVersionCode(
                "cat.company.qrreader",
                file("../service-account.json")
            ) + 1
        } catch (e: Exception) {
            logger.warn("Failed to fetch Play Store version: ${e.message}")
            // Fallback to git-based calculation
            GitVersioning.getVersionCode(project)
        }
    }
}
```

### Pros ✓
1. **Always accurate**: Version code always based on actual Play Store state
2. **Self-healing**: Automatically adapts if Play Store version changes
3. **No manual calculation**: No need to determine offset
4. **Robust**: Handles history rewrites, merges, etc.
5. **Source of truth**: Play Store is the authoritative source

### Cons ✗
1. **Complex setup**: Requires Google Cloud project, service account, API credentials
2. **Network dependency**: Requires internet connection during build
3. **Build time**: Adds 2-5 seconds to every build for API call
4. **API rate limits**: Google Play API has quotas (10,000 requests/day)
5. **Credentials management**: Need to securely store and manage service account JSON
6. **Security risk**: Service account has broad permissions (can publish apps)
7. **Failure handling**: Need fallback strategy if API call fails
8. **Local builds**: Developers need service account access
9. **CI/CD complexity**: Must provision credentials in CI environment
10. **Testing difficulty**: Hard to test locally without Play Store access
11. **Cost**: May incur Google Cloud API costs
12. **Caching needed**: Without caching, slows every build

### Additional Challenges

#### Credential Security
```yaml
# GitHub Actions secrets needed
GOOGLE_PLAY_SERVICE_ACCOUNT_JSON: <base64-encoded JSON>

# Must ensure:
# - Service account has minimal permissions
# - Credentials are encrypted at rest
# - Access is logged and audited
# - Rotation policy in place
```

#### Offline Development
```kotlin
// Must handle offline scenarios
try {
    versionCode = fetchFromPlayStore() + 1
} catch (NetworkException e) {
    logger.warn("Offline mode - using git-based versioning")
    versionCode = gitBasedVersion()
}
```

#### Build Cache Issues
```kotlin
// Version code changes invalidate build cache
// Need to cache API response to avoid frequent calls
val cachedVersion = loadFromCache()
if (cacheIsValid(cachedVersion)) {
    return cachedVersion + 1
} else {
    return fetchFromPlayStore() + 1
}
```

### Use Cases
- ✓ When history is frequently rewritten
- ✓ When multiple independent repositories publish to same app
- ✓ When absolute accuracy is critical
- ✓ When team has Google Cloud expertise
- ? When build time is not critical (adds 2-5s)
- ✗ For most standard Android projects (overkill)

---

## Comparison Matrix

| Criterion | Static Offset | Dynamic API |
|-----------|--------------|-------------|
| **Setup Complexity** | ⭐⭐⭐⭐⭐ Simple | ⭐⭐ Complex |
| **Build Speed** | ⭐⭐⭐⭐⭐ Instant | ⭐⭐⭐ +2-5s |
| **Reliability** | ⭐⭐⭐⭐⭐ Always works | ⭐⭐⭐ Network dependent |
| **Security** | ⭐⭐⭐⭐⭐ No credentials | ⭐⭐ Service account risk |
| **Maintenance** | ⭐⭐⭐⭐⭐ Zero | ⭐⭐ API updates, credentials |
| **Accuracy** | ⭐⭐⭐⭐ Very good | ⭐⭐⭐⭐⭐ Perfect |
| **Offline Support** | ⭐⭐⭐⭐⭐ Full | ⭐ Fallback only |
| **Local Development** | ⭐⭐⭐⭐⭐ Easy | ⭐⭐ Needs credentials |
| **CI/CD Integration** | ⭐⭐⭐⭐⭐ Simple | ⭐⭐⭐ Needs secrets |
| **Cost** | ⭐⭐⭐⭐⭐ Free | ⭐⭐⭐ API costs |

**Legend:** ⭐⭐⭐⭐⭐ = Excellent, ⭐ = Poor

---

## Recommendation

### For This Project: **Static Offset (Current Implementation)**

**Reasons:**
1. **Simplicity**: The repository has linear history from a single source
2. **Speed**: No impact on build time (important for CI/CD)
3. **Security**: No credentials to manage or leak
4. **Reliability**: Always works, even offline
5. **Sufficient**: The offset correctly handles the historical discrepancy

### When to Use Dynamic API Approach

Consider the dynamic approach if:
- Multiple repositories publish to the same app
- Git history is frequently rewritten
- Team already has Google Cloud infrastructure
- Build time is not a concern
- Absolute accuracy is mission-critical

---

## Hybrid Approach (Optional)

For best of both worlds:

```kotlin
object VersionCodeStrategy {
    private const val BASE_OFFSET = 25
    private const val ENABLE_API_FETCH = false // Feature flag
    
    fun getVersionCode(project: Project): Int {
        if (ENABLE_API_FETCH && hasCredentials()) {
            try {
                return fetchFromPlayStore() + 1
            } catch (e: Exception) {
                project.logger.warn("API fetch failed, using git-based: ${e.message}")
            }
        }
        
        // Fallback to git-based with offset
        val count = getGitCommitCount(project)
        return count + BASE_OFFSET
    }
    
    private fun hasCredentials(): Boolean {
        return File("service-account.json").exists()
    }
}
```

**Benefits:**
- Default to fast, simple git-based approach
- Optional API fetch for when needed
- Graceful fallback if API unavailable

---

## Migration Path (If Switching to API)

### Phase 1: Add API Support (Optional)
1. Create Google Cloud service account
2. Enable Google Play Developer API
3. Implement API fetch with fallback
4. Test in CI/CD

### Phase 2: Enable Feature Flag
```kotlin
ENABLE_API_FETCH = true // in production builds only
```

### Phase 3: Monitor
- Track API success rate
- Monitor build time impact
- Watch for API quota issues

### Phase 4: Remove Static Offset (Optional)
- If API proves reliable for 3+ months
- Remove BASE_OFFSET constant
- Update documentation

---

## Decision Log

### 2026-02-18: Chose Static Offset Approach
- **Decision**: Implement static `BASE_VERSION_CODE_OFFSET = 25`
- **Reason**: Simplicity, speed, reliability outweigh the marginal accuracy benefit
- **Alternative considered**: Dynamic Google Play API fetch
- **Trade-off**: Manual offset calculation vs. API complexity
- **Result**: Successfully implemented, version codes now consistent

---

## References

### Static Offset Approach
- Implementation: `buildSrc/src/main/kotlin/GitVersioning.kt`
- Documentation: `VERSIONING.md`, `VERSION_CODE_TESTING.md`

### Dynamic API Approach
- [Google Play Developer API](https://developers.google.com/android-publisher/)
- [Gradle Play Publisher Plugin](https://github.com/Triple-T/gradle-play-publisher)
- [API Authentication](https://developers.google.com/android-publisher/getting_started)
- [Edits.tracks.get endpoint](https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks/get)

---

**Last Updated**: 2026-02-18  
**Status**: ✓ Static Offset Implemented  
**Dynamic API**: Documented for future consideration
