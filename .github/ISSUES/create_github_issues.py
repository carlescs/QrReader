#!/usr/bin/env python3
"""
GitHub Issues Creator for Tag Suggestions Feature
Creates 11 Product Backlog Items (PBIs) as GitHub issues using the GitHub API.

Requirements:
    - Python 3.6+
    - requests library: pip install requests
    - GitHub Personal Access Token with 'repo' scope

Usage:
    export GITHUB_TOKEN="your_github_token_here"
    python3 create_github_issues.py
"""

import os
import sys
import json
from pathlib import Path

try:
    import requests
except ImportError:
    print("Error: 'requests' library not found.")
    print("Install it with: pip install requests")
    sys.exit(1)


# Configuration
REPO_OWNER = "carlescs"
REPO_NAME = "QrReader"
ISSUES_DIR = Path(__file__).parent / "tag-suggestions"

# PBI files in order
PBI_FILES = [
    "PBI-01-dependencies.md",
    "PBI-02-gemini-service.md",
    "PBI-03-domain-models.md",
    "PBI-04-use-cases.md",
    "PBI-05-repository-impl.md",
    "PBI-06-camera-integration.md",
    "PBI-07-creator-integration.md",
    "PBI-08-settings-toggle.md",
    "PBI-09-unit-tests.md",
    "PBI-10-ui-tests.md",
    "PBI-11-documentation.md",
]


def get_github_token():
    """Get GitHub token from environment."""
    token = os.environ.get('GITHUB_TOKEN')
    if not token:
        print("Error: GITHUB_TOKEN environment variable not set.")
        print("\nTo create a token:")
        print("1. Go to https://github.com/settings/tokens")
        print("2. Click 'Generate new token (classic)'")
        print("3. Select 'repo' scope")
        print("4. Copy the token and run:")
        print("   export GITHUB_TOKEN='your_token_here'")
        sys.exit(1)
    return token


def parse_pbi_file(filepath):
    """Parse PBI markdown file to extract title, labels, and body."""
    with open(filepath, 'r') as f:
        content = f.read()
    
    lines = content.split('\n')
    
    # Extract title (first line, remove # prefix)
    title = lines[0].replace('# ', '').strip()
    
    # Extract labels from the Labels line
    labels = []
    for line in lines[:10]:  # Check first 10 lines
        if line.startswith('**Labels:**'):
            # Extract labels like: **Labels:** `enhancement`, `dependencies`, `setup`
            label_text = line.replace('**Labels:**', '').strip()
            # Extract text between backticks
            import re
            labels = re.findall(r'`([^`]+)`', label_text)
            break
    
    # Body is the entire content
    body = content
    
    return title, labels, body


def create_github_issue(token, title, body, labels):
    """Create a GitHub issue using the API."""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/issues"
    
    headers = {
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github.v3+json",
    }
    
    data = {
        "title": title,
        "body": body,
        "labels": labels,
    }
    
    response = requests.post(url, headers=headers, json=data)
    
    if response.status_code == 201:
        issue_data = response.json()
        return True, issue_data['html_url'], issue_data['number']
    else:
        return False, response.status_code, response.text


def check_repository_access(token):
    """Verify we can access the repository."""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}"
    headers = {
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github.v3+json",
    }
    
    response = requests.get(url, headers=headers)
    
    if response.status_code == 200:
        repo_data = response.json()
        print(f"✓ Successfully authenticated with GitHub API")
        print(f"✓ Repository: {repo_data['full_name']}")
        print(f"✓ Issues enabled: {repo_data['has_issues']}")
        return True
    else:
        print(f"✗ Unable to access repository: {response.status_code}")
        print(f"  {response.text}")
        return False


def create_label_if_not_exists(token, label_name, color, description=""):
    """Create a label if it doesn't exist."""
    # Check if label exists
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/labels/{label_name}"
    headers = {
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github.v3+json",
    }
    
    response = requests.get(url, headers=headers)
    
    if response.status_code == 200:
        return True  # Label already exists
    
    # Create label
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/labels"
    data = {
        "name": label_name,
        "color": color,
        "description": description,
    }
    
    response = requests.post(url, headers=headers, json=data)
    return response.status_code == 201


def setup_labels(token):
    """Create necessary labels for the project."""
    labels = [
        ("enhancement", "a2eeef", "New feature or request"),
        ("dependencies", "0366d6", "Pull requests that update a dependency file"),
        ("setup", "d4c5f9", "Initial setup and configuration"),
        ("core", "d93f0b", "Core functionality"),
        ("ml", "e99695", "Machine learning related"),
        ("domain", "0e8a16", "Domain layer changes"),
        ("architecture", "1d76db", "Architecture-related changes"),
        ("use-case", "5319e7", "Use case implementations"),
        ("data", "fbca04", "Data layer changes"),
        ("repository", "c5def5", "Repository implementations"),
        ("ui", "d876e3", "User interface changes"),
        ("feature", "0052cc", "New feature"),
        ("camera", "bfdadc", "Camera feature"),
        ("code-creator", "c2e0c6", "Code creator feature"),
        ("settings", "f9d0c4", "Settings feature"),
        ("testing", "fef2c0", "Testing related"),
        ("quality", "d4c5f9", "Code quality"),
        ("unit-tests", "e4e669", "Unit testing"),
        ("instrumentation", "c5def5", "Instrumentation testing"),
        ("documentation", "0075ca", "Documentation updates"),
    ]
    
    print("\n📋 Setting up labels...")
    for label_name, color, description in labels:
        if create_label_if_not_exists(token, label_name, color, description):
            print(f"  ✓ Label '{label_name}' ready")
        else:
            print(f"  ⚠ Could not create label '{label_name}'")


def main():
    """Main function to create all GitHub issues."""
    print("=" * 60)
    print("GitHub Issues Creator for Tag Suggestions Feature")
    print("=" * 60)
    print()
    
    # Get GitHub token
    token = get_github_token()
    
    # Verify repository access
    if not check_repository_access(token):
        sys.exit(1)
    
    # Setup labels
    setup_labels(token)
    
    print("\n📝 Creating issues from PBI files...")
    print()
    
    created_issues = []
    failed_issues = []
    
    for pbi_file in PBI_FILES:
        filepath = ISSUES_DIR / pbi_file
        
        if not filepath.exists():
            print(f"⚠ File not found: {filepath}")
            failed_issues.append((pbi_file, "File not found"))
            continue
        
        # Parse PBI file
        title, labels, body = parse_pbi_file(filepath)
        
        print(f"Creating: {title}")
        print(f"  Labels: {', '.join(labels)}")
        
        # Create issue
        success, result, extra = create_github_issue(token, title, body, labels)
        
        if success:
            print(f"  ✓ Created: {result} (#{extra})")
            created_issues.append((title, result, extra))
        else:
            print(f"  ✗ Failed: {result}")
            print(f"    {extra}")
            failed_issues.append((title, f"API error: {result}"))
        
        print()
    
    # Summary
    print("=" * 60)
    print("Summary")
    print("=" * 60)
    print(f"✓ Created: {len(created_issues)} issues")
    print(f"✗ Failed:  {len(failed_issues)} issues")
    print()
    
    if created_issues:
        print("Created issues:")
        for title, url, number in created_issues:
            print(f"  #{number}: {title}")
            print(f"         {url}")
        print()
    
    if failed_issues:
        print("Failed issues:")
        for title, reason in failed_issues:
            print(f"  - {title}: {reason}")
        print()
    
    print(f"View all issues: https://github.com/{REPO_OWNER}/{REPO_NAME}/issues")
    print()


if __name__ == "__main__":
    main()
