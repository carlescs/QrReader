# Version Code Testing Guide

This document explains how the version code system works and how to test it for different branches.

## How Version Codes Work

### All Branches (Simplified Formula)
- **Formula**: `version_code = commit_count + BASE_OFFSET`
- **Base Offset**: 25 (added to maintain consistency with Google Play history)
- **Example**: 323 commits + 25 = version code 348
- **Historical Context**: The repository was restructured in the past, causing a discrepancy
  between git commit count and Google Play version codes. The base offset ensures
  monotonically increasing version codes that don't conflict with existing versions.

**Why Simplified?**
- Previous approach used branch-specific offsets (e.g., +891,400,000 for feature branches)
- This created extremely large version codes approaching Int.MAX_VALUE (2.1 billion)
- Simplified approach keeps version codes reasonable (25-500 range for typical projects)
- Teams coordinate sequential deployments or use different Google Play tracks

## Why This Matters

Google Play requires that each APK/AAB uploaded to a track has a **unique, monotonically increasing version code**.

### Handling Multiple Feature Branches

**Approach: Sequential Deployment or Different Tracks**

1. **Sequential (Recommended)**:
   ```
   Branch A: feature/scanner  → 260 commits → Version code: 285 (260 + 25)
   Deploy to Alpha, test, then merge to master
   
   Branch B: feature/tags     → 265 commits → Version code: 290 (265 + 25)  
   Deploy to Alpha, test, then merge to master
                                               ✓ No conflict (different commit counts)
   ```

2. **Parallel with Different Tracks**:
   ```
   Branch A: feature/scanner  → 260 commits → Internal Testing: 285
   Branch B: feature/tags     → 260 commits → Alpha Track: 285
                                               ✓ No conflict (different tracks)
   ```

3. **Conflict Scenario** (Avoid):
   ```
   Branch A: feature/scanner  → 260 commits → Alpha: 285
   Branch B: feature/tags     → 260 commits → Alpha: 285
                                               ❌ CONFLICT! Same track, same version
   ```

**Solution**: Coordinate deployments or use different tracks for parallel testing.

## Testing Version Code Generation

### Manual Testing

1. **Check current version code:**
   ```bash
   ./gradlew printVersionName
   ```
   This will show the version name and trigger version code calculation.

2. **Test on master branch:**
   ```bash
   git checkout master
   git rev-list --count HEAD  # Shows commit count
   # Version code should equal commit count + 25 (base offset)
   ```

3. **Test on feature branch:**
   ```bash
   git checkout feature/my-feature
   git rev-list --count HEAD  # Shows commit count
   # Version code should also be: commit_count + 25 (same formula)
   ```

### Verification Script

Run this script to calculate version codes for different branches:

```bash
#!/bin/bash
# Save as: scripts/verify-version-codes.sh

echo "Version Code Calculation"
echo "=================================="

# Get current commit count
COMMIT_COUNT=$(git rev-list --count HEAD)
BASE_OFFSET=25
echo "Current commit count: $COMMIT_COUNT"
echo "Base offset: $BASE_OFFSET"
echo ""

# Test various branch names
declare -a branches=(
    "master"
    "main"
    "feature/ai-descriptions"
    "feature/tag-suggestions"
    "feature/improved-scanner"
    "bugfix/crash-on-scan"
    "hotfix/security-patch"
)

echo "Branch Name                          Version Code"
echo "------------------------------------------------"

for branch in "${branches[@]}"; do
    # All branches use the same formula: commit_count + base_offset
    version_code=$((COMMIT_COUNT + BASE_OFFSET))
    printf "%-35s %10d\n" "$branch" "$version_code"
done

echo ""
echo "Note: All branches with the same commit count have the same version code"
echo "Deploy to Alpha track sequentially or use different tracks for parallel testing"
```

## Implementation Details

The version code logic is implemented in `buildSrc/src/main/kotlin/GitVersioning.kt`:

```kotlin
fun getVersionCode(project: Project): Int {
    val countProvider = gitCommand(project, listOf("git", "rev-list", "--count", "HEAD"))
    val count = countProvider.orNull?.toIntOrNull() ?: 1
    
    // Historical base offset to maintain consistency with Google Play
    val BASE_VERSION_CODE_OFFSET = 25
    val versionCode = count + BASE_VERSION_CODE_OFFSET
    
    // All branches use the same formula (no branch-specific offsets)
    // This keeps version codes reasonable (25-500 range for typical projects)
    // Teams coordinate deployments or use different Google Play tracks
    
    return versionCode
}
```

**Key Changes from Previous Implementation:**
- Removed branch-specific offset calculation
- Simplified formula applies to all branches
- Prevents extremely large version codes (previously 891M+)
- Version codes now stay in reasonable range (25-500 for typical projects)

## Best Practices

1. **Master Branch**: Always deploy production releases from master
   - Clean version codes with base offset (e.g., 348, 349, 350)
   - Sequential and predictable (commit_count + 25)

2. **Feature Branches**: Sequential deployment recommended
   - Test on Alpha track one at a time
   - Or use different tracks (Internal, Alpha, Beta) for parallel testing
   - Coordinate with team to avoid version conflicts

3. **Merging to Master**: After testing on Alpha
   - Merge feature branch to master
   - Next master build gets updated version code (commit_count + 25)
   - Production releases maintain sequential numbering

4. **Version Code Ranges**: Understand the simplicity
   - All branches: 25 to ~500+ (for typical projects with <500 commits)
   - Version codes stay reasonable and easy to understand
   - No risk of integer overflow or extremely large values

## Troubleshooting

### "Version code already exists" error on Google Play

**Cause**: Two builds with the same version code were uploaded to the same track.

**Solution**: 
- Verify you're on the correct branch: `git branch --show-current`
- Check commit count: `git rev-list --count HEAD`
- Ensure full Git history: `git fetch --unshallow` if using shallow clone
- **Deploy sequentially**: Wait for previous feature branch to be merged before deploying next
- **Use different tracks**: Deploy parallel branches to different tracks (Internal, Alpha, Beta)

### Version code seems wrong

**Cause**: Might be on detached HEAD or shallow clone.

**Solution**:
```bash
# Check if on detached HEAD
git branch --show-current

# Check commit count
git rev-list --count HEAD

# Convert shallow clone to full
git fetch --unshallow
```

### Feature branch has same version code as master

**This is expected behavior**: All branches now use the same formula (commit_count + 25).

**Solution**: Coordinate deployments
- Deploy feature branches sequentially to the same track
- Or use different Google Play tracks for parallel testing
- Version name includes commit hash for uniqueness (e.g., `5.1.8-dev.5+abc1234`)

## Continuous Integration

In CI/CD workflows (`.github/workflows/android-ci-cd.yml`):

- The workflow uses `fetch-depth: 0` to ensure full Git history
- This is required for accurate commit counting
- Version codes are automatically calculated during the build
- No manual version management needed

## References

- **VERSIONING.md**: High-level versioning guide
- **GitVersioning.kt**: Implementation source code
- **.github/CICD.md**: CI/CD workflow documentation
- **Google Play Console**: [Version Codes Documentation](https://developer.android.com/studio/publish/versioning)
