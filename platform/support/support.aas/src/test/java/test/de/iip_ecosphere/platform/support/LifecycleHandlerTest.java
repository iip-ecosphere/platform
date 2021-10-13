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

import java.io.File;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.PidLifecycleDescriptor;
import de.iip_ecosphere.platform.support.TerminatingLifecycleDescriptor;

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

        @Override
        public int priority() {
            return INIT_PRIORITY;
        }

    }

    /**
     * A test handler with shutdown hook.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class LcDesc2 implements TerminatingLifecycleDescriptor, PidLifecycleDescriptor {

        private int waitingCount = 0;
        
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

        @Override
        public int priority() {
            return AAS_PRIORITY;
        }

        @Override
        public boolean continueWaiting() {
            return waitingCount++ < 10;
        }

        @Override
        public String getPidFileName() {
            return "lcdesc2.pid";
        }
        
    }
    
    /**
     * Tests the lifecycle handler with no waiting.
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
       
        Assert.assertNotNull(LifecycleHandler.descriptors());
        Assert.assertTrue(LifecycleHandler.getAnyDescriptor(LifecycleDescriptor.class).isPresent());
        
        LifecycleHandler.attachShutdownHooks();
        Assert.assertEquals(2, shutdownHookCount);
        LifecycleHandler.startup(cmdArgs);
        Assert.assertEquals(2, startupCount);
        LifecycleHandler.shutdown();
        Assert.assertEquals(2, shutdownCount);
        // no assert for shutdownHookCalledCount
        
        startupCount = 0;
        shutdownCount = 0;
        shutdownHookCount = 0;
        shutdownHookCalledCount = 0;
        countDescriptors = 0;
        LifecycleHandler.OneShotStarter.main(cmdArgs);
        Assert.assertTrue(new File(FileUtils.getTempDirectory(), "lcdesc2.pid").exists());
        Assert.assertEquals(2, shutdownHookCount);
        Assert.assertEquals(2, startupCount);
        Assert.assertEquals(2, shutdownCount);
    }

    /**
     * Tests the lifecycle handler with waiting and shutdown by hook. May end up in an endless loop in case of bugs. 
     * {@link LcDesc2} shall terminate the loop after 10 iterations.
     */
    @Test(timeout = 10 * 1000)
    public void testLifecycleHandlerWaiting() {
        cmdArgs = new String[] {"arg1", "arg2"};
        startupCount = 0;
        shutdownCount = 0;
        shutdownHookCount = 0;
        shutdownHookCalledCount = 0;
        countDescriptors = 0;

        LifecycleHandler.WaitingStarter.main(cmdArgs);
        Assert.assertTrue(new File(FileUtils.getTempDirectory(), "lcdesc2.pid").exists());
        Assert.assertEquals(2, shutdownHookCount);
        Assert.assertEquals(2, startupCount);
        // shutdowns not guaranteed
    }

    /**
     * Tests the lifecycle handler with waiting and shutdown at the end. May end up in an endless loop in case of bugs. 
     * {@link LcDesc2} shall terminate the loop after 10 iterations.
     */
    @Test(timeout = 10 * 1000)
    public void testLifecycleHandlerWaitingShutdown() {
        cmdArgs = new String[] {"arg1", "arg2"};
        startupCount = 0;
        shutdownCount = 0;
        shutdownHookCount = 0;
        shutdownHookCalledCount = 0;
        countDescriptors = 0;

        LifecycleHandler.WaitingStarterWithShutdown.main(cmdArgs);
        Assert.assertTrue(new File(FileUtils.getTempDirectory(), "lcdesc2.pid").exists());
        Assert.assertEquals(2, shutdownHookCount);
        Assert.assertEquals(2, startupCount);
        Assert.assertEquals(2, shutdownCount);
    }

}
