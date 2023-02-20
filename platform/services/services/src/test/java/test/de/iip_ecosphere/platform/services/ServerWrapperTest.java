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

package test.de.iip_ecosphere.platform.services;

import org.junit.Test;

import de.iip_ecosphere.platform.services.ServerWrapper;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import org.junit.Assert;

/**
 * Tests {@class ServerWrapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerWrapperTest {

    /**
     * A server wrapper that allows defining whether a thread is required without defining a respective
     * server class.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestWrapper extends ServerWrapper {
        
        private boolean requiresThread;

        /**
         * Creates a server wrapper.
         * 
         * @param server the server instance
         * @param requiresThread whether a thread shall be assumed to be created
         */
        public TestWrapper(Server server, boolean requiresThread) {
            super(server);
            this.requiresThread = requiresThread;
        }

        @Override
        protected boolean requiresThread() {
            return super.requiresThread() || requiresThread;
        }
        
    }
    
    /**
     * Test server that just emits some lines and does noting. For testing platform-managed servers.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class TestServer implements Server, Runnable {
        
        private boolean running;
        private int startedCount;
        private int stoppedCount;

        @Override
        public Server start() {
            startedCount++;
            new Thread(this).start();
            return this;
        }

        @Override
        public void stop(boolean dispose) {
            stoppedCount++;
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

    /**
     * Tests {@class ServerWrapper}.
     */
    @Test
    public void testWrapper() {
        TestServer server = new TestServer();
        TestWrapper wrapper = new TestWrapper(server, false);
        wrapper.start();
        TimeUtils.sleep(2000);
        wrapper.stop(true);
        TimeUtils.sleep(2000);
        Assert.assertEquals(1, server.startedCount);
        Assert.assertEquals(1, server.stoppedCount);
        
        server = new TestServer();
        wrapper = new TestWrapper(server, true);
        wrapper.start();
        TimeUtils.sleep(2000);
        wrapper.stop(true);
        TimeUtils.sleep(2000);
        Assert.assertEquals(1, server.startedCount);
        Assert.assertEquals(1, server.stoppedCount);
    }

}
