#!/usr/bin/env bash
GO_SERVER_DIR=~/Library/Application\ Support/Go\ Server
LOG_FILE="$GO_SERVER_DIR/logs/go-server.log"
ARTIFACT_NAME=build-watcher-plugin
ARTIFACT_FILE="plugin/build/libs/$ARTIFACT_NAME-0.5.jar"

pkill -f "Go Server"
./gradlew ":$ARTIFACT_NAME:build" "$@" && \
    mkdir -p "$GO_SERVER_DIR/plugins/external/" && \
    rm -f "$GO_SERVER_DIR/plugins/external/$ARTIFACT_NAME*" && \
    cp -f "$ARTIFACT_FILE" "$GO_SERVER_DIR/plugins/external/" && \
    echo "Starting Go Server..." && \
    open /Applications/Go\ Server.app/ && \
    tail -n 0 -f "$LOG_FILE" -f "$GO_SERVER_DIR/logs/plugin-build-watcher.notifier.log"
