#!/usr/bin/env bash

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
BIN="$ROOT/bin"

mkdir -p "$BIN"

cleanup() {
    if [[ -n "${JAVA_PID:-}" ]]; then
        kill "$JAVA_PID" 2>/dev/null || true
        wait "$JAVA_PID" 2>/dev/null || true
    fi

    exit 0
}

trap cleanup INT TERM

javac \
  -cp "$ROOT/lib/json-20250107.jar:$ROOT/lib/junit-platform-console-standalone-1.11.0.jar" \
  -d "$BIN" \
  $(find "$ROOT" -name "*.java")

java \
  -cp "$BIN:$ROOT/lib/json-20250107.jar:$ROOT/lib/junit-platform-console-standalone-1.11.0.jar" \
  src.Main &

JAVA_PID=$!

wait "$JAVA_PID"