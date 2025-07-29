/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which is available
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.ecsRuntime.ssh;

import de.iip_ecosphere.platform.deviceMgt.Credentials;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.ssh.Ssh;
import de.iip_ecosphere.platform.support.ssh.Ssh.SshServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A RemoteAccessServer grants access to the local terminal through a remote connection.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class RemoteAccessServer implements Server {

    public static final String SSH_HOST = "0.0.0.0";
    public static final int SSH_PORT = 5555;
    
    private SshServer server;
    private boolean started = false;

    private CredentialsManager credentialsManager = new CredentialsManager();

    /**
     * Default constructor, restricts creation to package only.
     */
    RemoteAccessServer() {
    }

    @Override
    public Server start() {
        if (server != null && !started) {
            try {
                server = Ssh.getInstance().createServer(new ServerAddress(Schema.SSH, SSH_HOST, SSH_PORT));
                server.setHostKey(new File("file.ser")); // not there, seems to be ignored
                server.setAuthenticator((username, password) -> credentialsManager.authenticate(username, password));
                server = server.start();
            } catch (IOException e) {
                LoggerFactory.getLogger(RemoteAccessServer.class).error("Starting Device Management "
                    + "SSH server: " + e.getMessage());                
            }
        }
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        if (null != this.server) {
            this.server.stop(dispose);
        }
    }

    /**
     * Gets the started status.
     *
     * @return started
     */
    public boolean isStarted() {
        return null != this.server && this.server.isStarted();
    }

    /**
     * The credentials manager connected to the server.
     *
     * @return the credentials manager
     */
    public CredentialsManager getCredentialsManager() {
        return credentialsManager;
    }

    /**
     * A CredentialsManager stores credentials for the underlying ssh server.
     *
     * @author Dennis Pidun, University of Hildesheim
     */
    public class CredentialsManager {

        private List<Credentials> credentials = new ArrayList<>();

        /**
         * Gets all currently saved credentials.
         *
         * @return the credentials list.
         */
        public List<Credentials> getCredentials() {
            return credentials;
        }

        /**
         * Get specific credentials for a key.
         *
         * @param key the key
         * @return the credentials
         */
        public Credentials getCredentials(String key) {
            return credentials.stream()
                .filter(ts -> ts.getKey().equals(key))
                .findFirst()
                .orElse(null);
        }

        /**
         * Checks if the given key and secret combination is stored in the credentials list.
         *
         * @param key the key
         * @param secret the secret
         * @return true if the credentials are right
         */
        public boolean authenticate(String key, String secret) {
            Credentials edgeTunnelSettings = getCredentials(key);
            return edgeTunnelSettings != null && edgeTunnelSettings.getSecret().equals(secret);
        }

        /**
         * Adds credentails to the credentials manager.
         *
         * @param credentials the credentials
         */
        public void addCredentials(Credentials credentials) {
            this.credentials.add(credentials);
        }

        /**
         * Add and generate some credentials.
         * The key and secret is exactly 16 characters long
         *
         * @return the generated credentials
         */
        public Credentials addGeneratedCredentials() {
            Credentials credentials = new Credentials(
                    UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16),
                    UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16));
            this.credentials.add(credentials);
            return credentials;
        }
    }
}
