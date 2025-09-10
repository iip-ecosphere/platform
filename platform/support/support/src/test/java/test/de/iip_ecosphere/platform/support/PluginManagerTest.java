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
import de.iip_ecosphere.platform.support.plugins.ResourceClasspathPluginSetupDescriptor;

/**
 * Tests {@link PluginManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginManagerTest {
    
    private static final String JAR = "target/pluginTest.jar";
    
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
            super(toURLSafe(new File(JAR))); // built by Maven before testing
        }

        @Override
        public boolean preventDuplicates() {
            return false; // TESTING ONLY!!!
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
        } catch (IllegalArgumentException e) {
            Assert.fail("There shall be no exceptions");
        }
        try {
            URLPluginSetupDescriptor.toURL("aa");
            Assert.fail("There shall be an exception");
        } catch (IllegalArgumentException e) {
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
     * Tests {@link ResourceClasspathPluginSetupDescriptor}.
     */
    @Test
    public void testResourceClasspathPluginSetupDescriptor() {
        String resourceName = "test-plugin.classpath";
        URL[] url = ResourceClasspathPluginSetupDescriptor.loadResourceSafe(resourceName);
        Assert.assertNotNull(url);
        Assert.assertEquals(1, url.length);
        Assert.assertTrue(url[0].toString().contains(JAR));

        // the URL-based setup descriptor is used above, would lead to same result
        new ResourceClasspathPluginSetupDescriptor(resourceName);
        
        // plugin jar extracted to temp, further contained files extracted there and classpath based on those
        url = ResourceClasspathPluginSetupDescriptor.loadResourceSafe("test-plugin.zip");
        Assert.assertNotNull(url);
        Assert.assertEquals(1, url.length);
        Assert.assertTrue(url[0].toString().contains(JAR));
    }

    /**
     * Tests {@link PluginManager}.
     */
    @Test
    public void testPluginManager() {
        PluginManager.loadPlugins();
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

        Plugin<Server> plugin3 = PluginManager.getPlugin(Server.class);
        Assert.assertNotNull(plugin3);

        Plugin<Server> plugin4 = PluginManager.getPlugin(Server.class, id);
        Assert.assertNotNull(plugin4);

        PluginManager.loadPlugins(); // nothing shall happen
        PluginManager.cleanup();
    }

}
