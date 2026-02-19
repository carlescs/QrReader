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

## Solution Implemented: Update Static Offset

### Approach: Recalculate BASE_VERSION_CODE_OFFSET

**New Calculation**:
```
Current Play Store version: 367
Current git commit count: 2
New offset: 367 - 2 = 365
```

**Updated Formula**:
```kotlin
private const val BASE_VERSION_CODE_OFFSET = 365

fun getVersionCode(project: Project): Int {
    val count = getCommitCount() // 2
    return count + BASE_VERSION_CODE_OFFSET // 2 + 365 = 367
}
```

### Changes Made

1. **GitVersioning.kt**:
   - Updated `BASE_VERSION_CODE_OFFSET` from 25 → **365**
   - Added detailed comment explaining the calculation
   - Documented previous offset history

2. **Validation Script** (`scripts/validate_version_offset.sh`):
   - Validates current offset against Google Play version
   - Calculates correct offset if mismatch detected
   - Provides clear guidance for fixing issues

3. **Documentation Updates**:
   - `VERSIONING.md`: Updated offset value and examples
   - `VERSION_CODE_FIX_SUMMARY.md`: Added new fix section
   - `docs/VERSION_CODE_TESTING.md`: Updated all references
   - `scripts/README.md`: Documented validation tool

### Verification

```bash
$ ./scripts/validate_version_offset.sh 367
=== Version Code Offset Validator ===

Current git commit count: 2
Current BASE_VERSION_CODE_OFFSET: 365
Calculated version code: 367 (commit count + offset)

Expected Play Store version: 367

✓ VALID: Calculated version matches expected Play Store version

Next commit will produce version: 368
```

✅ **Confirmed working**: Next build will produce version 367, matching Play Store.

---

## Alternative Approaches Considered

### Option 1: Static Offset (IMPLEMENTED) ✅

**Pros**:
- ✅ Simple and fast (zero build time impact)
- ✅ No external dependencies or credentials
- ✅ Works offline
- ✅ Easy to understand and debug
- ✅ No security concerns

**Cons**:
- ⚠️ Requires manual recalculation if repository history changes again
- ⚠️ Not self-healing if Play Store version changes independently

**Best for**: Standard development workflows with stable git history.

---

### Option 2: Dynamic Google Play API Fetch

**Implementation**: Use existing `scripts/fetch_play_version.py` to query Google Play Developer API at build time.

**How it works**:
```kotlin
fun getVersionCode(project: Project): Int {
    try {
        // Fetch latest version from Play Store API
        val playStoreVersion = fetchFromGooglePlay()
        return playStoreVersion + 1
    } catch (e: Exception) {
        // Fallback to git-based calculation
        return getGitBasedVersionCode(project)
    }
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

### Current Decision: Static Offset ✅

**Reasons**:
1. **Simplicity**: Easy to understand, maintain, and debug
2. **Performance**: Zero build time impact
3. **Reliability**: Always works, even offline
4. **Security**: No credentials to manage
5. **Sufficient**: Handles the current situation effectively

### When to Reconsider Dynamic API

Consider switching to dynamic API approach if:
- Repository history is frequently rewritten (e.g., rebasing, squashing)
- Multiple independent repositories deploy to the same Google Play app
- Absolute accuracy is mission-critical
- Team has Google Cloud expertise and infrastructure
- Build time impact is acceptable (+2-5 seconds per build)

### Future-Proofing

The validation script (`validate_version_offset.sh`) provides early warning if offset becomes outdated again:

```bash
# Run periodically or in CI/CD to detect issues
./scripts/validate_version_offset.sh 367

# Will show error if offset is wrong and provide fix:
# new_offset = play_store_version - commit_count
```

**Recommendation**: Add this check to CI/CD as a validation step (non-blocking warning).

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
