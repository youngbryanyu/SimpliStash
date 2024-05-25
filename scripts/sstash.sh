#!/bin/bash

# Ensure the correct usage
if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <primary_port> <read_only_port> [master_ip] [master_port]"
    exit 1
fi

echo "Starting SimpliStash..."

# Get the directory of the actual script
SCRIPT_DIR=$(dirname "$(realpath "$0")")

# Compile and run the server
mvn -q -f "$SCRIPT_DIR/../pom.xml" compile
mvn -q -f "$SCRIPT_DIR/../pom.xml" exec:java -Pserver -DprimaryPort=$1 -DreadOnlyPort=$2 -DmasterIp=$3 -DmasterPort=$4

