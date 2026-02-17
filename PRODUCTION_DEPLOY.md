# Production Deployment Guide

This guide explains how to deploy to production with version increases in the QR Reader project.

## Quick Reference

**To deploy to production:**
1. Create a version tag (e.g., `v5.2.0`)
2. Push to master branch
3. Wait for automatic Alpha deployment
4. Approve production promotion when ready

## Step-by-Step Process

### 1. Prepare Your Release

Ensure all code changes are complete, tested, and merged to master:

```bash
# Switch to master and update
git checkout master
git pull origin master

# Verify everything is ready
./gradlew testDebugUnitTest  # Run tests locally
./gradlew assembleRelease    # Test build locally
```

### 2. Increment Version

The project uses **Git-based automatic versioning**. Version is controlled by Git tags:

#### Version Format
- **Version Code**: Automatically calculated from total commit count
- **Version Name**: Derived from Git tags (semantic versioning)

#### Create a Version Tag

Choose the appropriate version increment based on your changes:

- **Patch** (`v5.2.1`): Bug fixes and minor updates
- **Minor** (`v5.3.0`): New features, backward compatible  
- **Major** (`v6.0.0`): Breaking changes

```bash
# Create a version tag (replace with your version)
git tag v5.3.0

# Push the tag to GitHub
git push origin v5.3.0
```

**Important**: The tag format must be `vX.Y.Z` (e.g., `v5.3.0`, not `5.3.0`)

### 3. Automatic Alpha Deployment

Once you push to master (or push a tag on master):

1. **GitHub Actions automatically triggers** the CI/CD workflow
2. **Tests run** (unit tests, coverage reports)
3. **Build completes** (release bundle is built and signed)
4. **Alpha deployment** happens automatically to Google Play Alpha track
5. **Version used**: `5.3.0` (clean version from your tag)

You can monitor progress at: `https://github.com/carlescs/QrReader/actions`

### 4. Test on Alpha Track

After Alpha deployment completes:
- Install from Google Play Alpha track
- Verify the app works correctly
- Test key features and recent changes
- Check version number in app (should match your tag)

### 5. Promote to Production

After Alpha testing is complete:

#### Manual Approval Process

1. **Navigate to GitHub Actions**:
   - Go to: https://github.com/carlescs/QrReader/actions
   - Click on the running workflow (yellow dot = waiting for approval)

2. **Find the Promote Job**:
   - Click on the workflow run
   - You'll see "Promote to Production" job with status "Waiting"
   - Click on the job

3. **Review and Approve**:
   - Click **"Review deployments"** button
   - Check the **"PlayStore"** checkbox
   - Add a comment (optional): e.g., "Tested on Alpha, ready for production"
   - Click **"Approve and deploy"**

