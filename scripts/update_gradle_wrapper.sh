#!/bin/bash

# find all files in ../* recursively and find all settings.gradle(.kts) files, in that folder. bump gradle wrapper to latest version

# Directory of where this script is located
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR" || exit

# find all settings.gradle(.kts) files
files=$(find ../* -type f -name "settings.gradle*")
for file in $files; do
  echo "Bumping gradle wrapper in $file"
  # find the directory of the file
  dir=$(dirname $file)
  # change to the directory
  cd $dir
  # bump the gradle wrapper
  ./gradlew wrapper --gradle-version latest
  ./gradlew updateDaemonJvm
  # change back to the original directory
  cd -
done