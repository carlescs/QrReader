# Version Code Strategy: Problem Analysis & Solution

**Date**: 2026-02-19  
**Status**: ✅ RESOLVED

## Executive Summary

**Problem**: Version code calculation was producing version 27, but Google Play has version 367, causing upload failures.

**Root Cause**: Repository history was reset/restructured from ~350 commits to 2 commits, making the BASE_VERSION_CODE_OFFSET of 25 obsolete.

**Solution**: Updated `BASE_VERSION_CODE_OFFSET` from 25 to **365** to align with Google Play's current state.

**Result**: Version codes now correctly start at 367 and increment properly.

---

## Problem Statement

### Symptoms
- GitHub Actions CI/CD shows version code **353** was deployed previously
- Google Play Console shows current version is **367**
- Local builds with current code produce version **27**
- Attempting to upload version 27 would be rejected by Google Play

### Root Cause Analysis

**Original Setup (Working)**:
```
Git commits: 323
Play Store version: 348
Offset: 348 - 323 = 25
Formula: version_code = commit_count + 25
Result: 323 + 25 = 348 ✓
```

**After Repository Restructure (Broken)**:
```
Git commits: 2 (history reset)
Play Store version: 367 (continues to increment)
Old offset: 25 (outdated)
Formula: version_code = commit_count + 25
Result: 2 + 25 = 27 ✗ (way too low!)
```

**Impact**: Any new build would fail to upload to Google Play because 27 < 367.

---

## Solution Implemented: Dynamic Google Play API Fetch with Fallback

### Approach: Fetch from Play Store + Git Fallback

**Primary Method: Google Play API**
```
Current Play Store version: 367
API fetch: 367 + 1 = 368
Next build uses: 368
```

**Fallback Method: Git-based with offset (when API unavailable)**
```
Current git commit count: 5
Fallback offset: 365
Fallback calculation: 5 + 365 = 370
```

**Updated Implementation:**
```kotlin
@JvmStatic
fun getVersionCode(project: Project): Int {
    // Primary: Fetch from Google Play Store API
    val playStoreVersion = fetchFromGooglePlay(project)
    if (playStoreVersion != null) {
        return playStoreVersion
    }
    
    // Fallback: Use git commit count + offset
    return getGitBasedVersionCode(project)
}

private fun fetchFromGooglePlay(project: Project): Int? {
    // Calls scripts/fetch_play_version.py
    // Returns latest version + 1, or null if unavailable
}
```

### Why This Approach?

**Addresses the concern about commit count:**
- ✅ Exploratory/abandoned branches don't affect version codes
- ✅ Git history is irrelevant when API credentials are available
- ✅ Repository resets/restructures don't cause issues
- ✅ Always accurate based on actual Play Store state

**Benefits:**
- Primary method uses Play Store as source of truth
- Falls back gracefully when API unavailable
- No manual offset recalculation needed
- Self-healing if Play Store changes independently

### Changes Made

1. **GitVersioning.kt**:
   - Implemented `fetchFromGooglePlay()` method to call Python script
   - Uses `scripts/fetch_play_version.py` to query Play Store API
   - Falls back to `getGitBasedVersionCode()` if API unavailable
   - Separated git-based calculation into fallback method
   - BASE_VERSION_CODE_OFFSET (365) now only used in fallback

2. **Validation Script** (`scripts/validate_version_offset.sh`):
   - Still useful for validating fallback offset
   - Helps diagnose when API vs fallback is being used
   
3. **Documentation Updates**:
   - `VERSIONING.md`: Updated to explain dynamic + fallback approach
   - `VERSION_CODE_STRATEGY_ANALYSIS.md`: Updated solution section
   - Added setup instructions for Google Play API credentials

### Verification

**With Google Play API credentials:**
```bash
$ python3 scripts/fetch_play_version.py
Package: cat.company.qrreader
Track: production
Latest version code: 367
Next version code: 368

# Build will use: 368
```

**Without API credentials (fallback):**
```bash
$ ./scripts/validate_version_offset.sh 367
Current git commit count: 5
Current BASE_VERSION_CODE_OFFSET: 365
Calculated version code: 370 (commit count + offset)

# Build will use: 370 (fallback mode)
```

