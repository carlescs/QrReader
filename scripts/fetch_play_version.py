#!/usr/bin/env python3
"""
Google Play Store Version Code Fetcher

This script fetches the highest version code currently in use across ALL
Google Play tracks for the QR Reader app, then returns that value + 1 as
the next safe version code to use.

Checking all tracks is important because a version code uploaded to alpha
or beta is still "used" by Google Play even if it has never reached
production. Using only the production track can cause "version code already
used" errors when the same code was previously uploaded to a test track.

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
  The next version code to use (max version across all tracks + 1)
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
# Check all tracks so we never reuse a version code that was uploaded to any track
ALL_TRACKS = ["internal", "alpha", "beta", "production"]


def fetch_latest_version_code():
    """
    Fetch the highest version code in use across ALL Google Play tracks.

    Checking every track (internal → alpha → beta → production) prevents
    "version code already used" errors that occur when a code was uploaded
    to a test track but never promoted to production.

    Returns:
        int: The highest version code found across all tracks (0 if none)

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
    
    # Create an edit (required for all read operations)
    edit_request = service.edits().insert(body={}, packageName=PACKAGE_NAME)
    edit_response = edit_request.execute()
    edit_id = edit_response['id']
    
    try:
        max_version_code = 0

        for track in ALL_TRACKS:
            try:
                track_response = service.edits().tracks().get(
                    editId=edit_id,
                    track=track,
                    packageName=PACKAGE_NAME
                ).execute()

                track_codes = [
                    int(vc)
                    for release in track_response.get('releases', [])
                    for vc in release.get('versionCodes', [])
                ]
                track_max = max(track_codes) if track_codes else 0
                if track_max > max_version_code:
                    max_version_code = track_max

                print(
                    f"Track '{track}': max version code = "
                    f"{track_max if track_codes else 'no releases'}",
                    file=sys.stderr
                )
            except Exception as track_err:
                # A 404 simply means this track has no releases yet; skip it.
                print(
                    f"Track '{track}': skipped ({track_err})",
                    file=sys.stderr
                )

        # Delete the edit (we're only reading, not making changes)
        service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
        
        return max_version_code
        
    except Exception as e:
        # Clean up edit if something went wrong
        try:
            service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
        except Exception:
            pass
        raise


def main():
    """Main entry point."""
    try:
        latest_version = fetch_latest_version_code()
        next_version = latest_version + 1
        
        # Output results
        print(f"Package: {PACKAGE_NAME}", file=sys.stderr)
        print(f"Tracks checked: {', '.join(ALL_TRACKS)}", file=sys.stderr)
        print(f"Highest version code across all tracks: {latest_version}", file=sys.stderr)
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
        return 1


if __name__ == '__main__':
    sys.exit(main())
