# Build Environment Notes

## AGP Version Change

During implementation, the Android Gradle Plugin (AGP) version was temporarily changed from 9.0.1 to 8.5.2 due to network connectivity issues in the build environment.

### Issue
The build environment lacked internet connectivity, preventing Gradle from downloading the AGP 9.0.1 plugin:
```
Plugin [id: 'com.android.application', version: '9.0.1', apply: false] was not found
```

### Temporary Solution
Changed `gradle/libs.versions.toml`:
```
agp = "8.5.2"  # Was 9.0.1 in master
```

### Recommendation
Once this branch is merged or tested in an environment with internet connectivity:

1. **Option A - Revert to master's version:**
   ```bash
   # In gradle/libs.versions.toml
   agp = "9.0.1"
   ```

2. **Option B - Keep 8.5.2 if it works:**
   - Test that 8.5.2 works in CI/CD
   - If stable, update master to match
   - If not, revert to 9.0.1

### Testing in CI/CD
When GitHub Actions runs with internet connectivity, it should successfully download whichever AGP version is specified. The Google Play version code fetching implementation is independent of the AGP version.

## Impact
This AGP version change does not affect the core implementation of Google Play version code fetching. It was purely a workaround for the local build environment limitations.
