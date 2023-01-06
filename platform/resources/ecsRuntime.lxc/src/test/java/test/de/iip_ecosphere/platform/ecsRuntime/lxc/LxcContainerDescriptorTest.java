/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.lxc.LxcContainerDescriptor;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Tests a given docker descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LxcContainerDescriptorTest {

    /**
     * Tests reading descriptors.
     */
    @Test
    public void testDescriptor() {
        Assert.assertNull(LxcContainerDescriptor.readFromYaml(ResourceLoader.getResourceAsStream("xyz.yml"),
                new File("xyz.yml").toURI()));

        LxcContainerDescriptor desc = LxcContainerDescriptor
                .readFromYaml(ResourceLoader.getResourceAsStream("mesh-info.yml"), new File("mesh-info.yml").toURI());
        Assert.assertNotNull(desc);

        Assert.assertEquals("test-serviceMgr", desc.getId());
        Assert.assertEquals("Test Service Manager", desc.getName());
        // Assert.assertEquals("1.0", desc.getVersion().toString());
        Assert.assertEquals("iip/serviceMgr", desc.getLxcImageAlias());
        Assert.assertEquals("serviceMgr.tar.gz", desc.getLxcZip());
        Assert.assertEquals(1, desc.getEnv().size());
        Assert.assertEquals("IIP_PORT=${port}", desc.getEnv().get(0));
        Assert.assertEquals(1, desc.instantiateEnv(1234, 0).size());
        Assert.assertEquals("IIP_PORT=1234", desc.instantiateEnv(1234, 0).get(0));
        Assert.assertEquals(5, desc.getExposedPorts().size());
        Assert.assertEquals("${port}/TCP", desc.getExposedPorts().get(0));
        Assert.assertEquals("22/UDP", desc.getExposedPorts().get(1));
        Assert.assertEquals("80", desc.getExposedPorts().get(2));
        Assert.assertEquals("8080/DEFAULT", desc.getExposedPorts().get(3));
        Assert.assertEquals("8443", desc.getExposedPorts().get(4));
        // List<ExposedPort> exp = desc.instantiateExposedPorts(1235, 0);
        // Assert.assertEquals(4, exp.size());
        // Assert.assertEquals(1235, exp.get(0).getPort());
        // Assert.assertEquals(InternetProtocol.TCP, exp.get(0).getProtocol());
        // Assert.assertEquals(22, exp.get(1).getPort());
        // Assert.assertEquals(InternetProtocol.UDP, exp.get(1).getProtocol());
        // Assert.assertEquals(80, exp.get(2).getPort());
        // Assert.assertEquals(InternetProtocol.DEFAULT, exp.get(2).getProtocol());
        // Assert.assertEquals(8080, exp.get(3).getPort());
        // Assert.assertEquals(InternetProtocol.DEFAULT, exp.get(3).getProtocol());
        Assert.assertTrue(desc.requiresPort(LxcContainerDescriptor.PORT_PLACEHOLDER));
        Assert.assertEquals("host", desc.getNetworkMode());
    }

    /**
     * Tests static image name functions.
     */
    @Test
    public void testImageNameFunctions() {
        Assert.assertEquals("", LxcContainerDescriptor.getRegistry("a/b:123"));
        Assert.assertEquals("", LxcContainerDescriptor.getRegistry("a/b"));
        Assert.assertEquals("host:123", LxcContainerDescriptor.getRegistry("host:123/a/b"));
        Assert.assertEquals("host:123", LxcContainerDescriptor.getRegistry("host:123/a/b:123"));

        Assert.assertEquals("123", LxcContainerDescriptor.getTag("a:123"));
        Assert.assertEquals("123", LxcContainerDescriptor.getTag("a/b:123"));
        Assert.assertEquals("", LxcContainerDescriptor.getTag("a/b"));
        Assert.assertEquals("", LxcContainerDescriptor.getTag("host:123/a/b"));
        Assert.assertEquals("123", LxcContainerDescriptor.getTag("host:123/a/b:123"));

        Assert.assertEquals("a/b", LxcContainerDescriptor.getRepository("a/b:123"));
        Assert.assertEquals("a/b", LxcContainerDescriptor.getRepository("a/b"));
        Assert.assertEquals("a/b", LxcContainerDescriptor.getRepository("host:123/a/b"));
        Assert.assertEquals("a/b", LxcContainerDescriptor.getRepository("host:123/a/b:123"));
    }

}
