#!/usr/bin/env bash

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
BIN="$ROOT/bin"
LIB="$ROOT/lib"

mkdir -p "$BIN"
mkdir -p "$LIB"

if [ ! -f "$LIB/json-20250107.jar" ]; then
    curl -L -o "$LIB/json-20250107.jar" \
      https://repo1.maven.org/maven2/org/json/json/20250107/json-20250107.jar
fi

if [ ! -f "$LIB/junit-platform-console-standalone-1.11.0.jar" ]; then
    curl -L -o "$LIB/junit-platform-console-standalone-1.11.0.jar" \
      https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.0/junit-platform-console-standalone-1.11.0.jar
fi

javac \
  -cp "$LIB/json-20250107.jar:$LIB/junit-platform-console-standalone-1.11.0.jar" \
  -d "$BIN" \
  $(find "$ROOT/src" -name "*.java")