4. **Production Deployment Completes**:
   - The job executes and promotes to production
   - Users will receive the update through Google Play Store
   - Rollout is typically gradual (Google Play's staged rollout)

#### Timeline

- **Alpha deployment**: Immediate (after push to master)
- **Production approval**: When you decide (recommended: 1-3 days of Alpha testing)
- **Production rollout**: Gradual over several days (managed by Google Play)

## Common Scenarios

### Scenario 1: Hotfix Release (Fast Track)

For urgent bug fixes, use the 2-tier flow (Alpha → Production):

```bash
# Fix the bug and commit
git add .
git commit -m "fix: Critical bug in QR scanner"
git push origin master

# Tag the hotfix version
git tag v5.2.1
git push origin v5.2.1
```

Wait for Alpha deployment → Test quickly → Approve production (can be same day)

### Scenario 2: Major Feature Release (Cautious Approach)

For major features, use the 3-tier flow (Alpha → Beta → Production):

```bash
# Merge feature and tag
git checkout master
git pull
git tag v5.3.0
git push origin v5.3.0
```

**Trigger Beta deployment:**
1. Go to GitHub → Actions → "Android CI/CD"
2. Click "Run workflow"
3. Select branch: `master`
4. ✅ Check "Upload to Google Play Alpha" 
5. ✅ Check "Use Beta track before Production"
6. Click "Run workflow"

Timeline: Alpha (2-3 days) → Beta (3-5 days) → Production (after approval)

### Scenario 3: Testing Feature Branch Before Merging

To test a feature branch on Alpha without affecting production:

```bash
# Push your feature branch
git push origin feature/new-feature
```

**Manual Alpha upload:**
1. Go to GitHub → Actions → "Android CI/CD"
2. Click "Run workflow"
3. Select your feature branch
4. ✅ Check "Upload to Google Play Alpha"
5. Click "Run workflow"

Version format: `5.2.0-dev.8+abc1234` (includes commit hash for tracking)

**Note**: Feature branch deployments do NOT trigger production promotion.

## Version History

To see current version and history:

```bash
# View all tags
git tag -l

# View current version (locally)
./gradlew printVersionName

# View commit history with tags
git log --oneline --decorate
```

## Troubleshooting

### Issue: "No version tag found"

**Problem**: Building without any Git tags results in version `0.0.1-dev+hash`

**Solution**: Create your first tag
```bash
git tag v5.2.0
git push origin v5.2.0
```

### Issue: "Production approval not showing"

**Problem**: GitHub Environment protection not configured

**Solution**: Configure the PlayStore environment
1. Go to: Repository Settings → Environments
2. Find or create "PlayStore" environment
3. Add yourself as required reviewer
4. Save protection rules

See [.github/CICD.md](./.github/CICD.md#setting-up-environment-protection) for detailed instructions.

### Issue: "Bundle signing failed"

**Problem**: Missing GitHub secrets for keystore

**Solution**: Add required secrets in Repository Settings → Secrets:
- `KEYSTORE_BASE64` (base64-encoded keystore)
- `KEYSTORE_PASSWORD` (keystore password)
- `KEY_ALIAS` (key alias)
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` (service account JSON)

### Issue: "Version already exists in Play Store"

**Problem**: Trying to use the same version tag twice

**Solution**: Increment to next version
```bash
# If v5.2.0 exists, use v5.2.1 or v5.3.0
git tag v5.2.1
git push origin v5.2.1
```

## Best Practices

### ✅ DO:
- Test locally before tagging (`./gradlew testDebugUnitTest`)
- Follow semantic versioning (major.minor.patch)
- Test on Alpha track before production approval (1-3 days)
- Add meaningful commit messages before tagging
- Tag from master branch only for production releases
- Use patch versions (v5.2.1) for hotfixes
- Use minor versions (v5.3.0) for new features

### ❌ DON'T:
- Don't create tags without testing first
- Don't tag from feature branches for production releases
- Don't skip Alpha testing (always test before production)
- Don't reuse version tags (they're permanent)
- Don't push tags with typos (format must be `vX.Y.Z`)

## Environment Setup (First Time Only)

If this is your first production deployment, ensure:

1. ✅ Google Play Service Account configured
2. ✅ GitHub Secrets added (keystore, passwords, service account)
3. ✅ GitHub Environments configured (PlayStore-Alpha, PlayStore)
4. ✅ Manual reviewers added to PlayStore environment
5. ✅ App manually uploaded to Play Console once (required for automation)

See [.github/CICD.md](./.github/CICD.md) for complete setup instructions.

## Additional Resources

- **Versioning Details**: [VERSIONING.md](VERSIONING.md)
- **CI/CD Configuration**: [.github/CICD.md](.github/CICD.md)
- **Deployment Strategy**: [CICD_STRATEGY.md](CICD_STRATEGY.md)
- **Visual Flow Diagram**: [CICD_VISUAL_FLOW.md](CICD_VISUAL_FLOW.md)
- **Implementation Status**: [CICD_IMPLEMENTATION_STATUS.md](CICD_IMPLEMENTATION_STATUS.md)

## Summary

**Simple 4-Step Process:**

```bash
# 1. Update and test
git checkout master && git pull
./gradlew testDebugUnitTest

# 2. Create version tag
git tag v5.3.0
git push origin v5.3.0

# 3. Wait for automatic Alpha deployment
# Monitor at: https://github.com/carlescs/QrReader/actions

# 4. After testing, approve production promotion
# Go to Actions → Click workflow → Review deployments → Approve
```

That's it! The CI/CD system handles building, signing, uploading, and promoting your release automatically.
