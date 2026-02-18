# Versioning Guide

This project uses **Git-based automatic versioning** following semantic versioning principles.

For detailed technical information about version code testing and troubleshooting, see [Version Code Testing Guide](docs/VERSION_CODE_TESTING.md).

## How It Works

### Version Code
- Automatically calculated from the total number of Git commits plus a base offset
- **Base offset**: A historical offset of 25 is added to maintain consistency with existing Google Play versions
  - This accounts for repository restructuring that occurred in the past
  - Ensures monotonically increasing version codes that don't conflict with existing Play Store versions
- Increments with every commit (on any branch)
- **Formula**: `version_code = commit_count + 25`
- Example: With 323 commits → version code 348 (323 + 25)
- **All branches use the same formula** - no branch-specific offsets
  - Simplified approach keeps version codes reasonable (300-400 range for typical projects)
  - Teams should deploy feature branches to Alpha track sequentially to avoid conflicts
  - Alternative: Use different Google Play tracks (Internal Testing, Alpha, Beta) for parallel testing

### Version Name
- Derived from Git tags following semantic versioning (major.minor.patch)
- Format: `v5.2.0`, `v5.2.1`, etc.

**Master/main branch builds**:
- Format: `5.2.0` (clean version without -dev suffix)
- Uses the latest tag version
- Ensures production deployments have proper version names
- Example: If last tag is `v5.2.0`, all commits on master use `5.2.0`

**Feature branch builds** (development branches):
- Format: `5.2.0-dev.3+abc1234`
- Where:
  - `5.2.0` = last tag version
  - `dev.3` = 3 commits since last tag
  - `abc1234` = short commit hash

**Release builds** (tagged commits):
- Format: `5.2.0`
- Clean version number from the tag
- Same on all branches when on a tagged commit

## Creating a New Release

### 1. Regular Development
Just commit and push as usual:
```bash
git add .
git commit -m "Add new feature"
git push
```
Versions are calculated automatically.

### 2. Feature Branch Development (Dev Versions)

When working on feature branches, the system automatically generates development versions:

**Example on feature branch `feature/new-scanner`:**
- Version Code: Commit count + base offset (e.g., 285 for 260 commits)
  - **Note**: Deploy feature branches sequentially to avoid version code conflicts
- Version Name: `5.1.8-dev.8+a1b2c3d` 
  - Based on last tag `v5.1.8`
  - `8` commits since that tag
  - `a1b2c3d` = unique commit hash

**To upload a dev version from a feature branch to Play Store Alpha:**

1. Push your feature branch to GitHub:
   ```bash
   git push origin feature/new-scanner
   ```

2. Go to GitHub Actions → Select "Android CI/CD" workflow

3. Click "Run workflow" → Select your feature branch

4. Check "Upload to Google Play Alpha" option

5. Click "Run workflow"

This uploads your dev version (e.g., `5.1.8-dev.8+a1b2c3d`) to the Alpha track for testing.

**Important: Sequential Deployment**
- Since all branches use the same version code formula, deploy feature branches to Alpha one at a time
- Or use different tracks (Internal Testing for Branch A, Alpha for Branch B)
- Avoids version code conflicts when multiple branches have the same commit count

**Why use manual upload for feature branches?**
- Safety: Prevents accidental uploads from every feature branch push
- Control: You decide when a feature branch is ready for Alpha testing
- Traceability: Each dev version includes commit hash for tracking

### 3. Creating a Production Release

When ready to release:

```bash
# Ensure you're on master and up to date
git checkout master
git pull

# Create and push a version tag
git tag v5.2.0
git push origin v5.2.0
```

This will:
1. ✅ Trigger the release workflow
2. ✅ Build release APK and AAB
3. ✅ Create a GitHub release with release notes
4. ✅ Attach build artifacts to the release

### Semantic Versioning

Follow semantic versioning for tags:
- **Major** (v6.0.0): Breaking changes
- **Minor** (v5.2.0): New features, backward compatible
- **Patch** (v5.2.1): Bug fixes

## Viewing Current Version

Locally:
```bash
./gradlew printVersionName
```

In code (already configured in `app/build.gradle`):
```gradle
versionCode = GitVersioning.getVersionCode(project)
versionName = GitVersioning.getVersionName(project)
```

## CI/CD Integration

### Existing Workflows
- `.github/workflows/android-ci-cd.yml`: Runs tests and builds on every push/PR
  - **Automatic uploads**: Master branch pushes automatically upload to Alpha track
  - **Manual uploads**: Feature branches can upload via workflow_dispatch
- `.github/workflows/release.yml`: Creates GitHub releases when tags are pushed

### Uploading Feature Branch Dev Versions

Feature branches generate unique dev versions that can be uploaded for testing:

**Automatic (Master only):**
- Every push to master automatically uploads to Play Store Alpha track
- Version: Clean version from last tag (e.g., `5.2.0`) or tagged release

**Manual (Any branch):**
1. Navigate to: GitHub → Actions → "Android CI/CD" workflow
2. Click "Run workflow" dropdown
3. Select your feature branch from the branch dropdown
4. Check "Upload to Google Play Alpha" option
5. Click "Run workflow" button

