#!/usr/bin/env bash

cleanup() {
    trap - INT TERM

    echo "Stopping all processes..."

    kill "$FRONTEND_PID" "$BACKEND_PID" 2>/dev/null

    wait "$FRONTEND_PID" "$BACKEND_PID" 2>/dev/null

    exit 0
}

trap cleanup INT TERM

(
    cd ./frontend/Jass || exit 1
    npm run dev
) &
FRONTEND_PID=$!

(
    cd ./backend || exit 1
    ./run.sh
) &
BACKEND_PID=$!

wait