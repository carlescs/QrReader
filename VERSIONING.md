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

### 2. Creating a Release

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
- `.github/workflows/release.yml`: Creates GitHub releases when tags are pushed

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
