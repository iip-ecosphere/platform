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

package test.de.iip_ecosphere.platform.simpleStream.spring;

import java.util.Arrays;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Test server that just emits some lines and does noting. For testing platform-managed servers.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestServer implements Server, Runnable {
    
    private boolean running;
    
    /**
     * Creates a test server instance from command line.
     * 
     * @param args the command line arguments supplied by the platform
     */
    public TestServer(String[] args) {
        System.out.println("Starting TestServer via command line " + Arrays.toString(args));
    }

    @Override
    public Server start() {
        new Thread(this).start();
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        running = false;
        System.out.println("TestServer stopping.");
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            System.out.println("TestServer running.");
            TimeUtils.sleep(3000);
        }
    }

}
