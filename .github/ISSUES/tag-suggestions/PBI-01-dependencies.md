# PBI #1: Add ML Kit GenAI Dependency and Configuration

**Priority:** High  
**Story Points:** 2  
**Labels:** `enhancement`, `dependencies`, `setup`

## Description
Add the ML Kit GenAI library to the project to enable on-device AI capabilities with Gemini Nano. This is the foundation for the tag suggestions feature.

## Acceptance Criteria
- [ ] ML Kit GenAI dependency added to `gradle/libs.versions.toml`
- [ ] Version catalog updated with appropriate version
- [ ] Dependency added to app's `build.gradle` file
- [ ] Project builds successfully with new dependency
- [ ] No conflicts with existing ML Kit Barcode Scanning library
- [ ] Minimum SDK requirements verified (should work with minSdk 29)

## Technical Details

**Files to Modify:**
- `gradle/libs.versions.toml`
- `app/build.gradle`

**Dependencies to Add:**
```toml
# In [versions]
mlkitGenai = "latest_stable_version"

# In [libraries]
google-mlkit-genai = { group = "com.google.mlkit", name = "genai", version.ref = "mlkitGenai" }
```

## Testing
- Build project with `./gradlew build`
- Verify no dependency conflicts
- Check that app still runs on test devices

## Related Issues
None

## Notes
- Check Google's ML Kit documentation for the latest stable version
- Ensure compatibility with existing ML Kit Barcode Scanning library (version 17.3.0)
- Consider adding ProGuard/R8 rules if needed

## Resources
- [ML Kit GenAI Documentation](https://developer.android.com/ai/gemini-nano/ml-kit-genai)
- [ML Kit on Maven](https://mvnrepository.com/artifact/com.google.mlkit/genai)
