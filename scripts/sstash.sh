#!/bin/bash

# Ensure the correct usage
if [ "$#" -ne 0 ]; then
    echo "Usage: $0"
    exit 1
fi

echo "Starting SimpliStash..."

# Get the directory of the actual script
SCRIPT_DIR=$(dirname "$(realpath "$0")")

# Compile and run the server
mvn -q -f "$SCRIPT_DIR/../pom.xml" compile
mvn -q -f "$SCRIPT_DIR/../pom.xml" exec:java -Pserver

