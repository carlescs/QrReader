# Versioning Guide

This project uses **Git-based automatic versioning** following semantic versioning principles.

For detailed technical information about version code testing and troubleshooting, see [Version Code Testing Guide](docs/VERSION_CODE_TESTING.md).

## How It Works

### Version Code (Dynamic + Fallback Approach)

**Primary Method: Google Play API Fetch**
- Fetches the highest version code in use across **all** Google Play tracks (internal, alpha, beta, production)
- Returns `max_version_across_all_tracks + 1` for the next build
- Checking every track prevents "version code already used" errors that occur when a code was uploaded to a test track but never reached production
- Eliminates dependency on git history (commit count, branches, etc.)
- Always accurate regardless of exploratory branches or repository restructuring
- Requires `service-account.json` credentials file in project root

**Fallback Method: Git Commit Count + Offset**
- Used only when Google Play API is unavailable (no credentials, network issues, etc.)
- Formula: `version_code = commit_count + 365`
- Base offset of **365** maintains consistency with current Play Store state
- This accounts for repository restructuring that reduced commit count

**Benefits of Dynamic Approach:**
- ✅ Always accurate - no manual offset recalculation needed
- ✅ Self-healing - adapts to any Play Store changes automatically
- ✅ Git-independent - exploratory/abandoned branches don't affect version codes
- ✅ Robust - handles repository resets, rebases, squashes gracefully
- ✅ Fast build times - API call adds ~2-5 seconds (cached in CI/CD)

**How it works:**
1. Build starts → Check for `service-account.json`
2. If found → Call `scripts/fetch_play_version.py` → Get latest version + 1
3. If not found or fails → Fall back to `commit_count + 365`
4. Version code is used in build

Example workflow:
- Play Store has version 367
- API fetch returns 368 (next version)
- Build uses version code 368
- After upload, Play Store has 368
- Next build fetches 369, and so on...

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

## Setup (First Time Only)

### Google Play API Credentials (Optional but Recommended)

For automatic version code fetching from Google Play Store:

1. **Create a Service Account** in [Google Cloud Console](https://console.cloud.google.com/)
2. **Enable Google Play Developer API** for your project
3. **Download the JSON key file** for the service account
4. **Grant access in Play Console**: Settings → API access → Add service account with "Release Manager" role
5. **Save credentials** as `service-account.json` in the project root (this file is in `.gitignore`)

**For CI/CD (GitHub Actions):**
- Add the JSON content as a GitHub Secret: `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
- The workflow automatically creates `service-account.json` from this secret
- Already configured in `.github/workflows/android-ci-cd.yml`

**Without credentials:**
- Builds still work using git-based fallback (`commit_count + 365`)
- Version codes may become out of sync with Play Store over time
- Manual offset updates needed if Play Store versions change independently

**Testing the setup:**
```bash
# Test if credentials work
python3 scripts/fetch_play_version.py

# Expected output (to stderr):
# Track 'internal'   : skipped or max version code
# Track 'alpha'      : skipped or max version code
# Track 'beta'       : skipped or max version code
# Track 'production' : max version code
# Package: cat.company.qrreader
# Tracks checked: internal, alpha, beta, production
# Highest version code across all tracks: 367
# Next version code: 368

# Version code will be output to stdout: 368
```

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
- **Version Code**: Fetched from Google Play API (e.g., latest is 367, so next is 368)
  - **With API credentials**: Version codes are independent of git history
  - **Without API (fallback)**: Uses `commit_count + 365`
  - No conflicts between branches - all fetch the same "next version" from Play Store
- **Version Name**: `5.1.8-dev.8+a1b2c3d` 
  - Based on last tag `v5.1.8`
  - `8` commits since that tag
  - `a1b2c3d` = unique commit hash

**Benefits of API-based versioning:**
- Exploratory/abandoned branches don't affect version codes
- No need to coordinate between parallel feature branches
- Version codes always align with Play Store state
- Repository restructuring doesn't cause version conflicts

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

**Parallel Development with API:**
- **With API credentials**: Multiple branches can be built simultaneously
  - All fetch the same "next version" from Play Store (e.g., 368)
  - Upload one at a time to Alpha track (Google Play doesn't allow multiple same versions)
  - No version conflicts - they'll just increment (368, 369, 370...)
- **Without API (fallback mode)**: 
  - Deploy sequentially or use different tracks (Internal Testing, Alpha, Beta)
  - Commit count differences between branches may cause conflicts

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

**With Google Play API (Recommended):**
- All branches fetch the latest version from Play Store
- Version codes are independent of git history
- No coordination needed between branches
- Just upload when ready - version codes increment automatically

**Without API (Fallback Mode):**
Deploy feature branches to Play Store Alpha one at a time to avoid version code conflicts:

1. **Sequential Deployment**:
   - Deploy and test Branch A first
   - Merge Branch A to master
   - Then deploy Branch B
   
2. **Parallel Testing with Different Tracks**:
   - Branch A → Internal Testing track
   - Branch B → Alpha track
   - Branch C → Beta track (if available)

3. **Version Uniqueness**:
   - Version Code: May be same for branches with equal commit count
   - Version Name: Unique due to commit hash (e.g., `5.1.8-dev.8+a1b2c3d`)
   - Google Play track selection prevents conflicts

### GitHub Secrets

**Required for Google Play API:**
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` - Service account credentials for Play Store API
- Already configured in `.github/workflows/android-ci-cd.yml`

**Other secrets:**
- Existing secrets for Google Play publishing remain unchanged

## Troubleshooting

### Google Play API Issues

**"Service account credentials not found"**
- Create `service-account.json` in project root
- Or add `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` secret in GitHub Actions
- Build will use git-based fallback if credentials are missing

**"Failed to fetch from Google Play"**
- Check service account has "Release Manager" role in Play Console
- Verify Google Play Developer API is enabled
- Test manually: `python3 scripts/fetch_play_version.py`
- Build continues with fallback version (commit count + offset)

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

**With Google Play API: NO** - Version conflicts are eliminated:

**Version Code Behavior:**
- All branches fetch latest version from Play Store API
- Example:
  - Play Store has version 367
  - Branch A builds: fetches 368
  - Branch B builds: fetches 369 (since A uploaded 368)
  - Branch C builds: fetches 370 (after B uploaded 369)
- Each upload increments the version automatically

**Without API (Fallback Mode): POSSIBLY** - Coordination needed:

**Version Code Behavior:**
- All branches use: `version_code = commit_count + 365`
- Example: 
  - Master: version code 370 (5 commits + 365)
  - Branch A: version code 370 (5 commits + 365) ← Same!
  - Branch B: version code 370 (5 commits + 365) ← Same!

**Solutions to Avoid Conflicts:**

1. **Use Google Play API** (Recommended):
   - Add `service-account.json` credentials
   - Automatic version management
   - No coordination needed

2. **Sequential Deployment**:
   - Test Branch A on Alpha → Merge to master
   - Then test Branch B on Alpha → Merge to master
   - Simplest approach without API

3. **Use Different Tracks**:
   - Branch A → Internal Testing track
   - Branch B → Alpha track  
   - Branch C → Beta track
   - Google Play allows one version per track

4. **Coordinate Timing**:
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
