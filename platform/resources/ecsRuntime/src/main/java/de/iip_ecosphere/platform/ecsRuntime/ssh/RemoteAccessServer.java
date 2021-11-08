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
import de.iip_ecosphere.platform.support.Server;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

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

    /**
     * Start the ssh server.
     *
     * @return the started server
     */
    @Override
    public Server start() {
        if (server != null && server.isStarted()) {
            throw new RuntimeException("Server already started");
        }

        server = org.apache.sshd.server.SshServer.setUpDefaultServer();
        server.setHost(SSH_HOST);
        server.setPort(SSH_PORT);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("file.ser").toPath()));
        server.setPasswordAuthenticator((username, password, session)
                -> credentialsManager.authenticate(username, password));
        // only works for Linux-like environments
        server.setShellFactory(new ProcessShellFactory("/bin/sh -i -l", "/bin/sh", "-i", "-l"));
        try {
            server.start();
            this.started = true;
        } catch (IOException e) {
            e.printStackTrace();
            server = null;
            return null;
        }
        return this;
    }

    /**
     * Stop the server.
     * 
     * @param immediately immediately
     */
    @Override
    public void stop(boolean immediately) {
        if (null != this.server) {
            try {
                this.server.stop(immediately);
                this.started = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the started status.
     *
     * @return started
     */
    public boolean isStarted() {
        return started;
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
