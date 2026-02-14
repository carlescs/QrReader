# How to Create GitHub Issues for Tag Suggestions Feature

This guide provides multiple methods to create the 11 Product Backlog Items (PBIs) as GitHub issues.

## Quick Links

- **Repository**: https://github.com/carlescs/QrReader
- **Issues Page**: https://github.com/carlescs/QrReader/issues
- **New Issue**: https://github.com/carlescs/QrReader/issues/new

---

## Method 1: Using Python Script (Recommended)

### Prerequisites

1. **Python 3.6+** installed
2. **requests library**: 
   ```bash
   pip install requests
   ```
3. **GitHub Personal Access Token** with `repo` scope

### Create GitHub Token

1. Go to https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Give it a name: "QrReader Issue Creator"
4. Select scope: **`repo`** (Full control of private repositories)
5. Click **"Generate token"**
6. Copy the token (you won't see it again!)

### Run the Script

```bash
# Set your GitHub token
export GITHUB_TOKEN="ghp_your_token_here"

# Navigate to the repository
cd /path/to/QrReader

# Run the Python script
python3 .github/ISSUES/create_github_issues.py
```

The script will:
- ✅ Verify GitHub API access
- ✅ Create necessary labels automatically
- ✅ Create all 11 issues in order
- ✅ Provide summary with links to created issues

---

## Method 2: Using GitHub CLI (Alternative)

### Prerequisites

1. **GitHub CLI** installed: https://cli.github.com/
2. Authenticated: `gh auth login`

### Run the Bash Script

```bash
cd /path/to/QrReader
./.github/ISSUES/create-issues.sh
```

---

## Method 3: Manual Creation (Step-by-Step)

If automated tools don't work, follow these steps:

### Step 1: Create Labels (One-Time Setup)

Go to https://github.com/carlescs/QrReader/labels and create these labels:

| Label Name | Color | Description |
|------------|-------|-------------|
| enhancement | #a2eeef | New feature or request |
| dependencies | #0366d6 | Dependency updates |
| setup | #d4c5f9 | Initial setup and configuration |
| core | #d93f0b | Core functionality |
| ml | #e99695 | Machine learning related |
| domain | #0e8a16 | Domain layer changes |
| architecture | #1d76db | Architecture-related changes |
| use-case | #5319e7 | Use case implementations |
| data | #fbca04 | Data layer changes |
| repository | #c5def5 | Repository implementations |
| ui | #d876e3 | User interface changes |
| feature | #0052cc | New feature |
| camera | #bfdadc | Camera feature |
| code-creator | #c2e0c6 | Code creator feature |
| settings | #f9d0c4 | Settings feature |
| testing | #fef2c0 | Testing related |
| quality | #d4c5f9 | Code quality |
| unit-tests | #e4e669 | Unit testing |
| instrumentation | #c5def5 | Instrumentation testing |
| documentation | #0075ca | Documentation updates |

### Step 2: Create Each Issue

For each PBI file, create a new issue:

#### Issue 1: PBI #1

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #1: Add ML Kit GenAI Dependency and Configuration`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-01-dependencies.md`
4. **Labels**: `enhancement`, `dependencies`, `setup`
5. Click **"Submit new issue"**

#### Issue 2: PBI #2

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #2: Create GeminiNanoService for On-Device AI`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-02-gemini-service.md`
4. **Labels**: `enhancement`, `core`, `ml`
5. Click **"Submit new issue"**

#### Issue 3: PBI #3

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #3: Define Domain Models and Repository Interface`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-03-domain-models.md`
4. **Labels**: `enhancement`, `domain`, `architecture`
5. Click **"Submit new issue"**

#### Issue 4: PBI #4

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #4: Implement Use Cases for Tag Suggestions`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-04-use-cases.md`
4. **Labels**: `enhancement`, `domain`, `use-case`
5. Click **"Submit new issue"**

#### Issue 5: PBI #5

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #5: Implement TagSuggestionRepository`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-05-repository-impl.md`
4. **Labels**: `enhancement`, `data`, `repository`
5. Click **"Submit new issue"**

#### Issue 6: PBI #6

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #6: Update Camera Feature with Tag Suggestions`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-06-camera-integration.md`
4. **Labels**: `enhancement`, `ui`, `feature`, `camera`
5. Click **"Submit new issue"**

#### Issue 7: PBI #7

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #7: Update Code Creator Feature with Tag Suggestions`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-07-creator-integration.md`
4. **Labels**: `enhancement`, `ui`, `feature`, `code-creator`
5. Click **"Submit new issue"**

#### Issue 8: PBI #8

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #8: Add Settings Toggle for Tag Suggestions`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-08-settings-toggle.md`
4. **Labels**: `enhancement`, `settings`, `ui`
5. Click **"Submit new issue"**

#### Issue 9: PBI #9

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #9: Write Comprehensive Unit Tests`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-09-unit-tests.md`
4. **Labels**: `testing`, `quality`, `unit-tests`
5. Click **"Submit new issue"**

#### Issue 10: PBI #10

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #10: Add UI/Instrumentation Tests`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-10-ui-tests.md`
4. **Labels**: `testing`, `ui`, `instrumentation`
5. Click **"Submit new issue"**

#### Issue 11: PBI #11

1. Go to: https://github.com/carlescs/QrReader/issues/new
2. **Title**: `PBI #11: Update Documentation`
3. **Body**: Copy entire content from `.github/ISSUES/tag-suggestions/PBI-11-documentation.md`
4. **Labels**: `documentation`
5. Click **"Submit new issue"**

---

## Method 4: Using GitHub Web Interface with Copy-Paste

### Quick Steps for Each Issue

1. Open two browser tabs:
   - Tab 1: GitHub repository file browser at `.github/ISSUES/tag-suggestions/`
   - Tab 2: https://github.com/carlescs/QrReader/issues/new

2. For each PBI file:
   - In Tab 1: Click on the PBI file, click "Raw" button, select all (Ctrl+A), copy (Ctrl+C)
   - In Tab 2: Paste into the issue body, add labels, submit
   - Open new issue for next PBI

---

## Method 5: Using curl (Advanced)

If you have a GitHub token, you can use curl:

```bash
#!/bin/bash
GITHUB_TOKEN="your_token_here"
REPO="carlescs/QrReader"

# Create an issue
curl -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/$REPO/issues \
  -d '{
    "title": "PBI #1: Add ML Kit GenAI Dependency and Configuration",
    "body": "Content from PBI-01-dependencies.md",
    "labels": ["enhancement", "dependencies", "setup"]
  }'
```

---

## After Creating Issues

### Optional: Create Project Board

1. Go to https://github.com/carlescs/QrReader/projects
2. Click **"New project"**
3. Choose **"Board"** template
4. Name it: "Tag Suggestions Implementation"
5. Add columns:
   - 📋 Backlog
   - 🏗️ In Progress
   - 🧪 Review/Testing
   - ✅ Done
6. Add all created issues to the board

### Optional: Set Milestones

1. Create milestone: "Tag Suggestions v1.0"
2. Assign all 11 issues to this milestone
3. Track progress at: https://github.com/carlescs/QrReader/milestones

### Optional: Assign to Team Members

1. Open each issue
2. Click "Assignees" on the right sidebar
3. Assign to appropriate developer

---

## Verification

After creating all issues, verify:

✅ All 11 issues created  
✅ Each issue has proper labels  
✅ Issue titles match PBI names  
✅ Issue descriptions are complete  
✅ Issues are numbered correctly  

View all issues: https://github.com/carlescs/QrReader/issues?q=is%3Aissue+PBI

---

## Troubleshooting

### "Permission denied" error
- Ensure you have write access to the repository
- Check your GitHub token has `repo` scope

### "Label does not exist" error
- Create labels first (see Step 1 in Method 3)
- Or use Python script which creates labels automatically

### Issues not appearing
- Check you're in the correct repository
- Refresh the issues page
- Check filters (make sure "Open" is selected)

### Script errors
- Verify Python 3.6+ is installed: `python3 --version`
- Install requests: `pip install requests`
- Check GITHUB_TOKEN is set: `echo $GITHUB_TOKEN`

---

## Quick Reference: PBI Summary

| PBI | Title | Labels | SP |
|-----|-------|--------|-----|
| #1 | Add ML Kit GenAI Dependency | enhancement, dependencies, setup | 2 |
| #2 | Create GeminiNanoService | enhancement, core, ml | 5 |
| #3 | Define Domain Models | enhancement, domain, architecture | 3 |
| #4 | Implement Use Cases | enhancement, domain, use-case | 3 |
| #5 | Implement Repository | enhancement, data, repository | 5 |
| #6 | Camera Integration | enhancement, ui, feature, camera | 5 |
| #7 | Creator Integration | enhancement, ui, feature, code-creator | 5 |
| #8 | Settings Toggle | enhancement, settings, ui | 3 |
| #9 | Unit Tests | testing, quality, unit-tests | 5 |
| #10 | UI Tests | testing, ui, instrumentation | 3 |
| #11 | Documentation | documentation | 2 |

**Total**: 41 Story Points (≈ 12-16 days)

---

## Support

For questions or issues:
1. Review the implementation plan: `TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md`
2. Check the architecture: `TAG_SUGGESTIONS_ARCHITECTURE.md`
3. Read the summary: `TAG_SUGGESTIONS_SUMMARY.md`
4. Open a discussion issue on GitHub

---

**Happy Issue Creating! 🚀**
