#!/usr/bin/env bash
GO_SERVER_DIR=~/Library/Application\ Support/Go\ Server
LOG_FILE="$GO_SERVER_DIR/logs/go-server.log"

./gradlew build && \
    killall java && \
    sleep 1 && \
    mkdir -p "$GO_SERVER_DIR/plugins/external/" && \
    cp -f plugin/build/libs/policeman-plugin-0.1.jar "$GO_SERVER_DIR/plugins/external/" && \
    echo "Starting Go Server..." && \
    open /Applications/Go\ Server.app/ && \
    tail -n 0 -f "$LOG_FILE" -f "$GO_SERVER_DIR/logs/plugin-policeman.notifier.log"
