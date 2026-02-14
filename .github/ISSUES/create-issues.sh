#!/bin/bash
# Script to create GitHub issues from PBI markdown files
# Requires: gh CLI authenticated with appropriate permissions

set -e

REPO="carlescs/QrReader"
ISSUES_DIR=".github/ISSUES/tag-suggestions"

echo "================================================"
echo "Creating GitHub Issues for Tag Suggestions Feature"
echo "Repository: $REPO"
echo "================================================"
echo ""

# Check if gh CLI is installed and authenticated
if ! command -v gh &> /dev/null; then
    echo "Error: GitHub CLI (gh) is not installed."
    echo "Install it from: https://cli.github.com/"
    exit 1
fi

if ! gh auth status &> /dev/null; then
    echo "Error: GitHub CLI is not authenticated."
    echo "Run: gh auth login"
    exit 1
fi

# Array of PBI files in order
PBI_FILES=(
    "PBI-01-dependencies.md"
    "PBI-02-gemini-service.md"
    "PBI-03-domain-models.md"
    "PBI-04-use-cases.md"
    "PBI-05-repository-impl.md"
    "PBI-06-camera-integration.md"
    "PBI-07-creator-integration.md"
    "PBI-08-settings-toggle.md"
    "PBI-09-unit-tests.md"
    "PBI-10-ui-tests.md"
    "PBI-11-documentation.md"
)

# Labels to add (create them if they don't exist)
LABELS="enhancement"

echo "Creating issues..."
echo ""

for pbi_file in "${PBI_FILES[@]}"; do
    filepath="$ISSUES_DIR/$pbi_file"
    
    if [ ! -f "$filepath" ]; then
        echo "Warning: File not found: $filepath"
        continue
    fi
    
    # Extract title (first line after removing # prefix)
    title=$(head -n 1 "$filepath" | sed 's/^# //')
    
    # Get the entire file content as body
    body=$(cat "$filepath")
    
    echo "Creating issue: $title"
    
    # Create the issue
    issue_url=$(gh issue create \
        --repo "$REPO" \
        --title "$title" \
        --body "$body" \
        --label "$LABELS")
    
    if [ $? -eq 0 ]; then
        echo "✓ Created: $issue_url"
    else
        echo "✗ Failed to create issue: $title"
    fi
    
    echo ""
    
    # Small delay to avoid rate limiting
    sleep 1
done

echo "================================================"
echo "Issue creation complete!"
echo "================================================"
echo ""
echo "Next steps:"
echo "1. Review the created issues on GitHub"
echo "2. Add story points and priorities as needed"
echo "3. Assign issues to team members"
echo "4. Create a project board to track progress"
echo ""
echo "View all issues: https://github.com/$REPO/issues"
