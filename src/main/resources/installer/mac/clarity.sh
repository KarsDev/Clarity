#!/bin/bash

# Get the directory of the current script
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Execute the JAR file with any passed arguments
java/java.exe -jar "$SCRIPT_DIR/clarity.jar" "$@"
