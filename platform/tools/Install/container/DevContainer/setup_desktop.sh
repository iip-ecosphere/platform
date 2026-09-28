#!/bin/bash

USER_NAME="${USER:-osboxes}"

# Remove osboxes from privileged groups
gpasswd -d "$USER_NAME" sudo || true
gpasswd -d "$USER_NAME" admin || true

USER_HOME="/home/${USER_NAME}"
DESKTOP="${USER_HOME}/Desktop"


if [ ! -L "$DESKTOP/platform" ]; then

    until [ -d "$DESKTOP" ]; do
        sleep 1
    done

    USER_GROUP="platform"
    usermod -aG "$USER_GROUP" "$USER_NAME"

    # ======================================
    # Give access to Maven repository
    # Allow "$USER_GROUP" to traverse the path
    setfacl -m g:"$USER_GROUP":x /root
    setfacl -m g:"$USER_GROUP":x /root/.m2

    # Give "$USER_GROUP" read/write access to existing Maven repository contents
    setfacl -R -m g:"$USER_GROUP":rwX /root/.m2/repository

    # Make new files/directories inherit "$USER_GROUP"'s access
    find /root/.m2/repository -type d -exec setfacl -m d:g:"$USER_GROUP":rwX {} \;

    # Create "$USER_GROUP"'s Maven directory
    mkdir -p /home/"$USER_NAME"/.m2

    # Link "$USER_GROUP"'s Maven repository to root's repository
    ln -s /root/.m2/repository /home/"$USER_NAME"/.m2/repository

    # ======================================

    cp /root/Desktop/eclipse.desktop "$DESKTOP/"
    cp /root/Desktop/firefox.desktop "$DESKTOP/"
    cp /root/Desktop/vscode.desktop "$DESKTOP/"

    mkdir -p "$DESKTOP/eclipse-workspace"
    cp /root/Desktop/eclipse-workspace/impl.model \
       "$DESKTOP/eclipse-workspace/"

    ln -sfn /opt/user/platform "$DESKTOP/platform"

    chown -R "${USER_NAME}:${USER_NAME}" "$DESKTOP"
    chown -h "${USER_NAME}:${USER_NAME}" "$DESKTOP/platform"

fi
