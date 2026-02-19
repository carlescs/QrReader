# Version Code Quick Reference

## Current Configuration (2026-02-19)

```kotlin
BASE_VERSION_CODE_OFFSET = 365
version_code = commit_count + 365
```

## Quick Checks

### ✅ Is my version code correct?

```bash
./scripts/validate_version_offset.sh 367
```

**Expected**: `✓ VALID` or `⚠ WARNING: Calculated version is HIGHER` (OK for new releases)

**Problem**: `✗ ERROR: Calculated version is LOWER` → Follow script's guidance to fix

### 📊 What version will be built?

```bash
# Show current configuration
./scripts/validate_version_offset.sh

# Or calculate manually
COMMITS=$(git rev-list --count HEAD)
echo "Version code: $((COMMITS + 365))"
```

### 🔧 How to fix if offset is wrong?

1. Run validation with current Play Store version:
   ```bash
   ./scripts/validate_version_offset.sh <play_store_version>
   ```

2. Script will show the correct offset to use

3. Update `buildSrc/src/main/kotlin/GitVersioning.kt`:
   ```kotlin
   private const val BASE_VERSION_CODE_OFFSET = <new_value>
   ```

4. Verify:
   ```bash
   ./scripts/validate_version_offset.sh <play_store_version>
   ```

## Common Scenarios

### Scenario 1: Google Play Upload Fails (Version Too Low)

**Error**: "Version code X has already been used"

**Fix**:
```bash
# 1. Find latest Play Store version (e.g., 400)
# 2. Validate
./scripts/validate_version_offset.sh 400

# 3. If ERROR shown, script will tell you new offset
# 4. Update GitVersioning.kt with new offset
# 5. Verify
./scripts/validate_version_offset.sh 400
```

### Scenario 2: After Repository History Changes

**When**: After rebase, squash, or repository reset

**Fix**: Same as Scenario 1 - recalculate offset based on current Play Store version

### Scenario 3: Manual Play Store Upload

**When**: Someone manually uploads a build outside of CI/CD

**Impact**: Offset may need recalculation if version jumped significantly

**Fix**: Run validation with new Play Store version and update offset if needed

## Calculation Formula

```
new_offset = latest_play_store_version - current_git_commit_count
```

**Example**:
- Play Store version: 367
- Git commits: 2
- Offset: 367 - 2 = **365** ✓

## Files to Know

| File | Purpose |
|------|---------|
| `buildSrc/src/main/kotlin/GitVersioning.kt` | Contains `BASE_VERSION_CODE_OFFSET` |
| `scripts/validate_version_offset.sh` | Validation and diagnostic tool |
| `VERSION_CODE_STRATEGY_ANALYSIS.md` | Detailed problem analysis and solutions |
| `VERSIONING.md` | User-facing versioning guide |

## When Offset Was Updated

| Date | Old Offset | New Offset | Play Store Version | Git Commits | Reason |
|------|-----------|-----------|-------------------|-------------|--------|
| 2026-02-18 | N/A | 25 | 348 | 323 | Initial offset calculation |
| 2026-02-19 | 25 | **365** | 367 | 2 | Repository reset/restructure |

## Quick Commands

```bash
# Check current offset
grep "BASE_VERSION_CODE_OFFSET =" buildSrc/src/main/kotlin/GitVersioning.kt

# Count commits
git rev-list --count HEAD

# Calculate current version code
echo "$(($(git rev-list --count HEAD) + 365))"

# Validate
./scripts/validate_version_offset.sh 367
```

## Need Help?

1. **First**: Run `./scripts/validate_version_offset.sh <play_store_version>`
2. **Check**: `VERSION_CODE_STRATEGY_ANALYSIS.md` for detailed analysis
3. **Review**: Git commit messages for offset changes
4. **Last resort**: Consult team lead or documentation maintainer

---

**Last Updated**: 2026-02-19  
**Current Offset**: 365  
**Next Version**: 369 (will increment from there)
