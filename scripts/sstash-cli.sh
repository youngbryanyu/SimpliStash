#!/bin/bash

# Ensure the correct usage
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <ip> <port>"
    exit 1
fi

# Pass the IP and port as system properties to Maven
mvn -f "$(dirname "$0")/../pom.xml" exec:java -Pcli -Dip=$1 -Dport=$2