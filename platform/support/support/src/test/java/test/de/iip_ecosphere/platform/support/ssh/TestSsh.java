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

package test.de.iip_ecosphere.platform.support.ssh;

import java.io.File;
import java.io.IOException;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.ssh.Ssh;

/**
 * Implements an empty Ssh interface for simple testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestSsh extends Ssh {

    /**
     * The SSH server with further setup options.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class TestSshServer implements SshServer {

        @Override
        public SshServer start() {
            return this;
        }

        @Override
        public void stop(boolean dispose) {
        }

        @Override
        public void setAuthenticator(Authenticator authenticator) {
        }

        @Override
        public void setShellInit(String command, String... args) {
        }

        @Override
        public void setHostKey(File hostKeyFile) {
        }
        
        @Override
        public boolean isStarted() {
            return false;
        }
        
    }
    
    /**
     * Creates an SSH server.
     * 
     * @param address the server address
     * @return the server instance
     */
    public SshServer createServer(ServerAddress address) throws IOException {
        return new TestSshServer();
    }

}
