# Tag Suggestions Feature - GitHub Issues

This directory contains Product Backlog Items (PBIs) for implementing the AI-powered tag suggestions feature using ML Kit GenAI with Gemini Nano.

## 🚀 Quick Start - Multiple Options

Choose the method that works best for you:

### Option 1: Python Script (Recommended - Automated)
```bash
# Install requirements
pip install requests

# Set your GitHub token
export GITHUB_TOKEN="your_token_here"

# Run the script
python3 ..create_github_issues.py
```

### Option 2: Bash Script (Alternative - Automated)
```bash
# Requires GitHub CLI (gh)
gh auth login
../create-issues.sh
```

### Option 3: Web Interface (Interactive)
Open `../create-issues.html` in your browser for a visual interface to create issues.

### Option 4: Manual (Step-by-Step)
See detailed instructions in `../MANUAL_ISSUE_CREATION.md`

## Issue Creation Instructions

### Automated Method (Recommended)

1. Get a GitHub Personal Access Token:
   - Go to https://github.com/settings/tokens
   - Click "Generate new token (classic)"
   - Select `repo` scope
   - Copy the token

2. Run the Python script:
   ```bash
   export GITHUB_TOKEN="your_token_here"
   python3 ../create_github_issues.py
   ```

The script will automatically:
- ✅ Create all necessary labels
- ✅ Create all 11 issues in order
- ✅ Apply proper labels to each issue
- ✅ Provide links to created issues

### Manual Method

1. Go to your GitHub repository: https://github.com/carlescs/QrReader/issues
2. Click "New Issue"
3. Copy the content from each PBI-XX-*.md file
4. Paste into the issue description
5. Apply the labels mentioned in each PBI
6. Set the priority/milestone as needed
7. Assign to appropriate team members

## Implementation Order

The PBIs should be implemented in this order due to dependencies:

```
Phase 1: Foundation & ML Kit Setup
├── PBI #1: Add ML Kit GenAI Dependency
└── PBI #2: Create GeminiNanoService

Phase 2: Domain Layer
├── PBI #3: Define Domain Models and Repository Interface
├── PBI #4: Implement Use Cases
└── PBI #5: Implement TagSuggestionRepository

Phase 3: Integration
├── PBI #6: Update Camera Feature with Tag Suggestions
├── PBI #7: Update Code Creator Feature with Tag Suggestions
└── PBI #8: Add Settings Toggle

Phase 4: Testing & Polish
├── PBI #9: Write Comprehensive Unit Tests
├── PBI #10: Add UI/Instrumentation Tests
└── PBI #11: Update Documentation
```

## Labels to Create

Create these labels in your GitHub repository:
- `enhancement` - Feature additions and improvements
- `dependencies` - Dependency-related changes
- `setup` - Initial setup and configuration
- `core` - Core functionality
- `ml` - Machine learning related
- `domain` - Domain layer changes
- `architecture` - Architecture-related changes
- `use-case` - Use case implementations
- `data` - Data layer changes
- `repository` - Repository implementations
- `ui` - User interface changes
- `feature` - New feature
- `camera` - Camera feature
- `code-creator` - Code creator feature
- `settings` - Settings feature
- `testing` - Testing related
- `quality` - Code quality
- `unit-tests` - Unit testing
- `instrumentation` - Instrumentation testing
- `documentation` - Documentation updates

## Story Points

Total: 41 story points
- High Priority: 31 points
- Medium Priority: 10 points

## Timeline Estimate

Assuming 1 story point = 2-3 hours of development:
- **Total time**: 12-16 days for a single developer
- **Phase 1**: 2-3 days
- **Phase 2**: 3-4 days
- **Phase 3**: 4-5 days
- **Phase 4**: 3-4 days

## Reference Documentation

For complete technical specifications, architecture details, and implementation guidelines, see:
- `../../../TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md` - Comprehensive implementation plan

## Notes

- Each PBI has detailed acceptance criteria and technical specifications
- All PBIs follow the project's Clean Architecture patterns
- Tests are a key part of the implementation
- Documentation should be updated as features are completed
- Consider device compatibility for Gemini Nano (Pixel 9+, etc.)

## Questions or Issues?

If you have questions about any PBI:
1. Review the detailed implementation plan
2. Check the AGENTS.md file for project conventions
3. Review existing similar features in the codebase
4. Open a discussion issue for clarification