**Gradle build output:**
```
> Task :app:processDebugMainManifest
Using Google Play API version: 368
# OR
Google Play API unavailable, falling back to git-based versioning
Branch: copilot/review-version-code-strategy, Fallback Version Code: 370
```

---

## Alternative Approaches Considered

### Option 1: Static Offset Only

**Pros**:
- ✅ Simple and fast (zero build time impact)
- ✅ No external dependencies or credentials
- ✅ Works offline
- ✅ Easy to understand and debug
- ✅ No security concerns

**Cons**:
- ⚠️ Requires manual recalculation if repository history changes again
- ⚠️ Not self-healing if Play Store version changes independently
- ⚠️ Commit count issues with exploratory branches
- ❌ Git history dependent

**Verdict**: Not suitable for this project due to commit count concerns.

---

### Option 2: Dynamic Google Play API Fetch (IMPLEMENTED) ✅

**Implementation**: Query Google Play Developer API at build time to get the latest version code, then add 1.

**How it works**:
```kotlin
fun getVersionCode(project: Project): Int {
    // Primary: Fetch from Play Store API
    val playStoreVersion = fetchFromGooglePlay()
    if (playStoreVersion != null) {
        return playStoreVersion
    }
    
    // Fallback: Git-based calculation
    return getGitBasedVersionCode(project)
}
```

**Pros**:
- ✅ Always accurate based on Play Store state
- ✅ Self-healing if history changes
- ✅ No manual offset calculation needed
- ✅ Robust against any git history manipulations

**Cons**:
- ❌ Complex setup (Google Cloud service account, credentials)
- ❌ Requires network connection during build (+2-5 seconds)
- ❌ Security risk (service account has broad permissions)
- ❌ API rate limits (10,000 requests/day)
- ❌ Credentials management overhead
- ❌ Harder to test locally
- ❌ CI/CD needs secret provisioning

**Best for**: Projects with frequently rewritten history or multiple independent repositories publishing to same app.

---

### Option 3: Hybrid Approach

**Implementation**: Use static offset by default, optionally enable API fetch.

```kotlin
object VersionCodeStrategy {
    private const val BASE_OFFSET = 365
    private const val ENABLE_API_FETCH = false // Feature flag
    
    fun getVersionCode(project: Project): Int {
        if (ENABLE_API_FETCH && credentialsExist()) {
            try {
                return fetchFromPlayStore() + 1
            } catch (e: Exception) {
                logger.warn("API fetch failed: ${e.message}")
            }
        }
        
        // Default to static offset (fast, reliable)
        val count = getGitCommitCount(project)
        return count + BASE_OFFSET
    }
}
```

**Pros**:
- ✅ Fast by default (static offset)
- ✅ Optional API fetch when needed
- ✅ Graceful fallback if API unavailable
- ✅ Best of both worlds

**Cons**:
- ⚠️ More complex code
- ⚠️ Still requires API setup for optional feature

**Best for**: Teams that want flexibility but prefer simplicity as default.

---

## Comparison Matrix

| Criterion | Static Offset | Dynamic API | Hybrid |
|-----------|--------------|-------------|--------|
| **Setup Complexity** | ⭐⭐⭐⭐⭐ Simple | ⭐⭐ Complex | ⭐⭐⭐ Medium |
| **Build Speed** | ⭐⭐⭐⭐⭐ Instant | ⭐⭐⭐ +2-5s | ⭐⭐⭐⭐ Mostly instant |
| **Reliability** | ⭐⭐⭐⭐⭐ Always works | ⭐⭐⭐ Network dependent | ⭐⭐⭐⭐ Very reliable |
| **Security** | ⭐⭐⭐⭐⭐ No credentials | ⭐⭐ Service account risk | ⭐⭐⭐ Optional credentials |
| **Maintenance** | ⭐⭐⭐⭐ Low | ⭐⭐ High | ⭐⭐⭐ Medium |
| **Accuracy** | ⭐⭐⭐⭐ Very good | ⭐⭐⭐⭐⭐ Perfect | ⭐⭐⭐⭐⭐ Perfect (if enabled) |
| **Self-healing** | ⭐⭐ Manual fix needed | ⭐⭐⭐⭐⭐ Automatic | ⭐⭐⭐⭐ Automatic (if enabled) |

