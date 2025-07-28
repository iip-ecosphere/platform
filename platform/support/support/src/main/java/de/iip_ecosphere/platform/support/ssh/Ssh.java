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

package de.iip_ecosphere.platform.support.ssh;

import java.io.File;
import java.io.IOException;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Generic access to SSH. Requires an implementing plugin of type {@link Ssh} or an active 
 * {@link SshProviderDescriptor}. Simplified interface akin to Spark.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Ssh {
    
    private static Ssh instance; 

    static {
        instance = PluginManager.getPluginInstance(Ssh.class, SshProviderDescriptor.class);
    }

    /**
     * Returns the Rest instance.
     * 
     * @return the instance
     */
    public static Ssh getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param rest the Rest instance
     */
    public static void setInstance(Ssh rest) {
        if (null != rest) {
            instance = rest;
        }
    }

    /**
     * Authenticates server accesses.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Authenticator {
        
        /**
         * Checks if the given key and secret combination is stored in the credentials list.
         *
         * @param key the key
         * @param secret the secret
         * @return true if the credentials are right
         */
        public boolean authenticate(String key, String secret);
        
    }
    
    /**
     * The SSH server with further setup options.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SshServer extends Server {
     
        /**
         * Sets the authenticator.
         * 
         * @param authenticator the authenticator
         */
        public void setAuthenticator(Authenticator authenticator);

        /**
         * Defines the command to initialize the shell. A default shall be implemented by the server.
         * 
         * @param command the command, ignored if <b>null</b>
         * @param args the command arguments, ignored if <b>null</b>
         */
        public void setShellInit(String command, String... args);        

        /**
         * Sets the host key for encryption in terms of a given file.
         * 
         * @param hostKeyFile the host key file
         */
        public void setHostKey(File hostKeyFile);
        
        @Override
        public SshServer start();

        /**
         * Returns whether the server is started/running.
         * 
         * @return {@code true} for started, {@code false}
         */
        public boolean isStarted();

    }
    
    /**
     * Creates an SSH server.
     * 
     * @param address the server address
     * @return the server instance, <b>null</b> if failed
     */
    public abstract SshServer createServer(ServerAddress address) throws IOException; 
    
}
