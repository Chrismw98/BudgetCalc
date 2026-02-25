#!/bin/bash
set -e

echo -e "\\033[36m1. Version (e.g. 1.2.0):\\033[0m"
read -rp " > " VERSION

echo -e "\\033[36m2. When is the release planned? (YYYY-MM-DD or Enter for today):\\033[0m"
read -rp " > " DATE
[ -z "$DATE" ] && DATE=$(date +%Y-%m-%d)

# Validate/clean JSON files
for file in .changelog/*.json; do
  if ! jq empty "$file" 2>/dev/null; then
    echo "Warning: Invalid JSON in $file, skipping"
    rm "$file"
  fi
done

TYPES=$(jq -s -r '[.[] | select(type=="object") | .type] | unique[]' .changelog/*.json 2>/dev/null || echo "")

NEW_SECTION="## [$VERSION] - $DATE"
for type in $TYPES; do
  CAP_TYPE=$(echo "$type" | awk '{for(i=1;i<=length($0);i++) if(i==1) printf "%s", toupper(substr($0,i,1)); else printf "%s", substr($0,i,1); print ""}')
  ENTRIES=$(jq -s -r --arg t "$type" '[.[] | select(type=="object" and .type==$t) | "- **[\(.ticket)]** \(.title)"] | sort | .[]' .changelog/*.json 2>/dev/null || echo "")
  [ -n "$ENTRIES" ] && NEW_SECTION="$NEW_SECTION\\n### $CAP_TYPE\\n$ENTRIES"
done

if [ -f CHANGELOG.md ]; then
  cat CHANGELOG.md > /tmp/old.md
  echo -e "$NEW_SECTION" > CHANGELOG.md
  echo "" >> CHANGELOG.md
  cat /tmp/old.md >> CHANGELOG.md
  rm /tmp/old.md
else
  echo -e "$NEW_SECTION" > CHANGELOG.md
fi

rm -f .changelog/*.json

echo "CHANGELOG.md prepended!"
