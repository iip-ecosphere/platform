/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support;

import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.LifecycleHandler;

/**
 * Tests {@link LifecycleDescriptor} and {@link LifecycleHandler}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LifecycleHandlerTest {

    private static String[] cmdArgs;
    private static int startupCount = 0;
    private static int shutdownCount = 0;
    private static int shutdownHookCount = 0;
    private static int shutdownHookCalledCount = 0;
    private static int countDescriptors = 0;

    /**
     * A test handler without shutdown hook.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class LcDesc1 implements LifecycleDescriptor {
        
        @Override
        public void startup(String[] args) {
            Assert.assertArrayEquals(args, cmdArgs);
            startupCount++;
        }

        @Override
        public void shutdown() {
            shutdownCount++;
        }

        @Override
        public Thread getShutdownHook() {
            shutdownHookCount++;
            return null;
        }
        
    }

    /**
     * A test handler with shutdown hook.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class LcDesc2 implements LifecycleDescriptor {

        @Override
        public void startup(String[] args) {
            Assert.assertArrayEquals(args, cmdArgs);
            startupCount++;
        }

        @Override
        public void shutdown() {
            shutdownCount++;
        }

        @Override
        public Thread getShutdownHook() {
            shutdownHookCount++;
            return new Thread(() -> { shutdownHookCalledCount++; } ); // anyway too late to assert
        }
        
    }
    
    /**
     * Tests the lifecycle handler.
     */
    @Test
    public void testLifecycleHandler() {
        cmdArgs = new String[] {"arg1", "arg2"};
        startupCount = 0;
        shutdownCount = 0;
        shutdownHookCount = 0;
        shutdownHookCalledCount = 0;
        countDescriptors = 0;
        
        ServiceLoader.load(LifecycleDescriptor.class).forEach(l -> { countDescriptors++; });
        Assert.assertEquals(2, countDescriptors);
        
        LifecycleHandler.attachShutdownHooks();
        Assert.assertEquals(2, shutdownHookCount);
        LifecycleHandler.startup(cmdArgs);
        Assert.assertEquals(2, startupCount);
        LifecycleHandler.shutdown();
        Assert.assertEquals(2, shutdownCount);
        // no assert for shutdownHookCalledCount
    }

}
