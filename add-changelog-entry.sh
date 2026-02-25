#!/bin/bash
set -e

echo -e "\\033[36m1. Ticket number of the corresponding board ticket (e.g. BC-201):\\033[0m"
read -rp " > " TICKET

echo -e "\\033[36m2. Title of the entry (e.g. "Fixed an issue with..."):\\033[0m"
read -rp " > " TITLE

echo -e "\\033[36m3. Type:\\033[0m"
TYPE=$(gum choose "fix" "story")

CREATED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

ESC_TICKET=$(printf '%s' "$TICKET" | jq -s -R .)
ESC_TITLE=$(printf '%s' "$TITLE" | jq -s -R .)
ESC_TYPE=$(printf '%s' "$TYPE" | jq -s -R .)

cat > ".changelog/${TICKET}.json" << EOF
{"ticket": $ESC_TICKET, "title": $ESC_TITLE, "type": $ESC_TYPE, "createdAt": "$CREATED_AT"}
EOF

echo "Entry saved (.changelog/${TICKET}.json)"
