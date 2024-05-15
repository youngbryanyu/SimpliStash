#!/bin/bash

# Absolute path to the directory containing this script
REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Path to the server and CLI scripts
SSTASH_SCRIPT="$REPO_DIR/scripts/sstash.sh"
SSTASH_CLI_SCRIPT="$REPO_DIR/scripts/sstash-cli.sh"

# Destination for symbolic links
DEST_DIR="/usr/local/bin"

# Create symbolic links
ln -sf "$SSTASH_SCRIPT" "$DEST_DIR/sstash"
ln -sf "$SSTASH_CLI_SCRIPT" "$DEST_DIR/sstash-cli"

# Echo to user
echo "Symbolic links created:
- sstash -> $SSTASH_SCRIPT
- sstash-cli -> $SSTASH_CLI_SCRIPT"
echo "You can now run 'sstash' and 'sstash-cli' from anywhere."
