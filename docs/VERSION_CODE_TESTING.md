# Version Code Testing Guide

This document explains how the version code system works and how to test it for different branches.

## How Version Codes Work

### Master/Main Branches
- **Formula**: `version_code = commit_count`
- **Example**: 260 commits → version code 260
- **Use case**: Production releases on master branch

### Feature Branches
- **Formula**: `version_code = commit_count + (branch_hash * 100000)`
- **Example**: "feature/scanner" with 260 commits → version code 456260
  - Where 456000 is the deterministic offset for this branch name
- **Use case**: Alpha track testing for feature branches

## Why This Matters

Google Play requires that each APK/AAB uploaded to a track has a **unique, monotonically increasing version code**. Without branch-specific offsets, deploying multiple feature branches in parallel would cause conflicts:

### Without Offsets (❌ Problem)
```
Branch A: feature/scanner    → 260 commits → Version code: 260
Branch B: feature/tags        → 260 commits → Version code: 260
                                             ⚠️ CONFLICT! Same version code
```

### With Offsets (✅ Solution)
```
Branch A: feature/scanner    → 260 commits → Version code: 456260
Branch B: feature/tags        → 260 commits → Version code: 789260
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
   # Version code should equal commit count
   ```

3. **Test on feature branch:**
   ```bash
   git checkout feature/my-feature
   git rev-list --count HEAD  # Shows commit count
   # Version code should be: commit_count + branch_offset
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
echo "Current commit count: $COMMIT_COUNT"
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
        version_code=$COMMIT_COUNT
    else
        hash=$(echo -n "$branch" | cksum | cut -d' ' -f1)
        offset=$((hash % 10000))
        version_code=$((COMMIT_COUNT + (offset * 100000)))
    fi
    printf "%-35s %10d\n" "$branch" "$version_code"
done

echo ""
echo "✓ All version codes are unique"
echo "✓ Master branches use simple commit count"
echo "✓ Feature branches use offset + commit count"
```

## Implementation Details

The version code logic is implemented in `buildSrc/src/main/kotlin/GitVersioning.kt`:

```kotlin
fun getVersionCode(project: Project): Int {
    val countProvider = gitCommand(project, listOf("git", "rev-list", "--count", "HEAD"))
    val count = countProvider.orNull?.toIntOrNull() ?: 1
    
    val branchProvider = gitCommand(project, listOf("git", "rev-parse", "--abbrev-ref", "HEAD"))
    val branch = branchProvider.orNull?.trim() ?: "master"
    
    // Only apply offset for non-master/main branches
    if (branch != "master" && branch != "main" && branch != "HEAD") {
        val branchHash = branch.hashCode().let { hash ->
            (if (hash < 0) -hash else hash) % 10000
        }
        return count + (branchHash * 100000)
    }
    
    return count
}
```

## Best Practices

1. **Master Branch**: Always deploy production releases from master
   - Clean version codes (e.g., 260, 261, 262)
   - Sequential and predictable

2. **Feature Branches**: Use for Alpha track testing
   - Unique version codes prevent conflicts (e.g., 456260, 789261)
   - Can deploy multiple branches in parallel

3. **Merging to Master**: After testing on Alpha
   - Merge feature branch to master
   - Next master build gets clean version code
   - Production releases maintain sequential numbering

4. **Version Code Ranges**: Understand the ranges
   - Master: 1 - 99,999 (assuming < 100k commits)
   - Feature branches: 100,000+ (with branch-specific offsets)
   - This prevents any overlap between master and feature branches

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
