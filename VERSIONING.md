# Versioning Guide

This project uses **Git-based automatic versioning** following semantic versioning principles.

## How It Works

### Version Code
- Automatically calculated from the total number of Git commits
- Increments with every commit
- Example: 259, 260, 261...

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
- Version Code: Current commit count (e.g., 267)
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
- **Version Code**: Monotonically increasing (commit count)
- **Version Name**: Includes commit hash for traceability
- **Examples**:
  - `5.1.8-dev.5+abc1234` - 5 commits after tag v5.1.8
  - `5.2.0-dev.12+def5678` - 12 commits after tag v5.2.0

This allows multiple feature branches to be tested in parallel without version conflicts.

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

**No.** Each feature branch has a unique version name because:
- The commit hash is different for each branch
- Example: 
  - Branch A: `5.1.8-dev.5+abc1234`
  - Branch B: `5.1.8-dev.3+def5678`

The version code (commit count) might be similar, but Google Play accepts this for Alpha/Beta tracks.

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
