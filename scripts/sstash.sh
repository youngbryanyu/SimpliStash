#!/bin/bash

# Ensure the correct usage
if [ "$#" -ne 0 ]; then
    echo "Usage: $0"
    exit 1
fi

# Pass the IP and port as system properties to Maven
mvn -f "$(dirname "$0")/../pom.xml" exec:java -Pserver
