# Scripts Directory

This directory contains utility scripts for the QR Reader project.

## Available Scripts

### `validate_version_offset.sh` ⭐ NEW

Validates that the `BASE_VERSION_CODE_OFFSET` in `GitVersioning.kt` is correctly configured for the current repository state and Google Play version.

**Purpose:** Ensures version codes match or exceed the latest Google Play version to prevent upload failures.

**Usage:**
```bash
# Show current configuration
./scripts/validate_version_offset.sh

# Validate against known Play Store version
./scripts/validate_version_offset.sh 367
```

**Output:**
- ✓ VALID: Calculated version matches or exceeds Play Store version
- ⚠ WARNING: Calculated version is higher (OK for new releases)
- ✗ ERROR: Calculated version is lower (will be rejected by Play Store)

**When to use:**
- After repository restructure or history changes
- Before making a release if version codes seem off
- To verify offset calculation after manual Google Play uploads
- When troubleshooting Play Store upload failures

**Example:**
```bash
$ ./scripts/validate_version_offset.sh 367
=== Version Code Offset Validator ===

Current git commit count: 2
Current BASE_VERSION_CODE_OFFSET: 365
Calculated version code: 367 (commit count + offset)

Expected Play Store version: 367

✓ VALID: Calculated version matches expected Play Store version

Next commit will produce version: 368
```

If validation fails, the script provides the exact offset value to update in `GitVersioning.kt`.

---

### `fetch_play_version.py`

Fetches the latest version code from Google Play Store using the Google Play Developer API.

**Purpose:** Provides an alternative to the static offset approach for version code calculation. Instead of using a fixed `BASE_VERSION_CODE_OFFSET`, this script queries the Play Store directly to get the current version code and returns the next version (current + 1).

**Status:** Reference implementation (not currently used in builds)

**Requirements:**
```bash
pip install google-api-python-client google-auth
```

**Setup:**
1. Create a service account in [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google Play Developer API
3. Download service account JSON key
4. Grant service account "Release Manager" role in Play Console
5. Save credentials as `service-account.json` in project root

**Usage:**
```bash
# Fetch the next version code to use
python3 scripts/fetch_play_version.py

# Output (to stderr):
Package: cat.company.qrreader
Track: production
Latest version code: 348
Next version code: 349

# Output (to stdout, for scripting):
349
```

**Integration with Gradle:**

See `docs/VERSION_CODE_APPROACHES.md` for examples of integrating this script with the build system.

Example:
```gradle
// Get version from Python script
def getPlayStoreVersionCode() {
    try {
        def process = "python3 scripts/fetch_play_version.py".execute()
        process.waitFor()
        if (process.exitValue() == 0) {
            return process.text.trim().toInteger()
        }
    } catch (Exception e) {
        logger.warn("Failed to fetch Play Store version: ${e.message}")
    }
    // Fallback to git-based versioning
    return GitVersioning.getVersionCode(project)
}

android {
    defaultConfig {
        versionCode = getPlayStoreVersionCode()
    }
}
```

**Pros:**
- Always accurate based on Play Store state
- Self-healing if version history changes

**Cons:**
- Requires network connection
- Adds 2-5 seconds to build time
- Needs Google Cloud setup and credentials
- API rate limits apply

**Current Approach:**

The project currently uses a **static base offset** (`BASE_VERSION_CODE_OFFSET = 25`) in `buildSrc/src/main/kotlin/GitVersioning.kt` for simplicity, speed, and reliability. This script is provided as a reference for future consideration or projects that need dynamic version fetching.

**See Also:**
- `docs/VERSION_CODE_APPROACHES.md` - Detailed comparison of approaches
- `buildSrc/src/main/kotlin/GitVersioning.kt` - Current implementation
- [Google Play Developer API docs](https://developers.google.com/android-publisher/)

---

**Last Updated:** 2026-02-18
