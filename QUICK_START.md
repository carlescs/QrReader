# Quick Start: Testing Google Play Version Code Fetching

## ✅ What's Already Done

The implementation is complete and ready for testing:

- [x] Modified `GitVersioning.kt` to fetch from Google Play API
- [x] Added automatic fallback to git-based versioning
- [x] Updated CI/CD workflow with Python setup and credential handling
- [x] Added security measures (gitignore)
- [x] Created comprehensive documentation

## 📋 What You Need to Do

### Step 1: Configure GitHub Secret (5 minutes)

If you already have a service account JSON file:

1. Go to: https://github.com/carlescs/QrReader/settings/secrets/actions
2. Click "New repository secret"
3. **Name:** `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
4. **Value:** Paste entire contents of your `service-account.json` file
5. Click "Add secret"

**Don't have a service account yet?** See detailed setup in `docs/GOOGLE_PLAY_VERSIONING.md`

### Step 2: Test the Build (2 minutes)

1. **Push a commit** to trigger the workflow:
   ```bash
   git commit --allow-empty -m "Test Google Play version fetching"
   git push
   ```

2. **Go to GitHub Actions:**
   https://github.com/carlescs/QrReader/actions

3. **Click on the running workflow**

### Step 3: Verify the Output (1 minute)

In the workflow logs, look for:

**✅ Success (using Google Play):**
```
Successfully fetched version code from Google Play: 350
Using Google Play version code: 351
```

**⚠️ Fallback (no credentials):**
```
Google Play API fetch failed, falling back to git-based versioning
Feature branch detected: copilot/get-latest-version-code, adding offset: 8914
```

### Step 4: Check Version Code (1 minute)

In the "Print Version Code and Version Name" step, verify:

**Expected with Google Play API:**
```
Version Code: 351
Version Name: 5.2.0-dev.20+abc1234
```

**Expected with fallback (without credentials):**
```
Version Code: 891400363  (or similar large number)
Version Name: 5.2.0-dev.20+abc1234
```

## 🎯 Success Criteria

✅ **You know it's working when:**
- Version code is reasonable (~350, not 891400363)
- Logs show "Successfully fetched version code from Google Play"
- Build completes successfully

## ⚠️ Troubleshooting

### Build fails with "Service account not found"

**This is OK!** The build will use fallback versioning. Add the GitHub secret to use Google Play API.

### Version code is still large (891400363)

**Cause:** Using fallback versioning (credentials not configured)  
**Solution:** Add `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` secret as described in Step 1

### "Failed to fetch version code from Google Play"

**Check:**
1. Service account JSON is valid
2. Service account has "Release Manager" role
3. Google Play Developer API is enabled
4. Package name matches: `cat.company.qrreader`

**Debug locally:**
```bash
# Test the Python script directly
python3 scripts/fetch_play_version.py
```

## 📚 Full Documentation

- **Quick Setup:** This file
- **Comprehensive Guide:** `docs/GOOGLE_PLAY_VERSIONING.md`
- **Implementation Details:** `IMPLEMENTATION_SUMMARY.md`
- **Versioning Guide:** `VERSIONING.md`

## 🚀 Ready to Deploy?

Once you've verified the version code is correct (~350), you can deploy as usual. The Google Play version fetching will ensure the version code is always correct and doesn't conflict with existing versions in the Play Store.

## 💬 Questions?

- Review troubleshooting in `docs/GOOGLE_PLAY_VERSIONING.md`
- Check implementation details in `IMPLEMENTATION_SUMMARY.md`
- Review decision context in `docs/VERSION_CODE_APPROACHES.md`

---

**Estimated Time:** 10-15 minutes total  
**Difficulty:** Easy (mostly configuration)  
**Breaking Changes:** None (fully backward compatible)
