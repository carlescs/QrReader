# Tag Suggestions Feature - Implementation Summary

## Overview

This repository now contains a comprehensive plan and ready-to-use GitHub issues for implementing AI-powered tag suggestions using **ML Kit GenAI with Gemini Nano**. All processing happens on-device for privacy and performance.

## What Has Been Created

### 1. Main Implementation Plan
📄 **`TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md`**
- Complete technical specification (27,500+ words)
- Architecture diagrams
- Code examples
- Risk assessment
- Success metrics
- Timeline estimates

### 2. GitHub Issue Templates (PBIs)
📁 **`.github/ISSUES/tag-suggestions/`**
- 11 ready-to-copy Product Backlog Items
- Each with acceptance criteria, technical details, and testing requirements
- Organized by implementation phase
- Numbered in recommended execution order

### 3. Automation Script
🔧 **`.github/ISSUES/create-issues.sh`**
- Automated issue creation script (requires gh CLI)
- Creates all 11 issues with proper formatting
- Adds appropriate labels

## Quick Start Guide

### Option 1: Manual Issue Creation (Recommended)

1. Go to https://github.com/carlescs/QrReader/issues
2. Click "New Issue"
3. Open `.github/ISSUES/tag-suggestions/PBI-01-dependencies.md`
4. Copy the entire content
5. Paste into the GitHub issue
6. Add labels: `enhancement`, `dependencies`, `setup`
7. Submit the issue
8. Repeat for PBI-02 through PBI-11

### Option 2: Automated Issue Creation

```bash
# Requires GitHub CLI (https://cli.github.com/)
# Authenticate first: gh auth login

cd /path/to/QrReader
./.github/ISSUES/create-issues.sh
```

## Implementation Roadmap

### Phase 1: Foundation (2-3 days, 7 story points)
- **PBI #1**: Add ML Kit GenAI dependency
- **PBI #2**: Create GeminiNanoService for AI operations

### Phase 2: Domain Layer (3-4 days, 11 story points)
- **PBI #3**: Define domain models and repository interface
- **PBI #4**: Implement use cases
- **PBI #5**: Implement repository

### Phase 3: Integration (4-5 days, 13 story points)
- **PBI #6**: Camera feature integration
- **PBI #7**: Code creator feature integration
- **PBI #8**: Settings toggle

### Phase 4: Quality & Documentation (3-4 days, 10 story points)
- **PBI #9**: Unit tests
- **PBI #10**: UI/instrumentation tests
- **PBI #11**: Documentation updates

**Total Estimated Time**: 12-16 days (single developer)

## Key Features

### What This Adds to Your App

✨ **AI-Powered Tag Suggestions**
- Automatic tag generation when scanning QR codes
- Smart suggestions when creating QR codes
- 3-5 relevant, context-aware tags per code
- On-device processing for privacy

🔒 **Privacy First**
- All AI processing happens locally
- No data sent to cloud servers
- Works offline after initial model download

⚙️ **User Control**
- Toggle to enable/disable feature
- Device compatibility detection
- Graceful degradation if unavailable

## Technical Architecture

```
┌─────────────────────────────────────────────────────┐
│                 Presentation Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │   Camera     │  │ Code Creator │  │ Settings  │ │
│  │  ViewModel   │  │   ViewModel  │  │ ViewModel │ │
│  └──────────────┘  └──────────────┘  └───────────┘ │
└─────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│  ┌────────────────────────────────────────────────┐ │
│  │  GenerateTagSuggestionsUseCase                 │ │
│  │  CheckGeminiNanoAvailabilityUseCase            │ │
│  └────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────┐ │
│  │  TagSuggestionRepository (interface)           │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│                   Data Layer                         │
│  ┌────────────────────────────────────────────────┐ │
│  │  TagSuggestionRepositoryImpl                   │ │
│  └────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────┐ │
│  │  GeminiNanoService                             │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│               ML Kit GenAI / Gemini Nano             │
│              (On-Device AI Processing)               │
└─────────────────────────────────────────────────────┘
```

## Device Requirements

### Minimum Requirements
- Android 10 (API 29) or higher
- App will run, feature gracefully disabled if AI unavailable

### For AI Features
- Pixel 9, Pixel 10, or compatible devices
- Tensor G3/G4 chipset or high-end Snapdragon/MediaTek
- ~100-200MB storage for Gemini Nano model
- Check device support at runtime

