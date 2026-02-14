# PBI #2: Create GeminiNanoService for On-Device AI

**Priority:** High  
**Story Points:** 5  
**Labels:** `enhancement`, `core`, `ml`

## Description
Create a service layer component that encapsulates all interactions with ML Kit GenAI and Gemini Nano.

## Acceptance Criteria
- [ ] `GeminiNanoService` class created in `data/service/` package
- [ ] Service checks Gemini Nano availability on device
- [ ] Service handles model download status
- [ ] Implements tag suggestion generation from barcode content
- [ ] Handles errors gracefully
- [ ] Implements proper prompt engineering for tag suggestions
- [ ] Supports cancellation of inference operations
- [ ] Uses coroutines for async operations
- [ ] Properly injected via Koin

## Dependencies
- Depends on PBI #1

See TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md for full details.
