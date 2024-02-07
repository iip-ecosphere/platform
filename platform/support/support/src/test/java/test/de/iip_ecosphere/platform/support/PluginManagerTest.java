/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;

import de.iip_ecosphere.platform.support.plugins.URLPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Tests {@link PluginManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginManagerTest {
    
    /**
     * Declares a configured plugin classpath.
     * 
     * @author Holger Eichelberger, SSE
     *
     */
    public static class MyPluginSetupDescriptor extends URLPluginSetupDescriptor {

        /**
         * Creates and configures the descriptor.
         */
        public MyPluginSetupDescriptor() {
            super(toURLSafe(new File("target/pluginTest.jar"))); // built by Maven before testing
        }
        
    }
    
    /**
     * Tests {@link URLPluginSetupDescriptor#toURL(String[])} and {@link URLPluginSetupDescriptor#toURLSafe(String[])}.
     */
    @Test
    public void testToUrl_strings() {
        final String testURL = "http://me.here.de";
        URL[] tmp;
        try {
            tmp = URLPluginSetupDescriptor.toURL(new String[0]);
            Assert.assertNotNull(tmp);
            Assert.assertTrue(tmp.length == 0);
            tmp = URLPluginSetupDescriptor.toURL(testURL);
            Assert.assertNotNull(tmp);
            Assert.assertTrue(tmp.length == 1);
            Assert.assertEquals(testURL, tmp[0].toString());
        } catch (MalformedURLException e) {
            Assert.fail("There shall be no exceptions");
        }
        try {
            URLPluginSetupDescriptor.toURL("aa");
            Assert.fail("There shall be an exception");
        } catch (MalformedURLException e) {
            // ok
        }

        tmp = URLPluginSetupDescriptor.toURLSafe(testURL);
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.length == 1);

        System.out.println("Next error message is intended:");
        tmp = URLPluginSetupDescriptor.toURLSafe(new String[] {"aa"});
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.length == 0);
    }

    /**
     * Tests {@link URLPluginSetupDescriptor#toURL(File[])} and {@link URLPluginSetupDescriptor#toURLSafe(File[])}.
     */
    @Test
    public void testToUrl_files() {
        final File testFile = new File("target/pluginTest.jar");
        URL[] tmp;
        try {
            tmp = URLPluginSetupDescriptor.toURL(new File[0]);
            Assert.assertNotNull(tmp);
            Assert.assertTrue(tmp.length == 0);
            tmp = URLPluginSetupDescriptor.toURL(testFile);
            Assert.assertNotNull(tmp);
            Assert.assertTrue(tmp.length == 1);
            Assert.assertEquals(testFile.toURI().toURL().toString(), tmp[0].toString());
        } catch (MalformedURLException e) {
            Assert.fail("There shall be no exceptions");
        }

        tmp = URLPluginSetupDescriptor.toURLSafe(testFile);
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.length == 1);
    }

    /**
     * Tests {@link PluginManager}.
     */
    @Test
    public void testPluginManager() {
        Assert.assertNull(PluginManager.getPlugin("whatever"));
        final String id = "test-plugin";
        Plugin<?> plugin = PluginManager.getPlugin(id); // must not reference class directly!
        Assert.assertNotNull(plugin);
        List<Plugin<?>> plugins = CollectionUtils.toList(PluginManager.plugins());
        Assert.assertEquals(1, plugins.size());
        Assert.assertEquals(id, plugin.getId());
        Assert.assertEquals(Server.class, plugin.getInstanceClass());
        Server plServer = (Server) plugin.getInstance();
        Assert.assertNotNull(plServer);
        plServer.start();
        plServer.stop(false);

        Plugin<Server> plugin2 = PluginManager.getPlugin(id, Server.class); // must not reference class directly!
        Assert.assertNotNull(plugin2);
        plServer = plugin2.getInstance();
        plServer.start();
        plServer.stop(false);

        PluginManager.loadPlugins(); // nothing shall happen
        PluginManager.cleanup();
    }

}
