# Version Code Fix Summary

## Latest Update (2026-02-19): BASE_VERSION_CODE_OFFSET Adjustment

### Problem Discovered
After repository restructure/reset, the BASE_VERSION_CODE_OFFSET of 25 became outdated:
- **Current repository state**: Only 2 commits (likely from major restructure or shallow clone conversion)
- **Old calculation**: `version_code = 2 + 25 = 27`
- **Reality**: 
  - GitHub deployed version: 353
  - Google Play current version: 367
- **Issue**: New builds would produce version 27, far below Google Play's 367, causing upload failures

### Root Cause
The offset of 25 was calculated based on:
- Play Store version: 348
- Git commit count: 323
- Formula: 348 - 323 = 25

However, the repository history changed dramatically (reset to 2 commits), making the offset invalid.

### Solution Implemented
Updated `BASE_VERSION_CODE_OFFSET` from **25** to **365**:

```kotlin
// OLD (outdated after repository reset)
private const val BASE_VERSION_CODE_OFFSET = 25

// NEW (correct for current state)
private const val BASE_VERSION_CODE_OFFSET = 365
```

**Calculation:**
- Latest Google Play version: **367**
- Current git commit count: **2**
- New offset: **367 - 2 = 365**
- Verification: 2 + 365 = 367 ✓

### Impact
- **Before fix**: version_code = 2 + 25 = **27** (would be rejected by Play Store)
- **After fix**: version_code = 2 + 365 = **367** (matches Play Store, ready for next version)
- **Next commit**: version_code = 3 + 365 = **368** (properly increments)

### Why the Offset is Necessary
The offset ensures version codes:
1. **Never conflict** with existing Play Store versions
2. **Monotonically increase** (Google Play requirement)
3. **Remain simple** (no complex API calls or credentials needed)
4. **Work offline** (no network dependency)

### Maintenance Note
If the Google Play version ever changes independently (e.g., manual upload from different source), recalculate:
```bash
new_offset = latest_play_store_version - $(git rev-list --count HEAD)
```

Then update `BASE_VERSION_CODE_OFFSET` in `buildSrc/src/main/kotlin/GitVersioning.kt`.

---

## Previous Fix (2026-02): Branch Offset Removal

## Problem

The version code on the `copilot/explore-barcode-description` branch was **891,400,363** - an extremely large value that:
- Approaches Int.MAX_VALUE (2,147,483,647) with only 2.4x headroom
- Makes debugging and understanding version codes difficult
- Provides no practical benefit for the complexity it introduces

### Root Cause

The version code calculation included a branch-specific offset:

```kotlin
// OLD CODE (problematic)
if (branch != "master" && branch != "main" && branch != "HEAD") {
    val branchHash = (kotlin.math.abs(branch.hashCode().toLong()) % 10000).toInt()
    return baseVersionCode + (branchHash * 100000)
}
```

**Breakdown of version code 891,400,363:**
- Branch name: `copilot/explore-barcode-description`
- Java hashCode: 996,068,914
- Hash modulo: 996,068,914 % 10,000 = 8,914
- Branch offset: 8,914 × 100,000 = **891,400,000**
- Base version: 363 (338 commits + 25 base offset)
- Total: 891,400,000 + 363 = **891,400,363**

## Solution

**Simplified the version code calculation** to use the same formula for all branches:

```kotlin
// NEW CODE (simplified)
val versionCode = count + BASE_VERSION_CODE_OFFSET
// All branches: commit_count + 25
```

### Changes Made

1. **GitVersioning.kt** - Removed branch offset logic (lines 74-91)
   - Simplified to: `version_code = commit_count + 25`
   - Added documentation explaining the change
   - Added logging for transparency

2. **VERSIONING.md** - Updated documentation
   - Removed references to branch-specific offsets
   - Added guidance on sequential deployment
   - Explained alternative approaches for parallel testing

3. **docs/VERSION_CODE_TESTING.md** - Updated testing guide
   - Updated implementation details
   - Revised troubleshooting section
   - Simplified verification scripts

## Impact

### Before Fix
- Master branch: ~350 (commit_count + 25)
- Feature branch: ~891,400,363 (huge offset)
- **Problem**: Approaching integer overflow, difficult to debug

### After Fix
- Master branch: ~350 (commit_count + 25)
- Feature branch: ~350 (commit_count + 25)
- **Result**: Simple, predictable, reasonable values

### Version Code Examples (After Fix)

For a project with 350 commits:
- All branches: `350 + 25 = 375`
- Version name differentiates builds: `5.2.0-dev.20+abc1234`

## Handling Parallel Feature Branch Deployments

The branch offset was designed to allow multiple feature branches to be deployed to Google Play Alpha track simultaneously. With the simplified approach, teams have these options:

### Option 1: Sequential Deployment (Recommended)
Deploy and test one feature branch at a time:
```
1. Deploy Branch A to Alpha → Test → Merge to master
2. Deploy Branch B to Alpha → Test → Merge to master
```

### Option 2: Use Different Tracks
Google Play provides multiple testing tracks:
```
Branch A → Internal Testing track (version 375)
Branch B → Alpha track (version 375)
Branch C → Beta track (version 375)
```
Each track accepts one version, no conflicts.

### Option 3: Coordinate Timing
Short-lived feature branches rarely conflict:
```
Branch A: 350 commits → Deploy to Alpha (version 375)
Branch B: 355 commits (by the time it's ready) → Deploy to Alpha (version 380)
```
Different commit counts = different version codes.

## Rationale

### Why Remove Branch Offsets?

1. **Extremely Large Values**
   - 891M+ version codes are unnecessarily large
   - Create confusion and debugging challenges
   - Risk integer overflow with longer branch names

2. **Rare Use Case**
   - Most projects don't need parallel Alpha deployments
   - Feature branches are typically tested sequentially
   - Teams can easily coordinate when needed

3. **Better Alternatives Available**
   - Use different Google Play tracks (Internal, Alpha, Beta)
   - Deploy sequentially (simplest approach)
   - Coordinate timing for short-lived branches

4. **Simplicity Wins**
   - Easier to understand and debug
   - Predictable version codes (300-400 range)
   - Less cognitive overhead for developers

## Testing

To verify the fix works correctly:

```bash
# Calculate version code
cd /home/runner/work/QrReader/QrReader
COMMIT_COUNT=$(git rev-list --count HEAD)
BASE_OFFSET=25
VERSION_CODE=$((COMMIT_COUNT + BASE_OFFSET))

echo "Commit count: $COMMIT_COUNT"
echo "Version code: $VERSION_CODE"
```

Expected result: Version code in the range 25-500 (for typical projects).

## Migration Notes

### For Existing Feature Branches

If you have a feature branch already deployed to Alpha with a high version code:
1. The next deployment will use the simplified version code (much lower)
2. Google Play will reject uploads with lower version codes
3. **Solution**: Merge the feature branch to master first, or manually increment the master version

### For Master Branch

No changes needed - master branch already used the simple formula.

## Conclusion

This fix:
- ✅ Eliminates extremely large version codes (891M → ~350)
- ✅ Simplifies code and reduces complexity
- ✅ Makes version codes easier to understand and debug
- ✅ Prevents risk of integer overflow
- ✅ Maintains compatibility with Google Play
- ⚠️ Teams must coordinate parallel Alpha deployments (rare scenario)

The trade-off is worth it: simplicity and maintainability over a rarely-used feature that created more problems than it solved.
