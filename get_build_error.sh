#!/data/data/com.termux/files/usr/bin/bash
# Fetches the latest GitHub Actions build log and prints just the
# important error lines, right here in the terminal.
# Run from inside Alert_project:
#   bash get_build_error.sh
set -e

echo "Looking up your saved GitHub login..."

# Ask git for the token it already has cached from your earlier pushes.
CRED_OUTPUT=$(printf "protocol=https\nhost=github.com\n" | git credential fill 2>/dev/null || true)
TOKEN=$(echo "$CRED_OUTPUT" | grep "^password=" | cut -d= -f2-)

if [ -z "$TOKEN" ]; then
    echo ""
    echo "Couldn't find a saved login automatically."
    echo "Paste your GitHub token (the one starting with ghp_) and press enter:"
    read -r TOKEN
fi

if [ -z "$TOKEN" ]; then
    echo "No token provided. Stopping."
    exit 1
fi

OWNER="blin-arttina"
REPO="Alert_project"

echo "Finding the most recent build..."
RUN_JSON=$(curl -s -H "Authorization: Bearer $TOKEN" \
    "https://api.github.com/repos/$OWNER/$REPO/actions/runs?per_page=1")

RUN_ID=$(echo "$RUN_JSON" | grep -m1 '"id":' | head -1 | sed 's/[^0-9]//g')
CONCLUSION=$(echo "$RUN_JSON" | grep -m1 '"conclusion":' | sed 's/.*"conclusion": *"\([^"]*\)".*/\1/')
STATUS=$(echo "$RUN_JSON" | grep -m1 '"status":' | sed 's/.*"status": *"\([^"]*\)".*/\1/')

if [ -z "$RUN_ID" ]; then
    echo "Could not find a build run. Check the token and try again."
    exit 1
fi

echo "Latest build: run $RUN_ID, status=$STATUS, conclusion=$CONCLUSION"

if [ "$STATUS" = "in_progress" ] || [ "$STATUS" = "queued" ]; then
    echo ""
    echo "This build is still running. Wait a minute or two and run this script again."
    exit 0
fi

if [ "$CONCLUSION" = "success" ]; then
    echo ""
    echo "GOOD NEWS: the latest build SUCCEEDED. Nothing to fix!"
    exit 0
fi

echo "Downloading the job list..."
JOBS_JSON=$(curl -s -H "Authorization: Bearer $TOKEN" \
    "https://api.github.com/repos/$OWNER/$REPO/actions/runs/$RUN_ID/jobs")

JOB_ID=$(echo "$JOBS_JSON" | grep -m1 '"id":' | head -1 | sed 's/[^0-9]//g')

echo "Downloading the log for job $JOB_ID..."
LOG_FILE="build_error_log.txt"
curl -s -L -H "Authorization: Bearer $TOKEN" \
    "https://api.github.com/repos/$OWNER/$REPO/actions/jobs/$JOB_ID/logs" \
    -o "$LOG_FILE"

echo ""
echo "===================================================="
echo "ERROR SUMMARY (the important part is usually here):"
echo "===================================================="
echo ""

# Pull out the most useful lines: the "what went wrong" section,
# any line with "error:", and the final failure line.
grep -n -A 15 "What went wrong" "$LOG_FILE" | head -40 || true
echo ""
echo "---- Compiler error lines ----"
grep -n -i "error:" "$LOG_FILE" | head -20 || true

echo ""
echo "===================================================="
echo "Full log saved to: $LOG_FILE"
echo "You can copy/paste the text above back into the chat."
echo "===================================================="
