#!/bin/bash

# Ensure the correct usage
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <ip> <port>"
    exit 1
fi

# Get the directory of the actual script
SCRIPT_DIR=$(dirname "$(realpath "$0")")

# Compile and run the CLI
mvn -q -f "$SCRIPT_DIR/../pom.xml" compile
mvn -q -f "$SCRIPT_DIR/../pom.xml" exec:java -Pcli -Dip=$1 -Dport=$2