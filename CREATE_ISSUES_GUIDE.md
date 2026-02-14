# 🚀 Create GitHub Issues - Quick Start

This guide helps you create 11 Product Backlog Items (PBIs) as GitHub issues for the Tag Suggestions feature.

## TL;DR - Fastest Method

```bash
# 1. Install requirements
pip install requests

# 2. Get GitHub token from: https://github.com/settings/tokens
export GITHUB_TOKEN="ghp_your_token_here"

# 3. Run script
python3 .github/ISSUES/create_github_issues.py
```

Done! All 11 issues will be created automatically. ✅

---

## Choose Your Method

| Method | Difficulty | Speed | Best For |
|--------|------------|-------|----------|
| **Python Script** | Easy | Fast | Developers with Python |
| **Web Interface** | Very Easy | Medium | Non-technical users |
| **Bash Script** | Easy | Fast | GitHub CLI users |
| **Manual** | Easy | Slow | Learning or verification |

### Method 1: Python Script ⭐ Recommended

**File**: `.github/ISSUES/create_github_issues.py`

```bash
pip install requests
export GITHUB_TOKEN="ghp_your_token_here"
python3 .github/ISSUES/create_github_issues.py
```

✨ **Features**:
- Fully automated
- Creates labels automatically
- Creates all 11 issues
- Provides summary with links

### Method 2: Web Interface

**File**: `.github/ISSUES/create-issues.html`

1. Open `.github/ISSUES/create-issues.html` in your browser
2. Enter your GitHub token
3. Click "Create Issue" for each PBI

✨ **Features**:
- Visual interface
- Token verification
- Progress tracking
- No installation needed

### Method 3: Bash Script

**File**: `.github/ISSUES/create-issues.sh`

```bash
gh auth login
./.github/ISSUES/create-issues.sh
```

✨ **Features**:
- Uses GitHub CLI
- Command-line automation

### Method 4: Manual

**Guide**: `.github/ISSUES/MANUAL_ISSUE_CREATION.md`

Follow the comprehensive step-by-step guide to create issues manually by copy-pasting from PBI templates.

✨ **Features**:
- No technical setup
- Complete control
- Learn as you go

---

## What Gets Created

**11 GitHub Issues** (41 Story Points total):

1. **PBI #1**: Add ML Kit GenAI Dependency (2 SP)
2. **PBI #2**: Create GeminiNanoService (5 SP)
3. **PBI #3**: Define Domain Models (3 SP)
4. **PBI #4**: Implement Use Cases (3 SP)
5. **PBI #5**: Implement Repository (5 SP)
6. **PBI #6**: Camera Integration (5 SP)
7. **PBI #7**: Code Creator Integration (5 SP)
8. **PBI #8**: Settings Toggle (3 SP)
9. **PBI #9**: Unit Tests (5 SP)
10. **PBI #10**: UI Tests (3 SP)
11. **PBI #11**: Documentation (2 SP)

Each issue includes:
- ✅ Detailed acceptance criteria
- ✅ Technical implementation details
- ✅ Testing requirements
- ✅ Dependencies
- ✅ Code examples

---

## Get GitHub Token

1. Go to: https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Name: "QrReader Issue Creator"
4. Scope: Select **`repo`**
5. Click **"Generate token"**
6. Copy token (starts with `ghp_...`)

---

## After Creating Issues

### Optional: Create Project Board

1. Go to: https://github.com/carlescs/QrReader/projects
2. Create new board: "Tag Suggestions Implementation"
3. Add columns: Backlog → In Progress → Review → Done
4. Add all created issues to the board

### Optional: Set Milestone

1. Create milestone: "Tag Suggestions v1.0"
2. Assign all 11 issues to this milestone

### View Issues

https://github.com/carlescs/QrReader/issues

---

## Files Reference

```
.github/ISSUES/
├── create_github_issues.py      # Python automation
├── create-issues.sh              # Bash automation
├── create-issues.html            # Web interface
├── MANUAL_ISSUE_CREATION.md      # Step-by-step guide
└── tag-suggestions/
    ├── PBI-01-dependencies.md    # Issue template 1
    ├── PBI-02-gemini-service.md  # Issue template 2
    ├── ...                        # Templates 3-10
    └── PBI-11-documentation.md   # Issue template 11
```

---

## Troubleshooting

**"Permission denied"**
- Ensure you have write access to the repository
- Check token has `repo` scope

**"Label does not exist"**
- Python script creates labels automatically
- Manual method: Create labels first (see guide)

**Script errors**
- Check Python version: `python3 --version` (need 3.6+)
- Install requests: `pip install requests`
- Verify token: `echo $GITHUB_TOKEN`

---

## Need More Details?

- **Full Implementation Plan**: `TAG_SUGGESTIONS_IMPLEMENTATION_PLAN.md`
- **Quick Summary**: `TAG_SUGGESTIONS_SUMMARY.md`
- **Architecture**: `TAG_SUGGESTIONS_ARCHITECTURE.md`
- **Deliverables Overview**: `DELIVERABLES_SUMMARY.md`

---

**Ready? Pick a method above and create your issues! 🎯**
