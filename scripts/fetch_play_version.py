#!/usr/bin/env python3
"""
Google Play Store Version Code Fetcher

This script fetches the latest version code from Google Play Store
for the QR Reader app. It can be used as an alternative to the static
offset approach.

Requirements:
  pip install google-api-python-client google-auth

Setup:
  1. Create a service account in Google Cloud Console
  2. Enable Google Play Developer API
  3. Download service account JSON key
  4. Grant service account access to Play Console
  5. Save as 'service-account.json' in project root

Usage:
  python3 scripts/fetch_play_version.py
  
Returns:
  The next version code to use (current Play Store version + 1)
"""

import sys
import json
from pathlib import Path

try:
    from googleapiclient.discovery import build
    from google.oauth2 import service_account
except ImportError:
    print("Error: Required packages not installed", file=sys.stderr)
    print("Install with: pip install google-api-python-client google-auth", file=sys.stderr)
    sys.exit(1)

# Configuration
PACKAGE_NAME = "cat.company.qrreader"
CREDENTIALS_FILE = "service-account.json"
TRACK = "production"  # or "alpha", "beta", "internal"


def fetch_latest_version_code():
    """
    Fetch the latest version code from Google Play Store.
    
    Returns:
        int: The latest version code in the specified track
    
    Raises:
        FileNotFoundError: If credentials file doesn't exist
        Exception: For API errors
    """
    # Check credentials file exists
    creds_path = Path(CREDENTIALS_FILE)
    if not creds_path.exists():
        raise FileNotFoundError(
            f"Credentials file not found: {CREDENTIALS_FILE}\n"
            "Please create a service account and download the JSON key."
        )
    
    # Authenticate
    credentials = service_account.Credentials.from_service_account_file(
        str(creds_path),
        scopes=['https://www.googleapis.com/auth/androidpublisher']
    )
    
    # Build the API client
    service = build('androidpublisher', 'v3', credentials=credentials)
    
    # Create an edit (required for all operations)
    edit_request = service.edits().insert(body={}, packageName=PACKAGE_NAME)
    edit_response = edit_request.execute()
    edit_id = edit_response['id']
    
    try:
        # Get the track information
        track_response = service.edits().tracks().get(
            editId=edit_id,
            track=TRACK,
            packageName=PACKAGE_NAME
        ).execute()
        
        # Extract version codes from all releases in the track
        version_codes = []
        for release in track_response.get('releases', []):
            release_version_codes = release.get('versionCodes', [])
            # Convert string version codes to integers
            version_codes.extend([int(vc) for vc in release_version_codes])
        
        # Delete the edit (we're just reading, not making changes)
        service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
        
        if not version_codes:
            print(f"Warning: No version codes found in {TRACK} track", file=sys.stderr)
            return 0
        
        return max(version_codes)
        
    except Exception as e:
        # Clean up edit if something went wrong
        try:
            service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
        except:
            pass
        raise


def main():
    """Main entry point."""
    try:
        latest_version = fetch_latest_version_code()
        next_version = latest_version + 1
        
        # Output results
        print(f"Package: {PACKAGE_NAME}", file=sys.stderr)
        print(f"Track: {TRACK}", file=sys.stderr)
        print(f"Latest version code: {latest_version}", file=sys.stderr)
        print(f"Next version code: {next_version}", file=sys.stderr)
        
        # Output next version to stdout (for scripting)
        print(next_version)
        
        return 0
        
    except FileNotFoundError as e:
        print(f"Error: {e}", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"Error fetching version code: {e}", file=sys.stderr)
        print("\nTroubleshooting:", file=sys.stderr)
        print("  1. Verify service account has 'Release Manager' role in Play Console", file=sys.stderr)
        print("  2. Check that Google Play Developer API is enabled", file=sys.stderr)
        print("  3. Ensure package name is correct", file=sys.stderr)
        print(f"  4. Verify '{TRACK}' track exists and has releases", file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
