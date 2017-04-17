#!/usr/bin/env bash
GO_SERVER_DIR=~/Library/Application\ Support/Go\ Server
LOG_FILE="$GO_SERVER_DIR/logs/go-server.log"

./gradlew build && \
    killall java && \
    sleep 1 && \
    cp -f plugin/build/libs/policeman-plugin-0.1.jar "$GO_SERVER_DIR/plugins/external/" && \
    > "$LOG_FILE" && \
    echo "Starting Go Server..." && \
    open /Applications/Go\ Server.app/ && \
    tail -f "$LOG_FILE"
