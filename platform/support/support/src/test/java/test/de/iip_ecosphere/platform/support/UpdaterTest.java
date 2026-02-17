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

package test.de.iip_ecosphere.platform.support;

import java.io.File;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Updater;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Tests {@link Updater}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class UpdaterTest {
    
    /**
     * Tests {@link Updater} for assumed/mocked plugin dependencies.
     */
    @Test
    public void testPluginUpdate() {
        File pluginsFolder = FileUtils.createTmpFolder("support.updater-test", true);

        InputStream in = ResourceLoader.getResourceAsStream("update/resolved-plugin");
        Updater.updatePluginsQuiet(in, pluginsFolder, false);
        
        
        File expected = new File(pluginsFolder, "slf4j-api-1.7.25.jar");
        Assert.assertTrue(expected.exists());
        
        FileUtils.deleteQuietly(pluginsFolder);
    }

    /**
     * Tests {@link Updater} for a client user of an assumed/mocked plugin.
     */
    @Test
    public void testClientUpdate() {
        File pluginsFolder = FileUtils.createTmpFolder("support.updater-test", true);

        InputStream in = ResourceLoader.getResourceAsStream("update/resolved-client");
        Updater.updatePluginsQuiet(in, pluginsFolder, false);
        Assert.assertTrue(new File(pluginsFolder, "support.log-slf4j-simple-0.7.1-SNAPSHOT").exists());
        Assert.assertTrue(new File(pluginsFolder, "target").exists());
        Assert.assertTrue(new File(pluginsFolder, ".metadata").exists());
        Assert.assertTrue(new File(pluginsFolder, "support.log-slf4j-simple2-0.7.1-SNAPSHOT").exists());
        Assert.assertTrue(new File(pluginsFolder, "support.log-slf4j-simple2-0.7.1-SNAPSHOT.idx").exists());
        
        FileUtils.deleteQuietly(pluginsFolder);
    }

}
