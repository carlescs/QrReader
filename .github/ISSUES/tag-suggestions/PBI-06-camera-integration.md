# PBI #6: Update Camera Feature with Tag Suggestions

**Priority:** High  
**Story Points:** 5  
**Labels:** `enhancement`, `ui`, `feature`, `camera`

## Description
Integrate tag suggestions into the camera/scanning feature.

## Acceptance Criteria
- [ ] `QrCameraViewModel` updated to request tag suggestions
- [ ] UI displays suggested tags below scanned barcode
- [ ] User can tap suggested tag to apply it
- [ ] Loading state shown while generating
- [ ] Gracefully handles when Gemini Nano unavailable
- [ ] Does not block barcode saving
- [ ] Follows Material3 design patterns

## Dependencies
- Depends on PBI #4 and PBI #5

See TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md for full details.