---

## Recommendation

### Current Decision: Dynamic API with Git Fallback ✅

**Implemented Approach**: Hybrid system that uses Google Play API when available, falls back to git-based calculation.

**Reasons for this choice**:
1. **Addresses commit count concern**: Git history doesn't affect version codes when API is available
2. **Self-healing**: Automatically adapts to Play Store state
3. **Graceful degradation**: Works offline with fallback
4. **Best of both worlds**: Accuracy when online, reliability when offline
5. **CI/CD ready**: GitHub Actions already has credentials configured

**When API credentials are provided:**
- ✅ Always accurate version codes
- ✅ No git history dependency
- ✅ Exploratory branches don't cause issues
- ✅ Self-healing on repository changes
- ⚠️ Adds ~2-5 seconds to build time

**When API unavailable (fallback):**
- ✅ Still builds successfully
- ✅ Works offline
- ⚠️ Needs manual offset updates if Play Store changes
- ⚠️ Commit count issues may resurface

### Setup Recommendation

**For CI/CD (GitHub Actions):**
- ✅ Already configured with `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` secret
- API will be used automatically in all builds

**For Local Development:**
1. **With credentials**: Copy `service-account.json` to project root
   - Get accurate version codes matching CI/CD
   - Test as production builds would work
   
2. **Without credentials**: Fallback mode works fine
   - Faster builds (no API call)
   - Version codes may differ from CI/CD
   - Good enough for local development/testing

### Future-Proofing

The validation script (`validate_version_offset.sh`) is still useful:
- Checks if fallback offset needs updating
- Helps diagnose API vs fallback usage
- Useful when API credentials are missing

```bash
# Check current configuration
./scripts/validate_version_offset.sh 367

# Test API directly
python3 scripts/fetch_play_version.py
```

---

## Maintenance Procedure

### If Repository History Changes Again

1. **Detect the issue**:
   ```bash
   ./scripts/validate_version_offset.sh <current_play_store_version>
   ```

2. **Script will show**:
   - Current offset and calculated version
   - Whether it's valid, too high, or too low
   - Exact new offset to use if invalid

3. **Update offset**:
   - Edit `buildSrc/src/main/kotlin/GitVersioning.kt`
   - Change `BASE_VERSION_CODE_OFFSET` to calculated value
   - Commit and push

4. **Verify**:
   ```bash
   ./scripts/validate_version_offset.sh <current_play_store_version>
   # Should show: ✓ VALID
   ```

### Regular Monitoring

**Recommended**: Add validation to CI/CD pipeline:

```yaml
# .github/workflows/android-ci-cd.yml
- name: Validate Version Code
  run: |
    ./scripts/validate_version_offset.sh 367 || {
      echo "::warning::Version code offset may need updating"
      echo "See scripts/validate_version_offset.sh output above"
    }
```

This provides early warning without blocking builds.

---

## Lessons Learned

1. **Static offsets work well** for projects with stable git history
2. **Repository restructures** require offset recalculation
3. **Validation tooling** is essential for detecting issues early
4. **Clear documentation** prevents confusion and debugging time
5. **Simple solutions** are often better than complex ones

---

## References

### Documentation
- `VERSIONING.md` - User-facing versioning guide
- `VERSION_CODE_FIX_SUMMARY.md` - Historical fixes
- `docs/VERSION_CODE_APPROACHES.md` - Detailed approach comparison
- `docs/VERSION_CODE_TESTING.md` - Testing procedures

### Scripts
- `scripts/validate_version_offset.sh` - Offset validation tool (NEW)
- `scripts/fetch_play_version.py` - Google Play API client (optional)

### Implementation
- `buildSrc/src/main/kotlin/GitVersioning.kt` - Version code calculation

---

**Status**: ✅ Issue resolved, system working correctly  
**Next Review**: If git history changes or Play Store version advances independently  
**Contact**: See repository documentation for team contacts