The build will use the dev version from your branch (e.g., `5.1.8-dev.8+a1b2c3d`).

### Dev Version Benefits

Each dev version is unique and traceable:
- **Version Code**: Unique per branch (commit count + base offset)
- **Version Name**: Includes commit hash for traceability on feature branches
- **Examples**:
  - Master: `5.2.0` (clean version, no dev suffix)
  - Feature branch: `5.2.0-dev.12+def5678` (version code: 456012 with offset)

This allows multiple feature branches to be tested in parallel without version conflicts.

### Feature Branch Deployment Strategy

When deploying multiple feature branches to the Alpha track:

**Automatic Protection:**
- Each feature branch gets a unique version code based on branch name + commit count
- Prevents Google Play upload rejections due to version code conflicts
- Branch offset is deterministic (same branch name always gets the same offset)

**Recommended Workflow:**
1. **Parallel Development**: Work on multiple feature branches simultaneously
2. **Independent Testing**: Deploy any branch to Alpha track for testing
3. **Sequential Promotion**: Only promote tested branches to Production one at a time

**Example on feature branch `feature/new-scanner`:**

Deploy feature branches to Play Store Alpha one at a time to avoid version code conflicts:

1. **Sequential Deployment (Recommended)**:
   - Deploy and test Branch A first
   - Merge Branch A to master
   - Then deploy Branch B
   
2. **Parallel Testing with Different Tracks**:
   - Branch A → Internal Testing track
   - Branch B → Alpha track
   - Branch C → Beta track (if available)

3. **Version Uniqueness**:
   - Version Code: Same for branches with equal commit count
   - Version Name: Unique due to commit hash (e.g., `5.1.8-dev.8+a1b2c3d`)
   - Google Play track selection prevents conflicts

This allows independent testing while avoiding version code collisions.

### GitHub Secrets
No additional secrets needed for automatic versioning. Existing secrets for Google Play publishing remain unchanged.

## Troubleshooting

### "No tags found" warning
If you see version `0.0.1-dev+abc1234`, it means no Git tags exist yet. Create your first tag:
```bash
git tag v5.2.0
git push origin v5.2.0
```

### Version not updating
- Ensure Git is available in your build environment
- Check that `.git` directory exists
- Verify you have commit history: `git log --oneline`
- **Important**: Shallow clones are not supported. If you cloned with `--depth`, convert to full clone:
  ```bash
  git fetch --unshallow
  ```

### Can I upload dev versions from feature branches?

**Yes!** The system supports uploading dev versions from any branch:

1. Feature branches automatically get unique dev versions (e.g., `5.1.8-dev.8+a1b2c3d`)
2. Use GitHub Actions workflow_dispatch to manually trigger upload
3. Go to Actions → "Android CI/CD" → "Run workflow" → Select branch → Check "Upload to Google Play Alpha"

### Will different feature branches have version conflicts?

**Possibly** - Teams should coordinate deployments:

**Version Code Behavior:**
- All branches use: `version_code = commit_count + 25`
- Example: 
  - Master: version code 285 (260 commits + 25)
  - Branch A: version code 285 (260 commits + 25) ← Same!
  - Branch B: version code 285 (260 commits + 25) ← Same!

**Solutions to Avoid Conflicts:**

1. **Sequential Deployment** (Recommended):
   - Test Branch A on Alpha → Merge to master
   - Then test Branch B on Alpha → Merge to master
   - Simplest approach, works for most projects

2. **Use Different Tracks**:
   - Branch A → Internal Testing track
   - Branch B → Alpha track  
   - Branch C → Beta track
   - Google Play allows one version per track

3. **Coordinate Timing**:
   - Short-lived feature branches rarely conflict
   - By the time Branch B is ready, Branch A is likely merged (increasing commit count)

**Version Name Uniqueness:**
- Version names include commit hash, making them unique:
  - Branch A: `5.1.8-dev.5+abc1234`
  - Branch B: `5.1.8-dev.5+def5678`
- Helpful for tracking which build is deployed

**Why This Approach?**
- Keeps version codes simple and reasonable (300-400 range)
- Avoids extremely large version codes (891M+ was the problem)
- Most projects don't need parallel Alpha deployments
- Teams can easily coordinate when needed

### Should I use dev versions for production releases?

**Master branch automatically uses clean versions** (without -dev suffix), so production deployments from master are always properly versioned. However, for official releases, it's still best practice to tag commits:
- Tag the commit: `git tag v5.2.0`
- Push the tag: `git push origin v5.2.0`
- This creates a formal release point with version `5.2.0`

Tags provide clear release milestones and make it easy to track which commits represent official releases.

## Migration Notes

### Previous Manual Versioning
- **Old**: Versions manually set in `gradle/libs.versions.toml`
  - `versionCode = "259"`
  - `versionName = "5.1.8"`
- **New**: Automatically calculated from Git
  - versionCode = commit count
  - versionName = from git tags

### First Tag Recommendation
Based on your current version (5.1.8), create your first tag:
```bash
git tag v5.1.8
git push origin v5.1.8
```

Then future releases increment from there (v5.1.9, v5.2.0, etc.).
