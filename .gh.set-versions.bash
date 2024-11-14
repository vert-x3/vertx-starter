#!/bin/bash

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <stable-version> <stable-snapshot-version>"
    exit 1
fi

tmp=$(mktemp)
jq ".versions[0].number=\"$1\" | .defaults.vertxVersion=\"$1\" | .versions[1].number=\"$2\"" src/main/resources/starter.json > "$tmp"
mv "$tmp" src/main/resources/starter.json
