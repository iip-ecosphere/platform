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

package test.de.iip_ecosphere.platform.services.spring;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * A simple server for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestServer implements Server {

    private static int created = 0;
    private static int stopped = 0;
    private TestRunnable runnable;

    /**
     * Creates the server instance.
     */
    public TestServer() {
        System.out.println("Server created");
    }
    
    private static class TestRunnable implements Runnable {
        
        private boolean active = true;

        @Override
        public void run() {
            while (active) {
                TimeUtils.sleep(500);
            }
        }
        
    }
    
    @Override
    public Server start() {
        if (null == runnable) {
            runnable = new TestRunnable();
            new Thread(runnable).start();
            created++;
            System.out.println("Server running");
        }
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        if (null != runnable) {
            runnable.active = false;
            runnable = null;
            stopped++;
            System.out.println("Server stopped");
        }
    }
    
    /**
     * Returns the counter for the number of created instances and resets the counter.
     * 
     * @return the number of created instances since the last call
     */
    public static int getAndResetCreatedCount() {
        int c = created;
        created = 0;
        return c;
    }

    /**
     * Returns the counter for the number of stopped instances and resets the counter.
     * 
     * @return the number of stopped instances since the last call
     */
    public static int getAndResetStoppedCount() {
        int c = stopped;
        stopped = 0;
        return c;
    }

}
