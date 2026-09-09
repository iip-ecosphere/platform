#!/bin/bash

USER_NAME="${USER:-osboxes}"
USER_HOME="/home/${USER_NAME}"
DESKTOP="${USER_HOME}/Desktop"

until [ -d "$DESKTOP" ]; do
    sleep 1
done

USER_GROUP="platform"
usermod -aG "$USER_GROUP" "$USER_NAME"

cp /root/Desktop/eclipse.desktop "$DESKTOP/"
cp /root/Desktop/firefox.desktop "$DESKTOP/"
cp /root/Desktop/vscode.desktop "$DESKTOP/"

mkdir -p "$DESKTOP/eclipse-workspace"
cp /root/Desktop/eclipse-workspace/impl.model \
   "$DESKTOP/eclipse-workspace/"

ln -sfn /opt/user/platform "$DESKTOP/platform"

chown -R "${USER_NAME}:${USER_NAME}" "$DESKTOP"
chown -h "${USER_NAME}:${USER_NAME}" "$DESKTOP/platform"

