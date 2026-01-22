#!/bin/bash

set -e

if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <old-stable-version> <current-stable-version> <snapshot-version>"
    exit 1
fi

tmp=$(mktemp)
jq ".versions[0].number=\"$1\" | .versions[1].number=\"$2\" | .defaults.vertxVersion=\"$2\" | .versions[2].number=\"$3\"" src/main/resources/starter.json > "$tmp"
mv "$tmp" src/main/resources/starter.json
