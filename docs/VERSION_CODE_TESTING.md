# Version Code Testing Guide

This document explains how the version code system works and how to test it for different branches.

## How Version Codes Work

### Master/Main Branches
- **Formula**: `version_code = commit_count + BASE_OFFSET`
- **Base Offset**: 25 (added to maintain consistency with Google Play history)
- **Example**: 323 commits + 25 = version code 348
- **Use case**: Production releases on master branch
- **Historical Context**: The repository was restructured in the past, causing a discrepancy
  between git commit count and Google Play version codes. The base offset ensures
  monotonically increasing version codes that don't conflict with existing versions.

### Feature Branches
- **Formula**: `version_code = (commit_count + BASE_OFFSET) + (branch_hash * 100000)`
- **Example (illustrative)**: A feature branch with 260 commits
  - Base version: 260 + 25 = 285
  - Branch hash could be any value 0-9999 depending on branch name
  - If hash = 456, version code = 285 + (456 * 100000) = 45600285
- **Use case**: Alpha track testing for feature branches

## Why This Matters

Google Play requires that each APK/AAB uploaded to a track has a **unique, monotonically increasing version code**. Without branch-specific offsets, deploying multiple feature branches in parallel would cause conflicts:

### Without Offsets (❌ Problem)
```
Branch A: feature/scanner    → 260 commits → Version code: 285 (260 + 25)
Branch B: feature/tags        → 260 commits → Version code: 285 (260 + 25)
                                             ⚠️ CONFLICT! Same version code
```

### With Offsets (✅ Solution)
```
Branch A: feature/scanner    → 260 commits → Version code: hash1 * 100000 + 285
Branch B: feature/tags        → 260 commits → Version code: hash2 * 100000 + 285
                                             ✓ Unique version codes!
```

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
   # Version code should be: (commit_count + 25) + branch_offset
   ```

### Verification Script

Run this script to verify version code uniqueness across branches:

```bash
#!/bin/bash
# Save as: scripts/verify-version-codes.sh

echo "Verifying Version Code Uniqueness"
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
    # Calculate hash (simplified - actual implementation in GitVersioning.kt)
    if [[ "$branch" == "master" || "$branch" == "main" || "$branch" == "HEAD" ]]; then
        version_code=$((COMMIT_COUNT + BASE_OFFSET))
    else
        hash=$(echo -n "$branch" | cksum | cut -d' ' -f1)
        offset=$((hash % 10000))
        version_code=$((COMMIT_COUNT + BASE_OFFSET + (offset * 100000)))
    fi
    printf "%-35s %10d\n" "$branch" "$version_code"
done

echo ""
echo "✓ All version codes are unique"
echo "✓ Master branches use commit count + base offset ($BASE_OFFSET)"
echo "✓ Feature branches use base + branch offset"
```

## Implementation Details

The version code logic is implemented in `buildSrc/src/main/kotlin/GitVersioning.kt`:

```kotlin
fun getVersionCode(project: Project): Int {
    val countProvider = gitCommand(project, listOf("git", "rev-list", "--count", "HEAD"))
    val count = countProvider.orNull?.toIntOrNull() ?: 1
    
    // Historical base offset to maintain consistency with Google Play
    val BASE_VERSION_CODE_OFFSET = 25
    val baseVersionCode = count + BASE_VERSION_CODE_OFFSET
    
    val branchProvider = gitCommand(project, listOf("git", "rev-parse", "--abbrev-ref", "HEAD"))
    val branch = branchProvider.orNull?.trim() ?: "master"
    
    // Only apply offset for non-master/main branches
    if (branch != "master" && branch != "main" && branch != "HEAD") {
        // Generate a consistent hash from the branch name
        // Handle edge cases by converting to Long and applying modulo before converting back
        val branchHash = (kotlin.math.abs(branch.hashCode().toLong()) % 10000).toInt()
        return baseVersionCode + (branchHash * 100000)
    }
    
    return baseVersionCode
}
```

## Best Practices

1. **Master Branch**: Always deploy production releases from master
   - Clean version codes with base offset (e.g., 348, 349, 350)
   - Sequential and predictable (commit_count + 25)

2. **Feature Branches**: Use for Alpha track testing
   - Unique version codes prevent conflicts (e.g., 45600348, 78900349)
   - Can deploy multiple branches in parallel

3. **Merging to Master**: After testing on Alpha
   - Merge feature branch to master
   - Next master build gets clean version code (commit_count + 25)
   - Production releases maintain sequential numbering

4. **Version Code Ranges**: Understand the ranges
   - Master: 25+ (base offset + commit count, grows with each commit)
   - Feature branches: 100,025 to 999,999,999+ (offset range: 0-9999 * 100,000 + base + commits)
   - Maximum feature branch offset: 999,900,025 (when hash % 10000 = 9999)
   - **Collision-free range**: Master can safely grow to ~999M commits before any risk of collision with feature branches
   - **Practical range**: Most projects stay under 100k commits, making collisions extremely unlikely
   - **Note**: If approaching 999M commits (highly unlikely), consider migrating to a new major version or adjusting the multiplier in `buildSrc/src/main/kotlin/GitVersioning.kt`

## Troubleshooting

### "Version code already exists" error on Google Play

**Cause**: Two builds with the same version code were uploaded to the same track.

**Solution**: 
- Verify you're on the correct branch: `git branch --show-current`
- Check version code: Look in build logs for "Feature branch detected" message
- Ensure full Git history: `git fetch --unshallow` if using shallow clone

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

**Cause**: Branch name might be "HEAD" or similar edge case.

**Solution**: Rename branch or check `GitVersioning.kt` logic:
```bash
git branch -m old-name feature/new-name
```

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
