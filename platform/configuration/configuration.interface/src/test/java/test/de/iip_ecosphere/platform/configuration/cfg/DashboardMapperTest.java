/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.configuration.cfg;

import java.io.File;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.cfg.DashboardMapper;
import de.iip_ecosphere.platform.configuration.cfg.DashboardMapper.MapperParams;
import org.junit.Assert;

/**
 * Tests {@link DashboardMapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DashboardMapperTest {
    
    /**
     * Tests {@link MapperParams}.
     */
    @Test
    public void testMapperParams() {
        MapperParams params = new MapperParams("PlatformCfg", new File("."));
        
        Assert.assertEquals("PlatformCfg", params.getMainModelName());
        Assert.assertNotNull(params.getModelFolder());
        Assert.assertEquals(".", params.getModelFolder().getPath());
        Assert.assertFalse(params.hasPluginId());
        Assert.assertFalse(params.hasPostUrl());
        Assert.assertNull(params.getOutputFile());
        Assert.assertNull(params.getMetaModelFolder());

        params.setPluginId("myPlugin")
            .setInContainer(true)
            .setPostUrl("http://x.y.z")
            .setOutputFile(new File("out"))
            .setMetaModelFolder(new File("meta"));

        Assert.assertTrue(params.hasPluginId());
        Assert.assertTrue(params.hasPostUrl());
        Assert.assertEquals("myPlugin", params.getPluginId());
        Assert.assertTrue(params.isInContainer());
        Assert.assertEquals("http://x.y.z", params.getPostUrl());
        Assert.assertNotNull(params.getOutputFile());
        Assert.assertEquals("out", params.getOutputFile().getPath());
        Assert.assertNotNull(params.getMetaModelFolder());
        Assert.assertEquals("meta", params.getMetaModelFolder().getPath());
    }

}
