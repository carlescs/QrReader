#!/bin/bash
# Validate Version Code Offset
#
# This script helps validate that the BASE_VERSION_CODE_OFFSET in GitVersioning.kt
# is correct for the current repository state and Google Play version.
#
# Usage:
#   ./scripts/validate_version_offset.sh [expected_play_store_version]
#
# Examples:
#   ./scripts/validate_version_offset.sh 367
#   ./scripts/validate_version_offset.sh
#
# If no argument is provided, it will show the current calculation and prompt for validation.

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get current directory (script location)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo -e "${BLUE}=== Version Code Offset Validator ===${NC}"
echo ""

# Get current git commit count
cd "$PROJECT_ROOT"
COMMIT_COUNT=$(git rev-list --count HEAD 2>/dev/null || echo "0")

if [ "$COMMIT_COUNT" = "0" ]; then
    echo -e "${RED}Error: Failed to get git commit count${NC}"
    echo "Make sure you're in a git repository with commits"
    exit 1
fi

echo -e "${GREEN}Current git commit count:${NC} $COMMIT_COUNT"

# Extract current BASE_VERSION_CODE_OFFSET from GitVersioning.kt
VERSIONING_FILE="$PROJECT_ROOT/buildSrc/src/main/kotlin/GitVersioning.kt"
if [ ! -f "$VERSIONING_FILE" ]; then
    echo -e "${RED}Error: GitVersioning.kt not found at: $VERSIONING_FILE${NC}"
    exit 1
fi

CURRENT_OFFSET=$(grep -oP 'BASE_VERSION_CODE_OFFSET\s*=\s*\K\d+' "$VERSIONING_FILE" || echo "")
if [ -z "$CURRENT_OFFSET" ]; then
    echo -e "${RED}Error: Could not extract BASE_VERSION_CODE_OFFSET from GitVersioning.kt${NC}"
    exit 1
fi

echo -e "${GREEN}Current BASE_VERSION_CODE_OFFSET:${NC} $CURRENT_OFFSET"

# Calculate what version code will be produced
CALCULATED_VERSION=$((COMMIT_COUNT + CURRENT_OFFSET))
echo -e "${GREEN}Calculated version code:${NC} $CALCULATED_VERSION (commit count + offset)"
echo ""

# If Play Store version provided as argument, validate against it
if [ -n "$1" ]; then
    EXPECTED_PLAY_VERSION="$1"
    echo -e "${BLUE}Expected Play Store version:${NC} $EXPECTED_PLAY_VERSION"
    echo ""
    
    if [ "$CALCULATED_VERSION" -eq "$EXPECTED_PLAY_VERSION" ]; then
        echo -e "${GREEN}✓ VALID:${NC} Calculated version matches expected Play Store version"
        echo ""
        echo "Next commit will produce version: $((CALCULATED_VERSION + 1))"
        exit 0
    elif [ "$CALCULATED_VERSION" -gt "$EXPECTED_PLAY_VERSION" ]; then
        echo -e "${YELLOW}⚠ WARNING:${NC} Calculated version ($CALCULATED_VERSION) is HIGHER than Play Store version ($EXPECTED_PLAY_VERSION)"
        echo ""
        echo "This is OK if you're preparing a new release, but verify this is intentional."
        echo "Next commit will produce version: $((CALCULATED_VERSION + 1))"
        exit 0
    else
        echo -e "${RED}✗ ERROR:${NC} Calculated version ($CALCULATED_VERSION) is LOWER than Play Store version ($EXPECTED_PLAY_VERSION)"
        echo ""
        echo "Google Play will reject uploads with lower version codes!"
        echo ""
        
        # Calculate correct offset
        CORRECT_OFFSET=$((EXPECTED_PLAY_VERSION - COMMIT_COUNT))
        echo -e "${YELLOW}Recommended fix:${NC}"
        echo "1. Update BASE_VERSION_CODE_OFFSET in $VERSIONING_FILE"
        echo "   From: $CURRENT_OFFSET"
        echo "   To:   $CORRECT_OFFSET"
        echo ""
        echo "2. Calculation:"
        echo "   new_offset = play_store_version - commit_count"
        echo "   new_offset = $EXPECTED_PLAY_VERSION - $COMMIT_COUNT = $CORRECT_OFFSET"
        echo ""
        echo "3. Verify:"
        echo "   version_code = commit_count + new_offset"
        echo "   version_code = $COMMIT_COUNT + $CORRECT_OFFSET = $EXPECTED_PLAY_VERSION"
        
        exit 1
    fi
else
    # No Play Store version provided - just show info
    echo -e "${BLUE}Current configuration will produce:${NC}"
    echo "  Version code: $CALCULATED_VERSION"
    echo "  Next version: $((CALCULATED_VERSION + 1))"
    echo ""
    echo -e "${YELLOW}To validate against Play Store:${NC}"
    echo "  $0 <play_store_version>"
    echo ""
    echo "Example:"
    echo "  $0 367"
    echo ""
    echo -e "${YELLOW}To recalculate offset for a different Play Store version:${NC}"
    echo "  new_offset = play_store_version - $COMMIT_COUNT"
    echo ""
    echo "Examples:"
    echo "  For Play Store version 367: offset = 367 - $COMMIT_COUNT = $((367 - COMMIT_COUNT))"
    echo "  For Play Store version 400: offset = 400 - $COMMIT_COUNT = $((400 - COMMIT_COUNT))"
fi