## Development Guidelines

### Code Standards
✅ Follow existing Clean Architecture patterns
✅ Use Kotlin coroutines for async operations
✅ Inject dependencies via Koin
✅ Write comprehensive tests (>80% coverage)
✅ Add KDoc to all public APIs
✅ Follow Material3 design patterns

### Testing Strategy
- **Unit Tests**: Mock ML Kit, test business logic
- **Integration Tests**: Test repository → service flow
- **UI Tests**: Test user interactions, loading states
- **Device Tests**: Verify on real devices with Gemini Nano

### Prompt Engineering Tips
```kotlin
// Good prompt structure for tag suggestions
val prompt = """
Given a QR code containing: "$barcodeContent"
Type: ${getBarcodeTypeName(barcodeType)}

Suggest 3-5 short, relevant tags (1-2 words each) that categorize this content.
Return only the tags as a comma-separated list.

Examples:
- URL to shopping site: "shopping, online, retail"
- Contact information: "contact, personal, phone"
- Product barcode: "product, inventory, retail"

Tags:
""".trimIndent()
```

## Success Metrics

### Technical Metrics
- ✓ Build passes with new dependencies
- ✓ All tests pass (unit + instrumentation)
- ✓ Code coverage >80% for new code
- ✓ No regressions in existing features

### User Experience Metrics
- ✓ Tag suggestions appear in <2 seconds
- ✓ Feature available to 80%+ of users (device support)
- ✓ Graceful fallback when unavailable
- ✓ User can toggle feature on/off

### Quality Metrics
- ✓ Suggestions are relevant (manual review)
- ✓ Works with all barcode types
- ✓ No crashes or ANRs
- ✓ Follows Android best practices

## Resources & References

### Google Documentation
- [ML Kit GenAI APIs](https://developer.android.com/ai/gemini-nano/ml-kit-genai)
- [Gemini Nano Overview](https://developer.android.com/ai/gemini-nano)
- [Prompt API Guide](https://android-developers.googleblog.com/2025/10/ml-kit-genai-prompt-api-alpha-release.html)

### Project Documentation
- `AGENTS.md` - AI assistant guidelines
- `README.md` - Project overview
- `TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md` - Detailed technical plan

### External Resources
- [ML Kit on Maven Repository](https://mvnrepository.com/artifact/com.google.mlkit/genai)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)

## Risk Mitigation

### High Risk: Device Compatibility
**Risk**: Not all devices support Gemini Nano
**Mitigation**: 
- Feature detection at runtime
- Graceful degradation
- Clear user messaging
- Toggle to disable feature

### Medium Risk: AI Quality
**Risk**: Suggestions might not be relevant
**Mitigation**:
- Iterate on prompt engineering
- User feedback mechanism (future)
- Allow manual tag entry always

### Low Risk: Performance
**Risk**: AI inference might be slow
**Mitigation**:
- Async processing
- Loading indicators
- 2-second timeout
- Caching common results

## Future Enhancements

After initial implementation, consider:
- 🎯 Learning from user's manual tag choices
- 🌍 Multi-language support for suggestions
- 📊 User feedback on suggestion quality
- 🤝 Suggesting tag combinations based on history
- 💾 Offline caching of common suggestions
- 🎨 Custom prompt templates per user

## Support & Questions

### Getting Help
1. Review the detailed implementation plan
2. Check AGENTS.md for project conventions
3. Review existing features for patterns
4. Open a discussion issue for clarification

### Contributing
- Follow the implementation order (dependencies matter)
- Write tests for all new code
- Update documentation as you go
- Request code review before merging

## Next Steps

1. ✅ **Review the implementation plan** - Read `TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md`
2. ✅ **Create GitHub issues** - Use templates in `.github/ISSUES/tag-suggestions/`
3. ✅ **Set up project board** - Organize issues by phase
4. ✅ **Assign issues** - Distribute work among team
5. ✅ **Start with PBI #1** - Add ML Kit dependency
6. ✅ **Follow the roadmap** - Complete phases in order
7. ✅ **Test thoroughly** - Verify on real devices
8. ✅ **Update docs** - Keep documentation current

---

## Questions?

If you need clarification on any aspect:
- Technical details → See `TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md`
- Code standards → See `AGENTS.md`
- GitHub issues → See `.github/ISSUES/tag-suggestions/README.md`

**Happy Coding! 🚀**
