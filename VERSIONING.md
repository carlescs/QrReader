# Versioning Guide

This project uses **Git-based automatic versioning** following semantic versioning principles.

For detailed technical information about version code testing and troubleshooting, see [Version Code Testing Guide](docs/VERSION_CODE_TESTING.md).

## How It Works

### Version Code
- Automatically calculated from the total number of Git commits
- Increments with every commit on master branch
- Example: 259, 260, 261...
- **Feature branches**: Additional branch-specific offset added to prevent collisions
  - Ensures each feature branch has unique version codes when deployed to Alpha track
  - Example: A feature branch with 260 commits gets an offset based on branch name hash
  - Formula: version_code = 260 + (hash % 10000) * 100000
  - Result could be 45600260 if hash yields offset value of 456

### Version Name
- Derived from Git tags following semantic versioning (major.minor.patch)
- Format: `v5.2.0`, `v5.2.1`, etc.

**Development builds** (untagged commits):
- Format: `5.2.0-dev.3+abc1234`
- Where:
  - `5.2.0` = last tag version
  - `dev.3` = 3 commits since last tag
  - `abc1234` = short commit hash

**Release builds** (tagged commits):
- Format: `5.2.0`
- Clean version number from the tag

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
- Version Code: Commit count + branch-specific offset (e.g., 100267)
  - Prevents conflicts when deploying multiple feature branches to Alpha
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
- Version: Either a tagged release or a dev version

**Manual (Any branch):**
1. Navigate to: GitHub → Actions → "Android CI/CD" workflow
2. Click "Run workflow" dropdown
3. Select your feature branch from the branch dropdown
4. Check "Upload to Google Play Alpha" option
5. Click "Run workflow" button

The build will use the dev version from your branch (e.g., `5.1.8-dev.8+a1b2c3d`).

### Dev Version Benefits

Each dev version is unique and traceable:
- **Version Code**: Unique per branch (commit count + branch offset for feature branches)
- **Version Name**: Includes commit hash for traceability
- **Examples**:
  - Master: `5.1.8-dev.5+abc1234` (version code: 5)
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

**Example Scenario (illustrative offsets):**
```bash
# Three developers working on different features
# Each branch gets a deterministic offset based on its name hash
Branch: feature/ai-descriptions     → Version: offset1 + 456 commits
Branch: feature/tag-suggestions      → Version: offset2 + 460 commits
Branch: feature/improved-scanner     → Version: offset3 + 458 commits

# Actual values depend on (hash(branchName) % 10000) * 100000
# For example: 
#   hash("feature/ai-descriptions") % 10000 = 1234 → offset = 123400000
#   Plus 456 commits = version code 123400456

# All can be deployed to Alpha simultaneously without conflicts
```

**Important Notes:**
- Master branch deployments use simple commit count (no offset)
- Feature branches should be merged to master before Production promotion
- After merging to master, the version code resets to the master commit count
- This ensures Production releases have clean, sequential version codes

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

**No.** The system now prevents version code conflicts:

**Version Code Protection:**
- Each feature branch gets a unique offset based on the branch name
- Example: 
  - Master: version code 260 (just commit count)
  - Branch A (`feature/scanner`): version code 456260 (456000 offset + 260 commits)
  - Branch B (`feature/tags`): version code 789260 (789000 offset + 260 commits)
- Google Play requires unique, monotonically increasing version codes
- The branch offset ensures no collisions when deploying multiple branches to Alpha

**Version Name Uniqueness:**
- Version names include the commit hash, making them unique
- Example: 
  - Branch A: `5.1.8-dev.5+abc1234`
  - Branch B: `5.1.8-dev.5+def5678`

**Best Practice:** While collisions are now prevented, it's still recommended to:
- Deploy feature branches to Alpha track sequentially when possible
- Test each feature branch separately before promoting to Production
- Use the Beta track for more extensive testing of feature branches before Production

### Should I use dev versions for production releases?

**No.** Production releases should always use clean tag versions:
- Tag the commit: `git tag v5.2.0`
- Push the tag: `git push origin v5.2.0`
- This creates version `5.2.0` (without `-dev` suffix)

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
