/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.support.ssh.sshd;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.ssh.Ssh;

import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

/**
 * Implements the SSH interface by Apache Sshd.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SshdSsh extends Ssh {
    
    /**
     * The SSH server with further setup options.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class SshdServer implements SshServer {

        private org.apache.sshd.server.SshServer server;
        private boolean started = false;
        private ServerAddress address;
        private Authenticator authenticator;
        private String shellCommand = "/bin/sh -i -l";
        private String[] shellArgs = new String[] {"/bin/sh", "-i", "-l"};
        private File hostKeyFile;
        
        /**
         * Creates a server instance.
         * 
         * @param address the server address
         */
        private SshdServer(ServerAddress address) {
            this.address = address;
        }
        
        @Override
        public SshServer start() {
            if (server == null && !started) {
                server = org.apache.sshd.server.SshServer.setUpDefaultServer();
                server.setHost(address.getHost());
                server.setPort(address.getPort());
                if (null != hostKeyFile) {
                    server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(hostKeyFile.toPath()));
                }
                server.setPasswordAuthenticator((username, password, session)
                        -> null != authenticator && authenticator.authenticate(username, password));
                // only works for Linux-like environments
                if (SystemUtils.IS_OS_WINDOWS) {
                    LoggerFactory.getLogger(SshdSsh.class).error("Cannot start SSH server on Windows.");
                    server = null;
                    return null;
                } else {
                    server.setShellFactory(new ProcessShellFactory(shellCommand, shellArgs));
                    try {
                        server.start();
                        this.started = true;
                    } catch (IOException e) {
                        LoggerFactory.getLogger(SshdSsh.class).error("Cannot start SSH server: " + e.getMessage());
                        server = null;
                        return null;
                    }
                }
            }
            return this;
        }

        @Override
        public void stop(boolean dispose) {
            if (null != this.server) {
                try {
                    this.server.stop(true);
                    this.started = false;
                } catch (IOException e) {
                    LoggerFactory.getLogger(SshdSsh.class).error("Stopping SSH server: " + e.getMessage());
                }
            }            
        }

        @Override
        public void setAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
        }

        @Override
        public void setShellInit(String command, String... args) {
            if (command != null && args != null) {
                shellCommand = command;
                shellArgs = args;
            }
        }
        
        @Override
        public void setHostKey(File hostKeyFile) {
            this.hostKeyFile = hostKeyFile;
        }
        
        @Override
        public boolean isStarted() {
            return started;
        }
        
    }
    
    /**
     * Creates an SSH server.
     * 
     * @param address the server address
     * @return the server instance
     */
    public SshServer createServer(ServerAddress address) throws IOException {
        return new SshdServer(address);
    }

}
