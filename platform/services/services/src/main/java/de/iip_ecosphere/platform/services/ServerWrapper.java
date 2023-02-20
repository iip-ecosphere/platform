/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Wraps a {@link Server} into a thread if considered necessary. A wrapped threaded server may fail more easily than
 * a direct instance controlled by a service manager, while a threaded server also allocates more resources if the 
 * server itself creates a thread.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerWrapper implements Server {

    private Server server;
    private Thread thread;
    private boolean running;
    
    /**
     * Creates a server wrapper.
     * 
     * @param server the server instance
     */
    public ServerWrapper(Server server) {
        this.server = server;
    }
    
    /**
     * Returns whether a wrapping thread is needed or the server shall be called as delegate.
     * 
     * @return {@code true} if a thread is required, {@code false} else
     */
    protected boolean requiresThread() {
        // well, not secure undless sealed
        return server.getClass().getPackage().getName().equals("iip.servers");
    }
    
    @Override
    public Server start() {
        if (requiresThread()) {
            running = true;
            thread = new Thread(() -> {
                server.start();
                while (running && thread.isAlive()) {
                    TimeUtils.sleep(1000);
                }
            });
            thread.start();
        } else {
            server.start();
        }
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        running = false;            
        server.stop(dispose);
    }

}
